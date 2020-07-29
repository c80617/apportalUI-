package brahma.vmi.brahmalibrary.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "apportal_fet.db";
    public static final int VERSION = 4;// 資料庫物件，固定的欄位變數
    private static SQLiteDatabase database;
    Context context;

//    public MyDBHelper(Context context, String name, CursorFactory factory,int version) {
//        super(context, name, factory, version);
//    }

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }

    // 需要資料庫的元件呼叫這個方法，這個方法在一般的應用都不需要修改
    public SQLiteDatabase getDatabase(Context context) {
//        if (database == null || !database.isOpen()) {
//            database = new MyDBHelper(context, DATABASE_NAME, null, VERSION).getWritableDatabase();
//        }
//
//        return database;

        if (database == null)
            database = this.getWritableDatabase();
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建立要存放資料的資料表格
        // 1. SQL語法不分大小寫
        // 2. 這裡大寫代表的是SQL標準語法, 小寫字是資料表/欄位的命名
        // 建立應用程式需要的表格

        for (int i = 0; i < 2; i++)
            createTable(i, db);
    }

    private void createTable(int i, SQLiteDatabase db) {
        try {
            db.execSQL(ItemBio.CREATE_TABLE);
            db.execSQL(ItemBio.CREATE_TABLE2);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //使用建構子時如果版本增加,便會呼叫onUpgrade()刪除舊的資料表與其內容,再重新呼叫onCreate()建立新的資料表
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // 刪除原有的表格
        db.execSQL("DROP TABLE IF EXISTS " + ItemBio.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ItemBio.TABLE_LOGIN);
        // 呼叫onCreate建立新版的表格
        onCreate(db);
    }

}