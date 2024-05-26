package me.loloed.bot.api.impl;

import com.mojang.authlib.properties.Property;
import me.loloed.bot.api.Bot;
import me.loloed.bot.api.platform.Platform;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class TotemBot extends Bot {
    private final ServerPlayer spawner;
    private int healTicks = 0;

    public TotemBot(Player vanillaPlayer, Platform platform, ServerPlayer spawner) {
        super(vanillaPlayer, platform);
        vanillaPlayer.getGameProfile().getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYxMTY4NDk5NDQzMCwKICAicHJvZmlsZUlkIiA6ICIzOWEzOTMzZWE4MjU0OGU3ODQwNzQ1YzBjNGY3MjU2ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkZW1pbmVjcmFmdGVybG9sIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVlODA5MTI4YTBlNWE0NGMyZTM5NTMyZTZiYmM2ODI1MmNiOGM5ZDVlY2QyNDZlNTk2NTA3N2MxNDdjNzk1ZTciCiAgICB9CiAgfQp9", "TZiXIPncfBLNe47xqZ2hsKa+iKpXfypi3cTHmpaGW44ckDKxZHst43sxXIW2r3N6HeZjsRaXpk8ZIL682OJO/NaIWouXhW1spzdj/onfprIqWUCHugBBycMT3IOe25n320VIU7KWitWbZQJ+cx3Y4CFNUiBzY2kLeRyfEh49KxMPgk/0rV0PG2EH24JK2P9Jb1Fw7ek6E4oYfjD7jjw2+zl+4UvJrtw8NmI4AbPbW4917Hf5bKvnDlzAGg9gZGepCWUII38yFupNGuPGPE2/XAiLBr4XuP/4n7VpeQUevESkUNlplnGTpwNh4SkE75xClBYbWgfmMV8rn+HoF1rNs4CTNEq0TEdQ278DvddYdaQWURLEF/nNPZeE7okwWn43x45XrVUoYBaly1lfIWeeR8gFC2l47rM6B9mWhntROmAZvjGt1GmVK8DT5XMYwmQNXmgyuoAJRcu3mldzAuRmLHJ4GASNe+jVNHOMCGuv8imCpt55dqm5PeLVQiUcvyF82j9eBXWitRERdHHwqRTkcOr6laVXvb0KYJS7uXwZEFFb9GXGL2JWeIhi3p6VsAUW/EdSWOVE6cNcDPmP92SrMop8evgVNaUXKw7GDwkaeyQkoUY7JrXWnAlSDwWWoJUrxCbl07lmDt/DChysTmqwhaOq47BohWUzDjpjfKwt4vo="));
        assert !platform.isClient();
        this.spawner = spawner;
        applyArmor(vanillaPlayer.getInventory());
    }

    @Override
    public void tick() {
        super.tick();
        ServerPlayer player = (ServerPlayer) getVanillaPlayer();
        Inventory inventory = player.getInventory();
        if (!player.server.getPlayerList().getPlayers().contains(spawner)) {
            destroy();
            return;
        }

        player.lookAt(EntityAnchorArgument.Anchor.EYES, spawner.getEyePosition());
        inventory.setItem(40, new ItemStack(Items.TOTEM_OF_UNDYING));

        if (player.invulnerableTime > 0) {
            inventory.setItem(inventory.selected, new ItemStack(Items.TOTEM_OF_UNDYING));
            healTicks = 0;
        } else {
            ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
            sword.enchant(Enchantments.MENDING, 1);
            inventory.setItem(inventory.selected, sword);
        }

        if (healTicks > 200) {
            player.setHealth(player.getMaxHealth());
            healTicks = 0;
        }

        player.removeEffect(MobEffects.REGENERATION);
        healTicks++;
    }

    private void applyArmor(Inventory inventory) {
        ItemStack helmet = new ItemStack(Items.NETHERITE_HELMET);
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        helmet.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(39, helmet);
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        chestplate.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(38, chestplate);
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.enchant(Enchantments.BLAST_PROTECTION, 4);
        leggings.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(37, leggings);
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        boots.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(36, boots);
    }

    public ServerPlayer getSpawner() {
        return spawner;
    }
}
