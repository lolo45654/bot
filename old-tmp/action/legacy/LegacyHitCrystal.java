package me.loloed.bot.api.blade.impl.action.legacy;

import me.loloed.bot.api.blade.planner.astar.AStarAction;
import me.loloed.bot.api.blade.state.BladeState;
import me.loloed.bot.api.blade.state.impl.StateEntity;
import me.loloed.bot.api.blade.state.impl.StateVector;
import me.loloed.bot.api.blade.state.StateKey;
import me.loloed.bot.api.blade.state.value.*;
import me.loloed.bot.api.entity.Entity;
import me.loloed.bot.api.entity.EntityFlag;
import me.loloed.bot.api.inventory.BotInventory;
import me.loloed.bot.api.inventory.Slot;
import me.loloed.bot.api.inventory.SlotFlag;
import me.loloed.bot.api.material.MaterialFlag;
import me.loloed.bot.api.util.Vector;
import me.loloed.bot.api.blade.impl.util.HitCrystalPosition;

import java.util.Optional;

public interface LegacyHitCrystal {
    // TODO: 06.05.24 Replace PLACED_OBSIDIAN and CRYSTALS with World#hasFlags World#getNearestEntities#!isEmpty

    //StateKey<Boolean> PLACED_OBSIDIAN = StateKey.key("HitCrystal#PLACED_OBSIDIAN", Boolean.class)
//            .setDefaultValue(new FixedValue<>(false));

//    StateKey<Integer> CRYSTALS = StateKey.key("HitCrystal#CRYSTALS", Integer.class)
//            .setDefaultValue(new FixedValue<>(0));

    ProducingKey<StateVector> FIXED_POSITION = StateKey.producingKey("HitCrystal#FIXED_POSITION", StateVector.class)
            .setDefaultValue(new FixedValue<>(StateVector.dummy(EntityTarget.TARGET.getDefaultValue().getPosition().get(HitCrystalPosition.HIT_CRYSTAL_STATE_POSITION).getName())))
            .setProducer((bot, state) -> new AtomicValue<>(EntityTarget.TARGET.getValue(state).getPosition().get(HitCrystalPosition.HIT_CRYSTAL_STATE_POSITION).getValue(bot, state)));

    double CRYSTAL_DISTANCE = 0.08;

    private static void reset(BladeState state, AStarAction next) {
        if (next instanceof LegacyHitCrystal) return;
        state.remove(FIXED_POSITION);
    }

    class LegacyHitEnemy extends AStarAction implements LegacyHitCrystal {
        @Override
        public void prepare() {
            StateEntity entity = EntityTarget.TARGET.getDefaultValue();
            ProducingKey<Double> distanceSquared = entity.getPosition().distanceSquared(expectations.getBot().getPosition());
            expectations.set(distanceSquared, new LessThanValue<>(3.0 * 3.0));
            expectProduced(distanceSquared);

            ProducingKey<Integer> hurtTime = entity.getHurtTime();
            expectProduced(hurtTime);
            results.set(hurtTime, 10);
            ProducingKey<Double> velocityY = EntityTarget.TARGET.getDefaultValue().getVelocity().getY();
            results.set(velocityY, new FixedValue<>(0.04));
            expectProduced(velocityY);
            ProducingKey<Double> y = EntityTarget.TARGET.getDefaultValue().getY();
            results.set(y, new SuppliedValue<>(() -> FIXED_POSITION.getValue(bot, state).up().getY().getValue(bot, state)));
            expectProduced(y);
        }

        @Override
        public void onTick() {
            BotInventory inventory = bot.getInventory();
            Slot slot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.KNOCKBACK_ONE), SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
            if (slot == null && tick == 0) {
                bot.setSprint(true);
                bot.setMoveForward(true);
                return;
            }
            if (slot != null && slot.isHotBar()) {
                inventory.setSelectedSlot(slot.getHotBarIndex());
            }
            StateEntity stateEntity = EntityTarget.TARGET.getValue(state);
            Optional<Entity> target = stateEntity.toRealEntity(bot, state);
            if (target.isEmpty()) return;
            bot.setSprint(false);
            bot.setMoveForward(false);
            Vector closestPosition = target.get().getBoundingBox().getClosestPosition(bot.getHeadPosition());
            bot.lookAt(closestPosition);
            if (target.get().getHurtTime() == 0) bot.attack();
        }

        @Override
        public double getCost() {
            return 0.7;
        }

