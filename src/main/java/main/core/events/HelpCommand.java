package main.core.events;

import main.config.BotStart;
import main.jsonparser.JSONParsers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class HelpCommand {

    private final static JSONParsers jsonParsers = new JSONParsers();

    public void help(@NotNull SlashCommandInteractionEvent event) {
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();

        EmbedBuilder info = new EmbedBuilder();
        info.setColor(Color.GREEN);
        info.setTitle("Giveaway");
        info.addField("Slash Commands",
                """
                        </start:941286272390037535> - Start Giveaway with lots of parameters
                        </stop:941286272390037536> - Stop Giveaway
                        </scheduling:1102283573349851166> - Run scheduled Giveaway
                        </cancel:1102283573349851167> - Cancel Giveaway
                        </reroll:957624805446799452> - Reroll one winner by Giveaway ID
                        </predefined:1049647289779630080> - Gather participants and immediately hold a drawing for a certain @Role.
                        </list:941286272390037538> - List of participants
                        </language:941286272390037534> - Setup Bot Language
                        </participants:952572018077892638> - Get a list of participants by the Giveaway ID
                        </check-bot-permission:1009065886335914054> - Check bot permissions
                        </change:1027901550456225842> - Change the active Giveaway time
                        </patreon:945299399855210527> - Patreon
                         """, false);
        String messagesEventsLinks = jsonParsers.getLocale("messages_events_links", guildId);
        String messagesEventsSite = jsonParsers.getLocale("messages_events_site", guildId);
        String messagesEventsAddMeToOtherGuilds = jsonParsers.getLocale("messages_events_add_me_to_other_guilds", guildId);

        info.addField(messagesEventsLinks, messagesEventsSite + messagesEventsAddMeToOtherGuilds, false);

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.link("https://discord.gg/UrWG3R683d", "Support"));
        if (BotStart.getMapLanguages().get(guildId) != null) {
            if (BotStart.getMapLanguages().get(guildId).equals("eng")) {
                buttons.add(Button.secondary(guildId + ":" + ButtonChangeLanguage.CHANGE_LANGUAGE, "Сменить язык ")
                        .withEmoji(Emoji.fromUnicode("U+1F1F7U+1F1FA")));
            } else {
                buttons.add(Button.secondary(guildId + ":" + ButtonChangeLanguage.CHANGE_LANGUAGE, "Change language ")
                        .withEmoji(Emoji.fromUnicode("U+1F1ECU+1F1E7")));
            }
        } else {
            buttons.add(Button.secondary(guildId + ":" + ButtonChangeLanguage.CHANGE_LANGUAGE, "Сменить язык ")
                    .withEmoji(Emoji.fromUnicode("U+1F1F7U+1F1FA")));
        }

        event.replyEmbeds(info.build()).setEphemeral(true).addActionRow(buttons).queue();
    }
}