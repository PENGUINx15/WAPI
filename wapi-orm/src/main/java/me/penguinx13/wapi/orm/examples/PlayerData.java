package me.penguinx13.wapi.orm.examples;

import me.penguinx13.wapi.orm.annotations.Column;
import me.penguinx13.wapi.orm.annotations.Id;
import me.penguinx13.wapi.orm.annotations.Table;

import java.util.UUID;

@Table("players")
public final class PlayerData {

    @Id
    private UUID uuid;

    @Column
    private int blocksBroken;

    public PlayerData() {
    }

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.blocksBroken = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void addBlocks(int amount) {
        this.blocksBroken += amount;
    }
}
