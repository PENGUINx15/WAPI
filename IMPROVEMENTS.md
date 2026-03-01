# Что можно улучшить и добавить в WAPI

Ниже — список приоритетных идей по улучшению библиотеки. Сгруппировано от быстрых wins к более крупным изменениям.

## 1) Критичные улучшения стабильности

1. **Сделать PlaceholderAPI опциональной зависимостью**
   - Сейчас `MessageManager` всегда вызывает `PlaceholderAPI.setPlaceholders(...)`.
   - Если PlaceholderAPI не установлен на сервере, возможен `NoClassDefFoundError`.
   - Что сделать:
     - В `plugin.yml` добавить `softdepend: [PlaceholderAPI]`.
     - В коде проверять наличие плагина через `Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null`.
     - Если нет PlaceholderAPI — просто пропускать этап подстановки.

2. **Закрытие ресурсов в `SQLiteManager#executeQuery(String)`**
   - Метод возвращает `ResultSet`, созданный из `Statement`, который нельзя закрыть автоматически внутри метода.
   - Это часто приводит к утечкам ресурсов.
   - Что сделать:
     - Предпочесть API, где чтение происходит через `prepareStatement(...)` + `try-with-resources`.
     - Либо добавить callback-метод наподобие `query(String sql, ResultSetConsumer consumer)`, который гарантированно закрывает и `Statement`, и `ResultSet`.

3. **Логи через `plugin.getLogger()` вместо `System.out.println`**
   - В `MessageManager` при `null` используется `System.out.println`.
   - Для серверного плагина лучше единообразно использовать логгер Bukkit/Paper.

## 2) Улучшения API и удобства использования

1. **Унифицировать пакетный стиль**
   - Сейчас есть пакеты с заглавной буквы (`Managers`, `Listeners`, `Events`).
   - Стандарт Java — lower-case (`managers`, `listeners`, `events`).

2. **Добавить JavaDoc ко всем публичным менеджерам**
   - `SQLiteManager` уже документирован хорошо.
   - В `ConfigManager`, `MessageManager`, `CooldownManager`, `CustomSkulls` стоит добавить JavaDoc по контрактам и edge-case поведению.

3. **Сделать более явный Message API**
   - Сейчас поддерживается несколько токенов (`{action}`, `{title}`, `{subtitle}`, `{message}`, `{json}`), но это не описано в README.
   - Стоит добавить отдельный раздел с примерами формата сообщений.

4. **Проверки входных аргументов**
   - Для публичных методов можно явно валидировать аргументы (например, `Objects.requireNonNull`), чтобы получать более понятные ошибки.

## 3) Качество кода и поддержка

1. **Добавить автотесты**
   - Минимум:
     - unit-тесты для `MessageManager.replaceValues(...)`;
     - unit-тесты на логику `CooldownManager`;
     - интеграционный тест на `SQLiteManager` с временной БД.

2. **Настроить линтер/форматтер**
   - Например, Spotless + Google Java Format или Checkstyle.
   - Это выровняет стиль и сократит шум в PR.

3. **CI (GitHub Actions)**
   - Автопроверка `./gradlew build` на PR.
   - Опционально — матрица по нескольким версиям Java (17/21).

## 4) Что добавить как новые возможности

1. **Асинхронные SQL-операции**
   - Добавить безопасные async-обёртки для тяжёлых запросов, чтобы не блокировать основной тред сервера.

2. **Typed DAO-слой поверх SQLiteManager**
   - Небольшой слой репозиториев (например, `PlayerRepository`), чтобы пользователи реже писали сырой SQL вручную.

3. **Расширенный Message API**
   - Поддержка MiniMessage/Adventure как современного формата сообщений.
   - Шаблоны сообщений с fallback на legacy-цвета.

4. **Система миграций БД**
   - Таблица `schema_version` и автоприменение SQL-миграций при старте плагина.
   - Это упрощает обновление структуры таблиц между версиями.

5. **Больше utility-компонентов**
   - Time formatter/parser;
   - ItemBuilder/InventoryBuilder;
   - Простая команда/сабкоманда-обвязка;
   - Pagination helper для чатов/GUI.

## 5) Быстрый roadmap (практичный)

- **Шаг 1 (быстро):** softdepend PlaceholderAPI + безопасный fallback + правка логгирования.
- **Шаг 2:** рефактор `executeQuery` и документация по безопасному чтению из БД.
- **Шаг 3:** тесты на `replaceValues` и `CooldownManager` + CI.
- **Шаг 4:** migration system + async SQL helpers.

---

Если нужно, следующим шагом могу подготовить отдельный PR уже с реализацией **Шага 1 и Шага 2** (это даст самый заметный прирост надёжности при минимальном объёме изменений).
