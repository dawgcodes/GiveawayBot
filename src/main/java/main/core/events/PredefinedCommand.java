package main.core.events;

import main.controller.UpdateController;
import main.giveaway.Giveaway;
import main.giveaway.GiveawayRegistry;
import main.jsonparser.JSONParsers;
import main.model.repository.ActiveGiveawayRepository;
import main.model.repository.ListUsersRepository;
import main.model.repository.ParticipantsRepository;
import main.service.GiveawayRepositoryService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class PredefinedCommand {

    private final GiveawayRepositoryService giveawayRepositoryService;

    private static final JSONParsers jsonParsers = new JSONParsers();

    @Autowired
    public PredefinedCommand(GiveawayRepositoryService giveawayRepositoryService) {
        this.giveawayRepositoryService = giveawayRepositoryService;
    }

    public void predefined(@NotNull SlashCommandInteractionEvent event, UpdateController updateController) {
        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
        var userIdLong = event.getUser().getIdLong();

        if (GiveawayRegistry.getInstance().hasGiveaway(guildIdLong)) {
            String messageGiftNeedStopGiveaway = jsonParsers.getLocale("message_gift_need_stop_giveaway", guildId);
            EmbedBuilder errors = new EmbedBuilder();
            errors.setColor(Color.GREEN);
            errors.setDescription(messageGiftNeedStopGiveaway);
            event.replyEmbeds(errors.build()).queue();
            return;
        }

        ChannelType channelType = event.getChannelType();
        GuildMessageChannel textChannel;

        if (channelType == ChannelType.NEWS) {
            textChannel = event.getChannel().asNewsChannel();
        } else if (channelType == ChannelType.TEXT) {
            textChannel = event.getChannel().asTextChannel();
        } else {
            event.reply("It`s not a TextChannel!").queue();
            return;
        }

        Role role = event.getOption("role", OptionMapping::getAsRole);
        String countString = event.getOption("count", OptionMapping::getAsString);
        String title = event.getOption("title", OptionMapping::getAsString);

        if (role != null) {
            if (role.getIdLong() == guildId) {
                String notificationForThisRole = String.format(jsonParsers.getLocale("gift_notification_for_everyone", guildId), "@everyone");
                event.reply(notificationForThisRole).queue();
            }
        } else {
            event.reply("Role is Null").queue();
            return;
        }

        if (countString == null) {
            event.reply("Count is Null").queue();
            return;
        } else {
            if (!countString.matches("[0-9]+")) {
                event.reply("Count not a number").queue();
                return;
            }
        }

        Giveaway giveaway = new Giveaway(guildIdLong,
                textChannel.getIdLong(),
                userIdLong,
                giveawayRepositoryService,
                updateController);

        GiveawayRegistry.getInstance().putGift(guildIdLong, giveaway);

        //TODO: Возможно будет проблема когда Guild слишком большая
        giveaway.startGiveaway(
                textChannel,
                title,
                Integer.parseInt(countString),
                "20s",
                role.getIdLong(),
                true,
                null,
                true,
                2);

        Task<List<Member>> listTask = event.getGuild().loadMembers()
                .onSuccess(members -> {
                    try {
                        if (!event.isAcknowledged()) {
                            String sendSlashMessage = String.format(jsonParsers.getLocale("send_slash_message", guildId), event.getChannel().getId());
                            event.reply(sendSlashMessage)
                                    .delay(5, TimeUnit.SECONDS)
                                    .flatMap(InteractionHook::deleteOriginal)
                                    .queue();
                        }
                    } catch (Exception ignored) {
                    }

                    if (role.getIdLong() == guildIdLong) {
                        List<User> userList = members.stream()
                                .map(Member::getUser)
                                .filter(user -> !user.isBot()).toList();

                        giveaway.addUser(userList);
                    } else {
                        List<User> userList = members.stream()
                                .filter(member -> member.getRoles().contains(role))
                                .map(Member::getUser)
                                .filter(user -> !user.isBot()).toList();

                        giveaway.addUser(userList);
                    }
                });
        listTask.isStarted();
    }
}
