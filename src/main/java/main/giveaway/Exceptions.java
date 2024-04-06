package main.giveaway;

import api.megoru.ru.entity.exceptions.UnsuccessfulHttpException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Exceptions {

    private final static Logger LOG = JDALogger.getLog(Exceptions.class);

    public static void handle(Throwable e, InteractionHook hook) {
        if (e instanceof UnsuccessfulHttpException uhe) {
            if (uhe.getCode() == 404) {
                hook.sendMessage(uhe.getMessage()).setEphemeral(true).queue();
                LOG.info(uhe.getMessage());
            } else {
                EmbedBuilder errors = new EmbedBuilder();
                errors.setColor(Color.RED);
                errors.setTitle("Errors with API");
                errors.setDescription("Repeat later. Or write to us about it.");

                List<net.dv8tion.jda.api.interactions.components.buttons.Button> buttons = new ArrayList<>();
                buttons.add(Button.link("https://discord.gg/UrWG3R683d", "Support"));
                hook.sendMessageEmbeds(errors.build()).addActionRow(buttons).queue();
                LOG.info(uhe.getMessage());
            }
        } else {
            LOG.info(e.getMessage());
        }
    }
}
