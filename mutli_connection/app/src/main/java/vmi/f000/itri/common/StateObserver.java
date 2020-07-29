package brahma.vmi.brahmalibrary.common;

import brahma.vmi.brahmalibrary.common.StateMachine.STATE;

/**
 * @developer Ian
 */
public interface StateObserver {
    // the observer receives a notification when the connection state changes
    public void onStateChange(STATE oldState, STATE newState, int resID,String result);
}
