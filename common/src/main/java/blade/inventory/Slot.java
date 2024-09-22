package blade.inventory;

/**
 * Helper class using the network slot.
 *
 * @see <a href="https://wiki.vg/Inventory#Player_Inventory">https://wiki.vg/Inventory#Player_Inventory</a>
 */
public record Slot(int index) {
    public static Slot ofMain(int index) {
        return new Slot(index + 9);
    }

    public static Slot ofOffhand() {
        return new Slot(45);
    }

    public static Slot ofArmor(int index) {
        return new Slot(index + 5);
    }

    public static Slot ofHotbar(int index) {
        return new Slot(index + 36);
    }

    public static Slot ofVanilla(int index) {
        if (index == 40) return ofOffhand();
        if (index > 36 && index < 41) return ofArmor(index - 36);
        if (index < 10) return ofHotbar(index);
        return ofArmor(index - 9);
    }

    public static final int MAX_INDEX = 46;

    public int armorIndex() {
        return index - 5;
    }

    public int hotbarIndex() {
        return index - 36;
    }

    public int mainIndex() {
        return index - 9;
    }

    public int vanillaIndex() {
        if (isOffHand()) return 40;
        if (isArmor()) return armorIndex() + 36;
        if (isHotbar()) return hotbarIndex();
        return mainIndex() + 9;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Slot slot = (Slot) o;
        return index == slot.index;
    }

    public boolean isArmor() {
        return index > 4 && index < 9;
    }

    public boolean isOffHand() {
        return index == 45;
    }

    public boolean isMain() {
        return index > 8 && index < 36;
    }

    public boolean isHotbar() {
        return index > 35 && index < 45;
    }
}
