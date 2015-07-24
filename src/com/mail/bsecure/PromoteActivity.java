package com.mail.bsecure;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

public class PromoteActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		if (intent == null) {
			return;
		}

		Uri uri = intent.getData();
		if (uri != null) {
			String activity = getFromStore("inactivity");
			/*if (activity.equalsIgnoreCase("yes")) {

				addToStore("promoteUri", uri.toString());
				Intent promoteIntent = new Intent(this, Login.class);
				promoteIntent.setData(uri);
				promoteIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				promoteIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				promoteIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

				startActivity(promoteIntent);
				PromoteActivity.this.finish();

			} else if (activity.equalsIgnoreCase("no")
					|| activity.equalsIgnoreCase("")) {

				Intent promoteIntent = new Intent(this, SplashActivity.class);
				promoteIntent.setData(uri);
				startActivity(promoteIntent);
				PromoteActivity.this.finish();

			}*/
			
			Intent promoteIntent = new Intent(this, SplashActivity.class);
			promoteIntent.setData(uri);
			startActivity(promoteIntent);
			PromoteActivity.this.finish();
			
		}
	}

	public String getFromStore(String key) {
		SharedPreferences pref = this.getSharedPreferences("musicbox",
				MODE_PRIVATE);
		String res = pref.getString(key, "");
		return res;
	}

	public void addToStore(String key, String value) {
		SharedPreferences pref = this.getSharedPreferences("musicbox",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

}
