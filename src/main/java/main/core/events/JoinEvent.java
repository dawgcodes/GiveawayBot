package main.core.events;

import main.config.BotStart;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class JoinEvent {

    public void join(@NotNull GuildJoinEvent event) {
        try {
            EmbedBuilder welcome = new EmbedBuilder();
            welcome.setColor(Color.GREEN);
            welcome.addField("Giveaway", "Thanks for adding " + "**" + "Giveaway" + "** " + "bot to " + event.getGuild().getName() + "!\n", false);
            welcome.addField("Setup Bot Language", "Use: </language:941286272390037534>", false);
            welcome.addField("Create Giveaway", "Use: </start:941286272390037535>", false);
            welcome.addField("Create predefined Giveaway", "Use: </predefined:1049647289779630080> (Only Administrators)", false);
            welcome.addField("Reroll Winner", "Use: </reroll:957624805446799452>", false);
            welcome.addField("Stop Giveaway manually", "Use: </stop:941286272390037536> (Only Administrators)", false);
            welcome.addField("List of commands", "Use: </help:941286272390037537>", false);
            welcome.addField("Support server", ":helmet_with_cross: [Discord server](https://discord.com/invite/UrWG3R683d)\n", false);
            welcome.addField("Information", "Our bot supports recovery of any Giveaway, upon request in support. " +
                    "Also, the bot automatically checks the lists of participants, even if the bot is turned off or there are problems in recording while working, " +
                    "it will automatically restore everything. This gives a 100% guarantee that each participant will be recorded.", false);

            List<Button> buttons = new ArrayList<>();
            buttons.add(Button.link("https://discord.gg/UrWG3R683d", "Support"));
            buttons.add(Button.link("https://patreon.com/ghbots", "Patreon"));

            if (BotStart.getMapLanguages().get(event.getGuild().getId()) != null) {
                if (BotStart.getMapLanguages().get(event.getGuild().getId()).equals("eng")) {
                    buttons.add(Button.secondary(event.getGuild().getId() + ":" + ButtonChangeLanguage.CHANGE_LANGUAGE, "Сменить язык ")
                            .withEmoji(Emoji.fromUnicode("U+1F1F7U+1F1FA")));
                } else {
                    buttons.add(Button.secondary(event.getGuild().getId() + ":" + ButtonChangeLanguage.CHANGE_LANGUAGE, "Change language ")
                            .withEmoji(Emoji.fromUnicode("U+1F1ECU+1F1E7")));
                }
            } else {
                buttons.add(Button.secondary(event.getGuild().getId() + ":" + ButtonChangeLanguage.CHANGE_LANGUAGE, "Сменить язык ")
                        .withEmoji(Emoji.fromUnicode("U+1F1F7U+1F1FA")));
            }

            DefaultGuildChannelUnion defaultChannel = event.getGuild().getDefaultChannel();

            if (defaultChannel != null) {
                if (defaultChannel.getType() == ChannelType.TEXT) {
                    TextChannel textChannel = defaultChannel.asTextChannel();
                    if (event.getGuild().getSelfMember().hasPermission(textChannel,
                            Permission.MESSAGE_SEND,
                            Permission.VIEW_CHANNEL,
                            Permission.MESSAGE_EMBED_LINKS)) {
                        defaultChannel
                                .asTextChannel()
                                .sendMessageEmbeds(welcome.build())
                                .setActionRow(buttons)
                                .queue();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Скорее всего нет `DefaultChannel`!");
            e.printStackTrace();
        }
    }
}