package me.loloed.bot.api.blade.impl.action.pvp.totem;

import me.loloed.bot.api.blade.BladeMachine;

public interface Totem {
    static void register(BladeMachine blade) {
        blade.addAction(new SelectTotem());
        blade.addAction(new EquipTotem());
        blade.addAction(new EquipHotBarTotem());
    }
}
