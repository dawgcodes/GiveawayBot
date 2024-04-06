package main.core.events;

import main.config.BotStart;
import main.giveaway.GiveawayUtils;
import main.jsonparser.JSONParsers;
import main.model.entity.Settings;
import main.model.repository.SettingsRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Optional;

@Service
public class SettingsCommand {

    private final SettingsRepository settingsRepository;

    private static final JSONParsers jsonParsers = new JSONParsers();

    @Autowired
    public SettingsCommand(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void language(@NotNull SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;
        var guildId = event.getGuild().getIdLong();

        Optional<Settings> optionalSettings = settingsRepository.findById(guildId);
        Settings settings;

        String language = event.getOptions().get(0).getAsString();
        if (optionalSettings.isPresent()) {
            settings = optionalSettings.get();
            settings.setLanguage(language);
        } else {
            settings = new Settings();
            settings.setServerId(guildId);
            settings.setLanguage(language);
        }

        if (event.getOptions().size() > 1) {
            String color = event.getOptions().get(1).getAsString();
            if (color.length() == 7) {
                char firstChar = color.charAt(0);
                if (firstChar == '#') {
                    settings.setColorHex(color.toUpperCase());
                }
            } else {
                String settingsColorError = jsonParsers.getLocale("settings_color_error", guildId);
                event.reply(settingsColorError).setEphemeral(true).queue();
                return;
            }
        }

        BotStart.getMapLanguages().put(guildId, settings);

        String lang = language.equals("rus") ? "Русский" : "English";
        String buttonLanguage;
        if (event.getOptions().size() > 1) {
            buttonLanguage = String.format(jsonParsers.getLocale("button_language_color", guildId), lang, settings.getColorHex());
        } else {
            buttonLanguage = String.format(jsonParsers.getLocale("button_language", guildId), lang);
        }

        EmbedBuilder button = new EmbedBuilder();
        Color userColor = GiveawayUtils.getUserColor(guildId);
        button.setColor(userColor);
        button.setDescription(buttonLanguage);

        event.replyEmbeds(button.build()).setEphemeral(true).queue();
        settingsRepository.save(settings);
    }
}