package blade.impl.action.attack.crystal;

import blade.debug.visual.VisualBox;
import blade.debug.visual.VisualText;
import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.utils.BotMath;
import blade.utils.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.isAttackSatisfied;

public class PlaceObsidian extends BladeAction implements Crystal {
    private CrystalPosition crystalPos = null;

    public Slot getObsidianSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.OBSIDIAN), SlotFlag.OFF_HAND, SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot obsidianSlot = getObsidianSlot();
        if (obsidianSlot == null) return;
        if (obsidianSlot.isHotbar()) {
            bot.getInventory().setSelectedSlot(obsidianSlot.hotbarIndex());
        }

        bot.getBlade().addVisualDebug(new VisualBox(new AABB(crystalPos.obsidian()), 0.3f, 0.8f, 0.3f));
        bot.getBlade().addVisualDebug(new VisualText(Vec3.atCenterOf(crystalPos.obsidian().above()), String.format("C: %.3f", crystalPos.confidence())));

        Vec3 lookAt = crystalPos.placeAgainst();
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 direction = lookAt.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.setRotationTarget(yaw, pitch, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 100);

        if (bot.getCrossHairTarget() instanceof BlockHitResult blockHitResult && blockHitResult.getBlockPos().relative(blockHitResult.getDirection()).equals(crystalPos.obsidian())) {
            bot.interact();
        }
    }

    @Override
    public boolean isSatisfied() {
        crystalPos = CrystalPosition.get(bot, crystalPos);
        return isAttackSatisfied(bot) && getObsidianSlot() != null && crystalPos != null &&
                bot.getVanillaPlayer().level().getBlockState(crystalPos.obsidian()).isAir();
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double targetY = target.getDeltaMovement().y;

        return state.getValue(StateKeys.CRYSTAL_MODE) / 2 +
                (Math.max(Math.min(crystalPos.confidence(), 1), 0)) +
                Math.min(targetY * 4, 0.4);
    }

    @Override
    public String toString() {
        return String.format("place_obsidian[pos=%s]", crystalPos);
    }
}
