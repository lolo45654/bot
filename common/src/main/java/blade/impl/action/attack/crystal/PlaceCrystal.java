package blade.impl.action.attack.crystal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreState;
import blade.util.BotMath;
import blade.util.blade.BladeAction;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static blade.impl.action.attack.Attack.isPvPSatisfied;
import static blade.impl.action.attack.crystal.Crystal.getCrystalScore;

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
            bot.getInventory().setSelectedSlot(crystalSlot.getHotbarIndex());
        }

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
        return isPvPSatisfied(bot) && (crystalPos = CrystalPosition.get(bot, crystalPos)) != null;
    }

    @Override
    public void getResult(ScoreState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        Level world = bot.getVanillaPlayer().level();
        List<EndCrystal> endCrystals = world.getEntitiesOfClass(EndCrystal.class, crystalPos.crystalAABB());
        BlockState obsidian = world.getBlockState(crystalPos.obsidian());
        return getCrystalScore(bot) +
                (Math.max(Math.min(crystalPos.confidence() / 3, 3), 0)) +
                (endCrystals.isEmpty() ? -12 : 2) +
                (obsidian.isAir() ? -12 : 0) +
                (getCrystalSlot() == null ? -12 : 0);
    }

    @Override
    public String toString() {
        return String.format("place_crystal[pos=%s]", crystalPos);
    }
}
