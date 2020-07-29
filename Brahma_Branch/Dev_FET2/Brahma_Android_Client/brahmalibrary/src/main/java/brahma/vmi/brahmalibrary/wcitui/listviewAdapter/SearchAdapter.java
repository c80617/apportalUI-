package brahma.vmi.brahmalibrary.wcitui.listviewAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import brahma.vmi.brahmalibrary.R;


public class SearchAdapter extends ArrayAdapter<ArrayList<Map<String, String>>> {

    private LayoutInflater myInflater;
    ArrayList<Map<String, String>> title = null;
    ArrayList<Map<String, String>> info = null;
    ArrayList<Map<String, Bitmap>> png = null;

    public SearchAdapter(Context ctxt, ArrayList<Map<String, String>> title, ArrayList<Map<String, String>> info, ArrayList<Map<String, Bitmap>> png){
        super(ctxt,R.layout.adapter);
        myInflater = LayoutInflater.from(ctxt);
        this.title = title;
        this.info = info;
        this.png = png;
    }

    @Override
    public int getCount() {
        return title.size();
    }

//    @Override
//    public Object getItem(int position) {
//        return title[position];
//    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //自訂類別，表達個別listItem中的view物件集合。
        ViewTag viewTag;

        if(convertView == null){
            //取得listItem容器 view
            convertView = myInflater.inflate(R.layout.adapter, null);

            //建構listItem內容view
            viewTag = new ViewTag(
                    (ImageView)convertView.findViewById(R.id.MyAdapter_ImageView_icon),
                    (TextView) convertView.findViewById(R.id.MyAdapter_TextView_title),
                    (TextView) convertView.findViewById(R.id.MyAdapter_TextView_info)
            );

            //設置容器內容
            convertView.setTag(viewTag);
        }
        else{
            viewTag = (ViewTag) convertView.getTag();
        }



        //設定標題文字
        viewTag.title.setText(title.get(position).get("RNAME"));
        //設定內容文字
        //viewTag.info.setText(info.get(position).get("RINFO"));
        //設定內容圖案
        viewTag.icon.setImageBitmap(png.get(position).get("RMAP"));

        return convertView;
    }

    //自訂類別，表達個別listItem中的view物件集合。
    class ViewTag{
        ImageView icon;
        TextView title;
        TextView info;

        public ViewTag(ImageView icon, TextView title, TextView info){
            this.icon = icon;
            this.title = title;
            this.info = info;
        }
    }
}