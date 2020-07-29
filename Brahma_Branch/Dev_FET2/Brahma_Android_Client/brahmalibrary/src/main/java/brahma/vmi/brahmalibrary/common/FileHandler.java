package brahma.vmi.brahmalibrary.common;

/**
 * Created by tung on 2017/3/8.
 */

import android.content.Context;

import com.google.protobuf.ByteString;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
import brahma.vmi.brahmalibrary.services.SessionService;

public class FileHandler {
    private static final String TAG = FileHandler.class.getName();

    public static void inspect(BRAHMAProtocol.Response response, Context context) {
        BRAHMAProtocol.File brahmaFile = response.getFile();
        SessionService.removeFromWaitingListStatic(brahmaFile.getFilename());
        saveToFile(brahmaFile);
    }

    protected static void saveToFile(BRAHMAProtocol.File f) {
        ByteString bs = f.getData();
        byte[] arr = bs.toByteArray();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream("/sdcard/" + f.getFilename());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            assert out != null;
            out.write(arr);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
