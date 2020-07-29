package brahma.vmi.covid2019.client;

import android.util.Log;

import brahma.vmi.covid2019.activities.AppRTCActivity;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.Request;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.Request.RequestType;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.VKeyboardReq;

public class VirtualKeyboardHandler{
	
	/*
	public interface OnKeyboardListener{
		public void OnKeyboardString(String input);
		public void OnKeyboardCode(int keycode);
	}
	private static OnKeyboardListener mOnKeyboardListener = null;
	public static void setOnKeyboardListener(OnKeyboardListener listener){
		mOnKeyboardListener = listener;
	}
	*/

	private final static String TAG = VirtualKeyboardHandler.class.getName();
	public final static int TYPE_KEYCODE 	= 1;
	public final static int TYPE_STRING 	= 2; 
	public final static int TYPE_HEIGHT 	= 3;
	
	private AppRTCActivity activity;
	
	public VirtualKeyboardHandler(AppRTCActivity activity) {
        this.activity = activity;
    }
	
	public void sendVKeyboardReqMessage(int type, Object obj){
		VKeyboardReq.Builder vBuilder = null;
		if(type == TYPE_HEIGHT){
			vBuilder = VKeyboardReq.newBuilder()
					.setKeycmd(TYPE_HEIGHT)
					.setKeycode((Integer)obj) //set keyboard height
					.setKeystring("none");
		}
		else if(type == TYPE_KEYCODE){
			vBuilder = VKeyboardReq.newBuilder()
					.setKeycmd(TYPE_KEYCODE)
					.setKeycode((Integer)obj)
					.setKeystring("none");
		}
		else if(type == TYPE_STRING){
			vBuilder = VKeyboardReq.newBuilder()
					.setKeycmd(TYPE_STRING)
					.setKeycode(0)
					.setKeystring((String)obj);
		}
		/*
		VKeyboardReq.Builder vBuilder = VKeyboardReq.newBuilder()
				.setKeycmd(2)
				.setKeycode(0)
				.setKeystring("test");
		*/
        Request.Builder msg = Request.newBuilder()
        		.setType(RequestType.VKeyboardReq)
        		.setVKeyboardReq(vBuilder);
        
        activity.sendMessage(msg.build());
        /*
        VKeyboardReq vKeyboardReq = null;
        if(vKeyboardReq.getKeycmd() == 1){
        	Log.d(TAG, "accept keycode = " + vKeyboardReq.getKeycode());
        	if(mOnKeyboardListener != null){
        		mOnKeyboardListener.OnKeyboardCode(vKeyboardReq.getKeycode());
        	}
        }
        else if(vKeyboardReq.getKeycmd() == 2){
        	Log.d(TAG, "accept string = " + vKeyboardReq.getKeystring());
        	if(mOnKeyboardListener != null){
        		mOnKeyboardListener.OnKeyboardString(vKeyboardReq.getKeystring());
        	}
        }
        */
        
    }
	public int handleKbShowStatus(BRAHMAProtocol.Response msg) {
        if (!msg.hasVkeyboardInfo()) return -1;
        
        return msg.getVkeyboardInfo().getData(); 
    }
	
	public boolean handleVKeyboardInfoResponse(BRAHMAProtocol.Response msg) {
		
        if (!msg.hasVkeyboardInfo())
            return false;
        final int x = msg.getVkeyboardInfo().getData();
        String getMsg = msg.getVkeyboardInfo().getMsg();
        Log.d(TAG, "Get Virtual Keyboard Info: " + x);
        Log.d(TAG, "Get Virtual Keyboard Info: " + getMsg);
        return true;
    }


}