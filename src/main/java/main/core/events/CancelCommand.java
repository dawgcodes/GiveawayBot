package main.core.events;

import main.giveaway.GiveawayRegistry;
import main.model.entity.ActiveGiveaways;
import main.model.entity.Scheduling;
import main.model.repository.ActiveGiveawayRepository;
import main.model.repository.SchedulingRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class CancelCommand {

    private final SchedulingRepository schedulingRepository;
    private final ActiveGiveawayRepository activeGiveawayRepository;

    @Autowired
    public CancelCommand(SchedulingRepository schedulingRepository, ActiveGiveawayRepository activeGiveawayRepository) {
        this.schedulingRepository = schedulingRepository;
        this.activeGiveawayRepository = activeGiveawayRepository;
    }

    public void cancel(@NotNull SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;
        long guildId = event.getGuild().getIdLong();
        long userId = event.getUser().getIdLong();

        Scheduling scheduling = schedulingRepository.findByCreatedUserIdAndGuildId(userId, guildId);
        ActiveGiveaways activeGiveaways = activeGiveawayRepository.findByCreatedUserIdAndGuildId(userId, guildId);

        GiveawayRegistry instance = GiveawayRegistry.getInstance();

        EmbedBuilder cancel = new EmbedBuilder();
        cancel.setColor(Color.GREEN);

        if (scheduling != null) {
            schedulingRepository.deleteById(guildId);
            instance.removeGuildFromGiveaway(guildId);
            cancel.setDescription("Успешно отменили запланированный Giveaway!");
        } else if (activeGiveaways != null) {
            activeGiveawayRepository.deleteById(guildId);
            instance.removeGuildFromGiveaway(guildId);
            cancel.setDescription("Успешно отменили Giveaway!");
        } else {
            cancel.setDescription("Нет доступа или нет активных Giveaway!");
        }

        event.replyEmbeds(cancel.build())
                .setEphemeral(true)
                .queue();
    }
}