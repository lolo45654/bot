package me.loloed.bot.api.util;

import me.loloed.bot.api.util.fake.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

public class ClientSimulator {
    public static Method LivingEntity$updatingUsingItem;

    static {
        try {
            LivingEntity$updatingUsingItem = LivingEntity.class.getDeclaredMethod("updatingUsingItem");
            LivingEntity$updatingUsingItem.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
        }
    }

    public static HitResult findCrosshairTarget(Entity camera, double blockInteractionRange, double entityInteractionRange, float tickDelta) {
        double d = Math.max(blockInteractionRange, entityInteractionRange);
        double e = Mth.square(d);
        Vec3 eyePos = camera.getEyePosition(tickDelta);
        HitResult hitResult = camera.pick(d, tickDelta, false);
        double f = hitResult.getLocation().distanceToSqr(eyePos);
        if (hitResult.getType() != HitResult.Type.MISS) {
            e = f;
            d = Math.sqrt(f);
        }

        Vec3 viewVector = camera.getViewVector(tickDelta);
        Vec3 vec = eyePos.add(viewVector.x * d, viewVector.y * d, viewVector.z * d);
        float g = 1.0F;
        Vec3 tmp = viewVector.multiply(d, d, d);
        AABB box = camera.getBoundingBox().expandTowards(tmp.x, tmp.y, tmp.z).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(camera, eyePos, vec, box, (entity) -> {
            return !entity.isSpectator() && entity.canBeHitByProjectile();
        }, e);
        return entityHitResult != null && entityHitResult.getLocation().distanceToSqr(eyePos) < f ? ensureTargetInRange(entityHitResult, eyePos, entityInteractionRange) : ensureTargetInRange(hitResult, eyePos, blockInteractionRange);
    }

    private static HitResult ensureTargetInRange(HitResult hitResult, Vec3 cameraPos, double interactionRange) {
        Vec3 vec3d = hitResult.getLocation();
        if (!vec3d.closerThan(cameraPos, interactionRange)) {
            Vec3 vec3d2 = hitResult.getLocation();
            Direction direction = Direction.getNearest(vec3d2.x - cameraPos.x, vec3d2.y - cameraPos.y, vec3d2.z - cameraPos.z);
            return BlockHitResult.miss(vec3d2, direction, BlockPos.containing(vec3d2));
        } else {
            return hitResult;
        }
    }
    
    private final ServerPlayer player;
    private boolean forwardKey = false;
    private boolean leftKey = false;
    private boolean rightKey = false;
    private boolean backKey = false;
    private boolean useKey = false;
    private boolean attackKey = false;
    private int useTimes = 0;
    private int attackTimes = 0;
    private int itemUseCooldown = 0;
    private HitResult crosshairTarget = null;
    private int useItemRemaining = 0;
    private ItemStack previousStack = null;
    private boolean breakingBlock = false;
    private float currentBreakingProgress;
    private int attackCooldown = 0;
    private int blockBreakingCooldown = 0;
    private BlockPos currentBreakingPos;
    private BiConsumer<Vec3i, Float> blockDamageHandler = null;

    private float blockReach = 3.5f;
    private float entityReach = 3.0f;

    public ClientSimulator(ServerPlayer player) {
        this.player = player;
    }

    public void tick() {
        crosshairTarget = findCrosshairTarget(player, blockReach, entityReach, 1f);
        ensureVariables();
        tickMove();
        tickUsingItem();
        tickMouse();
        // player.getCooldowns().tick();
        if (!(player instanceof FakePlayer)) {
            player.tick();
            player.doTick();
        }
        // player.aiStep();
        if (itemUseCooldown > 0) {
            itemUseCooldown--;
        }
    }

