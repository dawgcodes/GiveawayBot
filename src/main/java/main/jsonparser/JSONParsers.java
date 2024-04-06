package main.jsonparser;

import main.config.BotStart;
import main.model.entity.Settings;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JSONParsers {

    private final static Logger LOGGER = Logger.getLogger(JSONParsers.class.getName());

    public String getLocale(String key, long guildId) {
        try {
            Settings settings = BotStart.getMapLanguages().get(guildId);
            return ParserClass.getInstance().getTranslation(key, settings != null ? settings.getLanguage() : "eng");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return "NO_FOUND_LOCALIZATION";
    }
}