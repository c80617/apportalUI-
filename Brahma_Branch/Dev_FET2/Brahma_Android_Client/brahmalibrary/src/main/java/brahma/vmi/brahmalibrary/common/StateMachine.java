package brahma.vmi.brahmalibrary.common;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @developer Ian
 */
public class StateMachine implements Serializable {
    private static final String TAG = StateMachine.class.getName();
    private STATE state = STATE.NEW;
    private List<StateObserver> observers = new ArrayList<StateObserver>();

    public STATE getState() {
        return state;
    }

    // sets the new state
    // resID is the message to display (0 for no message)
    public void setState(STATE newState, int resID, String result) {
        STATE oldState = state;
        state = newState;
        Log.d(TAG, String.format("State change: %s -> %s", oldState, newState));

        // inform observers of state change
        for (StateObserver observer : observers) {
            observer.onStateChange(oldState, newState, resID, result);
        }
    }

    public void addObserver(StateObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void removeObserver(StateObserver observer) {
        Log.d("LifeCycle", "removeObserver");
        if (observer != null) {
            observers.remove(observer);
        }
    }

    // state changes in sequential order, except ERROR
    public static enum STATE {
        NEW,       // before the service has been started
        STARTED,   // after the service has been started
        AUTH,      // after we have authenticated
        CONNECTED, // after the WebSocket has been connected
        RUNNING,   // after the VM is ready and we've started proxying
        ERROR,      // any other state can change to an error state
        RECONNECT,   //reconnect to VM,
        AUTOCLOSE,   //web auto close websocket
        TEST
    }
}
