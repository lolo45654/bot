package me.loloed.bot.api.blade.impl.action;

import me.loloed.bot.api.blade.planner.astar.AStarAction;
import me.loloed.bot.api.blade.state.impl.StateEntity;
import me.loloed.bot.api.blade.state.StateKey;
import me.loloed.bot.api.blade.state.value.AtomicValue;
import me.loloed.bot.api.blade.state.value.FixedValue;
import me.loloed.bot.api.blade.state.value.LessThanValue;
import me.loloed.bot.api.inventory.BotInventory;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import me.loloed.bot.api.material.MaterialFlag;
import me.loloed.bot.api.util.MathUtil;
import me.loloed.bot.api.util.Pair;
import me.loloed.bot.api.util.Rotation;
import me.loloed.bot.api.blade.impl.util.MineCartPosition;

public interface MineCartEnemy {
    StateKey<Boolean> BOW_SHOT = StateKey.key("MineCartEnemy#BOW_SHOT", Boolean.class)
            .setDefaultValue(new FixedValue<>(false));

    StateKey<Boolean> RAIL_PLACED = StateKey.key("MineCartEnemy#RAIL_PLACED", Boolean.class)
            .setDefaultValue(new FixedValue<>(false));

    ProducingKey<MineCartPosition> FIXED_POSITION = StateKey.producingKey("MineCartEnemy#FIXED_POSITION", MineCartPosition.class)
            .setProducer((bot, state) -> new AtomicValue<>(EntityTarget.TARGET.getValue(state).getPosition().get(MineCartPosition.MINECART_POSITION).getValue(bot, state)));

    class Bow extends AStarAction implements MineCartEnemy {
        @Override
        public void prepare() {
            ProducingKey<Boolean> hasBow = expectations.getInventory().hasItem(MaterialFlag.BOW);
            expectProduced(hasBow);
            expectations.set(hasBow, true);
            ProducingKey<Boolean> hasArrow = expectations.getInventory().hasItem(MaterialFlag.ARROW);
            expectProduced(hasArrow);
            expectations.set(hasArrow, true);
            expectations.set(BOW_SHOT, false);

            StateEntity entity = EntityTarget.TARGET.getDefaultValue();
            ProducingKey<Double> distanceSquared = entity.getPosition().distanceSquared(expectations.getBot().getPosition());
            expectations.set(distanceSquared, new LessThanValue<>(3.0 * 3.0));
            results.set(BOW_SHOT, true);
        }

        private int drawTicks = 0;

        @Override
        public void onTick() {
            MineCartPosition cartPos = FIXED_POSITION.getValue(bot, state);
            Pair<Rotation, Integer> charge = MathUtil.getBowChargeTicks(bot.getHeadPosition(), cartPos.position());
            if (charge == null) bot.lookAt(cartPos.position());
            else bot.setPitch(charge.a().pitch()).and(bot.setYaw(charge.a().yaw()));
            BotInventory inventory = bot.getInventory();
            Slot bowSlot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.BOW), SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
            if (!bowSlot.isHotBar()) {
                bot.chain("MineCartEnemy.Bow#prepareBow")
                        .startInventoryAction()
                        .delay(4)
                        .move(bowSlot, Slot.fromHotBar(0))
                        .delay(2)
                        .endInventoryAction()
                        .finish();
                return;
            }
            if (inventory.getSelectedSlot() != bowSlot.getHotBarIndex()) drawTicks = 0;
            inventory.setSelectedSlot(bowSlot.getHotBarIndex());
            bot.interact(true);
            drawTicks++;
            if (drawTicks >= (charge == null ? 24 : charge.b() + 2)) {
                bot.interact(false);
                state.set(BOW_SHOT, true);
            }
        }

        @Override
        public double getCost() {
            return 1;
        }

        @Override
        public void onRelease(AStarAction next) {
            super.onRelease(next);
            bot.interact(false);
            drawTicks = 0;
            if (!(next instanceof MineCartEnemy)) {
                state.remove(FIXED_POSITION);
            }
        }
    }

    class PlaceRail extends AStarAction implements MineCartEnemy {
        @Override
        public void prepare() {
            ProducingKey<Boolean> hasRail = expectations.getInventory().hasItem(MaterialFlag.RAIL);
            expectProduced(hasRail);
            expectations.set(hasRail, true);
            expectations.set(RAIL_PLACED, false);

            results.set(RAIL_PLACED, true);
        }

        @Override
        public void onTick() {
            MineCartPosition cartPos = FIXED_POSITION.getValue(bot, state);
            bot.lookAt(cartPos.position());
            BotInventory inventory = bot.getInventory();
            Slot railSlot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.RAIL), SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
            if (!railSlot.isHotBar()) {
                bot.chain("MineCartEnemy.Bow#prepareRail")
                        .startInventoryAction()
                        .delay(4)
                        .move(railSlot, Slot.fromHotBar(1))
                        .delay(2)
                        .endInventoryAction()
                        .finish();
                return;
            }
            inventory.setSelectedSlot(railSlot.getHotBarIndex());
            bot.interact();
            state.set(RAIL_PLACED, true);
        }

        @Override
        public double getCost() {
            return 0.8;
        }

        @Override
        public void onRelease(AStarAction next) {
            super.onRelease(next);
            if (!(next instanceof MineCartEnemy)) {
                state.remove(FIXED_POSITION);
            }
        }
    }

    class PlaceCart extends AStarAction implements MineCartEnemy {
        @Override
        public void prepare() {
            ProducingKey<Boolean> hasCart = expectations.getInventory().hasItem(MaterialFlag.TNT_MINECART);
            expectProduced(hasCart);
            expectations.set(hasCart, true);
            expectations.set(BOW_SHOT, true);
            expectations.set(RAIL_PLACED, true);

            results.set(BOW_SHOT, false);
            results.set(RAIL_PLACED, false);
            StateEntity target = EntityTarget.TARGET.getDefaultValue();
            results.set(target.isDead(), true);
        }

        @Override
        public void onTick() {
            MineCartPosition cartPos = FIXED_POSITION.getValue(bot, state);
            bot.lookAt(cartPos.position());
            BotInventory inventory = bot.getInventory();
            Slot cartSlot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.TNT_MINECART), SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
            if (!cartSlot.isHotBar()) {
                bot.chain("MineCartEnemy.Bow#prepareCart")
                        .startInventoryAction()
                        .delay(4)
                        .move(cartSlot, Slot.fromHotBar(2))
                        .endInventoryAction()
                        .finish();
                return;
            }
            inventory.setSelectedSlot(cartSlot.getHotBarIndex());
            bot.interact();
            state.set(BOW_SHOT, false);
            state.set(RAIL_PLACED, false);
            state.remove(FIXED_POSITION);
        }

        @Override
        public double getCost() {
            return 6 - state.get(EntityTarget.TARGET).getValue().getPosition().get(MineCartPosition.MINECART_POSITION).get(bot, state).getValue().confidence();
        }

        @Override
        public void onRelease(AStarAction next) {
            super.onRelease(next);
            if (!(next instanceof MineCartEnemy)) {
                state.remove(FIXED_POSITION);
            }
        }
    }
}
