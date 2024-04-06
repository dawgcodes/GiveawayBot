package main.core.events;

import main.config.BotStart;
import main.jsonparser.JSONParsers;
import main.model.entity.Settings;
import main.model.repository.SettingsRepository;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ButtonChangeLanguage {

    private final SettingsRepository settingsRepository;

    public static final String CHANGE_LANGUAGE = "CHANGE_LANGUAGE";
    private static final JSONParsers jsonParsers = new JSONParsers();
    private static final String FLAG_RUS = "\uD83C\uDDF7\uD83C\uDDFA"; //🇷🇺

    @Autowired
    public ButtonChangeLanguage(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void change(@NotNull ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;
        long guildId = event.getGuild().getIdLong();

        event.deferEdit().queue();
        if (event.getButton().getEmoji() != null) {
            String buttonName = event.getButton().getEmoji().getName().contains(FLAG_RUS) ? "rus" : "eng";
            event.editButton(event.getButton().asDisabled()).queue();

            Settings settings = new Settings();
            settings.setServerId(guildId);
            settings.setLanguage(buttonName);
            settingsRepository.save(settings);

            BotStart.getMapLanguages().put(guildId, settings);
            String buttonLanguage = String.format(jsonParsers.getLocale("button_language", guildId), buttonName);

            event.getHook().sendMessage(buttonLanguage).setEphemeral(true).queue();
        }
    }
}