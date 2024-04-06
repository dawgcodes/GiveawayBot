package main.core.events;

import main.jsonparser.JSONParsers;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LanguageHandler {

    private static final JSONParsers jsonParsers = new JSONParsers();

    public void handler(@NotNull SlashCommandInteractionEvent event, String json_line) {
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
        String startInThread = jsonParsers.getLocale(json_line, guildId);
        event.reply(startInThread).queue();
    }
}