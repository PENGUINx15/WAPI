package me.penguinx13.wapi;

import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public class EntityName implements Listener {
    public static EntityType getType(String entityName){
        switch (entityName) {
            case "Падающий предмет":
                return EntityType.DROPPED_ITEM;
            case "Опыт":
                return EntityType.EXPERIENCE_ORB;
            case "Облачность эффекта":
                return EntityType.AREA_EFFECT_CLOUD;
            case "Древний страж":
                return EntityType.ELDER_GUARDIAN;
            case "Визер скелет":
                return EntityType.WITHER_SKELETON;
            case "Зимогор":
                return EntityType.STRAY;
            case "Яйцо":
                return EntityType.EGG;
            case "Пводок":
                return EntityType.LEASH_HITCH;
            case "Картина":
                return EntityType.PAINTING;
            case "Стрела":
                return EntityType.ARROW;
            case "Снежок":
                return EntityType.SNOWBALL;
            case "Огненный шар":
                return EntityType.FIREBALL;
            case "Маленький огненный шар":
                return EntityType.SMALL_FIREBALL;
            case "Жемчуг Края":
                return EntityType.ENDER_PEARL;
            case "Сигнал Края":
                return EntityType.ENDER_SIGNAL;
            case "Бутылка с зельем":
                return EntityType.SPLASH_POTION;
            case "Бутылка с опытом":
                return EntityType.THROWN_EXP_BOTTLE;
            case "Рамка":
                return EntityType.ITEM_FRAME;
            case "Череп визера":
                return EntityType.WITHER_SKULL;
            case "Горящий динамит":
                return EntityType.PRIMED_TNT;
            case "Падающий блок":
                return EntityType.FALLING_BLOCK;
            case "Фейерверк":
                return EntityType.FIREWORK;
            case "Спектральная стрела":
                return EntityType.SPECTRAL_ARROW;
            case "Снаряд шалкера":
                return EntityType.SHULKER_BULLET;
            case "Огненный шар дракона":
                return EntityType.DRAGON_FIREBALL;
            case "Зомби житель":
                return EntityType.ZOMBIE_VILLAGER;
            case "Скелет конь":
                return EntityType.SKELETON_HORSE;
            case "Зомби конь":
                return EntityType.ZOMBIE_HORSE;
            case "Стойка для брони":
                return EntityType.ARMOR_STAND;
            case "Осел":
                return EntityType.DONKEY;
            case "Мул":
                return EntityType.MULE;
            case "Клыки призывателя":
                return EntityType.EVOKER_FANGS;
            case "Призыватель":
                return EntityType.EVOKER;
            case "Досаждатель":
                return EntityType.VEX;
            case "Разбойник":
                return EntityType.VINDICATOR;
            case "Иллюзионист":
                return EntityType.ILLUSIONER;
            case "Вагонетка с комендным блоком":
                return EntityType.MINECART_COMMAND;
            case "Лодка":
                return EntityType.BOAT;
            case "Вагонетка":
                return EntityType.MINECART;
            case "Вагонетка с сундуком":
                return EntityType.MINECART_CHEST;
            case "Вагонетка с печкой":
                return EntityType.MINECART_FURNACE;
            case "Вагонетка с ТНТ":
                return EntityType.MINECART_TNT;
            case "Вагонетка с воронкой":
                return EntityType.MINECART_HOPPER;
            case "Вагонетка с спавнером":
                return EntityType.MINECART_MOB_SPAWNER;
            case "Крипер":
                return EntityType.CREEPER;
            case "Скелет":
                return EntityType.SKELETON;
            case "Паук":
                return EntityType.SPIDER;
            case "Зомби гигант":
                return EntityType.GIANT;
            case "Зомби":
                return EntityType.ZOMBIE;
            case "Слизь":
                return EntityType.SLIME;
            case "Гаст":
                return EntityType.GHAST;
            case "Свинозомби":
                return EntityType.PIG_ZOMBIE;
            case "Эндермен":
                return EntityType.ENDERMAN;
            case "Пещерный паук":
                return EntityType.CAVE_SPIDER;
            case "Чешуйница":
                return EntityType.SILVERFISH;
            case "Ифрит":
                return EntityType.BLAZE;
            case "Магмокуб":
                return EntityType.MAGMA_CUBE;
            case "Дракон Края":
                return EntityType.ENDER_DRAGON;
            case "Визер":
                return EntityType.WITHER;
            case "Летучая мышь":
                return EntityType.BAT;
            case "Ведьма":
                return EntityType.WITCH;
            case "Эндермит":
                return EntityType.ENDERMITE;
            case "Страж":
                return EntityType.GUARDIAN;
            case "Шалкер":
                return EntityType.SHULKER;
            case "Свинья":
                return EntityType.PIG;
            case "Овца":
                return EntityType.SHEEP;
            case "Корова":
                return EntityType.COW;
            case "Курица":
                return EntityType.CHICKEN;
            case "Спрут":
                return EntityType.SQUID;
            case "Волк":
                return EntityType.WOLF;
            case "Грибная корова":
                return EntityType.MUSHROOM_COW;
            case "Снеговик":
                return EntityType.SNOWMAN;
            case "Оцелот":
                return EntityType.OCELOT;
            case "Железный голем":
                return EntityType.IRON_GOLEM;
            case "Лошадь":
                return EntityType.HORSE;
            case "Кролик":
                return EntityType.RABBIT;
            case "Белый медведь":
                return EntityType.POLAR_BEAR;
            case "Лама":
                return EntityType.LLAMA;
            case "Попугай":
                return EntityType.PARROT;
            case "Житель":
                return EntityType.VILLAGER;
            case "Кристалл Края":
                return EntityType.ENDER_CRYSTAL;
            case "Кадавр":
                return EntityType.HUSK;
            case "Игрок":
                return EntityType.PLAYER;
            case "Погода":
                return EntityType.WEATHER;
            case "Молния":
                return EntityType.LIGHTNING;
            case "Плевок":
                return EntityType.LLAMA_SPIT;
            case "Поплавок":
                return EntityType.FISHING_HOOK;
            case "Стрела с эффектом":
                return EntityType.TIPPED_ARROW;
            case "Туманные зелья":
                return EntityType.LINGERING_POTION;
            default:
                return null;
        }
    }

    public static String getName(EntityType entityType){
        switch (entityType) {
            case DROPPED_ITEM:
                return "Падающий предмет";
            case EXPERIENCE_ORB:
                return "Опыт";
            case AREA_EFFECT_CLOUD:
                return "Облачность эффекта";
            case ELDER_GUARDIAN:
                return "Древний страж";
            case WITHER_SKELETON:
                return "Визер скелет";
            case STRAY:
                return "Зимогор";
            case EGG:
                return "Яйцо";
            case LEASH_HITCH:
                return "Пводок";
            case PAINTING:
                return "Картина";
            case ARROW:
                return "Стрела";
            case SNOWBALL:
                return "Снежок";
            case FIREBALL:
                return "Огненный шар";
            case SMALL_FIREBALL:
                return "Маленький огненный шар";
            case ENDER_PEARL:
                return "Жемчуг Края";
            case ENDER_SIGNAL:
                return "Сигнал Края";
            case SPLASH_POTION:
                return "Бутылка с зельем";
            case THROWN_EXP_BOTTLE:
                return "Бутылка с опытом";
            case ITEM_FRAME:
                return "Рамка";
            case WITHER_SKULL:
                return "Череп визера";
            case PRIMED_TNT:
                return "Горящий динамит";
            case FALLING_BLOCK:
                return "Падающий блок";
            case FIREWORK:
                return "Фейерверк";
            case SPECTRAL_ARROW:
                return "Спектральная стрела";
            case SHULKER_BULLET:
                return "Снаряд шалкера";
            case DRAGON_FIREBALL:
                return "Огненный шар дракона";
            case ZOMBIE_VILLAGER:
                return "Зомби житель";
            case SKELETON_HORSE:
                return "Скелет конь";
            case ZOMBIE_HORSE:
                return "Зомби конь";
            case ARMOR_STAND:
                return "Стойка для брони";
            case DONKEY:
                return "Осел";
            case MULE:
                return "Мул";
            case EVOKER_FANGS:
                return "Клыки призывателя";
            case EVOKER:
                return "Призыватель";
            case VEX:
                return "Досаждатель";
            case VINDICATOR:
                return "Разбойник";
            case ILLUSIONER:
                return "Иллюзионист";
            case MINECART_COMMAND:
                return "Вагонетка с комендным блоком";
            case BOAT:
                return "Лодка";
            case MINECART:
                return "Вагонетка";
            case MINECART_CHEST:
                return "Вагонетка с сундуком";
            case MINECART_FURNACE:
                return "Вагонетка с печкой";
            case MINECART_TNT:
                return "Вагонетка с ТНТ";
            case MINECART_HOPPER:
                return "Вагонетка с воронкой";
            case MINECART_MOB_SPAWNER:
                return "Вагонетка с спавнером";
            case CREEPER:
                return "Крипер";
            case SKELETON:
                return "Скелет";
            case SPIDER:
                return "Паук";
            case GIANT:
                return "Зомби гигант";
            case ZOMBIE:
                return "Зомби";
            case SLIME:
                return "Слизь";
            case GHAST:
                return "Гаст";
            case PIG_ZOMBIE:
                return "Свинозомби";
            case ENDERMAN:
                return "Эндермен";
            case CAVE_SPIDER:
                return "Пещерный паук";
            case SILVERFISH:
                return "Чешуйница";
            case BLAZE:
                return "Ифрит";
            case MAGMA_CUBE:
                return "Магмокуб";
            case ENDER_DRAGON:
                return "Дракон Края";
            case WITHER:
                return "Визер";
            case BAT:
                return "Летучая мышь";
            case WITCH:
                return "Ведьма";
            case ENDERMITE:
                return "Эндермит";
            case GUARDIAN:
                return "Страж";
            case SHULKER:
                return "Шалкер";
            case PIG:
                return "Свинья";
            case SHEEP:
                return "Овца";
            case COW:
                return "Корова";
            case CHICKEN:
                return "Курица";
            case SQUID:
                return "Спрут";
            case WOLF:
                return "Волк";
            case MUSHROOM_COW:
                return "Грибная корова";
            case SNOWMAN:
                return "Снеговик";
            case OCELOT:
                return "Оцелот";
            case IRON_GOLEM:
                return "Железный голем";
            case HORSE:
                return "Лошадь";
            case RABBIT:
                return "Кролик";
            case POLAR_BEAR:
                return "Белый медведь";
            case LLAMA:
                return "Лама";
            case PARROT:
                return "Попугай";
            case VILLAGER:
                return "Житель";
            case ENDER_CRYSTAL:
                return "Кристалл Края";
            case HUSK:
                return "Кадавр";
            case PLAYER:
                return "Игрок";
            case WEATHER:
                return "Погода";
            case LIGHTNING:
                return "Молния";
            case LLAMA_SPIT:
                return "Плевок";
            case FISHING_HOOK:
                return "Поплавок";
            case TIPPED_ARROW:
                return "Стрела с эффектом";
            case LINGERING_POTION:
                return "Туманные зелья";
            default:
                return entityType.toString();
        }
    }
}
