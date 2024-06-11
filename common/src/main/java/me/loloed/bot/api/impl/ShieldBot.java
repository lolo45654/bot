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

import java.util.concurrent.ThreadLocalRandom;

public class ShieldBot extends Bot implements IServerBot {
    private final ServerPlayer spawner;
    private int noShieldTicks = 0;
    private int healTicks = 0;

    public ShieldBot(Player vanillaPlayer, Platform platform, ServerPlayer spawner) {
        super(vanillaPlayer, platform);
        vanillaPlayer.getGameProfile().getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTY3ODkxNzU0MTg2MCwKICAicHJvZmlsZUlkIiA6ICJhZjQwMzQxMTk1YTg0ZDcwODFkNWNmNDE3MDM5ODJmNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBdHJ4dSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83M2M3YTVjMGJhODRiM2Q0NjI1OWY1YWZhZWM4ZTNlMmYxNTUwZTkyYmViMGUzYWIzNWRiZWQ5ZjNiOGVjMTIiCiAgICB9CiAgfQp9", "T44C0pzHcWZ3N0b7uTKV2ctwSukSEEG3RsblCdybXec2njbqQBdj8qnuGpfLzoC7JaDwptr6XN8BJN1Q9IfpswxddD6EeMvjBf/YFgTKmeGgvFKQ1aV0Nu/Pp4Ev0/s6ayn1x65o64vi0WFH8J7iRBmk++XlfAdQCaHBHBfyePXkOWpI2IfmvQcdU/QzHX7czRwCVaMQ+3EIa2N7AoVGR22RtbgIhaJnY3ghzAK6Eg7YQTuVHndA8clqZRjXKcVm5QcM3EYLjyqPdjNF6v37PTpCcdRDimGWOveTGhIJlAwNTuIHnLVGekDLZI9GLGnQcc/HU7zJ4iarzvrgOBLdpKrjCZae/KKpj22rMEHVCotcZinlNkZI66IlCijFlmS0HEI7cNHj4C4LdADDWpRtwx+nTj6hSLkta8C/7Ss9ihktwDxCy4RSd/qms1Shta1oZs3hJIXz2TG6xmQMaTW6GJLJM5gk8tva5t2C72GP8hvSuNbq6NMASowSM81W5Vw/oDq3DdlcWaZWHAWr6hev01ZyrGQzOsb+zx93tc+HjvCCArU67Cpr2stF1szNH1qdtkVuWe+8+ubDFujAjAu7s2typIntAJfl32sclXoqRTMURbwVNuJV/w7UdqN9GML4bwVXJBL136LMPrilj6/0zqRm2SkjanHafnr22rjGhMo="));
        assert !platform.isClient();
        this.spawner = spawner;
        applyArmor(vanillaPlayer.getInventory());
    }

    @Override
    public void tick() {
        super.tick();
        ServerPlayer player = (ServerPlayer) getVanillaPlayer();
        Inventory inventory = player.getInventory();
        if (player.touchingUnloadedChunk() || player.distanceToSqr(spawner) > 120*120 || !player.server.getPlayerList().getPlayers().contains(spawner)) {
            destroy();
            return;
        }

        boolean shieldCooldown = getVanillaPlayer().getCooldowns().isOnCooldown(Items.SHIELD);

        player.lookAt(EntityAnchorArgument.Anchor.EYES, spawner.getEyePosition());
        inventory.setItem(40, new ItemStack(Items.TOTEM_OF_UNDYING));

        if (player.invulnerableTime > 0) {
            healTicks = 0;
        }
        ItemStack shield = new ItemStack(Items.SHIELD);
        shield.enchant(Enchantments.MENDING, 1);
        shield.getTag().putBoolean("Unbreakable", true);
        inventory.setItem(inventory.selected, shield);
        interact(!shieldCooldown && noShieldTicks <= 0);

        if (healTicks > 200) {
            player.setHealth(player.getMaxHealth());
            healTicks = 0;
        }

        healTicks++;
        if (!shieldCooldown && noShieldTicks > 0) {
            noShieldTicks--;
        }
        if (shieldCooldown && noShieldTicks <= 0) {
            noShieldTicks = ThreadLocalRandom.current().nextInt(42);
        }
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

    @Override
    public ServerPlayer getSpawner() {
        return spawner;
    }
}
