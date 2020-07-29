//package brahma.vmi.brahmalibrary.client;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.util.Log;
//
//import com.google.protobuf.ByteString;
//
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
//import brahma.vmi.brahmalibrary.services.SessionService;
//import brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity;
//
//public class SendNetIntent extends Activity{
//	private static final String TAG = "SendNetIntent";
//	private byte[] data=null;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		Intent photo = getIntent();
//		data = photo.getByteArrayExtra("data");
//		final Handler handler = new Handler();
//
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//					forwardIntent();
//
//			}
//		}).start();
//		finish();
//	}
//
//	private void forwardIntent() {
//		Log.i(TAG,"SendNetIntent forwardIntent");
//		//Put together the Intent protobuffer.
//		Log.i(TAG,"GOING TO SEND URL INTENT with URL: "+getIntent().getDataString());
//		final BRAHMAProtocol.Request.Builder msg = BRAHMAProtocol.Request.newBuilder();
//		BRAHMAProtocol.Intent.Builder intentProtoBuffer = BRAHMAProtocol.Intent.newBuilder();
//
//		Intent it = getIntent();
//		if(it.getAction().equals(Intent.ACTION_VIEW)) {
//			intentProtoBuffer.setAction(BRAHMAProtocol.IntentAction.ACTION_VIEW);
//			intentProtoBuffer.setData(getIntent().getDataString());
//			if(getIntent().getData().getScheme().equals("file")) { // handle file forwarding
//				BRAHMAProtocol.File.Builder f = BRAHMAProtocol.File.newBuilder();
//				f.setFilename(getIntent().getData().getLastPathSegment());
//				f.setData(getByteString(getIntent().getData()));
//				intentProtoBuffer.setFile(f);
//				SessionService.recordFilesStatic(getIntent().getData().getLastPathSegment());
//			}
//		}
//		else if(it.getAction().equals(Intent.ACTION_SEND)) {
//			intentProtoBuffer.setAction(BRAHMAProtocol.IntentAction.ACTION_SEND);
//			Uri data = (Uri) it.getParcelableExtra(Intent.EXTRA_STREAM);
//			if(data != null && data.getScheme().equals("file")) {
//				BRAHMAProtocol.File.Builder f = BRAHMAProtocol.File.newBuilder();
//				f.setFilename(data.getLastPathSegment());
//				f.setData(getByteString(data));
//				intentProtoBuffer.setFile(f);
//				SessionService.recordFilesStatic(data.getLastPathSegment());
//			}
//		}
//
//		//Set the Request message params and send it off.
//		msg.setType(BRAHMAProtocol.Request.RequestType.INTENT);
//
//		msg.setIntent(intentProtoBuffer.build());
////		RemoteServerClient.sendMessage(msg.build());
//
//		Intent intent = new Intent();
//		intent.setClass(this, BrahmaMainActivity.class);
//		intent.putExtra("connectionID", SessionService.getConnectionID());
//		startActivity(intent);
//
//		String timeStamp = new SimpleDateFormat("HH.mm.ss.SS").format(new Date());
//		Log.i(TAG, "Forwarding intent. Timestamp: " + timeStamp + " " + System.currentTimeMillis());
//
//		SessionService.sendMessageStatic(msg.build());
//	}
//
//
//	private ByteString getByteString(Uri uri) {
//		try {
//			InputStream iStream = getContentResolver().openInputStream(uri);
//			byte[] inputData = getBytes(iStream);
//			return ByteString.copyFrom(inputData);
//		} catch(Exception e) {}
//		return null;
//	}
//
//
//	private byte[] getBytes(InputStream inputStream) throws Exception {
//		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//		int bufferSize = 1024;
//		byte[] buffer = new byte[bufferSize];
//
//		int len = 0;
//		while ((len = inputStream.read(buffer)) != -1) {
//			byteBuffer.write(buffer, 0, len);
//		}
//		return byteBuffer.toByteArray();
//	}
//}
