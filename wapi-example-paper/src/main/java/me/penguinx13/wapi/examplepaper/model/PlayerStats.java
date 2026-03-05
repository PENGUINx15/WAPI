package me.penguinx13.wapi.examplepaper.model;

import me.penguinx13.wapi.orm.annotations.Column;
import me.penguinx13.wapi.orm.annotations.Id;
import me.penguinx13.wapi.orm.annotations.Table;

import java.util.UUID;

@Table("example_player_stats")
public final class PlayerStats {

    @Id
    private UUID uuid;

    @Column
    private int points;

    public PlayerStats() {
    }

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
        this.points = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        this.points += amount;
    }
}
