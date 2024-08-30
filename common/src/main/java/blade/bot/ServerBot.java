package blade.bot;

import blade.Bot;
import blade.platform.Platform;
import blade.util.BotMath;
import blade.util.ItemUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ServerBot extends Bot implements IServerBot {
    private final ServerPlayer spawner;
    private ServerBotSettings settings;

    private int healTicks = 0;
    private int noShieldTicks = 0;
    private boolean prevShieldCooldown = false;
    private Vec3 prevPosition = null;


    public ServerBot(Player vanillaPlayer, Platform platform, ServerPlayer spawner, ServerBotSettings settings) {
        super(vanillaPlayer, platform);
        this.spawner = spawner;
        this.settings = settings;
        applyArmor(vanillaPlayer.getInventory());
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

        for (Holder<MobEffect> effect : settings.effects) {
            player.addEffect(new MobEffectInstance(effect, 3, 1));
        }

        boolean moving = settings.moveTowardsSpawner && player.distanceToSqr(spawner) > 2 * 2;
        Vec3 currentPosition = player.position();
        setMoveForward(moving);
        if (moving && prevPosition != null && (prevPosition.x == currentPosition.x || prevPosition.z == currentPosition.z)) {
            jump();
        }
        prevPosition = currentPosition;

        FoodData food = player.getFoodData();
        food.setExhaustion(0.0f);
        food.setFoodLevel(20);
        food.setSaturation(20.0f);

        player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE).setBaseValue(settings.reach);
        applyArmor(vanillaPlayer.getInventory());
    }

    protected void tickShield(ServerPlayer player, Inventory inventory) {
        boolean shieldCooldown = getVanillaPlayer().getCooldowns().isOnCooldown(Items.SHIELD);
        boolean shielding = !shieldCooldown && noShieldTicks <= 0;

        ItemStack shield = new ItemStack(Items.SHIELD);
        shield.enchant(ItemUtil.getEnchantment(Enchantments.MENDING, player.level()), 1);
        shield.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
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
            noShieldTicks = random.nextInt(42);
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
        Level world = vanillaPlayer.level();
        inventory.setItem(39, settings.armor.get(EquipmentSlot.HEAD).stack(EquipmentSlot.HEAD, world));
        inventory.setItem(38, settings.armor.get(EquipmentSlot.CHEST).stack(EquipmentSlot.CHEST, world));
        inventory.setItem(37, settings.armor.get(EquipmentSlot.LEGS).stack(EquipmentSlot.LEGS, world));
        inventory.setItem(36, settings.armor.get(EquipmentSlot.FEET).stack(EquipmentSlot.FEET, world));
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
