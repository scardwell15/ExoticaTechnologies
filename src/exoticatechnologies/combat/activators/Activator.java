package exoticatechnologies.combat.activators;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.input.Keyboard;

public abstract class Activator {
    protected State state = State.READY;
    protected IntervalUtil interval = new IntervalUtil(getInDuration(), getInDuration());

    public abstract String getKeySetting();

    public String getKey() {
        return Global.getSettings().getString(getKeySetting());
    }

    public State getState() {
        return state;
    }

    public float getInDuration() {
        return 0f;
    }

    public abstract float getActiveDuration();

    public float getOutDuration() {
        return 0f;
    }

    public abstract float getCooldownDuration();

    /**
     * Returns ratio of how "complete" the state is.
     * if state is 5 seconds long and 3 seconds have passed,
     * it will return 0.6f.
     * This method uses the internal interval.
     * @return
     */
    public float getStateCompleteRatio() {
        return interval.getElapsed() / interval.getIntervalDuration();
    }

    public boolean canActivate(ShipAPI ship) {
        return true;
    }

    /**
     * Should check for internal parameters, like if state == READY or if the system has charges or something.
     * @return
     */
    public boolean canActivate() {
        return state == State.READY;
    }

    public abstract boolean shouldActivateAI(ShipAPI ship);

    public void activated() {

    }

    public void onActivate(ShipAPI ship) {

    }

    public void onStateSwitched(ShipAPI ship, State state) {

    }

    public void advance(ShipAPI ship, State state, float amount) {

    }

    public void advanceInternal(ShipAPI ship, float amount) {
        if (state != State.READY) {
            interval.advance(amount);
        }

        boolean shouldActivate = false;
        if (Global.getCombatEngine().getPlayerShip() == ship && ship.getAI() == null) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex(getKey()))) {
                shouldActivate = true;
            }
        } else {
            shouldActivate = shouldActivateAI(ship);
        }

        if (shouldActivate) {
            boolean internalActivate = canActivate();
            boolean shipActivate = canActivate(ship);
            if (internalActivate && shipActivate) {
                onActivate(ship);
                activated();

                state = State.IN;
                interval.setInterval(getInDuration(), getInDuration());

                onStateSwitched(ship, state);
            }
        }

        if (interval.intervalElapsed() || interval.getIntervalDuration() == 0f) {
            switch(state) {
                case IN:
                    state = State.ACTIVE;
                    interval.setInterval(getActiveDuration(), getActiveDuration());
                    break;
                case ACTIVE:
                    state = State.OUT;
                    interval.setInterval(getOutDuration(), getOutDuration());
                    break;
                case OUT:
                    state = State.COOLDOWN;
                    interval.setInterval(getCooldownDuration(), getCooldownDuration());
                    break;
                case COOLDOWN:
                    state = State.READY;
                    break;
            }
            onStateSwitched(ship, state);
        }

        advance(ship, state, amount);
    }

    public enum State {
        READY,
        IN,
        ACTIVE,
        OUT,
        COOLDOWN
    }
}
