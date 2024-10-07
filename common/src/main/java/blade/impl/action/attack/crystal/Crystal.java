package blade.impl.action.attack.crystal;

import blade.BladeMachine;
import blade.impl.action.attack.Attack;

public interface Crystal extends Attack {
    static void register(BladeMachine blade) {
        blade.registerAction(new PearlTowardsEnemy());
        blade.registerAction(new DestroyCrystal());
        blade.registerAction(new PlaceObsidian());
        blade.registerAction(new PlaceCrystal());
        // blade.addAction(new ShieldCrystal());
    }
}
