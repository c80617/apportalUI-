//package brahma.vmi.brahmalibrary.wcitui.fragment;
//
//import android.annotation.SuppressLint;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.os.Bundle;
//import com.google.android.material.tabs.TabLayout;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentPagerAdapter;
//import androidx.fragment.app.FragmentTransaction;
//import androidx.viewpager.widget.ViewPager;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import brahma.vmi.brahmalibrary.R;
//import brahma.vmi.brahmalibrary.apprtc.ViewFragment;
//import brahma.vmi.brahmalibrary.common.ConnectionInfo;
//import brahma.vmi.brahmalibrary.common.DatabaseHandler;
//import brahma.vmi.brahmalibrary.wcitui.tab.BaseFragment;
//import brahma.vmi.brahmalibrary.wcitui.tab.SlidingTabLayout;
//
//import static brahma.vmi.brahmalibrary.apprtc.AppRTCClient.appListObject;
//
//@SuppressLint("ValidFragment")
//public class TabFragment extends Fragment {
//
//    private static ArrayList<String> app_nameALL = new ArrayList<>();
//    private static ArrayList<String> app_iconALL = new ArrayList<>();
//    private static ArrayList<String> packageNameALL = new ArrayList<>();
//    ArrayList<Map<String, String>> mtitle = new ArrayList<Map<String, String>>();
//    ArrayList<Map<String, String>> minfo = new ArrayList<Map<String, String>>();
//    ArrayList<Map<String, Bitmap>> mpng = new ArrayList<Map<String, Bitmap>>();
//    private String TAG = "TabFragment";
//    private SlidingTabLayout tabs;
//    private ViewPager pager;
//    private ViewPagerAdapter adapter;
//    private ConnectionInfo connectionInfo;
//    private DatabaseHandler dbHandler;
//    private JSONObject jsonResponse = null;
//    private ArrayList<JSONArray> kindString = new ArrayList();
//    private TabLayout mTabLayout;
//
//    @SuppressLint({"ValidFragment"})
//    public TabFragment(ConnectionInfo paramConnectionInfo) {
//        this.connectionInfo = paramConnectionInfo;
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.frg_tab, container, false);
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//
//        getAppList();
//
//        adapter = new ViewPagerAdapter(getFragmentManager());
//        final LinkedList<BaseFragment> fragments = getFragments();
//        pager = (ViewPager) view.findViewById(R.id.pager);
//
//
//        pager.setAdapter(adapter);
//        //tabs
////        tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);
////        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
////
////            @Override
////            public int getIndicatorColor(int position) {
////                return fragments.get(position).getIndicatorColor();
////            }
////
////            @Override
////            public int getDividerColor(int position) {
////                return fragments.get(position).getDividerColor();
////            }
////        });
////        tabs.setBackgroundResource(R.color.color_primary);
////        tabs.setCustomTabView(R.layout.tab_title, R.id.txtTabTitle, R.id.imgTabIcon);
////        tabs.setViewPager(pager);
//        mTabLayout = (TabLayout) view.findViewById(R.id.tabs);
//        mTabLayout.setupWithViewPager(pager);
//
//    }
//
//    private void getAppList() {
//        Log.d(TAG, "get APP list >>>>> " + appListObject);
//        jsonResponse = appListObject;
//    }
//
//    private LinkedList<BaseFragment> getFragments() {
//        LinkedList<BaseFragment> fragments = new LinkedList<BaseFragment>();
//        int indicatorColor = Color.parseColor(this.getResources().getString(R.color.color_accent));
//        int dividerColor = Color.TRANSPARENT;
//        Log.d(TAG, "getFragments");
//        // get kinds info
//        JSONArray kindsArray = null;
//        if (jsonResponse != null) {
//            Log.d(TAG, "jsonResponse is not null");
//            try {
//                kindsArray = jsonResponse.getJSONArray("kinds");
//                String kind_count = jsonResponse.getString("kind_count");
//                Log.d(TAG, "tabFragment kinds:" + kindsArray.toString() + " ,kind_count:" + kind_count);
//                Log.d(TAG, "kindsArray.length():" + kindsArray.length());
//                FragmentManager fm = getFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                for (int i = 0; i < kindsArray.length(); i++) {
//                    JSONArray applist = new JSONArray(jsonResponse.getJSONObject("applist").getString(kindsArray.get(i).toString()));
//                    ViewFragment f = new ViewFragment();
//                    adapter.addFragment(f, kindsArray.get(i).toString());
////                    f.setTitle(title);
////                    f.setIndicatorColor(indicatorColor);
////                    f.setDividerColor(dividerColor);
//
//                    //appList = applist;
//                    ArrayList<String> app_name = new ArrayList<>();
//                    ArrayList<String> app_icon = new ArrayList<>();
//                    ArrayList<String> packageName = new ArrayList<>();
//
//                    for (int j = 0; j < applist.length(); j++) {
//                        try {
//                            Log.d(TAG, "applist data:" + applist.get(j).toString());
//                            JSONObject jsonObject = applist.getJSONObject(j);
//                            app_name.add(jsonObject.getString("app_name"));
//                            app_icon.add(jsonObject.getString("app_icon"));
//                            packageName.add(jsonObject.getString("package_name"));
//
//                            app_nameALL.add(jsonObject.getString("app_name"));
//                            app_iconALL.add(jsonObject.getString("app_icon"));
//                            packageNameALL.add(jsonObject.getString("package_name"));
//                            //記得要清除arrayList
//                            //去除重复
////                app_nameALL = removeDuplicate(app_nameALL);
////                app_iconALL = removeDuplicate(app_iconALL);
////                packageNameALL = removeDuplicate(packageNameALL);
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    //pass data
//                    Bundle args = new Bundle();
//                    args.putString("app_name", String.valueOf(app_name));
//                    args.putString("app_icon", String.valueOf(app_icon));
//                    args.putString("packageName", String.valueOf(packageName));
//                    args.putString("needSearch", "false");
//
//                    f.setArguments(args);
//
//
//                    fragments.add(KindsFragment.newInstance(kindsArray.get(i).toString(), indicatorColor, dividerColor, applist));
//                    Log.d(TAG, "applist >>>>> " + applist.toString());
//                }
//                //全部都顯示的
//                fragments.add(KindsFragment.newInstance("Search", indicatorColor, dividerColor));
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//                Log.d(TAG, "getFragments error:" + e.getMessage());
//            }
//        } else {
//            Log.d(TAG, "jsonResponse is NULL!!");
//        }
//        return fragments;
//    }
//
//
////    //透過連線取得kids
////    public void getKinds() {
////
////        uriAPI = "https://" + connectionInfo.getHost() + ":" + connectionInfo.getPort() + "/mobile/mobile_applist";
////        Log.d(TAG, "getKinds uri:" + uriAPI);
////        this.dbHandler = new DatabaseHandler(context);//context = BrahmaMainActivity
////
////        Thread t = new Thread(new sendPostRunnable());
////        t.start();
////    }
//
////    private String sendPostDataToInternet() {
////        EasySSLSocketFactory socketFactory;
////        socketFactory = new EasySSLSocketFactory();
////        JSONObject http_data = new JSONObject();
////
////        try {
////            http_data.put("type", techDayType);
////        } catch (JSONException e) {
////            e.printStackTrace();
////        }
////        Log.d(TAG, "type:" + http_data.toString());
////        try {
////            // set up HttpParams
////            HttpParams params2 = new BasicHttpParams();
////            HttpProtocolParams.setVersion(params2, HttpVersion.HTTP_1_1);
////            HttpProtocolParams.setContentCharset(params2, HTTP.UTF_8);
////
////            SchemeRegistry registry = new SchemeRegistry();//定義使用的協議的類型
////            registry.register(new Scheme("https", socketFactory, connectionInfo.getPort()));
////            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params2, registry);
////            // create HttpClient
////            DefaultHttpClient httpclient = new DefaultHttpClient(ccm, params2);
////            HttpPost post = new HttpPost(uriAPI);
////            post.setHeader(HTTP.CONTENT_TYPE, "application/json");
////            post.addHeader("brahma-authtoken", "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6IjBhMDhkMGU4LWFhYmMtNGE3YS1iYjIyLWUxM2U0OWFhM2ZhYyIsImlzcyI6ImR5bGFuZHktZGV2Iiwicm9sZSI6ImFkbWluIiwiaWF0IjoxNTE3Mzc4NDM0fQ.Ac5EUkcwEX6b1N7FJWPd9HMyL9ULxBlQo_8k_-ClmYf3ZKA_E0vVqYfi5dT3TqXZYcEC93IKZZi0R1DrJAaeDxSwoyix7PILmNfYbmZ43YELfqamnDU52xQvMKgmRwTu0I-Yc5Xc6rTU6_FrfRp5EpWzXt_9lZ5G_6BlRJxYXkQ");
////
////            StringEntity entity = null;
////            int responseCode = 0;
////            HttpResponse response = null;
////            try {
////                entity = new StringEntity(http_data.toString());
////                post.setEntity(entity);
////                response = httpclient.execute(post);
////                responseCode = response.getStatusLine().getStatusCode();
////                Log.d("IanIan", "IanIan responseCode!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + responseCode);
////            } catch (UnsupportedEncodingException e) {
////                e.printStackTrace();
////            } catch (ClientProtocolException e) {
////                e.printStackTrace();
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////            /* 若狀態碼為200 ok */
////            if (responseCode == 200) {
////                // get JSON object
////                ByteArrayOutputStream out = new ByteArrayOutputStream();
////                response.getEntity().writeTo(out);
////                out.close();
////                //Log.d(TAG,"tabFragment response:"+out.toString());
////                jsonResponse = new JSONObject(out.toString());
////                Log.d(TAG, "tabFragment response:" + jsonResponse.toString());
////
////                if (jsonResponse.has("msg")) {
////                    //clerk.setProduct(3);//失敗
////                    Log.d(TAG, "失敗");
////                } else {
////                    getResult = true;
////                    isWaiting = false;
////                }
////                /* 取出回應字串 */
////                String strResult = "";
////                handler.post(new MyThread());//调用方法发送消息（原来想要执行的地方）
////
////                // 回傳回應字串
////                return strResult;
////            } else if (responseCode == 404) {
////                //clerk.setProduct(2);
////                Log.d(TAG, "responseCode =" + responseCode);
////                String strResult = "";
////                // 回傳回應字串
////                return strResult;
////            } else if (responseCode == 400 || responseCode == 401) { // "Unauthorized", code for PASSWORD_CHANGE_FAIL
////                //clerk.setProduct(3);
////                Log.d(TAG, "responseCode =" + responseCode);
////                String strResult = "";
////                // 回傳回應字串
////                return strResult;
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////        return null;
////    }
//
////    private void setView() {
////        Log.d(TAG,"setView");
////        //adapter
////        final LinkedList<BaseFragment> fragments = getFragments();
////        adapter = new TabFragmentPagerAdapter(getFragmentManager(), fragments);
////        //pager
////        pager = (ViewPager) cv.findViewById(R.id.pager);
////        pager.setAdapter(adapter);
////        //tabs
////        tabs = (SlidingTabLayout) cv.findViewById(R.id.tabs);
////        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
////
////            @Override
////            public int getIndicatorColor(int position) {
////                return fragments.get(position).getIndicatorColor();
////            }
////
////            @Override
////            public int getDividerColor(int position) {
////                return fragments.get(position).getDividerColor();
////            }
////        });
////        tabs.setBackgroundResource(R.color.color_primary);
////        tabs.setCustomTabView(R.layout.tab_title, R.id.txtTabTitle, R.id.imgTabIcon);
////        tabs.setViewPager(pager);
////    }
//
////    class sendPostRunnable implements Runnable {
////        // String user = null;
////        // String pd = null;
////        // private LoginStream.Clerk clerk;
////
////        // 建構子，設定要傳的字串
////        public sendPostRunnable() {
////            // this.user = user;
////            // this.pd = pd;
////            // this.clerk = clerk;
////        }
////
////        @Override
////        public void run() {
////            String result = sendPostDataToInternet();
////            //mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();
////        }
////    }
////
////    class MyThread implements Runnable {
////        public void run() {
////            //原来想要执行的代码
////            setView();
////        }
////    }
//}
//
//class ViewPagerAdapter extends FragmentPagerAdapter {
//    private final List<Fragment> mFragmentList = new ArrayList<>();
//    private final List<String> mFragmentTitleList = new ArrayList<>();
//
//    public ViewPagerAdapter(FragmentManager manager) {
//        super(manager);
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//        return mFragmentList.get(position);
//    }
//
//    @Override
//    public int getCount() {
//        return mFragmentList.size();
//    }
//
//    public void addFragment(Fragment fragment, String title) {
//        mFragmentList.add(fragment);
//        mFragmentTitleList.add(title);
//    }
//
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return mFragmentTitleList.get(position);
//    }
//}