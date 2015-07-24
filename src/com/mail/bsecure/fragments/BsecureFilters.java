package com.mail.bsecure.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.bsecure.mail.R;
import com.mail.bsecure.Bsecure;
import com.mail.bsecure.controls.ButtonsToggle;
import com.mail.bsecure.controls.ButtonsToggle.OnCheckedChangeListener;

public class BsecureFilters extends ParentFragment implements
		OnCheckedChangeListener {

	private View layout = null;

	private Bsecure bsecure = null;

	private EditText et_validuntil = null;

	private EditText et_pin = null;

	private ButtonsToggle bt_readonly = null, bt_replay = null,
			bt_autodelete = null;

	private Spinner timeSpinner = null;

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

		layout = inflater.inflate(R.layout.bsecurefilters, container, false);
		layout.setClickable(true);

		bt_readonly = (ButtonsToggle) layout.findViewById(R.id.bt_readonly);
		bt_readonly.setOnCheckedChangeListener(this);
		bt_readonly.setUnChecked();

		bt_replay = (ButtonsToggle) layout.findViewById(R.id.bt_replay);
		bt_replay.setOnCheckedChangeListener(this);

		bt_autodelete = (ButtonsToggle) layout.findViewById(R.id.bt_autodelete);
		bt_autodelete.setOnCheckedChangeListener(this);
		bt_autodelete.setUnChecked();

		et_pin = (EditText) layout.findViewById(R.id.et_pin);

		layout.findViewById(R.id.tv_filterdone).setOnClickListener(bsecure);

		String downloadOptions[] = new String[] { "Unlimited", "10 Sec",
				"20 Sec", "30 Sec", "40 Sec", "50 Sec", "60 Sec", "70 Sec",
				"80 Sec", "90 Sec", "100 Sec", "110 Sec", "120 Sec", "130 Sec",
				"140 Sec", "150 Sec", "160 Sec", "170 Sec", "180 Sec",
				"190 Sec", "200 Sec", "210 Sec", "220 Sec", "230 Sec",
				"240 Sec", "250 Sec", "260 Sec", "270 Sec", "280 Sec",
				"290 Sec", "300 Sec" };

		timeSpinner = (Spinner) layout.findViewById(R.id.sp_showtime);

		ArrayAdapter<String> downloadQualityAdapter = new ArrayAdapter<String>(
				getActivity(), R.layout.custom_spinner_dropdown_item,
				downloadOptions);

		downloadQualityAdapter
				.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		timeSpinner.setAdapter(downloadQualityAdapter);

		et_validuntil = (EditText) layout.findViewById(R.id.et_validuntil);

		layout.findViewById(R.id.et_validuntil_icon).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						showDate();
					}
				});

		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setHasOptionsMenu(true);

		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onDestroyView() {

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
	public void onFragmentChildClick(View view) {
		super.onFragmentChildClick(view);
	}

	@Override
	public String getFragmentName() {
		return "Protections";// getString(R.string.protections);
	}

	private void showDate() {

		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(
				((FragmentActivity) getActivity()).getSupportFragmentManager(),
				"datePicker");
	}

	public class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(getActivity(), R.style.DialogTheme,
					this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {

			try {

				String result = pad(day) + "-" + pad(month + 1) + "-"
						+ String.valueOf(year);

				String result1 = String.valueOf(year) + "-" + pad(month + 1)
						+ "-" + pad(day);

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date1 = sdf.parse(result1);

				Calendar c1 = Calendar.getInstance();

				String formattedDate = sdf.format(c1.getTime());
				Date date2 = sdf.parse(formattedDate);

				if (date1.after(date2)) {
				}

				if (date1.before(date2)) {

					bsecure.showToast(R.string.psvd);
					et_validuntil.setText("");
					return;
				}

				if (date1.equals(date2)) {
				}

				et_validuntil.setText(result);

			} catch (Exception e) {

			}

		}

	}

	private static String pad(int value) {
		if (value >= 10)
			return String.valueOf(value);
		else
			return "0" + String.valueOf(value);
	}

	@Override
	public void onCheckedChanged(ButtonsToggle button, boolean isChecked) {
		// TODO Auto-generated method stub
	}

	public JSONObject getFilterVlaues() {

		JSONObject object = new JSONObject();

		try {

			object.put("readonly", bt_readonly.isChecked() + "");
			object.put("reply", bt_replay.isChecked() + "");
			object.put("autodelete", bt_autodelete.isChecked() + "");
			object.put("pin", et_pin.getText().toString().trim() + "");
			object.put("time", timeSpinner.getSelectedItem().toString() + "");

			String validity = et_validuntil.getText().toString();
			validity = validity.trim();
			if (validity.length() == 0)
				validity = "Unlimited";
			// validity = "01-01-1970";

			object.put("validity", validity);

			return object;

		} catch (Exception e) {
			return object;
		}

	}

	@Override
	public int getFragmentActionBarColor() {
		return R.color.green;
	}

}
