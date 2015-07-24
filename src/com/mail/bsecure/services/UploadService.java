package com.mail.bsecure.services;

import java.io.File;
import java.io.InputStream;

import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.callbacks.IRequestCallback;
import com.mail.bsecure.common.Constants;
import com.mail.bsecure.common.Item;
import com.mail.bsecure.common.NetworkInfoAPI;
import com.mail.bsecure.database.App_Table;
import com.mail.bsecure.tasks.HTTPost;

public class UploadService extends Service implements IRequestCallback, IItemHandler {

	private Messenger mMessenger = new Messenger(new IncomingHandler());

	private Messenger client = null; // using this Messenger you can send data
										// back to activity

	public static final int MSG_REGISTER_CLIENT = 1;

	public static final int MSG_UNREGISTER_CLIENT = 2;

	public static final int DWL_START = 3;

	public static final int DWL_PROGS = 4;

	public static final int DWL_CANCEL = 5;

	public static final int DWL_FAILED = 6;

	public static final int DWL_COMPLT = 7;
	
	public static final int DWL_INFO = 8;
	
	public static final int MSG_INFO_CLIENT = 9;

	public static final int DWL_STOP = -2;
	
	//private HTTPGetTask task = null;
	
	private HTTPost post  = null;

	private App_Table table = null;
	
	private Item currentItem = null;
		
	private NetworkInfoAPI networkInfoAPI = null;
	
	private MediaScannerConnection mediaScannerConnection;
	
	private int currentCount = 0;
	
	private int totalCount = 0;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		networkInfoAPI = new NetworkInfoAPI();
		
		networkInfoAPI.initialize(this);
		
		startDownloading();
		
		return START_STICKY; // run until explicitly stopped.
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMessenger = null;
		client = null;
		currentCount = 0;
		totalCount = 0;
		
		if(networkInfoAPI != null)
			networkInfoAPI.onDestroy();
		networkInfoAPI = null;
		
		/*if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
			task = null;
		}*/
		
