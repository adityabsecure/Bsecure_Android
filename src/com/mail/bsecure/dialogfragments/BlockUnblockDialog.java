package com.mail.bsecure.dialogfragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.bsecure.mail.R;
import com.mail.bsecure.Bsecure;

public class BlockUnblockDialog extends DialogFragment {

	private Bsecure bsecure = null;

	Intent intent = new Intent(Intent.ACTION_SEND);

	public static BlockUnblockDialog newInstance() {
		return new BlockUnblockDialog();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		bsecure = (Bsecure) activity;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle mArgs = getArguments();

		View view = inflater.inflate(R.layout.blockunblock_popup, container,
				false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		getDialog().getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));

		((TextView) view.findViewById(R.id.txt)).setText(mArgs
				.getInt("titleId"));

		view.findViewById(R.id.bn_bu_cancel).setOnClickListener(bsecure);
		view.findViewById(R.id.bn_bu_submit).setOnClickListener(bsecure);

		view.findViewById(R.id.bn_bu_submit).setTag(mArgs.getInt("id"));

		return view;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

	}

}
