package blade.impl;

import blade.BladeMachine;
import blade.impl.action.pvp.PvP;

public class BladeImpl {
    public static void register(BladeMachine blade) {
        PvP.register(blade);
    }
}
