package brahma.vmi.brahmalibrary.wcitui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import brahma.vmi.brahmalibrary.R;
import brahma.vmi.brahmalibrary.wcitui.GridActivity;
import brahma.vmi.brahmalibrary.wcitui.GridActivityAfter;

public class CustomGridAfter extends BaseAdapter {
    private Context context;
    ArrayList<String> app_name = new ArrayList<>();
    ArrayList<String> app_icon = new ArrayList<>();
//    ArrayList<String> packageName = new ArrayList<>();

    public CustomGridAfter(GridActivityAfter context, ArrayList<String> app_name, ArrayList<String> app_icon, ArrayList<String> packageName) {
        this.context = context;
        this.app_name = app_name;
        this.app_icon = app_icon;
//        this.packageName = packageName;

    }

    @Override
    public int getCount() {
        return app_name.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        // Context 動態放入mainActivity
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            grid = layoutInflater.inflate(R.layout.app_grid, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);
            textView.setText(app_name.get(position));
            String app_icon1 = app_icon.get(position).replaceAll("jpeg", "png");
            String[] app_icon2 =  app_icon1.replaceAll("\\[data:image/png;base64,", "").replaceAll("\\]", "").split("data:image/png;base64,");
            byte[] decode = Base64.decode(app_icon2[1],Base64.DEFAULT);
            Bitmap bitmap2 = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            imageView.setImageBitmap(bitmap2);
//            textView.setText(text[position]);
//            imageView.setImageResource(imageId[position]);
        } else {
            grid = (View) convertView;
        }
        return grid;
    }
}
