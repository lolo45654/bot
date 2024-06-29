package blade.impl.action.pvp.crystal;

import blade.impl.ConfigKeys;
import blade.impl.StateKeys;
import blade.impl.util.CrystalPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreAction;
import blade.state.BladeState;
import blade.util.BotMath;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class PlaceCrystal extends ScoreAction implements Crystal {
    private CrystalPosition crystalPos = null;

    public Slot getCrystalSlot() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.END_CRYSTAL), SlotFlag.OFF_HAND, SlotFlag.HOT_BAR);
    }

    @Override
    public void onTick() {
        Slot crystalSlot = getCrystalSlot();
        if (crystalSlot.isHotBar()) {
            bot.getInventory().setSelectedSlot(crystalSlot.getHotBarIndex());
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
        return isPvPSatisfied(bot) && (crystalPos = CrystalPosition.get(bot)) != null;
    }

    @Override
    public void getResult(BladeState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        Level world = bot.getVanillaPlayer().level();
        List<EndCrystal> endCrystals = world.getEntitiesOfClass(EndCrystal.class, crystalPos.crystalAABB());
        BlockState obsidian = world.getBlockState(crystalPos.obsidian());
        return getCrystalScore(bot) + crystalPos.confidence() +
                (endCrystals.isEmpty() ? -12 : 2) +
                (obsidian.isAir() ? -8 : 2) +
                (getCrystalSlot() == null ? -12 : 0);
    }
}