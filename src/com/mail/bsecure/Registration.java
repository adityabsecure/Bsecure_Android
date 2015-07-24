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
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.AppSettings;
import com.mail.bsecure.tasks.HTTPTask;
import com.mail.bsecure.utils.Utils;

public class Registration extends FragmentActivity implements IItemHandler {

	private EditText et_username = null;
	private EditText et_ceid = null;

	private EditText et_password = null;
	private EditText et_passwordAgain = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.registration);

		et_username = (EditText) findViewById(R.id.et_username);

		et_ceid = (EditText) findViewById(R.id.et_ceid);

		et_password = (EditText) findViewById(R.id.et_password);

		et_passwordAgain = (EditText) findViewById(R.id.et_passwordAgain);

		findViewById(R.id.tv_RegisterSubmit).setOnClickListener(onClick);

		et_username.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String email = et_username.getText().toString();
					email = email.trim();

					if (email.length() == 0) {
						showToast(R.string.peei);
						et_username.setSelection(email.length());
						return;
					}

					if (!emailValidation(email)) {
						showToast(R.string.peavei);
						et_username.setSelection(email.length());
						return;
					}

				}
			}
		});

		et_ceid.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String cemail = et_ceid.getText().toString();
					cemail = cemail.trim();

					if (cemail.length() == 0) {
						showToast(R.string.peei);
						et_ceid.setSelection(cemail.length());
						return;
					}

					if (!emailValidation(cemail)) {
						showToast(R.string.peavei);
						et_ceid.setSelection(cemail.length());
						return;
					}

					String email = et_username.getText().toString();
					email = email.trim();
					if (!email.equals(cemail)) {
						showToast(R.string.eiaceidrnc);
						return;
					}

				}
			}
		});

		et_ceid.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				int textlength1 = et_username.getText().length();
				int textlength2 = et_ceid.getText().length();

				String username = et_username.getText().toString();
				username = username.trim();

				String ceid = et_ceid.getText().toString();
				ceid = ceid.trim();

				if (textlength1 == textlength2) {
					if (!username.equals(ceid)) {
						showToast(R.string.eiaceidrnc);
						return;
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

		et_password.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String password = et_password.getText().toString();
					password = password.trim();

					if (password.length() == 0 || password.length() < 8) {
						showToast(R.string.psmbc);
						et_password.setSelection(password.length());
						return;
					}
				}
			}
		});

		et_passwordAgain.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {

					String cpassword = et_passwordAgain.getText().toString();
					cpassword = cpassword.trim();

					if (cpassword.length() == 0 || cpassword.length() < 8) {
						showToast(R.string.psmbc);
						et_passwordAgain.setSelection(cpassword.length());
						return;
					}

					String password = et_password.getText().toString();
					password = password.trim();

					if (!password.equals(cpassword)) {
						showToast(R.string.cpmm);
						return;
					}

				}
			}
		});

		et_passwordAgain.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				int textlength1 = et_password.getText().length();
				int textlength2 = et_passwordAgain.getText().length();

				String password = et_password.getText().toString();
				password = password.trim();

				String cpassword = et_passwordAgain.getText().toString();
				cpassword = cpassword.trim();

				if (textlength1 == textlength2) {
					if (!password.equals(cpassword)) {
						showToast(R.string.cpmm);
						return;
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

	}

	OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View view) {

			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(view.getWindowToken(), 0);

			switch (view.getId()) {

			case R.id.tv_RegisterSubmit:
				registration();
				break;

			default:
				break;
			}

		}
	};

	private void registration() {

		try {

			String et_fname = ((EditText) findViewById(R.id.et_fname))
					.getText().toString().trim();

			if (et_fname.length() == 0) {
				showToast(R.string.peyfn);
				((EditText) findViewById(R.id.et_fname)).requestFocus();
				return;
			}

			String et_lname = ((EditText) findViewById(R.id.et_lname))
					.getText().toString().trim();

			if (et_lname.length() == 0) {
				showToast(R.string.peyln);
				((EditText) findViewById(R.id.et_lname)).requestFocus();
				return;
			}

			String email = et_username.getText().toString().trim();

			if (email.length() == 0) {
				showToast(R.string.peei);
				((EditText) findViewById(R.id.et_username)).requestFocus();
				return;
			}

			if (!emailValidation(email)) {
				showToast(R.string.peavei);
				((EditText) findViewById(R.id.et_username)).requestFocus();
				return;
			}

			String et_ceid_txt = et_ceid.getText().toString().trim();

			if (et_ceid_txt.length() == 0) {
				showToast(R.string.peei);
				((EditText) findViewById(R.id.et_ceid)).requestFocus();
				return;
			}

			if (!emailValidation(et_ceid_txt)) {
				showToast(R.string.peavei);
				((EditText) findViewById(R.id.et_ceid)).requestFocus();
				return;
			}

			if (!email.equals(et_ceid_txt)) {
				showToast(R.string.eiaceidrnc);
				((EditText) findViewById(R.id.et_ceid)).requestFocus();
				return;
			}

			String password = ((EditText) findViewById(R.id.et_password))
					.getText().toString().trim();

			if (password.length() == 0) {
				showToast(R.string.pePswd);
				((EditText) findViewById(R.id.et_password)).requestFocus();
				return;
			}

			if (password.length() < 8) {
				showToast(R.string.psmbc);
				((EditText) findViewById(R.id.et_password)).requestFocus();
				return;
			}

			String et_passwordAgain = ((EditText) findViewById(R.id.et_passwordAgain))
					.getText().toString().trim();
			if (et_passwordAgain.length() == 0) {
				showToast(R.string.pecPswd);
				((EditText) findViewById(R.id.et_passwordAgain)).requestFocus();
				return;
			}

			if (!password.equals(et_passwordAgain)) {
				showToast(R.string.psmbc);
				((EditText) findViewById(R.id.et_passwordAgain)).requestFocus();
				return;
			}

			String et_city = ((EditText) findViewById(R.id.et_city)).getText()
					.toString().trim();
			if (et_city.length() == 0) {
				showToast(R.string.pecn);
				((EditText) findViewById(R.id.et_city)).requestFocus();
				return;
			}

			String url = AppSettings.getInstance(this).getPropertyValue(
					"registration");
			url = url.replace("(FNAME)", et_fname);
			url = url.replace("(LNAME)", et_lname);
			url = url.replace("(EMAIL)", email);
			url = url.replace("(PWD)", password);
			url = url.replace("(CITY)", et_city);

			HTTPTask get = new HTTPTask(this, this);
			get.userRequest(getString(R.string.pwait), 1, url);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onFinish(Object results, int requestType) {
		Utils.dismissProgress();

		try {

			switch (requestType) {
			case 1:
				parseRegistrationResponse((String) results, requestType);
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
		Utils.dismissProgress();
		showToast(errorData);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == 4) {
		}

		return super.onKeyDown(keyCode, event);
	}

	public void showToast(int text) {
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	private void parseRegistrationResponse(String response, int requestId)
			throws Exception {

		if (response != null && response.length() > 0) {
			response = response.trim();
			JSONObject jsonObject = new JSONObject(response);

			if (jsonObject.has("status")
					&& jsonObject.optString("status").equalsIgnoreCase("0")) {
				showToast(jsonObject.optString("statusdescription"));

				Registration.this.finish();
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
