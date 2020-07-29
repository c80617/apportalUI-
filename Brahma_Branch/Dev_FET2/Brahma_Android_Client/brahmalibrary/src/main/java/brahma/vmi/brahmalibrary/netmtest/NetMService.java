//package brahma.vmi.brahmalibrary.netmtest;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.app.TaskStackBuilder;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageManager;
//import android.net.TrafficStats;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.os.PowerManager;
//import android.preference.PreferenceManager;
//import androidx.annotation.Nullable;
//import android.util.Log;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//import brahma.vmi.brahmalibrary.R;
//
///**
// * Created by tung on 2017/6/14.
// */
//
//public class NetMService extends Service {
//    private long previousBytesSentSinceBoot;
//    private long previousBytesReceivedSinceBoot;
//
//    private Notification.Builder mBuilder;
//    private NotificationManager mNotifyMgr;
//
//    private SharedPreferences sharedPref;
//
//    private UnitConverter converter;
//
//    private String unitMeasurement;
//
//    private PowerManager pm;
//
//    private boolean showTotalValueNotification;
//    private boolean hideNotification;
//    private boolean firstUpdate;
//
//    private ScheduledFuture updateHandler;
//
//    private int uid;
//
//    private long start = 0l;
//
//    private double totalSecondsSinceLastPackageRefresh = 0d;
//    private double totalSecondsSinceNotificaitonTimeUpdated = 0d;
//
//    private static int ping;
//
//    @Override public void onCreate() {
//        super.onCreate();
//        createService(this);
//    }
//
//    private void createService(final Service service) {
//        firstUpdate = true;
//
//        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        sharedPref = PreferenceManager.getDefaultSharedPreferences(service);
//
//
//        unitMeasurement = "Mbps";
//        showTotalValueNotification = false;
//        long pollRate = Long.parseLong("1");
//        hideNotification = false;
//
//        converter = getUnitConverter(unitMeasurement);
//
//            mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            int mId = 1;
//            mBuilder = new Notification.Builder(service).setSmallIcon(R.drawable.idle)
//                    .setContentTitle("")
//                    .setContentText("")
//                    .setOngoing(true);
//
//            if (hideNotification) {
//                mBuilder.setPriority(Notification.PRIORITY_MIN);
//            } else {
//                mBuilder.setPriority(Notification.PRIORITY_HIGH);
//            }
//
//            Intent resultIntent = new Intent();
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(service);
//
//            stackBuilder.addNextIntent(resultIntent);
//            PendingIntent resultPendingIntent =
//                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.setContentIntent(resultPendingIntent);
//
//            Notification notification = mBuilder.build();
//
//            mNotifyMgr.notify(mId, notification);
//
//            startForeground(mId, notification);
//        Log.d("IANN", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!NetMService");
//        startUpdateService(pollRate);
//    }
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//    @Override public void onDestroy() {
//        try {
//            updateHandler.cancel(true);
//        } catch (NullPointerException e) {
//            //The only way there will be a null pointer, is if the disabled preference is checked.  Because if it is, onDestory() is called right away, without creating the updateHandler
//        }
//        super.onDestroy();
//    }
//
//    @Override public int onStartCommand(Intent intent, int flags, int startId) {
//        Bundle extras = null;
//        if (intent != null) {
//            extras = intent.getExtras();
//        }
//        boolean wasPackageAdded = false;
//        //int newAppUid = 0;
//        if (extras != null) {
//            wasPackageAdded = extras.getBoolean("PACKAGE_ADDED");
//            //newAppUid = extras.getInt("EXTRA_UID");
//        }
//        return START_STICKY;
//    }
//
//
//    private UnitConverter getUnitConverter(String unitMeasurement) {
//
//        if (unitMeasurement.equals("bps")) {
//            return (new UnitConverter() {
//                @Override public double convert(double bytesPerSecond) {
//                    return (bytesPerSecond * 8.0);
//                }
//            });
//        }
//        if (unitMeasurement.equals("Kbps")) {
//            return (new UnitConverter() {
//                @Override public double convert(double bytesPerSecond) {
//                    return (bytesPerSecond * 8.0) / 1000.0;
//                }
//            });
//        }
//        if (unitMeasurement.equals("Mbps")) {
//            return (new UnitConverter() {
//                @Override public double convert(double bytesPerSecond) {
//                    return (bytesPerSecond * 8.0) / 1000000.0;
//                }
//            });
//        }
//        if (unitMeasurement.equals("Gbps")) {
//            return (new UnitConverter() {
//                @Override public double convert(double bytesPerSecond) {
//                    return (bytesPerSecond * 8.0) / 1000000000.0;
//                }
//            });
//        }
//        if (unitMeasurement.equals("Bps")) {
//            return (new UnitConverter() {
//                @Override public double convert(double bytesPerSecond) {
//                    return bytesPerSecond;
//                }
//            });
//        }
//        if (unitMeasurement.equals("KBps")) {
//            return (new UnitConverter() {
//                @Override public double convert(double bytesPerSecond) {
//                    return bytesPerSecond / 1000.0;
//                }
//            });
//        }
//        if (unitMeasurement.equals("MBps")) {
//            return (new UnitConverter() {
//                @Override public double convert(double bytesPerSecond) {
//                    return bytesPerSecond / 1000000.0;
//                }
//            });
//        }
//        if (unitMeasurement.equals("GBps")) {
//            return (new UnitConverter() {
//                @Override public double convert(double bytesPerSecond) {
//                    return bytesPerSecond / 1000000000.0;
//                }
//            });
//        }
//
//        return (new UnitConverter() {
//            @Override public double convert(double bytesPerSecond) {
//                return (bytesPerSecond * 8.0) / 1000000.0;
//            }
//        });
//    }
//
//    private void startUpdateService(long pollRate) {
//        final Runnable updater = new Runnable() {
//            public void run() {
//                update();
//            }
//        };
//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        updateHandler = scheduler.scheduleAtFixedRate(updater, 0, pollRate, TimeUnit.SECONDS);
//    }
//
//    @SuppressWarnings("deprecation") private void update() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (!pm.isInteractive()) {
//                return;
//            }
//        } else if (!pm.isScreenOn()) {
//            return;
//        }
//
//        initiateUpdate();
//    }
//
//    private synchronized void initiateUpdate() {
//
//        try {
//            PackageManager pm = getPackageManager();
//            ApplicationInfo ai = pm.getApplicationInfo("brahma.vmi.apportal", PackageManager.GET_ACTIVITIES);
//            uid=ai.uid;
//            Log.d("!!", "!!" + ai.uid);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        if (firstUpdate) {
//            previousBytesSentSinceBoot =
//                    TrafficStats.getUidTxBytes(uid);//i dont initialize these to 0, because if i do, when app first reports, the rate will be crazy high
//            previousBytesReceivedSinceBoot = TrafficStats.getUidRxBytes(uid);
//            firstUpdate = false;
//        }
//
//
//        long end = System.nanoTime();
//        long totalElapsed = end - start;
//        long bytesSentSinceBoot = TrafficStats.getUidTxBytes(uid);
//        long bytesReceivedSinceBoot = TrafficStats.getUidRxBytes(uid);
//        start = System.nanoTime();
//
//        double totalElapsedInSeconds = (double) totalElapsed / 1000000000.0;
//        totalSecondsSinceLastPackageRefresh += totalElapsedInSeconds;
//        totalSecondsSinceNotificaitonTimeUpdated += totalElapsedInSeconds;
//        long bytesSentOverPollPeriod = bytesSentSinceBoot - previousBytesSentSinceBoot;
//        long bytesReceivedOverPollPeriod = bytesReceivedSinceBoot - previousBytesReceivedSinceBoot;
//
//        double bytesSentPerSecond = bytesSentOverPollPeriod / totalElapsedInSeconds;
//        double bytesReceivedPerSecond = bytesReceivedOverPollPeriod / totalElapsedInSeconds;
//
//        previousBytesSentSinceBoot = bytesSentSinceBoot;
//        previousBytesReceivedSinceBoot = bytesReceivedSinceBoot;
//
//            updateNotification(bytesSentPerSecond, bytesReceivedPerSecond);
//    }
//
//    public void setPing(int ping){
//        this.ping = ping;
//    }
//
//    private void updateNotification(double bytesSentPerSecond, double bytesReceivedPerSecond) {
//        String sentString = String.format("%.3f", (converter.convert(bytesSentPerSecond)));
//        String receivedString = String.format("%.3f", (converter.convert(bytesReceivedPerSecond)));
//
//        String displayValuesText = "";
//        if (showTotalValueNotification) {
//            double total =
//                    (converter.convert(bytesSentPerSecond) + converter.convert(bytesReceivedPerSecond));
//            String totalString = String.format("%.3f", total);
//            displayValuesText = "Total: " + totalString;
//        }
//
//        displayValuesText += " Up: " + sentString + " Down: " + receivedString+ " Ping: " + this.ping + "ms";
//        String contentTitleText = unitMeasurement;
//
//
//        mBuilder.setContentText(displayValuesText);
//        mBuilder.setContentTitle(contentTitleText);
//
//        if (totalSecondsSinceNotificaitonTimeUpdated > 10800) { //10800 seconds is three hours
//            mBuilder.setWhen(System.currentTimeMillis());
//            totalSecondsSinceNotificaitonTimeUpdated = 0;
//        }
//
//        int mId = 1;
//        if (!hideNotification) {
//
//            if (bytesSentPerSecond < 13107 && bytesReceivedPerSecond < 13107) {
//                mBuilder.setSmallIcon(R.drawable.idle);
//                mNotifyMgr.notify(mId, mBuilder.build());
//                return;
//            }
//
//            if (!(bytesSentPerSecond > 13107) && bytesReceivedPerSecond > 13107) {
//                mBuilder.setSmallIcon(R.drawable.download);
//                mNotifyMgr.notify(mId, mBuilder.build());
//                return;
//            }
//
//            if (bytesSentPerSecond > 13107 && bytesReceivedPerSecond < 13107) {
//                mBuilder.setSmallIcon(R.drawable.upload);
//                mNotifyMgr.notify(mId, mBuilder.build());
//                return;
//            }
//
//            if (bytesSentPerSecond > 13107
//                    && bytesReceivedPerSecond > 13107) {//1307 bytes is equal to .1Mbit
//                mBuilder.setSmallIcon(R.drawable.both);
//                mNotifyMgr.notify(mId, mBuilder.build());
//            }
//        }
//        mNotifyMgr.notify(mId, mBuilder.build());
//    }
//
//}
