package main.core.events;

import main.controller.UpdateController;
import main.giveaway.ChecksClass;
import main.giveaway.Giveaway;
import main.giveaway.GiveawayRegistry;
import main.giveaway.GiveawayUtils;
import main.jsonparser.JSONParsers;
import main.model.entity.Scheduling;
import main.model.repository.ActiveGiveawayRepository;
import main.model.repository.ListUsersRepository;
import main.model.repository.ParticipantsRepository;
import main.model.repository.SchedulingRepository;
import main.service.GiveawayRepositoryService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Objects;

@Service
public class StartCommand {

    private final ActiveGiveawayRepository activeGiveawayRepository;
    private final SchedulingRepository schedulingRepository;
    private final GiveawayRepositoryService giveawayRepositoryService;

    private static final JSONParsers jsonParsers = new JSONParsers();

    @Autowired
    public StartCommand(ActiveGiveawayRepository activeGiveawayRepository,
                        SchedulingRepository schedulingRepository,
                        GiveawayRepositoryService giveawayRepositoryService) {
        this.activeGiveawayRepository = activeGiveawayRepository;
        this.schedulingRepository = schedulingRepository;
        this.giveawayRepositoryService = giveawayRepositoryService;
    }

    public void start(@NotNull SlashCommandInteractionEvent event, UpdateController updateController) {
        boolean canSendGiveaway = ChecksClass.canSendGiveaway(event.getGuildChannel(), event);
        if (!canSendGiveaway) return; //Сообщение уже отправлено

        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
        var userIdLong = event.getUser().getIdLong();
        String title = event.getOption("title", OptionMapping::getAsString);
        String countString = event.getOption("count", OptionMapping::getAsString);
        String time = event.getOption("duration", OptionMapping::getAsString);
        if (time != null) time = time.replaceAll("-", ".");
        Long role = event.getOption("mention", OptionMapping::getAsLong);
        Message.Attachment image = event.getOption("image", OptionMapping::getAsAttachment);
        Integer minParticipants = event.getOption("min_participants", OptionMapping::getAsInt);

        Scheduling schedulingByGuildLongId = schedulingRepository.findByGuildId(guildIdLong);
        if (GiveawayRegistry.getInstance().hasGiveaway(guildIdLong)) {
            String messageGiftNeedStopGiveaway = jsonParsers.getLocale("message_gift_need_stop_giveaway", guildId);
            EmbedBuilder errors = new EmbedBuilder();
            errors.setColor(Color.GREEN);
            errors.setDescription(messageGiftNeedStopGiveaway);
            event.replyEmbeds(errors.build()).queue();
        } else if (schedulingByGuildLongId != null) {
            String messageGiftNeedStopGiveaway = jsonParsers.getLocale("message_gift_need_cancel_giveaway", guildId);
            EmbedBuilder errors = new EmbedBuilder();
            errors.setColor(Color.GREEN);
            errors.setDescription(messageGiftNeedStopGiveaway);
            event.replyEmbeds(errors.build()).queue();
        } else {
            try {
                String urlImage = null;
                int count = 1;
                if (countString != null) count = Integer.parseInt(countString);

                if (minParticipants == null || minParticipants == 0 || minParticipants == 1) {
                    minParticipants = 2;
                }

                if (image != null && image.isImage()) {
                    urlImage = image.getUrl();
                }

                boolean isOnlyForSpecificRole = Objects.equals(event.getOption("role", OptionMapping::getAsString), "yes");

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.BLACK);

                if (time != null && !time.matches(GiveawayUtils.TIME_REGEX)) {
                    String startExamples = jsonParsers.getLocale("start_examples", guildId);
                    String startWrongTime = String.format(jsonParsers.getLocale("start_wrong_time", guildId), time, startExamples);

                    embedBuilder.setDescription(startWrongTime);
                    event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                    return;
                }

                if (title != null && title.length() >= MessageEmbed.TITLE_MAX_LENGTH) {
                    String slashError256 = jsonParsers.getLocale("slash_error_256", guildId);
                    embedBuilder.setDescription(slashError256);
                    event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                    return;
                }

                if (time != null && time.matches(GiveawayUtils.ISO_TIME_REGEX)) {
                    if (GiveawayUtils.timeHandler(event, guildId, time)) return;
                }

                if (role == null && isOnlyForSpecificRole) {
                    String slashErrorOnlyForThisRole = jsonParsers.getLocale("slash_error_only_for_this_role", guildId);
                    event.reply(slashErrorOnlyForThisRole).setEphemeral(true).queue();
                    return;
                } else if (role != null && role == guildIdLong && isOnlyForSpecificRole) {
                    String slashErrorRoleCanNotBeEveryone = jsonParsers.getLocale("slash_error_role_can_not_be_everyone", guildId);
                    event.reply(slashErrorRoleCanNotBeEveryone).setEphemeral(true).queue();
                    return;
                } else if (role != null && !isOnlyForSpecificRole) {
                    String giftNotificationForThisRole = String.format(jsonParsers.getLocale("gift_notification_for_this_role", guildId), role);
                    if (role == guildIdLong) {
                        giftNotificationForThisRole = String.format(jsonParsers.getLocale("gift_notification_for_everyone", guildId), "@everyone");
                        event.reply(giftNotificationForThisRole).queue();
                    } else {
                        event.reply(giftNotificationForThisRole).queue();
                    }
                } else if (role != null) {
                    String giftNotificationForThisRole = String.format(jsonParsers.getLocale("gift_notification_for_this_role", guildId), role);
                    event.reply(giftNotificationForThisRole).queue();
                }

                Giveaway giveaway = new Giveaway(guildIdLong,
                        event.getChannel().getIdLong(),
                        userIdLong,
                        giveawayRepositoryService,
                        updateController);

                GiveawayRegistry.getInstance().putGift(guildIdLong, giveaway);

                if (!event.isAcknowledged()) {
                    try {
                        String sendSlashMessage = String.format(jsonParsers.getLocale("send_slash_message", guildId), event.getChannel().getId());
                        event.reply(sendSlashMessage).setEphemeral(true).queue();
                    } catch (Exception ignored) {
                    }
                }

                giveaway.startGiveaway(
                        event.getGuildChannel(),
                        title,
                        count,
                        time,
                        role,
                        isOnlyForSpecificRole,
                        urlImage,
                        false,
                        minParticipants);

            } catch (Exception e) {
                if (!e.getMessage().contains("Time in the past")) {
                    e.printStackTrace();
                }
                String slashErrors = jsonParsers.getLocale("slash_errors", guildId);
                EmbedBuilder errors = new EmbedBuilder();
                errors.setColor(Color.GREEN);
                errors.setDescription(slashErrors);
                if (event.isAcknowledged()) event.getHook().editOriginalEmbeds(errors.build()).queue();
                else event.getChannel().sendMessageEmbeds(errors.build()).queue();
                GiveawayRegistry.getInstance().removeGuildFromGiveaway(guildIdLong);
                activeGiveawayRepository.deleteById(guildIdLong);
            }
        }
    }
}