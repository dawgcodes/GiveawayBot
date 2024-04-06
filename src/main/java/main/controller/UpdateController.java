package main.controller;

import lombok.Getter;
import main.core.CoreBot;
import main.core.events.*;
import main.model.repository.*;
import main.service.GiveawayRepositoryService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Getter
@Component
public class UpdateController {

    //REPO
    private final ActiveGiveawayRepository activeGiveawayRepository;
    private final ParticipantsRepository participantsRepository;
    private final ListUsersRepository listUsersRepository;
    private final SchedulingRepository schedulingRepository;
    private final SettingsRepository settingsRepository;
    private final GiveawayRepositoryService giveawayRepositoryService;

    //LOGGER
    private final static Logger LOGGER = Logger.getLogger(UpdateController.class.getName());

    //CORE
    private CoreBot coreBot;

    @Autowired
    public UpdateController(ActiveGiveawayRepository activeGiveawayRepository,
                            ParticipantsRepository participantsRepository,
                            ListUsersRepository listUsersRepository,
                            SchedulingRepository schedulingRepository,
                            SettingsRepository settingsRepository,
                            GiveawayRepositoryService giveawayRepositoryService) {
        this.activeGiveawayRepository = activeGiveawayRepository;
        this.participantsRepository = participantsRepository;
        this.listUsersRepository = listUsersRepository;
        this.schedulingRepository = schedulingRepository;
        this.settingsRepository = settingsRepository;
        this.giveawayRepositoryService = giveawayRepositoryService;
    }

    public void registerBot(CoreBot coreBot) {
        this.coreBot = coreBot;
    }

    public void processEvent(Object event) {
        distributeEventsByType(event);
    }

    private void distributeEventsByType(Object event) {
        if (event instanceof SlashCommandInteractionEvent) {
            slashEvent((SlashCommandInteractionEvent) event);
        } else if (event instanceof ButtonInteractionEvent) {
            buttonEvent((ButtonInteractionEvent) event);
        } else if (event instanceof GuildJoinEvent) {
            joinEvent((GuildJoinEvent) event);
        } else if (event instanceof MessageReactionAddEvent) {
            reactionEvent((MessageReactionAddEvent) event);
        } else if (event instanceof GuildLeaveEvent) {
            leaveEvent((GuildLeaveEvent) event);
        }
    }

    private void slashEvent(@NotNull SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) return;

        if (event.getGuild() != null && event.getChannelType().isThread()) {
            LanguageHandler languageHandler = new LanguageHandler();
            languageHandler.handler(event, "start_in_thread");
            return;
        }

        switch (event.getName()) {
            case "help" -> {
                HelpCommand helpCommand = new HelpCommand();
                helpCommand.help(event);
            }
            case "start" -> {
                StartCommand startCommand = new StartCommand(activeGiveawayRepository, schedulingRepository, giveawayRepositoryService);
                startCommand.start(event, this);
            }
            case "stop" -> {
                StopCommand stopCommand = new StopCommand();
                stopCommand.stop(event);
            }
            case "predefined" -> {
                PredefinedCommand predefinedCommand = new PredefinedCommand(giveawayRepositoryService);
                predefinedCommand.predefined(event, this);
            }
            case "settings" -> {
                SettingsCommand settingsCommand = new SettingsCommand(settingsRepository);
                settingsCommand.language(event);
            }
            case "list" -> {
                ListCommand listCommand = new ListCommand(participantsRepository);
                listCommand.list(event);
            }
            case "reroll" -> {
                RerollCommand rerollCommand = new RerollCommand(listUsersRepository);
                rerollCommand.reroll(event);
            }
            case "change" -> {
                ChangeCommand changeCommand = new ChangeCommand(activeGiveawayRepository);
                changeCommand.change(event, this);
            }
            case "scheduling" -> {
                SchedulingCommand schedulingCommand = new SchedulingCommand(schedulingRepository, activeGiveawayRepository);
                schedulingCommand.scheduling(event);
            }
            case "participants" -> {
                ParticipantsCommand participantsCommand = new ParticipantsCommand(listUsersRepository);
                participantsCommand.participants(event);
            }
            case "patreon" -> {
                PatreonCommand patreonCommand = new PatreonCommand();
                patreonCommand.patreon(event);
            }
            case "cancel" -> {
                CancelCommand cancelCommand = new CancelCommand(schedulingRepository, activeGiveawayRepository);
                cancelCommand.cancel(event);
            }
            case "check-bot-permission" -> {
                CheckBot checkBot = new CheckBot();
                checkBot.check(event);
            }
        }
    }

    private void buttonEvent(@NotNull ButtonInteractionEvent event) {
        if (event.getGuild() == null) return;
        if (Objects.equals(event.getButton().getId(), event.getGuild().getId() + ":" + ButtonChangeLanguage.CHANGE_LANGUAGE)) {
            ButtonChangeLanguage buttonChangeLanguage = new ButtonChangeLanguage(settingsRepository);
            buttonChangeLanguage.change(event);
        }
    }

    private void joinEvent(@NotNull GuildJoinEvent event) {
        JoinEvent joinEvent = new JoinEvent();
        joinEvent.join(event);
    }

    private void leaveEvent(@NotNull GuildLeaveEvent event) {
        LeaveEvent leaveEvent = new LeaveEvent(activeGiveawayRepository, schedulingRepository);
        leaveEvent.leave(event);
    }

    private void reactionEvent(@NotNull MessageReactionAddEvent event) {
        ReactionEvent reactionEvent = new ReactionEvent();
        reactionEvent.reaction(event, this);
    }

    public void setView(EmbedBuilder embedBuilder, final long guildId, final long textChannel) {
        coreBot.editMessage(embedBuilder, guildId, textChannel);
    }

    public void setView(MessageEmbed messageEmbed, final long guildId, final long textChannel, long messageId) {
        coreBot.editMessage(messageEmbed, guildId, textChannel, messageId);
    }

    public void setView(MessageEmbed embedBuilder, String messageContent, Long guildId, Long textChannel) {
        coreBot.sendMessage(embedBuilder, messageContent, guildId, textChannel);
    }

    public void setView(MessageEmbed embedBuilder, Long guildId, Long textChannel, List<Button> buttons) {
        coreBot.sendMessage(embedBuilder, guildId, textChannel, buttons);
    }

    public void setView(JDA jda, String userId, MessageEmbed messageEmbed) {
        coreBot.sendMessage(jda, userId, messageEmbed);
    }
}