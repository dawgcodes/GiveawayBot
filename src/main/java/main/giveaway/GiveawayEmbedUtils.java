package main.giveaway;

import main.jsonparser.JSONParsers;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;
import java.sql.Timestamp;
import java.time.Instant;

public class GiveawayEmbedUtils {

    private static final JSONParsers jsonParsers = new JSONParsers();

    public static EmbedBuilder giveawayPattern(final long guildId) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        GiveawayRegistry instance = GiveawayRegistry.getInstance();
        Giveaway giveaway = instance.getGiveaway(guildId);
        Color userColor = GiveawayUtils.getUserColor(guildId);

        if (giveaway != null) {
            GiveawayData giveawayData = giveaway.getGiveawayData();
            String title = giveawayData.getTitle();
            long createdUserId = giveaway.getUserIdLong();
            String giftReaction = jsonParsers.getLocale("gift_reaction", guildId);
            int countWinners = giveawayData.getCountWinners();
            String imageUrl = giveawayData.getUrlImage();
            Long role = giveawayData.getRoleId();
            boolean isForSpecificRole = giveawayData.isForSpecificRole();
            Timestamp endGiveaway = giveawayData.getEndGiveawayDate();

            //Title
            embedBuilder.setTitle(title);
            //Color
            embedBuilder.setColor(userColor);

            String footer;
            if (countWinners == 1) {
                footer = String.format("1 %s", GiveawayUtils.setEndingWord(1, guildId));
            } else {
                footer = String.format("%s %s", countWinners, GiveawayUtils.setEndingWord(countWinners, guildId));
            }

            //Reaction
            embedBuilder.setDescription(giftReaction);

            //Giveaway only for Role
            if (isForSpecificRole) {
                String giftOnlyFor;
                if (role == guildId) {
                    giftOnlyFor = String.format(jsonParsers.getLocale("gift_only_for", guildId), role)
                            .replace("<@&" + guildId + ">", "@everyone");
                } else {
                    giftOnlyFor = String.format(jsonParsers.getLocale("gift_only_for", guildId), role);
                }
                embedBuilder.appendDescription(giftOnlyFor);
            }
            
            //EndGiveaway
            if (endGiveaway != null) {
                long endTime = endGiveaway.getTime() / 1000;
                String endTimeFormat =
                        String.format(jsonParsers.getLocale("gift_ends_giveaway", guildId), endTime, endTime);
                embedBuilder.appendDescription(endTimeFormat);
            }

            String giftHosted = String.format(jsonParsers.getLocale("gift_hosted", guildId), createdUserId);

            //Hosted By
            embedBuilder.appendDescription(giftHosted);
            //Image
            embedBuilder.setImage(imageUrl);
            //Footer
            embedBuilder.setFooter(footer);
        }
        return embedBuilder;
    }

    public static EmbedBuilder giveawayEnd(final String winners, int countWinners, final long guildId) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        GiveawayRegistry instance = GiveawayRegistry.getInstance();
        Giveaway giveaway = instance.getGiveaway(guildId);
        Color userColor = GiveawayUtils.getUserColor(guildId);

        if (giveaway != null) {
            GiveawayData giveawayData = giveaway.getGiveawayData();
            String title = giveawayData.getTitle();
            long createdUserId = giveaway.getUserIdLong();

            embedBuilder.setColor(userColor);
            embedBuilder.setTitle(title);

            if (countWinners == 1) {
                String giftWinner = String.format(jsonParsers.getLocale("gift_winner", guildId), winners);
                embedBuilder.appendDescription(giftWinner);
            } else {
                String giftWinners = String.format(jsonParsers.getLocale("gift_winners", guildId), winners);
                embedBuilder.appendDescription(giftWinners);
            }

            String footer = countWinners + " " + GiveawayUtils.setEndingWord(countWinners, guildId);
            embedBuilder.setTimestamp(Instant.now());
            String giftEnds = String.format(jsonParsers.getLocale("gift_ends", guildId), footer);
            embedBuilder.setFooter(giftEnds);

            if (giveawayData.isForSpecificRole()) {
                Long roleId = giveawayData.getRoleId();
                String giftOnlyFor;

                if (roleId == guildId) {
                    giftOnlyFor = String.format(jsonParsers.getLocale("gift_only_for", guildId), roleId)
                            .replaceAll("<@&" + guildId + ">", "@everyone");
                } else {
                    giftOnlyFor = String.format(jsonParsers.getLocale("gift_only_for", guildId), roleId);
                }

                embedBuilder.appendDescription(giftOnlyFor);
            }
            long giveawayIdLong = giveawayData.getMessageId();

            String giftHosted = String.format(jsonParsers.getLocale("gift_hosted", guildId), createdUserId);

            String giveawayIdDescription = String.format("\n\nGiveaway ID: `%s`", giveawayIdLong);

            //Hosted By
            embedBuilder.appendDescription(giftHosted);
            embedBuilder.appendDescription(giveawayIdDescription);

            if (giveawayData.getUrlImage() != null) {
                embedBuilder.setImage(giveawayData.getUrlImage());
            }
        }
        return embedBuilder;
    }
}