        @Override
        public void onRelease(AStarAction next) {
            super.onRelease(next);
            LegacyHitCrystal.reset(state, next);
        }
    }

    class PlaceObsidian extends AStarAction implements LegacyHitCrystal {
        @Override
        public void prepare() {
            ProducingKey<Double> velocityY = EntityTarget.TARGET.getDefaultValue().getVelocity().getY();
            expectations.set(velocityY, new MoreThanValue<>(0.03));
            expectProduced(velocityY);

            StateVector obsidianPos = FIXED_POSITION.getDefaultValue();
            ProducingKey<Boolean> obsidianPlaced = obsidianPos.hasBlockFlags(MaterialFlag.CRYSTAL_PLACEABLE);
            expectations.set(obsidianPlaced, false);
            expectProduced(obsidianPlaced);

            ProducingKey<Boolean> hasObsidian = expectations.getInventory().hasItem(MaterialFlag.CRYSTAL_PLACEABLE);
            expectations.set(hasObsidian, true);
            expectProduced(hasObsidian);

            results.set(obsidianPlaced, true);
        }

        @Override
        public void onTick() {
            bot.setSprint(false);
            bot.setMoveForward(false);
            if (tick == 1) {
                bot.interact();
                return;
            }
            Vector pos = FIXED_POSITION.getValue(bot, state).toRealVector(bot, state);
            BotInventory inventory = bot.getInventory();
            Slot slot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.CRYSTAL_PLACEABLE), SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
            if (!slot.isHotBar()) {
                bot.chain("HitCrystal.PlaceObsidian#prepareObsidian")
                        .startInventoryAction()
                        .delay(4)
                        .move(slot, Slot.fromHotBar(4))
                        .delay(2)
                        .endInventoryAction()
                        .finish();
                return;
            }
            inventory.setSelectedSlot(slot.getHotBarIndex());
            bot.lookAt(pos);
        }

        @Override
        public double getCost() {
            return 0.6;
        }

        @Override
        public void onRelease(AStarAction next) {
            super.onRelease(next);
            LegacyHitCrystal.reset(state, next);
        }
    }

    class PlaceCrystalLegacy extends AStarAction implements LegacyHitCrystal {

        @Override
        public void prepare() {
            expectProduced(FIXED_POSITION);
            StateEntity target = EntityTarget.TARGET.getDefaultValue();
            expectations.set(target.getY(), new CustomComparingValue<>(-1.0, that -> {
                return (int) Math.floor(that.getValue()) >= FIXED_POSITION.getValue(bot, state).toRealVector(bot, state).up().getBlockY();
            }));

            StateVector obsidianPos = FIXED_POSITION.getDefaultValue();
            ProducingKey<Boolean> obsidianPlaced = obsidianPos.hasBlockFlags(MaterialFlag.CRYSTAL_PLACEABLE);
            expectations.set(obsidianPlaced, true);
            expectProduced(obsidianPlaced);


            StateVector crystalPos = obsidianPos.up();
            ProducingKey<Boolean> crystalPlaced = crystalPos.hasEntityNearby(CRYSTAL_DISTANCE, EntityFlag.END_CRYSTAL);
            expectations.set(crystalPlaced, false);
            expectProduced(crystalPlaced);

            ProducingKey<Boolean> hasCrystal = expectations.getInventory().hasItem(MaterialFlag.CRYSTAL);
            expectations.set(hasCrystal, true);
            expectProduced(hasCrystal);

            results.set(crystalPlaced, true);
        }

        @Override
        public void onTick() {
            BotInventory inventory = bot.getInventory();
            Slot slot = inventory.findFirst(material -> material.hasFlag(MaterialFlag.CRYSTAL), SlotFlag.HOT_BAR, SlotFlag.MAIN, SlotFlag.OFF_HAND, SlotFlag.ARMOR);
            if (!slot.isHotBar()) {
                bot.chain("HitCrystal.PlaceCrystal#prepareCrystal")
                        .startInventoryAction()
                        .delay(4)
                        .move(slot, Slot.fromHotBar(1))
                        .delay(2)
                        .endInventoryAction()
                        .finish();
                return;
            }
            inventory.setSelectedSlot(slot.getHotBarIndex());
            bot.lookAt(FIXED_POSITION.getValue(bot, state).toRealVector(bot, state));
            bot.interact();
        }

        @Override
        public double getCost() {
            return 0.3;
        }

        @Override
        public void onRelease(AStarAction next) {
            super.onRelease(next);
            LegacyHitCrystal.reset(state, next);
        }
    }

    class BreakCrystalLegacy extends AStarAction implements LegacyHitCrystal {

        @Override
        public void prepare() {
            expectProduced(FIXED_POSITION);
            ProducingKey<Integer> hurtTime = EntityTarget.TARGET.getDefaultValue().getHurtTime();
            expectations.set(hurtTime, new MoreThanValue<>(0));
            expectProduced(hurtTime);

            StateVector crystalPos = FIXED_POSITION.getDefaultValue().up();
            ProducingKey<Boolean> crystalPlaced = crystalPos.hasEntityNearby(CRYSTAL_DISTANCE, EntityFlag.END_CRYSTAL);
            expectations.set(crystalPlaced, true);
            expectProduced(crystalPlaced);

            StateEntity target = EntityTarget.TARGET.getDefaultValue();
            results.set(target.isDead(), true);
        }

        @Override
        public void onTick() {
            bot.lookAt(FIXED_POSITION.getValue(bot, state).toRealVector(bot, state).up());
            bot.attack();
        }

        @Override
        public double getCost() {
            return 1.2 + EntityTarget.TARGET.getDefaultValue().toRealEntity(bot, state).get().getPosition().distanceSquared(FIXED_POSITION.getValue(bot, state).toRealVector(bot, state)) / 4;
        }

        @Override
        public void onRelease(AStarAction next) {
            super.onRelease(next);
            LegacyHitCrystal.reset(state, next);
        }
    }
}
