package com.mail.bsecure;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.tasks.HTTPTask;
import com.mail.bsecure.utils.Utils;

public class MailPin extends FragmentActivity implements IItemHandler {

	private EditText ed_pwd1 = null;
	private EditText ed_pwd2 = null;
	private EditText ed_pwd3 = null;
	private EditText ed_pwd4 = null;

	private Intent intent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pin_layout);

		intent = getIntent();

		ed_pwd1 = (EditText) findViewById(R.id.ed_pwd1);
		ed_pwd2 = (EditText) findViewById(R.id.ed_pwd2);
		ed_pwd3 = (EditText) findViewById(R.id.ed_pwd3);
		ed_pwd4 = (EditText) findViewById(R.id.ed_pwd4);

		findViewById(R.id.tv_forgotpin).setOnClickListener(onClick);
		findViewById(R.id.tv_forgotpin).setVisibility(View.GONE);

		ed_pwd1.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Integer textlength1 = ed_pwd1.getText().length();
				if (textlength1 >= 1) {
					ed_pwd2.requestFocus();

					if (ed_pwd2.getText().length() > 0
							&& ed_pwd3.getText().length() > 0
							&& ed_pwd4.getText().length() > 0) {
						makePinRequest();
					}

				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		ed_pwd2.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Integer textlength1 = ed_pwd2.getText().length();
				if (textlength1 >= 1) {
					ed_pwd3.requestFocus();

					if (ed_pwd1.getText().length() > 0
							&& ed_pwd3.getText().length() > 0
							&& ed_pwd4.getText().length() > 0) {
						makePinRequest();
					}

				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		ed_pwd3.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Integer textlength1 = ed_pwd3.getText().length();
				if (textlength1 >= 1) {
					ed_pwd4.requestFocus();

					if (ed_pwd1.getText().length() > 0
							&& ed_pwd2.getText().length() > 0
							&& ed_pwd4.getText().length() > 0) {
						makePinRequest();
					}

				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

		ed_pwd4.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				Integer textlength1 = ed_pwd4.getText().length();
				if (textlength1 >= 1) {
					makePinRequest();
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
		});

	}

	OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View view) {

			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(view.getWindowToken(), 0);

			switch (view.getId()) {

			case R.id.tv_forgotpin:
				launchActivity(PinRecovery.class);
				break;

			default:
				break;
			}

		}
	};

	private void makePinRequest() {

		String ed_pwd1_txt = ed_pwd1.getText().toString().trim();
		String ed_pwd2_txt = ed_pwd2.getText().toString().trim();
		String ed_pwd3_txt = ed_pwd3.getText().toString().trim();
		String ed_pwd4_txt = ed_pwd4.getText().toString().trim();

		if (ed_pwd1_txt.length() == 0) {
			ed_pwd1.requestFocus();
			showToast(R.string.pepin);
			return;
		}

		if (ed_pwd2_txt.length() == 0) {
			ed_pwd2.requestFocus();
			showToast(R.string.pepin);
			return;
		}

		if (ed_pwd3_txt.length() == 0) {
			ed_pwd3.requestFocus();
			showToast(R.string.pepin);
			return;
		}

		if (ed_pwd4_txt.length() == 0) {
			ed_pwd4.requestFocus();
			showToast(R.string.pepin);
			return;
		}

		String pin = ed_pwd1_txt + ed_pwd2_txt + ed_pwd3_txt + ed_pwd4_txt;

		String url = intent.getStringExtra("url") + "";
		url = url.replace("(PIN)", pin);

		HTTPTask get = new HTTPTask(this, this);
		get.userRequest(getString(R.string.pwait), 1, url);

	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	private void launchActivity(Class<?> cls) {
		Intent mainIntent = new Intent(this, cls);
		startActivity(mainIntent);
	}

	@Override
	public void onFinish(Object results, int requestType) {
		Utils.dismissProgress();

		try {

			switch (requestType) {

			case 1:
				parseMailPinResponse((String) results, requestType);
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

			setResult(RESULT_CANCELED);
			MailPin.this.finish();

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

	private void parseMailPinResponse(String response, int requestId)
			throws Exception {

		if (response != null && response.length() > 0) {

			JSONObject jsonObject = new JSONObject(response);

			if (jsonObject.optString("status").equalsIgnoreCase("0")) {

				setResult(RESULT_OK);
				MailPin.this.finish();

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
