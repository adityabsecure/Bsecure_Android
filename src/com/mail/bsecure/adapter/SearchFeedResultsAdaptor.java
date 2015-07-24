package com.mail.bsecure.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

public class SearchFeedResultsAdaptor extends SimpleCursorAdapter {

	public SearchFeedResultsAdaptor(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);

	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		// super.bindView(view, arg1, cursor);

		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setText(cursor.getString(1));

	}

}
