package blade.impl.action.attack.crystal;

import blade.debug.visual.VisualBox;
import blade.debug.visual.VisualText;
import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static blade.impl.action.attack.Attack.isPvPSatisfied;

public class PlaceCrystal extends BladeAction implements Crystal {
    private CrystalPosition crystalPos = null;

    public Slot getCrystalSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.END_CRYSTAL), SlotFlag.OFF_HAND, SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot crystalSlot = getCrystalSlot();
        if (crystalSlot == null) return;
        if (crystalSlot.isHotbar()) {
            bot.getInventory().setSelectedSlot(crystalSlot.hotbarIndex());
        }

        bot.getBlade().addVisualDebug(new VisualBox(crystalPos.crystalAABB(), 0.9f, 0.8f, 0.9f, 0.5f));
        bot.getBlade().addVisualDebug(new VisualText(Vec3.atCenterOf(crystalPos.obsidian().above()), String.format("C: %.3f", crystalPos.confidence())));

        float time = ConfigKeys.getDifficultyReversedCubic(bot) * 0.3f;
        Vec3 lookAt = crystalPos.placeAgainst();
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 direction = lookAt.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, bot.getBlade().get(ConfigKeys.DIFFICULTY) * 0.2f);
        if (tick >= time) {
            bot.interact();
        }
    }

    @Override
    public boolean isSatisfied() {
        crystalPos = CrystalPosition.get(bot, crystalPos);
        if (crystalPos == null) return false;
        Level world = bot.getVanillaPlayer().level();
        List<EndCrystal> endCrystals = world.getEntitiesOfClass(EndCrystal.class, crystalPos.crystalAABB());

        return isPvPSatisfied(bot) && crystalPos != null && getCrystalSlot() != null &&
                !world.getBlockState(crystalPos.obsidian()).isAir() &&
                endCrystals.isEmpty();
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        LivingEntity target = bot.getBlade().get(ConfigKeys.TARGET);
        double targetY = target.getDeltaMovement().y;

        return state.getValue(StateKeys.CRYSTAL_MODE) +
                (Math.max(Math.min(crystalPos.confidence() / 3, 3), 0)) +
                Math.min(targetY * 4, 0.4);
    }

    @Override
    public String toString() {
        return String.format("place_crystal[pos=%s]", crystalPos);
    }
}
