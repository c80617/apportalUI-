package brahma.vmi.covid2019.expandmenu.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import brahma.vmi.covid2019.R;

import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.heightPixels_org;
import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.widthPixels_org;

public class FloatingView extends FrameLayout {
    private Context mContext;
    private View mView;
    private Button back;
    private Button home;
    private Button menu;
    private Button multiapp;
    private HorizontalExpandMenu horizontalExpandMenu;
    private int mTouchStartX, mTouchStartY;//手指按下时坐标
    private WindowManager.LayoutParams mParams;
    private FloatingManager mWindowManager;

    private FabListener fabListener;

    public final static int BACK = 1;
    public final static int HOME = 2;
    public final static int MENU = 3;
    public final static int MULTIAPP = 4;


    public FloatingView(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        LayoutInflater mLayoutInflater = LayoutInflater.from(context);
        mView = mLayoutInflater.inflate(R.layout.floating_view, null);

        horizontalExpandMenu = (HorizontalExpandMenu) mView.findViewById(R.id.fab);
        back = (Button) mView.findViewById(R.id.back_button);
        back.setOnClickListener(backListener);
        menu = (Button) mView.findViewById(R.id.menu_button);
        menu.setOnClickListener(menuListener);
        home = (Button) mView.findViewById(R.id.home_button);
        home.setOnClickListener(homeListener);
        multiapp = (Button) mView.findViewById(R.id.multiapp_button);
        multiapp.setOnClickListener(multiappListener);

        mWindowManager = FloatingManager.getInstance(mContext);
    }

    public void show(int rotation) {
        mParams = new WindowManager.LayoutParams();
        mParams.gravity = Gravity.TOP | Gravity.LEFT;
        mParams.x = 0;
        if (rotation == 0 || rotation == 2) {
            mParams.y = heightPixels_org / 3;
        } else {
            mParams.y = widthPixels_org / 3;
        }

        //总是出现在应用程序窗口之上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        //设置图片格式，效果为背景透明
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
        mParams.width = LayoutParams.WRAP_CONTENT;
        mParams.height = LayoutParams.WRAP_CONTENT;
//        if(mView == null)
        mWindowManager.addView(mView, mParams);
    }

    public void hide() {
        mWindowManager.removeView(mView);
    }

//    private OnTouchListener mOnTouchListener = new OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent event) {
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    mTouchStartX = (int) event.getRawX();
//                    mTouchStartY = (int) event.getRawY();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    mParams.x += (int) event.getRawX() - mTouchStartX;
//                    mParams.y += (int) event.getRawY() - mTouchStartY;//相对于屏幕左上角的位置
//                    mWindowManager.updateView(mView, mParams);
//                    break;
//                case MotionEvent.ACTION_UP:
//                    break;
//            }
//            return true;
//        }
//    };

    public void setFabListener(FabListener fabListener) {
        this.fabListener = fabListener;
    }

    private Button.OnClickListener menuListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            horizontalExpandMenu.expandMenu();
            if (fabListener != null)
                fabListener.click(MENU);
        }
    };
    private Button.OnClickListener backListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            horizontalExpandMenu.expandMenu();
            if (fabListener != null)
                fabListener.click(BACK);
        }
    };
    private Button.OnClickListener homeListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            horizontalExpandMenu.expandMenu();
            if (fabListener != null)
                fabListener.click(HOME);
        }
    };
    private Button.OnClickListener multiappListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method
            horizontalExpandMenu.expandMenu();
            if (fabListener != null)
                fabListener.click(MULTIAPP);
        }
    };

    public interface FabListener//需实现此接口以便接受点击事件
    {
        void click(int i);
    }

}