package me.penguinx13.wapi;


import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;


public class CustomSkulls {
    public static ItemStack getSkull(String url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if(url.isEmpty())return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        Field profileField;
        try
        {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }
    public static OfflinePlayer getSkullOwner(String url){
        SkullMeta skullMeta = (SkullMeta) getSkull(url).getItemMeta();
        if (skullMeta == null) {
            return null;
        }

        OfflinePlayer owner = skullMeta.getOwningPlayer();
        if (owner != null) {
            return owner;
        }

        if (skullMeta.getOwner() != null) {
            return Bukkit.getOfflinePlayer(skullMeta.getOwner());
        }

        return null;
    }
}
