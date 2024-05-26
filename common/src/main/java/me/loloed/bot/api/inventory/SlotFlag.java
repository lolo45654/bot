package me.loloed.bot.api.inventory;

import java.util.function.Predicate;

public enum SlotFlag {
    MAIN(Slot::isMain),
    HOT_BAR(Slot::isHotBar),
    ARMOR(Slot::isArmor),
    OFF_HAND(Slot::isOffHand);

    private final Predicate<Slot> matcher;

    SlotFlag(Predicate<Slot> matcher) {
        this.matcher = matcher;
    }

    public boolean matchesSlot(Slot slot) {
        return matcher.test(slot);
    }
}
