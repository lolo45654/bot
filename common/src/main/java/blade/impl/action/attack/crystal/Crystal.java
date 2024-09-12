package blade.impl.action.attack.crystal;

import blade.BladeMachine;
import blade.impl.action.attack.Attack;

public interface Crystal extends Attack {
    static void register(BladeMachine blade) {
        blade.addAction(new PearlTowardsEnemy());
        blade.addAction(new DestroyCrystal());
        blade.addAction(new PlaceObsidian());
        blade.addAction(new PlaceCrystal());
        blade.addAction(new ShieldCrystal());
        // TODO CrystalShield, shielding specific to crystal
    }
}
