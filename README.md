# WAPI

[![](https://jitpack.io/v/PENGUINx15/WAPI.svg)](https://jitpack.io/#PENGUINx15/WAPI)

## SQLiteManager

`SQLiteManager` — это простая библиотека-менеджер для работы с SQLite внутри Bukkit/Paper плагина.

### Что умеет
- Создавать подключение к БД в папке плагина (`connect`).
- Проверять состояние подключения (`isConnected`).
- Закрывать подключение (`disconnect`).
- Выполнять SQL-обновления без параметров (`executeUpdate(String sql)`).
- Выполнять SQL-обновления с параметрами (`executeUpdate(String sql, Object... params)`).
- Подготавливать `PreparedStatement` с автопроставлением параметров (`prepareStatement`).
- Выполнять запросы на чтение (`executeQuery`).

### Пример использования

```java
package me.penguinx13.wapi;

import me.penguinx13.wapi.Managers.SQLiteManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class WAPI extends JavaPlugin {

    private SQLiteManager sqliteManager;

    @Override
    public void onEnable() {
        sqliteManager = new SQLiteManager(this, "players.db");

        // Подключаемся
        sqliteManager.connect();

        // Создаём таблицу
        sqliteManager.executeUpdate(
                "CREATE TABLE IF NOT EXISTS players (" +
                        "uuid TEXT PRIMARY KEY," +
                        "coins INTEGER NOT NULL DEFAULT 0" +
                ")"
        );

        // Добавляем/обновляем игрока
        sqliteManager.executeUpdate(
                "INSERT INTO players (uuid, coins) VALUES (?, ?) " +
                        "ON CONFLICT(uuid) DO UPDATE SET coins = excluded.coins",
                "player-uuid",
                150
        );

        // Чтение через prepareStatement + ResultSet
        try (PreparedStatement statement = sqliteManager.prepareStatement(
                "SELECT coins FROM players WHERE uuid = ?",
                "player-uuid"
        ); ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                int coins = resultSet.getInt("coins");
                getLogger().info("Coins: " + coins);
            }
        } catch (Exception ex) {
            getLogger().severe("SQL error: " + ex.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (sqliteManager != null) {
            sqliteManager.disconnect();
        }
    }
}
```

### Рекомендации
- Для чтения данных лучше использовать `prepareStatement(...)` + `try-with-resources`, чтобы корректно закрывать `PreparedStatement` и `ResultSet`.
- Вызывайте `disconnect()` в `onDisable()`.
- Один экземпляр `SQLiteManager` на плагин обычно достаточно.
