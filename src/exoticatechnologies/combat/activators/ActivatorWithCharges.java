package exoticatechnologies.combat.activators;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public abstract class ActivatorWithCharges extends Activator {
    protected int charges = getMaxCharges();
    protected IntervalUtil chargeInterval = new IntervalUtil(getChargeRechargeTime(), getChargeRechargeTime());

    public abstract int getMaxCharges();

    public abstract float getChargeRechargeTime();

    @Override
    public boolean canActivate() {
        return state == State.READY && charges > 0;
    }

    @Override
    public void activated() {
        charges--;
    }

    @Override
    public void advanceInternal(ShipAPI ship, float amount) {
        super.advanceInternal(ship, amount);

        if (charges < getMaxCharges()) {
            chargeInterval.advance(amount);
            if (chargeInterval.intervalElapsed()) {
                charges++;
            }
        }
    }
}
