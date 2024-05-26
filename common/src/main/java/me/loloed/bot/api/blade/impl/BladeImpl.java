package me.loloed.bot.api.blade.impl;

import me.loloed.bot.api.blade.BladeMachine;
import me.loloed.bot.api.blade.impl.action.pvp.cart.UseBow;
import me.loloed.bot.api.blade.impl.action.pvp.totem.Totem;

public class BladeImpl {
    public static void register(BladeMachine blade) {
        Totem.register(blade);
        blade.addAction(new UseBow());
        /*blade.addAction(new MineCartEnemy.Bow());
        blade.addAction(new MineCartEnemy.PlaceRail());
        blade.addAction(new MineCartEnemy.PlaceCart());
        blade.addAction(new PlayCrystal());
        blade.addAction(new MoveTowards());
        blade.addAction(new PearlTowards());
        blade.addAction(new ReTotem.OffHand());
        blade.addAction(new ReTotem.HotBar());
        blade.addAction(new HoldTotem.GetHit());*/
    }
}
