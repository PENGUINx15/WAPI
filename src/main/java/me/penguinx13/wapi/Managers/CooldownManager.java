package me.penguinx13.wapi.Managers;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {

    private final HashMap<String, Long> cooldowns = new HashMap<>();

	public void setCooldown(UUID uuid, String name) {
        String key = uuid.toString() + ":" + name;
        cooldowns.put(key, System.currentTimeMillis());
    }

    private long getCooldown(String key) {
        return (System.currentTimeMillis() - cooldowns.get(key))/1000;
    }

    public String getCooldownValue(UUID uuid, String name, Integer timecd) {
    	String key = uuid.toString() + ":" + name;
        if(!cooldowns.containsKey(key)) {
            return "No cooldown";
        }
        long cd = timecd-getCooldown(key);
        String time = cd+" &fсек.";
        if(cd >= 60) {
            time = (cd/60)+" &fмин.";
        }
        if(cd >= 60*60) {
            time = ((cd/60)/60)+" &fч.";
        }
        if(cd >= 60*60*24) {
            time = ((cd/60)/60)/24+" &fд.";
        }
        return time;
    }

    public boolean hasCooldown(UUID uuid, String name, Integer time) {
    	String key = uuid.toString() + ":" + name;
        if(cooldowns.containsKey(key)) {
            return getCooldown(key) > time;
        }
        return true;
    }
}
