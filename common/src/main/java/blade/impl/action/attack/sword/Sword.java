package blade.impl.action.attack.sword;

import blade.BladeMachine;
import blade.impl.action.attack.Attack;
import blade.impl.action.attack.ConsumeHealing;
import blade.impl.action.attack.HitEnemy;
import blade.impl.action.attack.MoveTowardsEnemy;

public interface Sword extends Attack {
    static void register(BladeMachine blade) {
        blade.registerAction(new HitEnemy());
        blade.registerAction(new BackOff());
        blade.registerAction(new Jump());
        blade.registerAction(new MoveTowardsEnemy());
        blade.registerAction(new ConsumeHealing());
        blade.registerAction(new StrafeRight());
        blade.registerAction(new StrafeLeft());
    }
}
