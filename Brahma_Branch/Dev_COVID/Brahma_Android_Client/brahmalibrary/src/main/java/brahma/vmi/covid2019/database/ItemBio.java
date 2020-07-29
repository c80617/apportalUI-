package brahma.vmi.covid2019.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// 資料功能類別
public class ItemBio {
    public static final String TABLE_NAME = "bio_data";
    public static final String TABLE_LOGIN = "login_data";
    public static final String KEY_ID = "_id";
    public static final String ISUSINGBIO_COLUMN = "isUsingBio";
    public static final String BIOUSERNAME_COLUMN = "bioUsername";
    public static final String BIOPASSWORD_COLUMN = "bioPassword";
    public static final String BIOIP_COLUMN = "bioIP";
    public static final String BIOPORT_COLUMN = "bioPort";

    public static final String LOGIN_TYPE_COLUMN = "loginType";
    public static final String USERNAME_COLUMN = "username";
    public static final String PASSWORD_COLUMN = "password";
    public static final String IP_COLUMN = "ip";
    public static final String PORT_COLUMN = "port";
    public static final String PACKAGE_COLUMN = "packageName";

    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ISUSINGBIO_COLUMN + " TEXT, " +
                    BIOUSERNAME_COLUMN + " TEXT, " +
                    BIOPASSWORD_COLUMN + " TEXT, " +
                    BIOIP_COLUMN + " TEXT, " +
                    BIOPORT_COLUMN + " TEXT)";

    public static final String CREATE_TABLE2 =
            "CREATE TABLE " + TABLE_LOGIN + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LOGIN_TYPE_COLUMN + " TEXT, " +
                    USERNAME_COLUMN + " TEXT, " +
                    PASSWORD_COLUMN + " TEXT, " +
                    IP_COLUMN + " TEXT, " +
                    PORT_COLUMN + " TEXT, " +
                    PACKAGE_COLUMN + " TEXT)";

    private SQLiteDatabase db;

    public ItemBio(Context context) {
        db = MyDBHelper.getDatabase(context);
    }

    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public Item insert(Item item) {
        ContentValues cv = new ContentValues();
        cv.put(ISUSINGBIO_COLUMN, item.getUsingBio());
        cv.put(BIOUSERNAME_COLUMN, item.getbioUsername());
        cv.put(BIOPASSWORD_COLUMN, item.getbioPassword());
        cv.put(BIOIP_COLUMN, item.getbioIP());
        cv.put(BIOPORT_COLUMN, item.getbioPort());
        long id = db.insert(TABLE_NAME, null, cv);
        item.setId(id);
        return item;
    }

    public Login insert(Login item2) {
        ContentValues cv = new ContentValues();
        cv.put(LOGIN_TYPE_COLUMN, item2.getLoginType());
        cv.put(USERNAME_COLUMN, item2.getUsername());
        cv.put(PASSWORD_COLUMN, item2.getPassword());
        cv.put(IP_COLUMN, item2.getIp());
        cv.put(PORT_COLUMN, item2.getPort());
        cv.put(PACKAGE_COLUMN, item2.getPackageName());
        long id = db.insert(TABLE_LOGIN, null, cv);
        item2.setId(id);
        return item2;
    }

    public boolean update(Item item) {
        ContentValues cv = new ContentValues();
        cv.put(ISUSINGBIO_COLUMN, item.getUsingBio());
        cv.put(BIOUSERNAME_COLUMN, item.getbioUsername());
        cv.put(BIOPASSWORD_COLUMN, item.getbioPassword());
        cv.put(BIOIP_COLUMN, item.getbioIP());
        cv.put(BIOPORT_COLUMN, item.getbioPort());
        String where = KEY_ID + "=" + item.getId();
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    public boolean update(Login item2) {
        ContentValues cv = new ContentValues();
        cv.put(LOGIN_TYPE_COLUMN, item2.getLoginType());
        cv.put(USERNAME_COLUMN, item2.getUsername());
        cv.put(PASSWORD_COLUMN, item2.getPassword());
        cv.put(IP_COLUMN, item2.getIp());
        cv.put(PORT_COLUMN, item2.getPort());
        cv.put(PACKAGE_COLUMN, item2.getPackageName());
        String where = KEY_ID + "=" + item2.getId();
        return db.update(TABLE_LOGIN, cv, where, null) > 0;
    }
    public boolean update_package(long id,String packagename) {
        ContentValues cv = new ContentValues();
        cv.put(PACKAGE_COLUMN, packagename);
        String where = KEY_ID + "=" + id;
        return db.update(TABLE_LOGIN, cv, where, null) > 0;
    }
    public boolean update_loginType(long id,String loginType) {
        ContentValues cv = new ContentValues();
        cv.put(LOGIN_TYPE_COLUMN, loginType);
        String where = KEY_ID + "=" + id;
        return db.update(TABLE_LOGIN, cv, where, null) > 0;
    }

    public boolean delete(long id) {
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    public boolean delete_login(long id) {
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_LOGIN, where, null) > 0;
    }

    public List<Item> getAll() {
        List<Item> result = new ArrayList<Item>();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }
        cursor.close();
        return result;
    }

    public List<Login> getAll_login() {
        List<Login> result = new ArrayList<Login>();
        Cursor cursor = db.query(TABLE_LOGIN, null, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            result.add(getRecord_login(cursor));
        }
        cursor.close();
        return result;
    }

    public Item get(long id) {
        Item item = null;
        String where = KEY_ID + "=" + id;
        Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);
        if (result.moveToFirst()) {
            item = getRecord(result);
        }
        result.close();
        return item;
    }

    public Login get_login(long id) {
        Login item = null;
        String where = KEY_ID + "=" + id;
        Cursor result = db.query(TABLE_LOGIN, null, where, null, null, null, null, null);
        if (result.moveToFirst()) {
            item = getRecord_login(result);
        }
        result.close();
        return item;
    }

    // 把游標Cursor取得的資料轉換成目前的資料包裝為物件
    public Item getRecord(Cursor cursor) {
        Item result = new Item();
        result.setId(cursor.getLong(0));
        result.setUsingBio(cursor.getString(1));
        result.setbioUsername(cursor.getString(2));
        result.setbioPassword(cursor.getString(3));
        result.setbioIP(cursor.getString(4));
        result.setbioPort(cursor.getString(5));
        return result;
    }

    public Login getRecord_login(Cursor cursor) {
        Login result = new Login();
        result.setId(cursor.getLong(0));
        result.setLoginType(cursor.getString(1));
        result.setUsername(cursor.getString(2));
        result.setPassword(cursor.getString(3));
        result.setIp(cursor.getString(4));
        result.setPort(cursor.getString(5));
        result.setPackageName(cursor.getString(6));
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    public int getCount_login() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LOGIN, null);
        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

    // 建立範例資料
    public void sample() {
        Item item = new Item(0, "false", "", "", "", "");
        Login item2 = new Login(0, "false", "", "", "", "", "");
        insert(item);
        insert(item2);
    }
    public void sample2() {
        Login item2 = new Login(0, "false", "", "", "", "", "");
        insert(item2);
    }
}