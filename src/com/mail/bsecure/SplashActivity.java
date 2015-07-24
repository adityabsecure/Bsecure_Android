package com.mail.bsecure;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.AppSettings;
import com.mail.bsecure.tasks.HTTPTask;

public class SplashActivity extends Activity implements IItemHandler {

	private Dialog dialog = null;
	
	private boolean isDestroyed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		fetchMSISDN();	
	}
	
	/**
	 * fetchUG - This method is called when activity is created. It will
	 * communicate with server to get user agent of device.
	 */
	private void fetchMSISDN() {

		String url = AppSettings.getInstance(this).getPropertyValue("ispin");
		
		HTTPTask task = new HTTPTask(this, this);
		task.disableProgress();
		task.userRequest("", 1, url);

	}

	private void showNetworkDialog() {
		findViewById(R.id.splash_pbar).setVisibility(View.GONE);
		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		dialog.setCancelable(false);
		View view = View.inflate(this, R.layout.nonet, null);
		view.findViewById(R.id.nonet_ok).setOnClickListener(onClick);
		dialog.setContentView(view);
		dialog.show();
	}

	private void closeDialog() {
		if (dialog != null)
			dialog.dismiss();
		dialog = null;
	}

	OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View view) {

			switch (view.getId()) {
			case R.id.nonet_ok:
				closeDialog();
				SplashActivity.this.finish();
				break;

			default:
				break;
			}

		}
	};
	
	private void showToast(String message) {
		findViewById(R.id.splash_pbar).setVisibility(View.GONE);
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {

		isDestroyed = true;		
		super.onDestroy();
	}

	@Override
	public void onFinish(Object results, int requestId) {

		if(isDestroyed)
			return;
		
		switch (requestId) {
		case 1:
						
			String authpin = getFromStore("authpin");
			if(authpin.length() == 0) {
				launchActivty(Login.class);	
			} else {
				launchActivty(LoginInWithPin.class);
			}
			
			break;		

		default:
			break;
		}

	}

	@Override
	public void onError(String errorCode, int requestType) {

		if (errorCode.equalsIgnoreCase(getString(R.string.nipcyns))) {
			showNetworkDialog();
			return;
		}

		showToast(errorCode);
		
		launchActivty(Login.class);

	}


	private void launchActivty(Class<?> cls) {
					
		Intent mainIntent = new Intent(SplashActivity.this, cls);
		SplashActivity.this.startActivity(mainIntent);
		
		SplashActivity.this.finish();
	}
	
	private String getFromStore(String key) {
		SharedPreferences pref = this.getSharedPreferences("bsecure",
				MODE_PRIVATE);
		String res = pref.getString(key, "");
		return res;
	}
		
}