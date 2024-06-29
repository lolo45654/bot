package blade.impl.action.pvp.crystal;

import blade.BladeMachine;
import blade.Bot;
import blade.impl.action.pvp.PvP;
import blade.inventory.BotInventory;
import net.minecraft.world.item.Items;

public interface Crystal extends PvP {
    static void register(BladeMachine blade) {
        blade.addAction(new PearlTowardsEnemy());
        blade.addAction(new DestroyCrystal());
        blade.addAction(new PlaceObsidian());
        blade.addAction(new PlaceCrystal());
    }

    default double getCrystalScore(Bot bot) {
        BotInventory inv = bot.getInventory();
        double score = 0.0;
        score += inv.findFirst(stack -> stack.is(Items.END_CRYSTAL)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.OBSIDIAN)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.RESPAWN_ANCHOR)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.GLOWSTONE)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.ENDER_PEARL)) != null ? 1 : 0;
        score += inv.findFirst(stack -> stack.is(Items.TOTEM_OF_UNDYING)) != null ? 1 : 0;
        return score / 3;
    }
}
