package main.core.events;

import main.giveaway.ChecksClass;
import main.jsonparser.JSONParsers;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CheckBot {

    private static final JSONParsers jsonParsers = new JSONParsers();

    public void check(@NotNull SlashCommandInteractionEvent event) {
        var guildId = Objects.requireNonNull(event.getGuild()).getIdLong();

        GuildChannelUnion textChannel = event.getOption("textchannel", OptionMapping::getAsChannel);
        GuildChannel guildChannel = textChannel != null ? textChannel : event.getGuildChannel().asTextChannel();
        boolean canSendGiveaway = ChecksClass.canSendGiveaway(guildChannel, event);
        if (canSendGiveaway) {
            String giftPermissions = String.format(jsonParsers.getLocale("gift_permissions", guildId), guildChannel.getId());
            event.reply(giftPermissions).queue();
        }
    }
}