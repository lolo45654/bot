package blade.impl;

import blade.BladeMachine;
import blade.impl.action.attack.Attack;

public class BladeImpl {
    public static void register(BladeMachine blade) {
        Attack.register(blade);
    }
}
