package main.giveaway;

import lombok.Getter;
import lombok.Setter;
import main.controller.UpdateController;
import main.core.events.ReactionEvent;
import main.model.entity.ActiveGiveaways;
import main.service.GiveawayRepositoryService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.logging.Logger;

public class Giveaway {

    private static final Logger LOGGER = Logger.getLogger(Giveaway.class.getName());

    //USER DATA
    @Getter
    private final long guildId;
    @Getter
    private final long textChannelId;
    @Getter
    private final long userIdLong;

    //GiveawayData
    @Getter
    private final GiveawayData giveawayData;

    private final UpdateController updateController;

    @Getter
    @Setter
    private volatile boolean isFinishGiveaway;

    @Getter
    @Setter
    private volatile boolean isLocked;

    @Getter
    @Setter
    private volatile boolean isRemoved;

    //REPO
    private final GiveawayRepositoryService giveawayRepositoryService;

    public Giveaway(long guildId,
                    long textChannelId,
                    long userIdLong,
                    GiveawayRepositoryService giveawayRepositoryService,
                    UpdateController updateController) {
        this.guildId = guildId;
        this.textChannelId = textChannelId;
        this.userIdLong = userIdLong;
        this.giveawayData = new GiveawayData();
        this.updateController = updateController;
        this.giveawayRepositoryService = giveawayRepositoryService;
    }

    public Giveaway(long guildId,
                    long textChannelId,
                    long userIdLong,
                    boolean isFinishGiveaway,
                    boolean isLocked,
                    GiveawayData giveawayData,
                    GiveawayRepositoryService giveawayRepositoryService,
                    UpdateController updateController) {
        this.guildId = guildId;
        this.textChannelId = textChannelId;
        this.userIdLong = userIdLong;
        this.giveawayData = giveawayData;
        this.isFinishGiveaway = isFinishGiveaway;
        this.isLocked = isLocked;
        this.updateController = updateController;
        this.giveawayRepositoryService = giveawayRepositoryService;
    }

    public Timestamp updateTime(final String time) {
        if (time == null) return giveawayData.getEndGiveawayDate();
        ZoneOffset offset = ZoneOffset.UTC;
        LocalDateTime localDateTime;

        if (time.matches(GiveawayUtils.ISO_TIME_REGEX)) {
            localDateTime = LocalDateTime.parse(time, GiveawayUtils.FORMATTER);
        } else {
            long seconds = GiveawayUtils.getSeconds(time);
            localDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).plusSeconds(seconds);
        }

        long toEpochSecond = localDateTime.toEpochSecond(offset);
        giveawayData.setEndGiveawayDate(new Timestamp(toEpochSecond * 1000));

        return giveawayData.getEndGiveawayDate();
    }

    public void startGiveaway(GuildMessageChannel textChannel,
                              String title,
                              int countWinners,
                              String time,
                              Long role,
                              boolean isOnlyForSpecificRole,
                              String urlImage,
                              boolean predefined,
                              int minParticipants) {
        //Записываем данные:
        LOGGER.info("\nGuild id: " + guildId
                + "\nTextChannel: " + textChannel.getName() + " " + textChannel.getId()
                + "\nTitle: " + title
                + "\nCount winners: " + countWinners
                + "\nTime: " + time
                + "\nRole: " + role
                + "\nisOnlyForSpecificRole: " + isOnlyForSpecificRole
                + "\nurlImage: " + urlImage
                + "\npredefined: " + predefined);

        giveawayData.setTitle(title);
        giveawayData.setCountWinners(countWinners);
        giveawayData.setRoleId(role);
        giveawayData.setUrlImage(urlImage);
        giveawayData.setForSpecificRole(isOnlyForSpecificRole);
        giveawayData.setMinParticipants(minParticipants);
        updateTime(time); //Обновляем время

        //Отправка сообщения
        if (predefined) {
            textChannel.sendMessageEmbeds(GiveawayEmbedUtils.giveawayPattern(guildId).build())
                    .queue(this::updateCollections);
        } else {
            textChannel.sendMessageEmbeds(GiveawayEmbedUtils.giveawayPattern(guildId).build())
                    .queue(message -> {
                        message.addReaction(Emoji.fromUnicode(ReactionEvent.TADA)).queue();
                        updateCollections(message);
                    });
        }
    }

    private void updateCollections(Message message) {
        giveawayData.setMessageId(message.getIdLong());

        ActiveGiveaways activeGiveaways = new ActiveGiveaways();
        activeGiveaways.setGuildId(guildId);
        activeGiveaways.setMessageId(message.getIdLong());
        activeGiveaways.setChannelId(message.getChannel().getIdLong());
        activeGiveaways.setCountWinners(giveawayData.getCountWinners());
        activeGiveaways.setTitle(giveawayData.getTitle());
        activeGiveaways.setMinParticipants(giveawayData.getMinParticipants());

        if (giveawayData.getRoleId() == null || giveawayData.getRoleId() == 0) {
            activeGiveaways.setRoleId(null);
        } else {
            activeGiveaways.setRoleId(giveawayData.getRoleId());
        }
        activeGiveaways.setIsForSpecificRole(giveawayData.isForSpecificRole());
        activeGiveaways.setUrlImage(giveawayData.getUrlImage());
        activeGiveaways.setCreatedUserId(userIdLong);
        activeGiveaways.setDateEnd(giveawayData.getEndGiveawayDate());

        giveawayRepositoryService.saveGiveaway(activeGiveaways);
    }

    public synchronized void addUser(List<User> user) {
        GiveawayUserHandler giveawayUserHandler = new GiveawayUserHandler(giveawayRepositoryService);
        giveawayUserHandler.saveUser(this, user);
    }

    public synchronized void addUser(User user) {
        GiveawayUserHandler giveawayUserHandler = new GiveawayUserHandler(giveawayRepositoryService);
        giveawayUserHandler.saveUser(this, List.of(user));
    }

    public synchronized void stopGiveaway(final int countWinner) {
        String logMessage = String.format(
                """
                        \n
                        stopGift method:
                                                
                        Guild ID: %s
                        ListUsersSize: %s
                        CountWinners: %s
                        """, guildId, giveawayData.getParticipantSize(), countWinner);
        LOGGER.info(logMessage);

        GiveawayEnds giveawayEnds = new GiveawayEnds(giveawayRepositoryService);
        giveawayEnds.stop(this, countWinner, updateController);
    }
}