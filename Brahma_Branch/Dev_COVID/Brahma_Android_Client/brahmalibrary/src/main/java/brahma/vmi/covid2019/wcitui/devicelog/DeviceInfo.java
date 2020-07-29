package brahma.vmi.covid2019.wcitui.devicelog;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.util.DisplayMetrics;

/**
 * Created by tung on 2017/8/31.
 */

public class DeviceInfo {
    private Activity activity;

    public static String mAppName = "";
    public static String mLocale = "";
    public static String mUser = "";
    //Device
    public static String mBoard = "";
    public static String mBrand = "";
    public static String mDevice = "";
    public static String mModel = "";
    public static String mProduct = "";
    public static String mTAGS = "";
    //OS
    public static String mOSRelease = "";
    public static String mDisplayBuild = "";
    public static String mFingerPrint = "";
    public static String mBuildID = "";
    public static String mBuildTime= "";
    public static String mBuildType = "";
    public static String mBuildUser = "";
    //Density
    public static String mDensity = "";
    public static String mDensityDpi = "";
    public static String mscaledDensity = "";
    public static String mxdpi = "";
    public static String mydpi = "";
    //Density reference
    public static String mDensityDEFAULT = "";
    public static String mDensityLOW = "";
    public static String mDensityMEDIUM = "";
    public static String mDensityHIGH = "";
    //Screen
    public static String mHeightPixels = "";
    public static String mWidthPixels = "";


    public static String getmTAGS() {
        return mTAGS;
    }

    public static String getmAppName() {
        return mAppName;
    }

    public static String getmUser() {
        return mUser;
    }

    public static String getmLocale() {
        return mLocale;
    }

    public static String getmBoard() {
        return mBoard;
    }

    public static String getmBrand() {
        return mBrand;
    }

    public static String getmDevice() {
        return mDevice;
    }

    public static String getmModel() {
        return mModel;
    }

    public static String getmProduct() {
        return mProduct;
    }

    public static String getmOSRelease() {
        return mOSRelease;
    }

    public static String getmDisplayBuild() {
        return mDisplayBuild;
    }

    public static String getmFingerPrint() {
        return mFingerPrint;
    }

    public static String getmBuildID() {
        return mBuildID;
    }

    public static String getmBuildTime() {
        return mBuildTime;
    }

    public static String getmBuildType() {
        return mBuildType;
    }

    public static String getmBuildUser() {
        return mBuildUser;
    }

    public static String getmDensity() {
        return mDensity;
    }

    public static String getmDensityDpi() {
        return mDensityDpi;
    }

    public static String getMscaledDensity() {
        return mscaledDensity;
    }

    public static String getMxdpi() {
        return mxdpi;
    }

    public static String getMydpi() {
        return mydpi;
    }

    public static String getmDensityDEFAULT() {
        return mDensityDEFAULT;
    }

    public static String getmDensityLOW() {
        return mDensityLOW;
    }

    public static String getmDensityMEDIUM() {
        return mDensityMEDIUM;
    }

    public static String getmDensityHIGH() {
        return mDensityHIGH;
    }

    public static String getmHeightPixels() {
        return mHeightPixels;
    }

    public static String getmWidthPixels() {
        return mWidthPixels;
    }

    public void deviceInit(Activity ac){

        this.activity = ac;

        try {
            PackageInfo info = activity.getApplication().getPackageManager().getPackageInfo(activity.getApplication().getPackageName(), 0);
            mAppName  = "APProtal v"+info.versionName+"("+info.versionCode+")";



            mLocale = "Locale: "+activity.getResources().getConfiguration().locale.toString();

            mBoard = "Board: "+android.os.Build.BOARD;
            mBrand = "Brand: "+android.os.Build.BRAND;
            mDevice = "Device: "+ android.os.Build.DEVICE;
            mModel = android.os.Build.MODEL;
            mProduct = "Product: "+android.os.Build.PRODUCT;
            mTAGS = "TAGS: "+android.os.Build.TAGS;

            mOSRelease = android.os.Build.VERSION.RELEASE;
            mDisplayBuild = "Display build: "+android.os.Build.DISPLAY;
            mFingerPrint = "Finger print: "+android.os.Build.FINGERPRINT;
            mBuildID = "Build ID: "+android.os.Build.ID;
            mBuildTime = "Time: "+android.os.Build.TIME;
            mBuildType = "Type: "+android.os.Build.TYPE;
            mBuildUser = "User: "+android.os.Build.USER;

            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            mDensity = "density: "+metrics.density;
            mDensityDpi = "densityDpi: "+metrics.densityDpi;
            mscaledDensity = "scaledDensity: "+metrics.scaledDensity;
            mxdpi = "xdpi: "+metrics.xdpi;
            mydpi = "ydpi: "+metrics.ydpi;

            mDensityDEFAULT = "DENSITY_DEFAULT: "+DisplayMetrics.DENSITY_DEFAULT;
            mDensityLOW = "DENSITY_LOW: "+DisplayMetrics.DENSITY_LOW;
            mDensityMEDIUM = "DENSITY_MEDIUM: "+DisplayMetrics.DENSITY_MEDIUM;
            mDensityHIGH = "DENSITY_HIGH: "+DisplayMetrics.DENSITY_HIGH;

            mHeightPixels = "heightPixels: "+metrics.heightPixels;
            mWidthPixels = "widthPixels: "+metrics.widthPixels;


        } catch (Exception e) {
            e.printStackTrace();
//            setTextOfLabel(true, "Exception: "+e.toString();
        }
    }

    public void setUser(String user) {
        this.mUser = user;
    }
}
