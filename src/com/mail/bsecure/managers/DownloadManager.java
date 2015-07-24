package com.mail.bsecure.managers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IDownloadCallback;
import com.mail.bsecure.common.Item;
import com.mail.bsecure.database.App_Table;
import com.mail.bsecure.services.DownloadService;

public class DownloadManager {

	private Context context = null;
	private Intent downloadIntent = null;
	private String userid = null;
	boolean mIsBound;
	private Messenger messenger = null;
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private IDownloadCallback callback = null;
	public App_Table table = null;

	public DownloadManager(Context context) {
		this.context = context;
	}

	public void setCallbackListener(IDownloadCallback callback) {
		this.callback = callback;
		onStart();
	}
	
	public void setUserId(String userid) {
		this.userid = userid;
	}
	
	public boolean startService() {	

		downloadIntent = new Intent(context, DownloadService.class);		
		
		doBindService(downloadIntent);

		Toast.makeText(context, R.string.ds, Toast.LENGTH_SHORT).show();

		context.startService(downloadIntent);

		callback.onStateChange(DownloadService.DWL_START, 0, 0, "");

		return false;
	}

	public boolean isRunning() {
		return isServiceRunning(DownloadService.class.getName());
	}

	private boolean isServiceRunning(String name) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (name.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void doBindService(Intent service) {
		context.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			messenger = new Messenger(service); // using this messenger your can
												// send data to service
			try {

				Message msg = Message.obtain(null, DownloadService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				messenger.send(msg);

			} catch (RemoteException e) {
				//e.printStackTrace();
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			messenger = null;
		}
	};

	private void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it,
			// then now is the time to unregister.
			if (messenger != null) {
				try {
					Message msg = Message.obtain(null,
							DownloadService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					messenger.send(msg);
				} catch (RemoteException e) {
					//e.printStackTrace();
				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
			// Detach our existing connection.
			context.unbindService(mConnection);
			mIsBound = false;
		}
	}

	public void stopService() {

		doUnbindService();

		if (downloadIntent == null)
			downloadIntent = new Intent(context, DownloadService.class);

		context.stopService(downloadIntent);
	}

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			//Log.e("-=-=-=-=-=-=-=-=-","-0-0-0-0-0-0-");
			
			if (callback == null)
				return;

			switch (msg.what) {

			case DownloadService.DWL_START:
				callback.onStateChange(msg.what, msg.arg1, msg.arg2, msg.obj);
				break;

			case DownloadService.DWL_STOP:
				stopService();
				callback.onStateChange(msg.what, msg.arg1, msg.arg2, msg.obj);
				break;

			case DownloadService.DWL_PROGS:
				callback.onStateChange(msg.what, msg.arg1, msg.arg2, msg.obj);
				break;

			case DownloadService.DWL_COMPLT:
				//stopService();
				callback.onStateChange(msg.what, msg.arg1, msg.arg2, msg.obj);
				break;
				
			case DownloadService.DWL_INFO:
				callback.onStateChange(msg.what, msg.arg1, msg.arg2, msg.obj);
				break;

			case DownloadService.DWL_FAILED:
				//stopService();
				callback.onStateChange(msg.what, msg.arg1, msg.arg2, msg.obj);
				break;

			case DownloadService.DWL_CANCEL:
				stopService();
				callback.onStateChange(msg.what, msg.arg1, msg.arg2, msg.obj);
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}

	public void onDestroy() {
		doUnbindService();
	}

	public void onStart() {

		if(isServiceRunning(DownloadService.class.getName())) {
			if (downloadIntent == null)
				downloadIntent = new Intent(context, DownloadService.class);
			doBindService(downloadIntent);	
		}
		
	}

	public void addTrackToDownloadQueue(Item item) {		
		
		/*if (isTrackExistInQueue(item.getAttribute("CID"))) {
			showToast(R.string.taatdq);
			return;
		}*/

		//if (addTrackToDB(item)) {
		long rawId = addTrackToDB(item); 
		if (rawId != -1) {
					
			if (!isServiceRunning(DownloadService.class.getName())) {						
				startService();
			}
			
			if(messenger != null) {
				try {

					Message msg = Message.obtain(null, DownloadService.MSG_INFO_CLIENT);					
					msg.arg1 = (int)rawId;
					messenger.send(msg);

				} catch (RemoteException e) {
					//e.printStackTrace();
				} catch (Exception ex) {
					//ex.printStackTrace();
				}	
			}
			
			
		} else {
			//showToast(R.string.ftattdqptl);
		}

	}

	private boolean isTrackExistInQueue(String id) {

		table = getDataBaseObject();
		return table.isRecordExits(id);

	}

	private long addTrackToDB(Item item) {
		
		Item track = new Item("");
		track.setAttribute("CID", item.getAttribute("CID") + "");
		track.setAttribute("TITLE", item.getAttribute("TITLE") + "");				
		track.setAttribute("CTYPE", item.getAttribute("CT") + "");
		track.setAttribute("DOWNLOADURL", item.getAttribute("DOWNLOADURL") + "");
		
		return table.addTrack(track);

	}

	private App_Table getDataBaseObject() {

		if (table == null)
			table = new App_Table(context);

		return table;
	}

	public void showToast(int value) {
		Toast.makeText(context, value, Toast.LENGTH_SHORT).show();
	}
	
	public void clearDatabase() {
		getDataBaseObject().deleteAllTracks();
	}
	
	
	
}
