package brahma.vmi.covid2019.common;

/**
 * Created by tung on 2017/3/8.
 */

import android.content.Context;

import com.google.protobuf.ByteString;

import java.io.FileOutputStream;

import brahma.vmi.covid2019.protocol.BRAHMAProtocol;
import brahma.vmi.covid2019.services.SessionService;

public class FileHandler {
    private static final String TAG = FileHandler.class.getName();

    public static void inspect(BRAHMAProtocol.Response response, Context context) {
        BRAHMAProtocol.File brahmaFile = response.getFile();
        SessionService.removeFromWaitingListStatic(brahmaFile.getFilename());
        saveToFile(brahmaFile);
    }
    protected static void saveToFile(BRAHMAProtocol.File f) {
        try {
            ByteString bs = f.getData();
            byte[] arr = bs.toByteArray();
            FileOutputStream out = new FileOutputStream("/sdcard/" + f.getFilename());
            out.write(arr);
            out.close();
        } catch(Exception e) {}
    }
}
