package me.loloed.bot.api.blade.impl.action.totem;

import me.loloed.bot.api.blade.planner.astar.AStarAction;
import me.loloed.bot.api.inventory.BotInventory;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import me.loloed.bot.api.material.MaterialFlag;

public interface ReTotem {
    class OffHand extends AStarAction implements ReTotem {
        @Override
        public void prepare() {
            ProducingKey<Boolean> hasTotem = expectations.getInventory().hasItem(MaterialFlag.RESURRECTING);
            expectations.set(hasTotem, true);
            expectProduced(hasTotem);
            ProducingKey<Boolean> hasTotemOffhand = expectations.getInventory().hasItem(Slot.fromOffHand(), MaterialFlag.RESURRECTING);
            expectations.set(hasTotemOffhand, false);
            expectProduced(hasTotemOffhand);
            results.set(hasTotemOffhand, true);
        }

        @Override
        public void onTick() {
            BotInventory inventory = bot.getInventory();
            Slot slot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.RESURRECTING), SlotFlag.MAIN, SlotFlag.ARMOR, SlotFlag.HOT_BAR);
            bot.chain("ReTotem#offHand")
                    .startInventoryAction()
                    .delay(2)
                    .move(slot, Slot.fromOffHand())
                    .endInventoryAction()
                    .finish();
        }

        @Override
        public double getCost() {
            return 0.1;
        }
    }

    class HotBar extends AStarAction implements ReTotem {
        @Override
        public void prepare() {
            ProducingKey<Boolean> hasTotem = expectations.getInventory().hasItem(MaterialFlag.RESURRECTING);
            expectations.set(hasTotem, true);
            expectProduced(hasTotem);
            ProducingKey<Boolean> hasTotemHotBar = expectations.getInventory().hasItem(Slot.fromHotBar(InvLayout.TOTEM_SLOT.getValue(state)), MaterialFlag.RESURRECTING);
            expectations.set(hasTotemHotBar, false);
            expectProduced(hasTotemHotBar);

            results.set(hasTotemHotBar, true);
        }

        @Override
        public void onTick() {
            BotInventory inventory = bot.getInventory();
            Slot slot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.RESURRECTING), SlotFlag.MAIN, SlotFlag.ARMOR, SlotFlag.HOT_BAR);
            bot.chain("ReTotem#hotBar")
                    .startInventoryAction()
                    .delay(2)
                    .move(slot, Slot.fromHotBar(InvLayout.TOTEM_SLOT.getValue(state)))
                    .delay(1)
                    .endInventoryAction()
                    .finish();
        }

        @Override
        public double getCost() {
            return 1;
        }
    }
}
