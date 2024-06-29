package blade.impl.action.pvp.cart;

import blade.impl.StateKeys;
import blade.impl.action.pvp.PvP;
import blade.impl.util.MineCartPosition;
import blade.inventory.Slot;
import blade.inventory.SlotFlag;
import blade.planner.score.ScoreAction;
import blade.state.BladeState;
import blade.util.BotMath;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class UseBow extends ScoreAction implements PvP {
    private MineCartPosition cartPosition = null;

    @Override
    public void onTick() {
        Pair<float[], Integer> bow = BotMath.getBowChargeTicks(bot.getVanillaPlayer().position(), cartPosition.position());
        Slot hotBar = getHotBar();
        if (bow == null || hotBar == null) return;
        float[] rotation = bow.getFirst();
        bot.setYaw(rotation[0]);
        bot.setPitch(rotation[1]);
        bot.getInventory().setSelectedSlot(hotBar.getHotBarIndex());
        bot.interact(true);
        if (tick >= bow.getSecond()) {
            bot.interact(false);
        }
    }

    @Override
    public boolean isSatisfied() {
        return isPvPSatisfied(bot);
    }

    @Override
    public void getResult(BladeState result) {
        result.setValue(StateKeys.DOING_PVP, 1.0);
    }

    @Override
    public double getScore() {
        Slot hotBar = getHotBar();
        cartPosition = MineCartPosition.get(bot);
        return 0 +
                cartPosition.confidence() +
                (hotBar == null ? -5 : 0);
    }

    @Override
    public void onRelease(ScoreAction next) {
        super.onRelease(next);
        bot.interact(false);
    }

    private Slot getHotBar() {
        return bot.getInventory().findFirst(stack -> stack.is(Items.BOW) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAME, stack) > 0, SlotFlag.HOT_BAR);
    }
}
