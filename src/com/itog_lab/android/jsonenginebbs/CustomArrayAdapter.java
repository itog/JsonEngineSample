package com.itog_lab.android.jsonenginebbs;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CustomArrayAdapter extends ArrayAdapter<BbsItem> {
	private BbsItem[] items;

	public CustomArrayAdapter(Context context, BbsItem[] items) {
		super(context, android.R.layout.simple_list_item_1, items);
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		view.setTag(items[position].getDocId());
		return view;
	}

	
}
