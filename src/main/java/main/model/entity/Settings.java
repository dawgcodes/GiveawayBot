package main.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "color_hex")
    private String colorHex;
}