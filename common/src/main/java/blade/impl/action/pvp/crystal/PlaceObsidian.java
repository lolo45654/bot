package blade.impl.action.pvp.crystal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreAction;
import blade.state.BladeState;
import blade.util.BotMath;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PlaceObsidian extends ScoreAction implements Crystal {
    private CrystalPosition crystalPos = null;

    public Slot getObsidianSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.OBSIDIAN), SlotFlag.OFF_HAND, SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot obsidianSlot = getObsidianSlot();
        if (obsidianSlot.isHotBar()) {
            bot.getInventory().setSelectedSlot(obsidianSlot.getHotBarIndex());
        }

        float time = ConfigKeys.getDifficultyReversedCubic(bot) * 0.3f;
        Vec3 lookAt = crystalPos.placeAgainst();
        Vec3 eyePos = bot.getVanillaPlayer().getEyePosition();
        Vec3 direction = lookAt.subtract(eyePos);
        float yaw = BotMath.getYaw(direction);
        float pitch = BotMath.getPitch(direction);
        bot.lookRealistic(yaw, pitch, (tick % time) / time, 0);
        if (tick >= time) {
            bot.interact();
        }
    }

    @Override
    public boolean isSatisfied() {
        crystalPos = CrystalPosition.get(bot);
        return isPvPSatisfied(bot) && crystalPos != null;
    }

    @Override
    public void getResult(BladeState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        Level world = bot.getVanillaPlayer().level();
        BlockState obsidian = world.getBlockState(crystalPos.obsidian());
        return getCrystalScore(bot) + crystalPos.confidence() +
                (getObsidianSlot() == null ? -8 : 0) +
                (obsidian.isAir() ? 2 : 0);
    }
}
