package main.core.events;

import main.giveaway.GiveawayRegistry;
import main.jsonparser.JSONParsers;
import main.model.entity.Participants;
import main.model.repository.ParticipantsRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Objects;

@Service
public class ListCommand {

    private final ParticipantsRepository participantsRepository;

    private static final JSONParsers jsonParsers = new JSONParsers();

    @Autowired
    public ListCommand(ParticipantsRepository participantsRepository) {
        this.participantsRepository = participantsRepository;
    }

    public void list(@NotNull SlashCommandInteractionEvent event) {
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();

        event.deferReply().setEphemeral(true).queue();
        if (GiveawayRegistry.getInstance().hasGiveaway(guildId)) {
            StringBuilder stringBuilder = new StringBuilder();
            List<Participants> participantsList = participantsRepository.findParticipantsByActiveGiveaways(guildId);

            if (participantsList.isEmpty()) {
                String slashListUsersEmpty = jsonParsers.getLocale("slash_list_users_empty", guildId);

                EmbedBuilder list = new EmbedBuilder();
                list.setColor(Color.GREEN);
                list.setDescription(slashListUsersEmpty);
                event.getHook().sendMessageEmbeds(list.build()).setEphemeral(true).queue();
                return;
            }

            for (Participants participants : participantsList) {
                if (stringBuilder.length() < 4000) {
                    stringBuilder.append(stringBuilder.isEmpty() ? "<@" : ", <@").append(participants.getUserId()).append(">");
                } else {
                    stringBuilder.append(" and others...");
                    break;
                }
            }

            String slashListUsers = jsonParsers.getLocale("slash_list_users", guildId);

            EmbedBuilder list = new EmbedBuilder();
            list.setColor(Color.GREEN);
            list.setTitle(slashListUsers);
            list.setDescription(stringBuilder);
            event.getHook().sendMessageEmbeds(list.build()).setEphemeral(true).queue();
        } else {
            String slashStopNoHas = jsonParsers.getLocale("slash_stop_no_has", guildId);

            EmbedBuilder noGiveaway = new EmbedBuilder();
            noGiveaway.setColor(Color.orange);
            noGiveaway.setDescription(slashStopNoHas);
            event.getHook().sendMessageEmbeds(noGiveaway.build()).setEphemeral(true).queue();
        }
    }
}
