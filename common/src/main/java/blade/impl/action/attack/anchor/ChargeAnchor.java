package blade.impl.action.attack.anchor;

import blade.debug.visual.VisualBox;
import blade.debug.visual.VisualText;
import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.AnchorPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.utils.BotMath;
import blade.utils.blade.BladeAction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static blade.impl.action.attack.Attack.isAttackSatisfied;

public class ChargeAnchor extends BladeAction implements Anchor {
    private AnchorPosition anchorPos = null;

    public Slot getGlowstoneSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.GLOWSTONE), SlotFlag.OFF_HAND, SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot glowstoneSlot = getGlowstoneSlot();
        if (glowstoneSlot == null) return;
        if (glowstoneSlot.isHotbar()) {
            bot.getInventory().setSelectedSlot(glowstoneSlot.hotbarIndex());
        }

        bot.getBlade().addVisualDebug(new VisualBox(new AABB(anchorPos.anchorPos()), 0.7f, 0.8f, 0.3f, 0.4f));
        bot.getBlade().addVisualDebug(new VisualText(Vec3.atCenterOf(anchorPos.anchorPos().above()), String.format("C: %.3f", anchorPos.confidence())));

        Vec3 lookAt = anchorPos.placeAgainst();
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 direction = lookAt.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.setRotationTarget(yaw, pitch, ConfigKeys.getDifficultyReversedCubic(bot) * 50);
        if (bot.getCrossHairTarget() instanceof BlockHitResult blockHitResult && blockHitResult.getBlockPos().equals(anchorPos.anchorPos())) {
            bot.interact();
        }
    }

    @Override
    public boolean isSatisfied() {
        anchorPos = AnchorPosition.get(bot, anchorPos);

        return isAttackSatisfied(bot) && anchorPos != null && getGlowstoneSlot() != null &&
                anchorPos.anchorState().is(Blocks.RESPAWN_ANCHOR) &&
                anchorPos.anchorState().getValue(RespawnAnchorBlock.CHARGE) == 0;
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        return state.getValue(StateKeys.CRYSTAL_MODE) / 2 +
                (Math.max(Math.min(anchorPos.confidence(), 1), 0));
    }

    @Override
    public String toString() {
        return String.format("charge_anchor[pos=%s]", anchorPos);
    }
}
