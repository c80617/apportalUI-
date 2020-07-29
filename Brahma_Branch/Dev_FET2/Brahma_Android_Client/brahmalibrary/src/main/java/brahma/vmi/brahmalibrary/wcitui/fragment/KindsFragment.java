//package brahma.vmi.brahmalibrary.wcitui.fragment;
//
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Base64;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.EditText;
//import android.widget.ListView;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//
//import brahma.vmi.brahmalibrary.R;
//import brahma.vmi.brahmalibrary.wcitui.listviewAdapter.SearchAdapter;
//import brahma.vmi.brahmalibrary.wcitui.tab.BaseFragment;
//
//
//public class KindsFragment extends BaseFragment {
//
//    private static final String DATA_NAME = "name";
//    static String TAG = "KindsFragment";
//    ArrayList<Map<String, String>> mtitle = new ArrayList<Map<String, String>>();
//    ArrayList<Map<String, String>> minfo = new ArrayList<Map<String, String>>();
//    ArrayList<Map<String, Bitmap>> mpng = new ArrayList<Map<String, Bitmap>>();
//
//
//    private static ArrayList<String> app_nameALL = new ArrayList<>();
//    private static ArrayList<String> app_iconALL = new ArrayList<>();
//    private static ArrayList<String> packageNameALL = new ArrayList<>();
//
//    JSONArray appList  = null;
//    private String title = "";
//
//    ArrayList<String> pkgName = new ArrayList<String>();
//    ArrayList<Bitmap> icon = new ArrayList<Bitmap>();
//
//    SearchAdapter lvAdapter;
//    ListView lvFrg;
//
//    EditText SearchText;
//
//    @SuppressLint("ValidFragment")
//    public KindsFragment(JSONArray applist) {
//        super();
//        this.appList = applist;
//    }
//    public KindsFragment() {
//        super();
//    }
//
////    public static KindsFragment newInstance(String title, int indicatorColor, int dividerColor, int iconResId) {
////        KindsFragment f = new KindsFragment();
////        f.setTitle(title);
////        f.setIndicatorColor(indicatorColor);
////        f.setDividerColor(dividerColor);
////        f.setIconResId(iconResId);
////
////
////        //pass data
////        Bundle args = new Bundle();
////        args.putString(DATA_NAME, title);
////        f.setArguments(args);
////
////        return f;
////    }
////
//    public static KindsFragment newInstance(String title, int indicatorColor, int dividerColor) {
//
//        //show all
//        Log.d(TAG,"KindsFragment newInstance ALL >>>>> "+title);
//        KindsFragment f = new KindsFragment();
//        f.setTitle(title);
//        f.setIndicatorColor(indicatorColor);
//        f.setDividerColor(dividerColor);
//
//        //pass data
//        Bundle args = new Bundle();
//        args.putString(DATA_NAME, title);
//        args.putString("app_name", String.valueOf(app_nameALL));
//        args.putString("app_icon", String.valueOf(app_iconALL));
//        args.putString("packageName", String.valueOf(packageNameALL));
//        args.putString("needSearch","true");
//
//        f.setArguments(args);
//
//
//        //clear ArrayList
//        app_nameALL.clear();
//        app_iconALL.clear();
//        packageNameALL.clear();
//        Log.d(TAG,"KindsFragment newInstance finish ALL");
//        return f;
//    }
//
//    public static ArrayList removeDuplicate(ArrayList arlList)
//    {
//        HashSet h = new HashSet(arlList);
//        arlList.clear();
//        arlList.addAll(h);
//        return arlList;
//    }
//
//    public static KindsFragment newInstance(String title, int indicatorColor, int dividerColor, JSONArray applist) {
//
//        Log.d(TAG,"KindsFragment newInstance >>>>> "+title);
//        KindsFragment f = new KindsFragment();
//        f.setTitle(title);
//        f.setIndicatorColor(indicatorColor);
//        f.setDividerColor(dividerColor);
//
//        //appList = applist;
//        ArrayList<String> app_name = new ArrayList<>();
//        ArrayList<String> app_icon = new ArrayList<>();
//        ArrayList<String> packageName = new ArrayList<>();
//
//        for(int i= 0 ;i<applist.length();i++){
//            try {
//                Log.d(TAG,"applist data:"+applist.get(i).toString());
//                JSONObject jsonObject = applist.getJSONObject(i);
//                app_name.add(jsonObject.getString("app_name"));
//                app_icon.add(jsonObject.getString("app_icon"));
//                packageName.add(jsonObject.getString("package_name"));
//
//                app_nameALL.add(jsonObject.getString("app_name"));
//                app_iconALL.add(jsonObject.getString("app_icon"));
//                packageNameALL.add(jsonObject.getString("package_name"));
//                //記得要清除arrayList
//                //去除重复
////                app_nameALL = removeDuplicate(app_nameALL);
////                app_iconALL = removeDuplicate(app_iconALL);
////                packageNameALL = removeDuplicate(packageNameALL);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        //pass data
//        Bundle args = new Bundle();
//        args.putString(DATA_NAME, title);
//        args.putString("app_name", String.valueOf(app_name));
//        args.putString("app_icon", String.valueOf(app_icon));
//        args.putString("packageName", String.valueOf(packageName));
//        args.putString("needSearch","false");
//
//        f.setArguments(args);
//        Log.d(TAG,"KindsFragment newInstance finish");
//        return f;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Log.d(TAG,"KindsFragment onCreate");
//
//        //get data
//        Log.d(TAG,""+getArguments());
//
//        title = getArguments().getString(DATA_NAME);
//        String[] app_name = getArguments().getString("app_name").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
//        //圖片格式轉換:將jpeg轉成png
//        String app_icon1 = getArguments().getString("app_icon").replaceAll("jpeg", "png");
//        String[] app_icon = app_icon1.replaceAll("\\[data:image/png;base64,", "").replaceAll("\\]", "").split("data:image/png;base64,");
//        String[] packageName = getArguments().getString("packageName").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
//        for(int i=0 ; i < app_name.length ; i++){
//            HashMap<String, String> recipe = new HashMap<String, String>();
//            recipe.put("RNAME", app_name[i].trim());
//            HashMap<String, String> info = new HashMap<String, String>();
//
//            packageName[i] = packageName[i].replace(" ", "");
//            info.put("RINFO", packageName[i]);
//
//            //處理圖片與縮小圖片
//            //int ii = app_icon[i].indexOf(",");
//            //String temp = app_icon[i].substring(ii+1);
//            //Log.d("YiWen","temp:"+temp);
//            byte[] decode = Base64.decode(app_icon[i],Base64.DEFAULT);
//            Bitmap bitmap2 = BitmapFactory.decodeByteArray(decode, 0, decode.length);
//
////            int oldwidth = bitmap2.getWidth();
////            int oldheight = bitmap2.getHeight();
////            Log.i("YiWen","SettingManager====>bmp size is :"+oldwidth+","+oldheight);
////            float scaleWidth = 51 / (float)oldwidth;
////            float scaleHeight = 51 / (float)oldheight;
////            Log.i("YiWen","SettingManager====> set scale value : "+scaleWidth + ":" + scaleHeight);
////            Matrix matrix = new Matrix();
////            matrix.postScale(scaleWidth, scaleHeight);
////            // create the new Bitmap object
////            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap2, 0, 0, oldwidth,oldheight, matrix, true);
//
//
//            HashMap<String, Bitmap> bitmap = new HashMap<String, Bitmap>();
//            bitmap.put("RMAP", bitmap2);
//            mtitle.add(recipe);
//            minfo.add(info);
//            mpng.add(bitmap);
//            pkgName.add(minfo.get(i).get("RINFO"));
//        }
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        Log.d(TAG,"KindsFragment onCreateView");
//        View view = inflater.inflate(R.layout.frg_common_table, container, false);
//        ArrayList<Map<String, String>> Mtitle = mtitle;
//        ArrayList<Map<String, String>> Minfo = minfo;
//        ArrayList<Map<String, Bitmap>> Mpng = mpng;
//
//        SearchText = (EditText) view.findViewById(R.id.textViewConnect);
//
//        lvFrg = (ListView) view.findViewById(R.id.lvFrgtab);
//
//        lvAdapter = new SearchAdapter(getActivity(), Mtitle, Minfo, Mpng);
//        lvFrg.setAdapter(lvAdapter);
//        lvFrg.setOnItemClickListener(onItemClickListener);
//
//        String needSearch = getArguments().getString("needSearch");
//
//        if(needSearch.equals("true")){
//            SearchText.setVisibility(View.VISIBLE);
//        }else{
//            SearchText.setVisibility(View.GONE);
//        }
//        SearchText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                //这个方法被调用，说明在s字符串中，从start位置开始的count个字符即将被长度为after的新文本所取代。在这个方法里面改变s，会报错。
//                lvAdapter.notifyDataSetChanged();
//                lvFrg.setVisibility(View.VISIBLE);
//                lvFrg.invalidate();
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                //这个方法被调用，说明在s字符串中，从start位置开始的count个字符刚刚取代了长度为before的旧文本。在这个方法里面改变s，会报错。
//                //get the text in the EditText
//                String searchString = SearchText.getText().toString();
//                Log.d(TAG,"searchString:"+searchString);
//
//                int textLength=searchString.length();
//                ArrayList<Map<String, String>> searchResults = new ArrayList<Map<String, String>>();
//                ArrayList<Map<String, String>> searchResultsInfo = new ArrayList<Map<String, String>>();
//                ArrayList<Map<String, Bitmap>> searchResultspng = new ArrayList<Map<String, Bitmap>>();
//
//                pkgName.clear();
//
//                for(int i =0;i<mtitle.size();i++)
//                {
//                    String rname = mtitle.get(i).get("RNAME").toString();
//                    String rinfo = minfo.get(i).get("RINFO").toString();
//                    Log.d("kinds","rname:"+rname);
////                    Bitmap rpng = mpng.get(i).get("RMAP");
//                    if(textLength <= rname.length())
//                    {
//                        Log.d("kinds","indexOF:"+rname.toLowerCase().indexOf(searchString.toLowerCase()));
//                        //compare the String in EditText with Names in the ArrayList
//                        //if(searchString.equalsIgnoreCase(rname.substring(0,textLength)))
//                        if(rname.toLowerCase().indexOf(searchString.toLowerCase()) != -1)
//                        {
//                            searchResults.add(mtitle.get(i));
//                            searchResultsInfo.add(minfo.get(i));
//                            searchResultspng.add(mpng.get(i));
//                            pkgName.add(rinfo);
//
//                            lvAdapter.notifyDataSetChanged();
//                            lvFrg.invalidate();
//                            lvAdapter=new SearchAdapter(getActivity(), searchResults, searchResultsInfo, searchResultspng);
//                            lvFrg.setAdapter(lvAdapter);
//
//                        }
//                    }
//                }
//                if(searchResults.isEmpty())
//                {
//                    lvFrg.setVisibility(View.INVISIBLE);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                //这个方法被调用，那么说明s字符串的某个地方已经被改变。
//                lvAdapter.notifyDataSetChanged();
//                lvFrg.invalidate();
//            }
//        });
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        Log.d(TAG,"KindsFragment onViewCreated");
//        //TextView txtName = (TextView) view.findViewById(R.id.txtName);
//        //txtName.setText(title);
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        Log.d(TAG,"KindsFragment onActivityCreated");
//    }
//    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//            //Toast.makeText(SlidingTab.context, "test!!!!!!!!!!!", Toast.LENGTH_SHORT).show();
//            // TODO Auto-generated method stub
//            //do your job here, position is the item position in ListView
//            // create explicit intent
//            Intent intent = new Intent();
//            intent.setClass(getActivity(), SlidingTab.class);
//            intent.putExtra("tagPkgName", pkgName.get(position));
//            intent.putExtra("connectionID", 1);
//
//            startActivity(intent);
//            getActivity().finish();
//
//            Log.i(TAG, "onListItemClick: " + pkgName.get(position));
//        }
//    };
//    @Override
//    public void onDestroy() {
//        Log.i(TAG, "onDestroy");
//        super.onDestroy();
//    }
//
//
//
//    @Override
//    public void onDestroyView() {
//        Log.i(TAG, "onDestroyView");
//        super.onDestroyView();
//    }
//
//}
//
