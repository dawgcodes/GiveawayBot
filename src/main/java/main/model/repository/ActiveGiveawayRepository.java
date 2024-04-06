package main.model.repository;

import main.model.entity.ActiveGiveaways;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ActiveGiveawayRepository extends JpaRepository<ActiveGiveaways, Long> {

    @Transactional
    @Modifying
    @Query(value = "UPDATE ActiveGiveaways ac SET ac.dateEnd = :dateEnd WHERE ac.guildId = :guildId")
    void updateGiveawayTime(@Param("guildId") Long guildId, @Param("dateEnd") Timestamp dateEnd);

    @Override
    @NotNull
    @EntityGraph(attributePaths = {"participants"})
    List<ActiveGiveaways> findAll();

    @Nullable
    ActiveGiveaways findByGuildId(Long guildLongId);

    @Nullable
    ActiveGiveaways findByCreatedUserIdAndGuildId(Long createdUserId, Long guildLongId);
}