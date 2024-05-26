package me.loloed.bot.api.impl;

import me.loloed.bot.api.Bot;
import me.loloed.bot.api.platform.Platform;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;

public class TotemBot extends Bot {
    private final ServerPlayer spawner;
    private int healTicks = 0;

    public TotemBot(Player vanillaPlayer, Platform platform, ServerPlayer spawner) {
        super(vanillaPlayer, platform);
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
        helmet.enchant(Enchantments.PROTECTION, 4);
        helmet.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        inventory.setItem(39, helmet);
        ItemStack chestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
        chestplate.enchant(Enchantments.PROTECTION, 4);
        chestplate.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        inventory.setItem(38, chestplate);
        ItemStack leggings = new ItemStack(Items.NETHERITE_LEGGINGS);
        leggings.enchant(Enchantments.BLAST_PROTECTION, 4);
        leggings.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        inventory.setItem(37, leggings);
        ItemStack boots = new ItemStack(Items.NETHERITE_BOOTS);
        boots.enchant(Enchantments.PROTECTION, 4);
        boots.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        inventory.setItem(36, boots);
    }

    public ServerPlayer getSpawner() {
        return spawner;
    }
}
