package blade.impl.action.attack.anchor;

import blade.BladeMachine;
import blade.impl.action.attack.Attack;

public interface Anchor extends Attack {
    static void register(BladeMachine blade) {
        blade.addAction(new PlaceAnchor());
        blade.addAction(new ChargeAnchor());
        blade.addAction(new ExplodeAnchor());
    }
}
