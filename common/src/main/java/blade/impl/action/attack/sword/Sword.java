package blade.impl.action.attack.sword;

import blade.BladeMachine;
import blade.impl.action.attack.Attack;
import blade.impl.action.attack.ConsumeHealing;
import blade.impl.action.attack.HitEnemy;
import blade.impl.action.attack.MoveTowardsEnemy;

public interface Sword extends Attack {
    static void register(BladeMachine blade) {
        blade.addAction(new HitEnemy());
        blade.addAction(new BackOff());
        blade.addAction(new Jump());
        blade.addAction(new MoveTowardsEnemy());
        blade.addAction(new ConsumeHealing());
        blade.addAction(new StrafeRight());
        blade.addAction(new StrafeLeft());
    }
}
