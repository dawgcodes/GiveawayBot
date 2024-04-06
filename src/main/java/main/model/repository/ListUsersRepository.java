package main.model.repository;

import main.model.entity.ListUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ListUsersRepository extends JpaRepository<ListUsers, Long> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO list_users(giveaway_id, guild_id, created_user_id, nick_name, user_id) " +
            "SELECT ag.message_id, ag.guild_id, ag.created_user_id, p.nick_name, p.user_id " +
            "FROM active_giveaways ag, participants p " +
            "WHERE ag.guild_id = :guildId AND p.guild_id = :guildId", nativeQuery = true)
    void saveAllParticipantsToUserList(@Param("guildId") Long guildId);

    List<ListUsers> findAllByGiveawayIdAndCreatedUserId(Long giveawayId, Long createdUserId);
}
