package com.mail.bsecure.adapter;

import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bsecure.mail.R;
import com.mail.bsecure.common.Item;

public class MenuAdapter extends BaseAdapter {
	private LayoutInflater inflater = null;
	Vector<Item> vector = new Vector<Item>();

	public MenuAdapter(Context context) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return vector.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Item item = vector.get(position);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.menu_layout, null);
		}

		TextView textView = (TextView) convertView.findViewById(R.id.tv_count);
		textView.setText(item.getAttribute("COUNT"));

		textView = (TextView) convertView.findViewById(R.id.label);
		textView.setText(item.getAttribute("TITLE"));

		int id = -1;

		id = Integer.parseInt(item.getAttribute("IMAGE"));

		ImageView imageView = (ImageView) convertView.findViewById(R.id.logo);
		imageView.setImageResource(id);

		convertView.setTag(item);
		return convertView;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void changeData(Vector<Item> vector) {
		this.vector = vector;
		notifyDataSetChanged();
	}

	public void setItems(Vector<Item> vector) {
		this.vector = vector;
	}

	public Vector<Item> getItems() {
		return this.vector;
	}

	public void clear() {
		vector.clear();
	}

}
