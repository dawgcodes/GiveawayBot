package main.service;

import lombok.AllArgsConstructor;
import main.model.entity.ActiveGiveaways;
import main.model.entity.Participants;
import main.model.repository.ActiveGiveawayRepository;
import main.model.repository.ListUsersRepository;
import main.model.repository.ParticipantsRepository;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class GiveawayRepositoryService {

    private final ActiveGiveawayRepository activeGiveawayRepository;
    private final ParticipantsRepository participantsRepository;
    private final ListUsersRepository listUsersRepository;

    @Transactional
    public void saveGiveaway(ActiveGiveaways activeGiveaways) {
        activeGiveawayRepository.save(activeGiveaways);
    }

    @Transactional
    @Nullable
    public ActiveGiveaways getGiveaway(long guildId) {
        return activeGiveawayRepository.findByGuildId(guildId);
    }

    @Transactional
    public void deleteGiveaway(long guildId) {
        activeGiveawayRepository.deleteById(guildId);
    }

    @Transactional
    public void saveParticipants(List<Participants> participants) {
        participantsRepository.saveAll(participants);
    }

    public List<Participants> findAllParticipants(long guildId) {
        return participantsRepository.findParticipantsByActiveGiveaways(guildId);
    }

    @Transactional
    public void backupAllParticipants(long guildId) {
        listUsersRepository.saveAllParticipantsToUserList(guildId);
    }

}