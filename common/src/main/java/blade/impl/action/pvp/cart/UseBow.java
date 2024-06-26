package blade.impl.action.pvp.cart;

import blade.impl.ConfigKeys;
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

public class UseBow extends ScoreAction {
    @Override
    public void onTick() {
        MineCartPosition cartPos = MineCartPosition.get(bot);
        Pair<float[], Integer> bow = BotMath.getBowChargeTicks(bot.getVanillaPlayer().position(), cartPos.position());
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
    public void getResult(BladeState result) {
    }

    @Override
    public double getScore() {
        Slot hotBar = getHotBar();
        bot.getBlade().set(ConfigKeys.MINE_CART_POSITION, null);
        return 0 +
                MineCartPosition.get(bot).confidence() +
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