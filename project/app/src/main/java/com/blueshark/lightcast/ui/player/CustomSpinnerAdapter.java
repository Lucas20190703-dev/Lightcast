package com.blueshark.lightcast.ui.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.blueshark.lightcast.R;

public class CustomSpinnerAdapter extends BaseAdapter {
    Context context;
    String[] fruit;
    LayoutInflater inflter;

    public CustomSpinnerAdapter(Context applicationContext, String[] fruit) {
        this.context = applicationContext;
        this.fruit = fruit;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return fruit.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.cell_play_speed_spinner, null);
        TextView names = (TextView) view.findViewById(R.id.text1);
        names.setText(fruit[i]);
        return view;
    }
}
