package main.core.events;

import api.megoru.ru.entity.Winners;
import api.megoru.ru.impl.MegoruAPI;
import main.giveaway.Exceptions;
import main.jsonparser.JSONParsers;
import main.model.entity.ListUsers;
import main.model.repository.ListUsersRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.*;

@Service
public class RerollCommand {

    private final ListUsersRepository listUsersRepository;

    private static final JSONParsers jsonParsers = new JSONParsers();
    private final static MegoruAPI api = new MegoruAPI.Builder().build();

    @Autowired
    public RerollCommand(ListUsersRepository listUsersRepository) {
        this.listUsersRepository = listUsersRepository;
    }

    public void reroll(@NotNull SlashCommandInteractionEvent event) {
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();

        event.deferReply().queue();
        String id = event.getOption("giveaway_id", OptionMapping::getAsString);

        if (id != null) {
            if (!id.matches("\\d+")) {
                event.getHook().sendMessage("ID is not Number!").setEphemeral(true).queue();
                return;
            }
            User user = event.getUser();
            List<ListUsers> listUsers = listUsersRepository.findAllByGiveawayIdAndCreatedUserId(Long.valueOf(id), user.getIdLong());

            if (listUsers.isEmpty()) {
                String noAccessReroll = jsonParsers.getLocale("no_access_reroll", guildId);
                event.getHook().sendMessage(noAccessReroll).setEphemeral(true).queue();
                return;
            }

            try {
                Winners winners = new Winners(1, 0, listUsers.size() - 1);
                List<String> setWinners = api.getWinners(winners);
                final Set<String> uniqueWinners = new LinkedHashSet<>();
                for (String setWinner : setWinners) {
                    uniqueWinners.add("<@" + listUsers.get(Integer.parseInt(setWinner)).getUserId() + ">");
                }
                String winnerList = Arrays.toString(uniqueWinners.toArray())
                        .replaceAll("\\[", "")
                        .replaceAll("]", "");
                String giftCongratulationsReroll = String.format(jsonParsers.getLocale("gift_congratulations_reroll",
                        guildId), winnerList);

                EmbedBuilder winner = new EmbedBuilder();
                winner.setColor(Color.GREEN);
                winner.setDescription(giftCongratulationsReroll);

                event.getHook().sendMessageEmbeds(winner.build()).queue();
            } catch (Exception ex) {
                Exceptions.handle(ex, event.getHook());
            }
        } else {
            event.getHook().sendMessage("Options is null").setEphemeral(true).queue();
        }
    }
}