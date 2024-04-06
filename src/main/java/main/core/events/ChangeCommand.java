package main.core.events;

import main.controller.UpdateController;
import main.giveaway.*;
import main.jsonparser.JSONParsers;
import main.model.repository.ActiveGiveawayRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Objects;

@Service
public class ChangeCommand {

    private final ActiveGiveawayRepository activeGiveawayRepository;

    private static final JSONParsers jsonParsers = new JSONParsers();

    @Autowired
    public ChangeCommand(ActiveGiveawayRepository activeGiveawayRepository) {
        this.activeGiveawayRepository = activeGiveawayRepository;
    }

    public void change(@NotNull SlashCommandInteractionEvent event, UpdateController updateController) {
        var guildIdLong = Objects.requireNonNull(event.getGuild()).getIdLong();
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();

        Giveaway giveaway = GiveawayRegistry.getInstance().getGiveaway(guildIdLong);
        if (giveaway == null) {
            String slashStopNoHas = jsonParsers.getLocale("slash_stop_no_has", guildId);
            event.reply(slashStopNoHas).setEphemeral(true).queue();
            return;
        }
        String time = event.getOption("duration", OptionMapping::getAsString);
        if (time != null) {
            if (time.matches(GiveawayUtils.ISO_TIME_REGEX)) {
                if (GiveawayUtils.timeHandler(event, guildId, time)) return;
                GiveawayData giveawayData = giveaway.getGiveawayData();
                long channelId = giveaway.getTextChannelId();
                long messageId = giveawayData.getMessageId();

                Timestamp timestamp = giveaway.updateTime(time);

                String changeDuration = jsonParsers.getLocale("change_duration", guildId);
                event.reply(changeDuration).setEphemeral(true).queue();

                activeGiveawayRepository.updateGiveawayTime(guildIdLong, timestamp);
                EmbedBuilder embedBuilder = GiveawayEmbedUtils.giveawayPattern(guildIdLong);

                updateController.setView(embedBuilder.build(), guildIdLong, channelId, messageId);
            }
        }
    }
}