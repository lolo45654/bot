package blade.impl.action.attack.anchor;

import blade.BladeMachine;
import blade.impl.action.attack.Attack;

public interface Anchor extends Attack {
    static void register(BladeMachine blade) {
        blade.registerAction(new PlaceAnchor());
        blade.registerAction(new ChargeAnchor());
        blade.registerAction(new ExplodeAnchor());
    }
}
