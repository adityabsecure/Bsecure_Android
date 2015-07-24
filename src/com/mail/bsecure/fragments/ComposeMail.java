package com.mail.bsecure.fragments;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bsecure.mail.R;
import com.mail.bsecure.Bsecure;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.AppSettings;
import com.mail.bsecure.common.Item;
import com.mail.bsecure.controls.ProgressControl;
import com.mail.bsecure.tasks.HTTPostJson;
import com.mail.bsecure.utils.Utils;

public class ComposeMail extends ParentFragment implements IItemHandler {

	private View layout = null;

	private Bsecure bsecure = null;

	private String readonly = "no";
	private String reply = "0"; /* 0 = yes, 1 = no */
	private String autodelete = "No";
	private String pin = ""; /* 0 = no, 1 = yes */
	private String time = "Unlimited";
	// private String validity = "01-01-1970";
	private String validity = "Unlimited";

	private Item item = null;

	private EditText ed_toText = null, ed_ccText = null, ed_bccText = null,
			ed_subText = null, ed_comText = null;

	private int attachmentCounter = 0;

	private LinearLayout attachmentLayout = null;

	private String rid = "";

	private String draftid = "";

	private int counter = 20;

	private boolean exitFlag = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.bsecure = (Bsecure) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle mArgs = getArguments();

		if (mArgs != null)
			item = (Item) mArgs.getSerializable("item");

		layout = inflater.inflate(R.layout.composemail, container, false);

		layout.findViewById(R.id.tv_ccText).setOnClickListener(bsecure);
		layout.findViewById(R.id.tv_bccText).setOnClickListener(bsecure);

		layout.findViewById(R.id.tv_addattachment).setOnClickListener(bsecure);

		ed_toText = (EditText) layout.findViewById(R.id.ed_toText);
		ed_ccText = (EditText) layout.findViewById(R.id.ed_ccText);
		ed_bccText = (EditText) layout.findViewById(R.id.ed_bccText);

		ed_subText = (EditText) layout.findViewById(R.id.ed_subText);

		ed_comText = (EditText) layout.findViewById(R.id.ed_comText);

		if (item != null) {
			ed_toText.setText(item.getAttribute("from"));

			rid = item.getAttribute("replyid");
			if (rid.length() > 0) {

				ed_toText.setText(item.getAttribute("to"));

				if (item.getAttribute("to").length() == 0)
					ed_toText.setText(item.getAttribute("from"));
			}

			if (item.getAttribute("drafts_values").equalsIgnoreCase("yes")) {
				ed_toText.setText(item.getAttribute("tomsg"));
				ed_subText.setText(item.getAttribute("subject"));
				ed_comText.setText(item.getAttribute("message"));

				if (item.getAttribute("cc").length() > 0) {
					layout.findViewById(R.id.ll_cc).setVisibility(View.VISIBLE);
					ed_ccText.setText(item.getAttribute("cc"));
				}

				if (item.getAttribute("bcc").length() > 0) {
					layout.findViewById(R.id.ll_bcc)
							.setVisibility(View.VISIBLE);
					ed_bccText.setText(item.getAttribute("bcc"));
				}

				readonly = item.getAttribute("readonly");
				time = item.getAttribute("showtime");
				autodelete = item.getAttribute("autodelete");
				pin = item.getAttribute("pin");
				validity = item.getAttribute("validuntil");
				reply = item.getAttribute("reply");

				draftid = item.getAttribute("draftid");
			}

		}

		attachmentLayout = (LinearLayout) layout
				.findViewById(R.id.ll_attachments_layout);

		saveDraft();

		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ed_comText.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				int textlength1 = ed_comText.getText().length();

