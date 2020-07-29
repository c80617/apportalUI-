package brahma.vmi.covid2019.common;

import brahma.vmi.covid2019.common.StateMachine.STATE;

/**
 * @developer Ian
 */
public interface StateObserver {
    // the observer receives a notification when the connection state changes
    public void onStateChange(STATE oldState, STATE newState, int resID,String result);
}