    public void ensureVariables() {
        player.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6);
    }

    public void tickMove() {
        player.xxa = 0;
        if (rightKey) {
            player.xxa += 1;
        }
        if (leftKey) {
            player.xxa -= 1;
        }

        player.zza = 0;
        if (forwardKey) {
            player.zza += 1;
        }
        if (backKey) {
            player.zza -= 1;
        }
    }

    public void tickUsingItem() {
        try {
            LivingEntity$updatingUsingItem.invoke(player);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void tickMouse() {
        boolean hasAttacked = false;
        if (player.isUsingItem() && !useKey) {
            ItemStack item = player.getUseItem();
            if (!item.isEmpty()) {
                item.releaseUsing(player.level(), player, useItemRemaining);
                if (item.useOnRelease()) {
                    tickUsingItem();
                }
            }
            player.stopUsingItem();
        } else if (!player.isUsingItem()) {
            while (useTimes > 0) {
                doItemUse();
                useTimes--;
            }
            while (attackTimes > 0) {
                doAttack();
                attackTimes--;
            }

            if (useKey && itemUseCooldown == 0) {
                doItemUse();
            }
        }
        handleBlockBreaking(!hasAttacked && attackKey);

        previousStack = player.getItemInHand(InteractionHand.MAIN_HAND);
    }

    private void doItemUse() {
        if (breakingBlock) return;
        if (crosshairTarget == null) return;
        itemUseCooldown = 4;
        InteractionHand[] hands = InteractionHand.values();
        for (InteractionHand hand : hands) {
            ItemStack stack = player.getItemInHand(hand);
            if (crosshairTarget.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) crosshairTarget;
                Entity entity = entityHitResult.getEntity();
                InteractionResult result = entity.interactAt(player, entityHitResult.getLocation(), hand);
                if (!result.consumesAction()) {
                    result = entity.interact(player, hand);
                }

                if (result.consumesAction()) {
                    if (result.shouldSwing()) player.swing(hand);
                    return;
                }
            } else if (crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) crosshairTarget;
                InteractionResult result = interactBlockInternal(hand, blockHitResult);
                if (result.consumesAction()) {
                    if (result.shouldSwing()) player.swing(hand);
                    return;
                }
            }

            if (stack.isEmpty()) continue;

            if (player.getCooldowns().isOnCooldown(stack.getItem())) {
                continue;
            }

            int preUseTicks = player.getUseItemRemainingTicks();
            InteractionResultHolder<ItemStack> use = stack.use(player.level(), player, hand);
            int postUseTicks = player.getUseItemRemainingTicks();
            if (preUseTicks != postUseTicks) {
                useItemRemaining = postUseTicks;
            }
            if (stack != use.getObject()) {
                player.setItemInHand(hand, use.getObject());
            }
            InteractionResult result = use.getResult();
            if (!result.consumesAction()) continue;
            if (!result.shouldSwing()) return;
            player.swing(hand);
        }
    }

    private InteractionResult interactBlockInternal(InteractionHand hand, BlockHitResult hit) {
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            return InteractionResult.SUCCESS;
        }

        ItemStack handItem = player.getItemInHand(hand);

        if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty() && !player.isSecondaryUseActive()) {
            BlockState blockState = player.level().getBlockState(hit.getBlockPos());
            InteractionResult use = blockState.useWithoutItem(player.level(), player, hit);
            if (use.consumesAction()) {
                return use;
            }
        }
        if (handItem.isEmpty() || player.getCooldowns().isOnCooldown(handItem.getItem())) {
            return InteractionResult.PASS;
        }

        UseOnContext context = new UseOnContext(player, hand, hit);
        if (player.isCreative()) {
            int preUseAmount = handItem.getCount();
            InteractionResult result = handItem.useOn(context);
            handItem.setCount(preUseAmount);
            return result;
        }

        return handItem.useOn(context);
    }

    private boolean doAttack() {
        if (attackCooldown > 0) return false;
        if (crosshairTarget == null) return false;
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) crosshairTarget;
            if (player.isSpectator()) return false;
            player.attack(entityHitResult.getEntity());
            player.resetAttackStrengthTicker();
        } else if (crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) crosshairTarget;
            BlockPos blockPos = blockHitResult.getBlockPos();
            Level level = player.level();
            if (level.getBlockState(blockPos).isAir()) {
                player.swing(InteractionHand.MAIN_HAND);
                return false;
            }
            attackBlockInternal(blockPos, blockHitResult.getDirection());
            return level.getBlockState(blockPos).isAir();
        } else if (crosshairTarget.getType() == HitResult.Type.MISS) {
            attackCooldown = 10;
            player.resetAttackStrengthTicker();
        }
        player.swing(InteractionHand.MAIN_HAND);
        return false;
    }

    private void attackBlockInternal(BlockPos pos, Direction direction) {
        Level level = player.level();
        if (player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) return;
        if (!level.getWorldBorder().isWithinBounds(pos)) return;
        BlockState state = level.getBlockState(pos);
        if (player.isCreative()) {
            if (state.isAir()) return;
            breakBlock(level, state, pos);
            return;
        }

        if (breakingBlock && (pos.equals(currentBreakingPos) || ItemStack.isSameItemSameComponents(player.getItemInHand(InteractionHand.MAIN_HAND), previousStack))) {
            player.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, direction, level.getMaxBuildHeight(), -1);
        }

        player.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, direction, level.getMaxBuildHeight(), -1);
        if (state.isAir()) return;
        if (currentBreakingProgress == 0.0F) {
            state.attack(level, pos, player);
        }

        if (state.getDestroyProgress(player, level, pos) >= 1.0f) {
            breakBlock(level, state, pos);
        } else {
            breakingBlock = true;
            currentBreakingPos = pos;
            currentBreakingProgress = 0.0F;
        }
    }

    private void breakBlock(Level level, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        block.playerWillDestroy(level, pos, state, player);
        FluidState fluidState = level.getFluidState(pos);
        if (level.setBlock(pos, fluidState.createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE)) {
            block.destroy(level, pos, state);
        }
    }

    private void handleBlockBreaking(boolean breaking) {
        if (!breaking) {
            attackCooldown = 0;
        }

        if (attackCooldown > 0 || player.isUsingItem()) return;
        if (!breaking || crosshairTarget == null || crosshairTarget.getType() != HitResult.Type.BLOCK) {
            if (breakingBlock) {
                breakingBlock = false;
                currentBreakingProgress = 0.0F;
                player.resetAttackStrengthTicker();
            }
            return;
        }

        BlockHitResult blockHitResult = (BlockHitResult) crosshairTarget;
        BlockPos pos = blockHitResult.getBlockPos();
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;
        Direction direction = blockHitResult.getDirection();
        if (!updateBlockBreakingProgress(level, state, pos, direction)) return;
        player.swing(InteractionHand.MAIN_HAND);
    }

    private boolean updateBlockBreakingProgress(Level level, BlockState state, BlockPos pos, Direction direction) {
        if (blockBreakingCooldown > 0) {
            blockBreakingCooldown--;
            return true;
        }

        if (player.isCreative()) {
            blockBreakingCooldown = 5;
            breakBlock(level, state, pos);
            return true;
        }

        if (!pos.equals(currentBreakingPos) || !ItemStack.isSameItemSameComponents(previousStack, player.getItemInHand(InteractionHand.MAIN_HAND))) {
            attackBlockInternal(pos, direction);
            return true;
        }

        if (state.isAir()) {
            breakingBlock = false;
            return false;
        }

        currentBreakingProgress += state.getDestroyProgress(player, level, pos);

        if (currentBreakingProgress < 1.0F) {
            if (blockDamageHandler != null) {
                blockDamageHandler.accept(pos, currentBreakingProgress);
                // player.getBukkitEntity().sendBlockDamage(new Location(player.getBukkitEntity().getWorld(), pos.getX(), pos.getY(), pos.getZ()), currentBreakingProgress);
            }
            return true;
        }
        player.gameMode.handleBlockBreakAction(pos, ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, direction, level.getMaxBuildHeight(), -1);
        breakingBlock = false;
        breakBlock(level, state, pos);
        currentBreakingProgress = 0.0F;
        blockBreakingCooldown = 5;
        return true;
    }

    public void setBlockDamageHandler(BiConsumer<Vec3i, Float> handler) {
        this.blockDamageHandler = handler;
    }

    public void setForward(boolean forwardKey) {
        this.forwardKey = forwardKey;
    }

    public void setLeft(boolean leftKey) {
        this.leftKey = leftKey;
    }

    public void setRight(boolean rightKey) {
        this.rightKey = rightKey;
    }

    public void setBack(boolean backKey) {
        this.backKey = backKey;
    }

    public void setUse(boolean useKey) {
        this.useKey = useKey;
    }

    public void setAttack(boolean attackKey) {
        this.attackKey = attackKey;
    }

    public void use() {
        useTimes++;
    }

    public void attack() {
        attackTimes++;
    }

    public void setBlockReach(float blockReach) {
        this.blockReach = blockReach;
    }

    public float getBlockReach() {
        return blockReach;
    }

    public void setEntityReach(float entityReach) {
        this.entityReach = entityReach;
    }

    public float getEntityReach() {
        return entityReach;
    }

    public HitResult getCrossHairTarget() {
        return crosshairTarget;
    }
}
