package me.penguinx13.wapi.orm.examples;

import me.penguinx13.wapi.managers.SQLiteManager;
import me.penguinx13.wapi.orm.Repository;
import me.penguinx13.wapi.orm.SimpleORM;

import java.util.UUID;

public final class ExampleUsage {

    private ExampleUsage() {
    }

    public static void run(SQLiteManager sqliteManager, UUID uuid) {
        SimpleORM orm = new SimpleORM(sqliteManager);
        orm.registerEntity(PlayerData.class);

        Repository<PlayerData, UUID> players = orm.getRepository(PlayerData.class);

        PlayerData data = players.findById(uuid)
            .orElse(new PlayerData(uuid));

        data.addBlocks(1);

        players.saveAsync(data);
    }
}