		if (post != null && post.getStatus() == Status.RUNNING) {
			post.cancel(true);
			post = null;
		}
	}

	private void startDownloading() {
		
		/*if(!checkForNetwork("download")) {
			stopIt();
			return;
		}*/
		currentCount++;
			
		currentItem = getTrackFromQueue();
		if(currentItem != null) {
			
			String path = currentItem.getAttribute("DOWNLOADURL");
			//String link = currentItem.getAttribute("DOWNLOADURL");
			
			post = new HTTPost(this, this, null);
			post.enableMutipart(false);
			post.execute("https://bsecure.email/assets/upload/compose/", path);
			
			/*task = new HTTPGetTask(this, this, true, currentItem.getAttribute("TITLE")+"_"+currentItem.getAttribute("CID"));
			task.execute(link);*/
			
			int count = getTracksCount();
			
			totalCount = count + (currentCount - 1);
			
			sendMessageToUI(UploadService.DWL_START, currentCount, totalCount, currentItem.getAttribute("TITLE"));	
			
			return;
		} 
		
		showToast(R.string.atad);
		stopIt();

	}	
	
	private Item getTrackFromQueue() {
		return getDataBaseObject().getTrackFromQueue();
	}
	
	private int getTracksCount() {
		return getDataBaseObject().getCount();
	}

	private void stopIt() {
		
		/*if (client == null) 
			stopSelf();
		
		sendMessageToUI(DWL_STOP, 0, 0, null);*/
		
		if (client == null) 
			stopSelf();
		
		if(client != null) {
			sendMessageToUI(DWL_STOP, 0, 0, null);
			stopSelf();
			return;
		}
		
		sendMessageToUI(DWL_STOP, 0, 0, null);
		
	}

	class IncomingHandler extends Handler { // Handler of incoming messages from
											// clients.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case MSG_REGISTER_CLIENT:
				client = msg.replyTo;
				// Log.e("IncomingHandler inside service", "2");
				
				if(currentItem != null) {
					int count = getTracksCount();
					
					totalCount = count + (currentCount - 1);
					
					sendMessageToUI(UploadService.DWL_START, currentCount, totalCount, currentItem.getAttribute("TITLE"));
					//sendMessageToUI(DownloadService.DWL_START, 1, count, currentItem.getAttribute("TITLE"));
				} else
					sendMessageToUI(UploadService.DWL_START, 1, 1, "");
				
				break;
				
			case MSG_UNREGISTER_CLIENT:
				client = null;
				break;
				
			case MSG_INFO_CLIENT:
				
				int count = msg.arg1;//getTracksCount();								
				
				totalCount = count + (currentCount - 1);
				
				sendMessageToUI(UploadService.DWL_INFO, currentCount, totalCount, "");
				
				break;

			default:
				super.handleMessage(msg);
			}
		}

	}

	private void sendMessageToUI(int state, int value, int value1, Object object) {

		try {
			if (client == null)
				return;

			// Log.e("sendMessageToUI inside service", "-=-=-=-=-=-=-=-=- 4");
			client.send(Message.obtain(null, state, value, value1, object));
		} catch (RemoteException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			//e.printStackTrace();
		}

	}

	@Override
	public void onRequestComplete(InputStream inputStream, String mimeType) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onRequestComplete(String data, String mimeType) {				
		
		if(data != null) {
			data.contains("###");
			String[] temp = data.split("###");
			String path = temp[1];
			File file = new File(Constants.DWLPATH+ path);
			new ScanContent(null, file.toString()).scanFile();
			
		}
		
		//File file = new File(Constants.DWLPATH+ downloadFileName);
		
		//requestUrl + "###" + downloadFileName, mimeType
		
		int cid = 0;
		String cid_txt = currentItem.getAttribute("CID");
		if(cid_txt.length() > 0)
			cid = Integer.parseInt(cid_txt);
		
		sendMessageToUI(DWL_COMPLT, cid, 0, data + "###" + mimeType);
		
		deleteRecord(currentItem.getAttribute("CID"));		
		
		//downloadNotify(currentItem.getAttribute("CID"), currentItem.getAttribute("DWNTYPE"), currentItem.getAttribute("CTYPE"));
		
		String msg=getString(R.string.track)+ "\""+currentItem.getAttribute("TITLE")+"\"" +getString(R.string.dwlsuccess);
		showToast(msg);
		startDownloading();
		
	}

	@Override
	public void onRequestFailed(String errorData) {
		sendMessageToUI(DWL_FAILED, 0, 0, errorData);
		if(errorData != null && errorData.equalsIgnoreCase("No Internet, Please check your network settings")) {
			sendMessageToUI(DWL_STOP, 0, 0, errorData);
			return;
		}
		deleteRecord(currentItem.getAttribute("CID"));
		showToast(getString(R.string.ftdt)+" \""+currentItem.getAttribute("TITLE")+"\""+getString(R.string.ptl));
		startDownloading();
	}

	@Override
	public void onRequestCancelled(String extraInfo) {
		sendMessageToUI(DWL_CANCEL, 0, 0, extraInfo);
	}

	@Override
	public void onRequestProgress(Long... values) {
		sendMessageToUI(DWL_PROGS, 0, 0, values);
	}

	
	public void showToast(int value) {
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}
	
	public void showToast(String value) {
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}
	
	private App_Table getDataBaseObject() {

		if (table == null)
			table = new App_Table(this);
		
		return table;
	}
	
	private void deleteRecord(final String id) {
		
		/*new Thread(new Runnable() {
			public void run() {				
				table.removeTrack(id);
			}
		}).start();*/
		
		table.removeTrack(id);
		
	}					

	@Override
	public void onFinish(Object results, int requestType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(String errorCode, int requestType) {
		// TODO Auto-generated method stub
		
	}
		
	class ScanContent implements MediaScannerConnectionClient {
		
		//private Item item = null;
		private String filePath = "";

		ScanContent(Item item, String filePath) {
			//this.item = item;
			this.filePath = filePath;
		}

		@Override
		public void onMediaScannerConnected() {
			try {
				mediaScannerConnection.scanFile(filePath, null);
			} catch (java.lang.IllegalStateException e) {
				//e.printStackTrace();
			} catch (Exception e) {
				//e.printStackTrace();
			}
			
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			mediaScannerConnection.disconnect();			
		}
		
		private void scanFile() {
			
			if (mediaScannerConnection != null)
				mediaScannerConnection.disconnect();
			
			mediaScannerConnection = new MediaScannerConnection(UploadService.this, this);
			mediaScannerConnection.connect();
		}
		
	}

}
