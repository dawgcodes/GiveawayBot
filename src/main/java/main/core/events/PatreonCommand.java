package main.core.events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class PatreonCommand {

    public void patreon(@NotNull SlashCommandInteractionEvent event) {
        EmbedBuilder patreon = new EmbedBuilder();
        patreon.setColor(Color.YELLOW);
        patreon.setTitle("Patreon", "https://www.patreon.com/ghbots");
        patreon.setDescription("If you want to support the work of our bots." +
                "\nYou can do it here click: [here](https://www.patreon.com/ghbots)");
        event.replyEmbeds(patreon.build()).setEphemeral(true).queue();
    }
}
