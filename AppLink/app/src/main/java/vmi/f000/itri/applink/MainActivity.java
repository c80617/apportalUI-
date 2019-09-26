package vmi.f000.itri.applink;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);
        String webLinkText = "<a href='apportal://?yiwen&a&com.microsoft.word'> APPortal</a>" ;
        tv.setText(Html.fromHtml(webLinkText));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
