package com.mail.bsecure;

import java.util.Random;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.AppSettings;
import com.mail.bsecure.tasks.HTTPTask;
import com.mail.bsecure.utils.Utils;

public class PinRecovery extends FragmentActivity implements IItemHandler {

	private String pin = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pinrecovery);

		findViewById(R.id.tv_recovery).setOnClickListener(onClick);

	}

	OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View view) {

			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(view.getWindowToken(), 0);

			switch (view.getId()) {

			case R.id.tv_recovery:
				makeForgetPwdRequest();
				break;

			default:
				break;
			}

		}
	};

	private void makeForgetPwdRequest() {

		String email = ((EditText) findViewById(R.id.account_txt)).getText()
				.toString().trim();

		if (email.length() == 0) {
			showToast(R.string.peei);
			return;
		}

		if (!emailValidation(email)) {
			showToast(R.string.peavei);
			return;
		}

		Random randomGenerator = new Random();

		for (int i = 1; i <= 4; ++i) {
			int randomInt = randomGenerator.nextInt(9);
			pin = pin + randomInt;
		}

		String url = AppSettings.getInstance(this).getPropertyValue(
				"pinRecover");
		url = url.replace("(EMAIL)", email);
		url = url.replace("(PIN)", pin);

		HTTPTask get = new HTTPTask(this, this);
		get.userRequest(getString(R.string.pwait), 1, url);

	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	public void addToStore(String key, String value) {
		SharedPreferences pref = this.getSharedPreferences("bsecure",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	@Override
	public void onFinish(Object results, int requestType) {
		Utils.dismissProgress();

		try {

			switch (requestType) {

			case 1:
				parsePinRecoveryResponse((String) results, requestType);
				break;

			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onError(String errorData, int requestType) {
		showToast(errorData);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 4) {
		}

		return super.onKeyDown(keyCode, event);
	}

	public void showToast(int text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	private void parsePinRecoveryResponse(String response, int requestId)
			throws Exception {

		if (response != null && response.length() > 0) {

			JSONObject jsonObject = new JSONObject(response);

			if (jsonObject.optString("status").equalsIgnoreCase("0")) {

				addToStore("authpin", pin + "");
				showToast(jsonObject.optString("statusdescription"));
				PinRecovery.this.finish();

				return;
			}

			showToast(jsonObject.optString("statusdescription"));

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public boolean emailValidation(String email) {

		if (email == null || email.length() == 0 || email.indexOf("@") == -1
				|| email.indexOf(" ") != -1) {
			return false;
		}
		int emailLenght = email.length();
		int atPosition = email.indexOf("@");

		String beforeAt = email.substring(0, atPosition);
		String afterAt = email.substring(atPosition + 1, emailLenght);

		if (beforeAt.length() == 0 || afterAt.length() == 0) {
			return false;
		}
		if (email.charAt(atPosition - 1) == '.') {
			return false;
		}
		if (email.charAt(atPosition + 1) == '.') {
			return false;
		}
		if (afterAt.indexOf(".") == -1) {
			return false;
		}
		char dotCh = 0;
		for (int i = 0; i < afterAt.length(); i++) {
			char ch = afterAt.charAt(i);
			if ((ch == 0x2e) && (ch == dotCh)) {
				return false;
			}
			dotCh = ch;
		}
		if (afterAt.indexOf("@") != -1) {
			return false;
		}
		int ind = 0;
		do {
			int newInd = afterAt.indexOf(".", ind + 1);

			if (newInd == ind || newInd == -1) {
				String prefix = afterAt.substring(ind + 1);
				if (prefix.length() > 1 && prefix.length() < 6) {
					break;
				} else {
					return false;
				}
			} else {
				ind = newInd;
			}
		} while (true);
		dotCh = 0;
		for (int i = 0; i < beforeAt.length(); i++) {
			char ch = beforeAt.charAt(i);
			if (!((ch >= 0x30 && ch <= 0x39) || (ch >= 0x41 && ch <= 0x5a)
					|| (ch >= 0x61 && ch <= 0x7a) || (ch == 0x2e)
					|| (ch == 0x2d) || (ch == 0x5f))) {
				return false;
			}
			if ((ch == 0x2e) && (ch == dotCh)) {
				return false;
			}
			dotCh = ch;
		}
		return true;
	}

}
