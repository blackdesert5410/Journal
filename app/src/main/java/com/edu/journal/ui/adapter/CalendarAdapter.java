package com.edu.journal.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.edu.journal.R;

import java.util.List;

public class CalendarAdapter extends BaseAdapter {

    private Context context;
    private List<String> daysOfMonth;

    public CalendarAdapter(Context context, List<String> daysOfMonth) {
        this.context = context;
        this.daysOfMonth = daysOfMonth;
    }

    @Override
    public int getCount() {
        return daysOfMonth.size();
    }

    @Override
    public Object getItem(int position) {
        return daysOfMonth.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        }

        TextView tvDay = convertView.findViewById(R.id.tv_day);
        tvDay.setText(daysOfMonth.get(position));

        return convertView;
    }
}
