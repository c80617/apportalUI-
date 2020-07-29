package brahma.vmi.covid2019.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

public class MyEditText extends EditText {

    public static OnBackPressedListener mOnBackPressedListener = null;
    CharSequence inputText;
    int inputNewCursorPosition;
    boolean commitText = false;

    public MyEditText(Context context) {
        super(context);
        //init();
        // TODO Auto-generated constructor stub
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
        // TODO Auto-generated constructor stub
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //init();
        // TODO Auto-generated constructor stub
    }

    public static void SetOnBackPressedListener(OnBackPressedListener listener) {
        mOnBackPressedListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
//        Log.d("IanIan", "KeyEvent.KEYCODE_ENTER: " + KeyEvent.KEYCODE_ENTER +", event.keyCode: "+event.getKeyCode());
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnBackPressedListener != null) {
                mOnBackPressedListener.OnBackPressed(keyCode, event);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
//        inputConnection.setTarget(super.onCreateInputConnection(outAttrs));
//        Log.d("KeyEvents", "onCreateInputConnection");
        return new ZanyInputConnection(super.onCreateInputConnection(outAttrs), true);
        //return inputConnection;
    }

//    private void init(){
//        inputConnection = new TInputConnection(null,true);
//    }

    public interface OnBackPressedListener {
        public void OnBackPressed(int keyCode, KeyEvent event);
    }

    private class ZanyInputConnection extends InputConnectionWrapper {
        public ZanyInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            Log.d("KeyEventss", "sendKeyEvent:" + String.valueOf(event.getKeyCode()));
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                // Un-comment if you wish to cancel the backspace:
                // return false;
            }
            return super.sendKeyEvent(event);
        }

        //當有文字刪除操作時（剪下，點選退格鍵），會觸發該方法
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }

        //當輸入法輸入了字元，包括表情，字母、文字、數字和符號等內容，會回撥該方法
        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            Log.d("KeyEventss", "commitText:" + text);
            Log.d("KeyEventss", "newCursorPosition:" + newCursorPosition);
            commitText = true;
            return super.commitText(text, newCursorPosition);
        }

        //執行super.setComposingText()後會直接送出text
        @Override
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            Log.d("KeyEventss", "setComposingText:" + text);
            int ascii = 0;
            if (text.length() != 0)
                ascii = (int) text.charAt(0);
            Log.d("KeyEventss", "ascii:" + ascii);
            inputText = text;
            inputNewCursorPosition = newCursorPosition;
            commitText = false;
            if (ascii >= 65 && ascii <= 122) {
                return super.setComposingText(text, newCursorPosition);
            }
            if (ascii >= 122 && ascii <= 1000) {
                return super.setComposingText(text, newCursorPosition);
            }
//            if (ascii >= 19968) {
//                return super.setComposingText(text, newCursorPosition);
//            }
            return false;
        }

        @Override
        public boolean finishComposingText() {
            Log.d("KeyEventss", "finishComposingText text >>>>> " + inputText);
            if (!commitText && inputText != null) {
                Log.d("KeyEventss", "boolean commitText >>>>> " + commitText);
                commitText(inputText, inputNewCursorPosition);
            }
            return super.finishComposingText();
        }
    }
}