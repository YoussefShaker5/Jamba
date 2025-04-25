package com.YouTech.jamba.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.YouTech.jamba.R;
import com.YouTech.jamba.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class list_adapter extends BaseAdapter {
    Map<String, ArrayList<Triple<Bitmap, String,String>>> folders;
    Object[] s;
    String type;
    Context context;

    public list_adapter(Map<String, ArrayList<Triple<Bitmap, String,String>>> folders, String type, Context context) {
        this.folders = folders;
        this.type = type;
        this.context = context;

        s = folders.keySet().toArray();
        Arrays.sort(s);
    }

    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public Object getItem(int position) {
        return folders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.songs, parent , false);
        ImageView image = (ImageView) rowView.findViewById(R.id.icon);
        TextView text = (TextView) rowView.findViewById(R.id.song_name);
        if (folders.get(s[position]).get(0).first != null) {
            image.setImageBitmap(folders.get(s[position]).get(0).first);
        }
        text.setText((CharSequence) s[position]);
        return rowView;
    }
}
