package com.mail.bsecure.common;

import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkInfoAPI {

	private boolean registered = false;

	private ConnectivityManager cmanager = null;

	private BroadcastReceiver receiver = null;

	public static final String TYPE_NONE = "none";

	private String lastStatus = "";

	private Context mContext = null;

	protected OnNetworkChangeListener callback = null;

	public interface OnNetworkChangeListener {
		public void onNetworkChange(String status);
	}

	public void setOnNetworkChangeListener(OnNetworkChangeListener aCallback) {
		this.callback = aCallback;
		setOnNetworkChange();
	}

	public void initialize(Context context) {
		this.cmanager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		this.mContext = context;

	}

	private void setOnNetworkChange() {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		if (this.receiver == null) {
			this.receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					updateConnectionInfo(cmanager.getActiveNetworkInfo());
				}
			};
			mContext.registerReceiver(this.receiver, intentFilter);
			this.registered = true;
		}
	}

	public String execute(String action) {
		if (action.equals("getConnectionInfo")) {
			NetworkInfo info = cmanager.getActiveNetworkInfo();
			return this.getConnectionInfo(info);
		}
		return TYPE_NONE;
	}

	private void updateConnectionInfo(NetworkInfo info) {
		String status = this.getConnectionInfo(info);

		if (!status.equals(lastStatus)) {
			sendUpdate(status);
			lastStatus = status;
		}
	}

	/**
	 * Get the latest network connection information
	 * 
	 * @param info
	 *            the current active network info
	 * @return a JSONObject that represents the network info
	 */
	private String getConnectionInfo(NetworkInfo info) {
		String type = TYPE_NONE;
		if (info != null) {
			// If we are not connected to any network set type to none
			if (!info.isConnected()) {
				type = TYPE_NONE;
			} else {
				type = getType(info);
			}
		}
		return type;
	}

	private void sendUpdate(String status) {
		callback.onNetworkChange(status);
	}

	private String getType(NetworkInfo info) {
		if (info != null) {
			String type = info.getTypeName();

			if (type.toLowerCase(Locale.ENGLISH).equalsIgnoreCase("wifi")) {
				return "wifi";
			} else if (type.toLowerCase(Locale.ENGLISH).equalsIgnoreCase(
					"mobile")) {
				type = info.getSubtypeName();
				if (type.toLowerCase(Locale.ENGLISH).equalsIgnoreCase("gsm")
						|| type.toLowerCase(Locale.ENGLISH).equals("gprs")
						|| type.toLowerCase(Locale.ENGLISH).equals("edge")) {
					return "2g";
				} else if (type.toLowerCase(Locale.ENGLISH).startsWith("cdma")
						|| type.toLowerCase(Locale.ENGLISH).equals("umts")
						|| type.toLowerCase(Locale.ENGLISH).equals("1xrtt")
						|| type.toLowerCase(Locale.ENGLISH).equals("ehrpd")
						|| type.toLowerCase(Locale.ENGLISH).equals("hsupa")
						|| type.toLowerCase(Locale.ENGLISH).equals("hsdpa")
						|| type.toLowerCase(Locale.ENGLISH).equals("hspa")) {
					return "3g";
				} else if (type.toLowerCase(Locale.ENGLISH).equals("lte")
						|| type.toLowerCase(Locale.ENGLISH).equals("umb")
						|| type.toLowerCase(Locale.ENGLISH).equals("hspa+")) {
					return "4g";
				}
			}
		} else {
			return TYPE_NONE;
		}
		return "unknown";
	}

	public void onDestroy() {
		cmanager = null;
		if (this.receiver != null && this.registered) {
			try {
				mContext.unregisterReceiver(this.receiver);
				this.registered = false;
			} catch (Exception e) {

			}
		}
	}

}
