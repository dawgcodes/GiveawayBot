package main.giveaway;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@NoArgsConstructor
public class GiveawayData {

    private final ConcurrentHashMap<String, String> participantsList = new ConcurrentHashMap<>();
    private long messageId;
    private int countWinners;
    private Long roleId;
    private boolean isForSpecificRole;
    private String urlImage;
    private String title;
    private Timestamp endGiveawayDate;
    private int minParticipants = 2;

    public GiveawayData(long messageId,
                        int countWinners,
                        Long roleId,
                        boolean isForSpecificRole,
                        String urlImage,
                        String title,
                        Timestamp endGiveawayDate,
                        int minParticipants) {
        this.messageId = messageId;
        this.countWinners = countWinners;
        this.roleId = roleId;
        this.isForSpecificRole = isForSpecificRole;
        this.urlImage = urlImage;
        this.title = title;
        this.endGiveawayDate = endGiveawayDate;
        this.minParticipants = minParticipants;
    }

    public boolean participantContains(String user) {
        return participantsList.containsKey(user);
    }

    public int getParticipantSize() {
        return participantsList.size();
    }

    public void addParticipant(String userId) {
        participantsList.put(userId, userId);
    }

    public void setParticipantsList(Map<String, String> participantsMap) {
        participantsMap.forEach((userId, user) -> participantsList.put(userId, userId));
    }

    public void setMinParticipants(int minParticipants) {
        if (minParticipants == 0) this.minParticipants = 2;
        else this.minParticipants = minParticipants;
    }

    public void setTitle(String title) {
        if (title == null) this.title = "Giveaway";
        else this.title = title;
    }
}