package main.giveaway;

import main.jsonparser.JSONParsers;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ChecksClass {

    public static final JSONParsers jsonParsers = new JSONParsers();

    public static boolean canSendGiveaway(GuildChannel dstChannel, SlashCommandInteractionEvent event) {
        Member selfMember = dstChannel.getGuild().getSelfMember();
        StringBuilder stringBuilder = new StringBuilder();

        boolean bool = true;

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_SEND)) {
            stringBuilder.append("`Permission.MESSAGE_SEND`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.VIEW_CHANNEL)) {
            stringBuilder.append(stringBuilder.isEmpty() ? "`Permission.VIEW_CHANNEL`" : ",\n`Permission.VIEW_CHANNEL`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_HISTORY)) {
            stringBuilder.append(stringBuilder.isEmpty() ? "`Permission.MESSAGE_HISTORY`" : ",\n`Permission.MESSAGE_HISTORY`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_EMBED_LINKS)) {
            stringBuilder.append(stringBuilder.isEmpty() ? "`Permission.MESSAGE_EMBED_LINKS`" : ",\n`Permission.MESSAGE_EMBED_LINKS`");
            bool = false;
        }

        if (!selfMember.hasPermission(dstChannel, Permission.MESSAGE_ADD_REACTION)) {
            stringBuilder.append(stringBuilder.isEmpty() ? "`Permission.MESSAGE_ADD_REACTION`" : ",\n`Permission.MESSAGE_ADD_REACTION`");
            bool = false;
        }

        if (!bool && event != null && event.getGuild() != null) {
            String checkPermissions = String.format(
                    jsonParsers.getLocale("check_permissions", event.getGuild().getIdLong()),
                    dstChannel.getId(),
                    stringBuilder);

            event.reply(checkPermissions).queue();
        }

        return bool;
    }
}