				if (textlength1 >= counter) {
					saveDraft();
					counter = counter * 2;
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

		setHasOptionsMenu(true);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (!bsecure.isDrawerOpen())
			inflater.inflate(R.menu.compose_menu, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.cm_filter:
			bsecure.showFilterFragment();
			break;

		case R.id.cm_send:
			sendMail();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {

		if (!exitFlag) {
			saveDraft();
		}

		layout = null;

		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onFinish(Object results, int requestType) {

		try {

			Utils.dismissProgress();

			switch (requestType) {
			case 1:

				if (results != null) {

					exitFlag = true;

					JSONObject object = new JSONObject(results.toString());

					bsecure.showToast(object.optString("statusdescription"));
					bsecure.onKeyDown(4, null);
					object = null;
				}

				break;

			case 2:

				if (results != null) {

					JSONObject object = new JSONObject(results.toString());

					draftid = object.optString("draftid");

					object = null;
				}

				break;

			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(String errorCode, int requestType) {

		switch (requestType) {
		case 1:
			bsecure.showToast(errorCode);
			Utils.dismissProgress();
			break;

		case 2:
			break;

		default:
			break;
		}
	}

	@Override
	public void onFragmentChildClick(View view) {
		super.onFragmentChildClick(view);
	}

	@Override
	public String getFragmentName() {
		return "Compose"; // getString(R.string.composemail);
	}

	public void showFilterValues(JSONObject object) {

		try {

			StringBuilder filterValues = new StringBuilder();
			filterValues.append("Read Once: ");

			if (object.getBoolean("readonly")) {
				filterValues.append("Yes");
				readonly = "Yes";
			} else {
				filterValues.append("No");
			}

			filterValues.append(" | ");

			filterValues.append("Reply: ");

			if (object.getBoolean("reply")) {
				filterValues.append("Yes");
				reply = "0";
			} else {
				filterValues.append("No");
				reply = "1";
			}

			filterValues.append(" | ");

			filterValues.append("Auto Delete: ");

			if (object.getBoolean("autodelete")) {
				filterValues.append("Yes");
				autodelete = "Yes";
			} else {
				filterValues.append("No");
			}

			filterValues.append(" | ");

			if (object.optString("pin").length() > 0) {
				filterValues.append("Pin: Yes");
				pin = object.optString("pin");
				filterValues.append(" | ");
			}

			filterValues.append("Time: " + object.optString("time"));
			time = object.optString("time");

			filterValues.append(" | ");

			filterValues.append("Valid Until: " + object.optString("validity"));
			validity = object.optString("validity");

			((TextView) layout.findViewById(R.id.tv_filter))
					.setVisibility(View.VISIBLE);
			((TextView) layout.findViewById(R.id.tv_filter))
					.setText(filterValues.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onCCClick() {
		layout.findViewById(R.id.ll_cc).setVisibility(View.VISIBLE);
	}

	public void onBCCClick() {
		layout.findViewById(R.id.ll_bcc).setVisibility(View.VISIBLE);
	}

	private void sendMail() {

		try {

			String ed_toText_val = ed_toText.getText().toString();
			if (ed_toText_val.length() == 0) {
				bsecure.showToast(R.string.peei);
				return;
			}

			if (!emailValidation(ed_toText_val)) {
				bsecure.showToast(R.string.peavei);
				return;
			}

			String ed_subText_val = ed_subText.getText().toString();
			if (ed_subText_val.length() == 0) {
				bsecure.showToast(R.string.pes);
				return;
			}

			String ed_ccText_Vlaue = ed_ccText.getText().toString().trim();
			if (ed_ccText_Vlaue.length() > 0
					&& !emailValidation(ed_ccText_Vlaue)) {
				bsecure.showToast(R.string.peavccEi);
				return;
			}

			String ed_bccText_Vlaue = ed_bccText.getText().toString().trim();
			if (ed_bccText_Vlaue.length() > 0
					&& !emailValidation(ed_bccText_Vlaue)) {
				bsecure.showToast(R.string.peavbccEi);
				return;
			}

			Utils.showProgress(getString(R.string.pwait), bsecure);

			JSONObject object = new JSONObject();

			if (rid.length() > 0)
				object.put("rid", rid + "");

			if (this.item != null) {
				object.put("composeid", this.item.getAttribValue("composeid"));
				object.put("memberid", this.item.getAttribValue("memberid"));
			}

			object.put("email", bsecure.getFromStore("email"));

			object.put("draftid", draftid);

			object.put("to", ed_toText_val);

			object.put("cc", ed_ccText_Vlaue);
			object.put("bcc", ed_bccText_Vlaue);

			object.put("subject", ed_subText_val);
			object.put("message", ed_comText.getText().toString().trim());

			object.put("readonly", readonly);
			object.put("showtime", time);
			object.put("autodelete", autodelete);
			object.put("pin", pin);

			if (validity.equalsIgnoreCase("Unlimited")) {
				validity = "01-01-1970";
			}

			object.put("validuntil", validity);
			object.put("reply", reply);

			String url = AppSettings.getInstance(bsecure).getPropertyValue(
					"compose_mail");

			HTTPostJson post = new HTTPostJson(this, bsecure, null,
					object.toString(), 1);
			post.enableMutipart(false);
			post.setContentType("application/json");
			post.execute(url, "");

		} catch (Exception e) {
			e.printStackTrace();
		}

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

	public void addAttachmentView(Item fullItem) {

		attachmentCounter++;

		View view = View.inflate(bsecure, R.layout.template_attachments_layout,
				null);
		view.setId(attachmentCounter);

		view.findViewById(R.id.tv_attachmentAction).setOnClickListener(bsecure);
		view.findViewById(R.id.tv_attachmentAction).setTag(attachmentCounter);

		((ProgressControl) view.findViewById(R.id.download_progress))
				.setText(fullItem.getAttribute("displayname"));

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 10);

		attachmentLayout.addView(view, params);

		bsecure.logE("----------------", fullItem + "");

	}

	public void removeAttachment(int id) {
		attachmentLayout.removeView(attachmentLayout.findViewById(id));
		attachmentLayout.invalidate();
		attachmentLayout.requestLayout();
	}

	@Override
	public int getFragmentActionBarColor() {
		return R.color.green;
	}

	private void saveDraft() {

		try {

			JSONObject object = new JSONObject();

			if (rid.length() > 0)
				object.put("rid", rid + "");

			if (this.item != null) {
				object.put("composeid", this.item.getAttribValue("composeid"));
				object.put("memberid", this.item.getAttribValue("memberid"));
			}

			object.put("email", bsecure.getFromStore("email"));
			object.put("draftid", draftid);
			object.put("to", ed_toText.getText().toString());

			object.put("cc", ed_ccText.getText().toString().trim());
			object.put("bcc", ed_bccText.getText().toString().trim());

			object.put("subject", ed_subText.getText().toString());
			object.put("message", ed_comText.getText().toString().trim());

			object.put("readonly", readonly);
			object.put("showtime", time);
			object.put("autodelete", autodelete);
			object.put("pin", pin);

			if (validity.equalsIgnoreCase("Unlimited")) {
				validity = "01-01-1970";
			}

			object.put("validuntil", validity);
			object.put("reply", reply);

			String url = AppSettings.getInstance(bsecure).getPropertyValue(
					"savedrafts");

			HTTPostJson post = new HTTPostJson(this, bsecure, null,
					object.toString(), 2);
			post.enableMutipart(false);
			post.setContentType("application/json");
			post.execute(url, "");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OnFocusChangeListener onFocusChange = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View view, boolean hasFocus) {

			switch (view.getId()) {

			case R.id.ed_toText:

				if (!hasFocus)
					saveDraft();

				break;

			case R.id.ed_ccText:

				if (!hasFocus)
					saveDraft();

				break;

			case R.id.ed_bccText:

				if (!hasFocus)
					saveDraft();

				break;

			case R.id.ed_subText:

				if (!hasFocus)
					saveDraft();

				break;

			case R.id.ed_comText:

				break;

			default:
				break;
			}

		}
	};

}
