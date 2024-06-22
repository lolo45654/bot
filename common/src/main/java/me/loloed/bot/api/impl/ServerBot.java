package me.loloed.bot.api.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import me.loloed.bot.api.Bot;
import me.loloed.bot.api.platform.Platform;
import me.loloed.bot.api.util.BotMath;
import me.loloed.bot.api.util.fake.FakePlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.concurrent.ThreadLocalRandom;

public class ServerBot extends Bot implements IServerBot {
    private final ServerPlayer spawner;
    private ServerBotSettings settings;

    private int healTicks = 0;
    private int noShieldTicks = 0;
    private boolean prevShieldCooldown = false;

    public ServerBot(Player vanillaPlayer, Platform platform, ServerPlayer spawner, ServerBotSettings settings) {
        super(vanillaPlayer, platform);
        this.spawner = spawner;
        this.settings = settings;
        applyArmor(vanillaPlayer.getInventory());
        if (vanillaPlayer instanceof FakePlayer fakePlayer) applySkin(fakePlayer);
    }

    @Override
    protected void tick() {
        super.tick();
        ServerPlayer player = getVanillaPlayer();
        Inventory inventory = player.getInventory();
        if (player.touchingUnloadedChunk() || player.distanceToSqr(spawner) > 120*120 || !player.server.getPlayerList().getPlayers().contains(spawner)) {
            destroy();
            return;
        }

        player.lookAt(EntityAnchorArgument.Anchor.EYES, BotMath.getClosestPoint(player.getEyePosition(), spawner.getBoundingBox()));
        inventory.setItem(40, new ItemStack(Items.TOTEM_OF_UNDYING));
        inventory.setItem(inventory.selected, new ItemStack(Items.TOTEM_OF_UNDYING));

        if (settings.autoHealing) {
            tickHealing(player);
        }
        if (settings.shield) {
            tickShield(player, inventory);
        }

        FoodData food = player.getFoodData();
        food.setExhaustion(0.0f);
        food.setFoodLevel(20);
        food.setSaturation(20.0f);

        clientSimulator.setEntityReach(settings.reach);
        applyArmor(vanillaPlayer.getInventory());
    }

    protected void tickShield(ServerPlayer player, Inventory inventory) {
        boolean shieldCooldown = getVanillaPlayer().getCooldowns().isOnCooldown(Items.SHIELD);
        boolean shielding = !shieldCooldown && noShieldTicks <= 0;

        ItemStack shield = new ItemStack(Items.SHIELD);
        shield.enchant(Enchantments.MENDING, 1);
        shield.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(inventory.selected, shield);
        interact(shielding);
        if (prevShieldCooldown && !shielding && settings.autoHit) {
            attack();
        }
        prevShieldCooldown = shielding;

        if (!shieldCooldown && noShieldTicks > 0) {
            noShieldTicks--;
        }
        if (shieldCooldown && noShieldTicks <= 0) {
            noShieldTicks = ThreadLocalRandom.current().nextInt(42);
        }
    }

    protected void tickHealing(ServerPlayer player) {
        if (player.invulnerableTime > 0) {
            healTicks = 0;
        }

        if (healTicks > 200) {
            player.setHealth(player.getMaxHealth());
            healTicks = 0;
        }

        healTicks++;
    }

    protected void applyArmor(Inventory inventory) {
        ItemStack helmet = new ItemStack(settings.armor.itemTypes.get(EquipmentSlot.HEAD));
        helmet.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        helmet.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(39, helmet);
        ItemStack chestplate = new ItemStack(settings.armor.itemTypes.get(EquipmentSlot.CHEST));
        chestplate.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        chestplate.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(38, chestplate);
        ItemStack leggings = new ItemStack(settings.armor.itemTypes.get(EquipmentSlot.LEGS));
        leggings.enchant(settings.blastProtection ? Enchantments.BLAST_PROTECTION : Enchantments.ALL_DAMAGE_PROTECTION, 4);
        leggings.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(37, leggings);
        ItemStack boots = new ItemStack(settings.armor.itemTypes.get(EquipmentSlot.FEET));
        boots.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        boots.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(36, boots);
    }

    @Override
    public ServerPlayer getSpawner() {
        return spawner;
    }

    @Override
    public ServerBotSettings getSettings() {
        return settings;
    }

    @Override
    public ServerPlayer getVanillaPlayer() {
        return (ServerPlayer) super.getVanillaPlayer();
    }
}
