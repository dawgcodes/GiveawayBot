package main.core;

import jakarta.annotation.PostConstruct;
import main.config.BotStart;
import main.controller.UpdateController;
import main.giveaway.Giveaway;
import main.giveaway.GiveawayData;
import main.giveaway.GiveawayRegistry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class CoreBot extends ListenerAdapter {

    private static final Logger LOGGER = Logger.getLogger(CoreBot.class.getName());

    private final UpdateController updateController;

    @Autowired
    public CoreBot(UpdateController updateController) {
        this.updateController = updateController;
    }

    @PostConstruct
    public void init() {
        updateController.registerBot(this);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        updateController.processEvent(event);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        updateController.processEvent(event);
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        updateController.processEvent(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        updateController.processEvent(event);
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        updateController.processEvent(event);
    }

    public void editMessage(EmbedBuilder embedBuilder, final long guildId, final long textChannel) {
        try {
            Guild guildById = BotStart.getJda().getGuildById(guildId);
            if (guildById != null) {
                GuildMessageChannel textChannelById = guildById.getTextChannelById(textChannel);
                if (textChannelById == null) textChannelById = guildById.getNewsChannelById(textChannel);
                if (textChannelById != null) {
                    GiveawayRegistry instance = GiveawayRegistry.getInstance();
                    Giveaway giveaway = instance.getGiveaway(guildId);
                    if (giveaway != null) {
                        GiveawayData giveawayData = giveaway.getGiveawayData();
                        textChannelById
                                .retrieveMessageById(giveawayData.getMessageId())
                                .complete()
                                .editMessageEmbeds(embedBuilder.build())
                                .submit();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            if (e.getMessage().contains("10008: Unknown Message")
                    || e.getMessage().contains("Missing permission: VIEW_CHANNEL")) {
                System.out.println(e.getMessage() + " удаляем!");
                updateController.getActiveGiveawayRepository().deleteById(guildId);
                GiveawayRegistry.getInstance().removeGuildFromGiveaway(guildId);
            } else {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public void editMessage(MessageEmbed messageEmbed, long guildId, long textChannel, long messageId) {
        try {
            Guild guildById = BotStart.getJda().getGuildById(guildId);
            if (guildById != null) {
                GuildMessageChannel textChannelById = guildById.getTextChannelById(textChannel);
                if (textChannelById == null) textChannelById = guildById.getNewsChannelById(textChannel);
                if (textChannelById == null) textChannelById = guildById.getThreadChannelById(textChannel);
                if (textChannelById != null) {
                    textChannelById
                            .editMessageEmbedsById(messageId, messageEmbed)
                            .queue();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void sendMessage(MessageEmbed embedBuilder, String messageContent, Long guildId, Long textChannel) {
        try {
            Guild guildById = BotStart.getJda().getGuildById(guildId);
            if (guildById != null) {
                GuildMessageChannel textChannelById = guildById.getTextChannelById(textChannel);
                if (textChannelById == null) textChannelById = guildById.getNewsChannelById(textChannel);
                if (textChannelById == null) textChannelById = guildById.getThreadChannelById(textChannel);
                if (textChannelById != null) {
                    textChannelById
                            .sendMessageEmbeds(embedBuilder)
                            .setContent(messageContent)
                            .queue();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void sendMessage(MessageEmbed embedBuilder, Long guildId, Long textChannel, List<Button> buttons) {
        try {
            Guild guildById = BotStart.getJda().getGuildById(guildId);
            if (guildById != null) {
                GuildMessageChannel textChannelById = guildById.getTextChannelById(textChannel);
                if (textChannelById == null) textChannelById = guildById.getNewsChannelById(textChannel);
                if (textChannelById == null) textChannelById = guildById.getThreadChannelById(textChannel);
                if (textChannelById != null) {
                    textChannelById
                            .sendMessageEmbeds(embedBuilder)
                            .setActionRow(buttons)
                            .queue();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void sendMessage(JDA jda, String userId, MessageEmbed messageEmbed) {
        RestAction<User> action = jda.retrieveUserById(userId);
        action.submit()
                .thenCompose((user) -> user.openPrivateChannel().submit())
                .thenCompose((channel) -> channel.sendMessageEmbeds(messageEmbed).submit())
                .whenComplete((v, throwable) -> {
                    if (throwable != null) {
                        if (throwable.getMessage().contains("50007: Cannot send messages to this user")) {
                            LOGGER.log(Level.SEVERE, "50007: Cannot send messages to this user", throwable);
                        }
                    }
                });
    }
}