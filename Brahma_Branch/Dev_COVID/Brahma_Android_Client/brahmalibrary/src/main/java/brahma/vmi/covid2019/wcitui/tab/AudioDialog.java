package brahma.vmi.covid2019.wcitui.tab;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import brahma.vmi.covid2019.R;

public class AudioDialog implements DialogInterface.OnCancelListener, View.OnClickListener{
    private Context mContext;
    private Dialog mDialog;
    private SeekBar seekBar;
    //need to capture keyevent
    //private TextView tv;
    private String TAG = "AudioDialog";
    public AudioDialog(Context context,int max,int progress){
        this.mContext = context;
        initAudioDialog(max,progress);
    }

    public void initAudioDialog(int max,int progress){
        mDialog = new Dialog(mContext, R.style.MyDialog);
        mDialog.setContentView(R.layout.dialog_audio);

        seekBar = (SeekBar)mDialog.findViewById(R.id.seekBar);
//        tv = (TextView)mDialog.findViewById(R.id.tv);
        mDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        //setting seekBar max
        seekBar.setMax(max);
        seekBar.setProgress(progress);// 当前的媒体音量
        //limit touch
//        seekBar.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d(TAG, "setOnTouchListener");
//                return true;
//            }
//        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //tv.setText("Volume:" + progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(mContext, "touch SeekBar", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(mContext, "leave SeekBar", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public AudioDialog show(int volume){
        seekBar.setProgress(volume);


        // 點邊取消
        mDialog.setCancelable(true);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setOnCancelListener(this);
        mDialog.show();
        return this;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mDialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        mDialog.dismiss();
    }

    public void dismiss() {
        mDialog.dismiss();
    }
}
