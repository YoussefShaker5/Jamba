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

public class list_adapter2 extends BaseAdapter {
    ArrayList<Triple<Bitmap, String,String>> folders;
    Context context;

    public list_adapter2(ArrayList<Triple<Bitmap, String,String>> folders, Context context) {
        this.folders = folders;
        this.context = context;
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
        View rowview = inflater.inflate(R.layout.songs, parent , false);
        ImageView image = (ImageView) rowview.findViewById(R.id.icon);
        TextView text = (TextView) rowview.findViewById(R.id.song_name);
        if (folders.get(position).first != null) {
            image.setImageBitmap(folders.get(position).first);
        }
        text.setText(folders.get(position).second);
        return rowview;
    }
}
