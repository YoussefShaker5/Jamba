package com.YouTech.jamba.adapters;

//import static com.YouTech.jamba.MusicService.params;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.YouTech.jamba.MainActivity;
import com.YouTech.jamba.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class ArtistRecycleAdapter extends RecyclerView.Adapter {
    Map<String, ArrayList<String>> folders;
    public static ArrayList<String> a;
    private static ArtistRecycleAdapter c;
    Object[] s;
    String type;
    Context context;

    public ArtistRecycleAdapter(Map<String, ArrayList<String>> folders, String type, Context context) {
        this.folders = folders;
        this.type = type;
        this.context = context;
        c = this;
        s = folders.keySet().toArray();
        Arrays.sort(s);
    }
    public static void back() {
        a = null;
        c.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        if(a==null) {
            final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            final String[] cursor_cols = {MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID};
            Cursor cursor = context.getContentResolver().query(uri,
                    cursor_cols, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{folders.get(s[position]).get(0)}, null);
            if (cursor.moveToFirst()) {
                String title = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                Long albumId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                //if(params.get(folders.get(s[position]).get(0)).IMAGE == null) {
                    Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(viewHolder.image, new Callback() {
                        @Override
                        public void onSuccess() {
                            Drawable drawable = viewHolder.image.getDrawable();

// Check if the Drawable object is a BitmapDrawable
                            if (drawable instanceof BitmapDrawable) {
                                // Cast the Drawable object to a Bitmap object
                                //params.get(folders.get(s[holder.getAdapterPosition()]).get(0)).IMAGE = ((BitmapDrawable) drawable).getBitmap();
                            } else {
                                // Get a Bitmap object from the Drawable object
                                //Bitmap bitmap = drawable.getBitmap();
                            }
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                /*}else {
                    //Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(viewHolder.image);
                    viewHolder.image.setImageBitmap(params.get(folders.get(s[holder.getAdapterPosition()]).get(0)).IMAGE);
                }*/
                viewHolder.text.setText(title);
            }
            viewHolder.text.setText((CharSequence) s[position]);
        }else{
            final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            final String[] cursor_cols = {MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID};
            Cursor cursor = context.getContentResolver().query(uri,
                    cursor_cols, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{a.get(position)}, null);
            if (cursor.moveToFirst()) {
                String title = cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                Long albumId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                //if(params.get(a.get(holder.getAdapterPosition())).IMAGE == null) {
                    Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(viewHolder.image, new Callback() {
                        @Override
                        public void onSuccess() {
                            Drawable drawable = viewHolder.image.getDrawable();

// Check if the Drawable object is a BitmapDrawable
                            if (drawable instanceof BitmapDrawable) {
                                // Cast the Drawable object to a Bitmap object
                                //params.get(a.get(holder.getAdapterPosition())).IMAGE = ((BitmapDrawable) drawable).getBitmap();
                            } else {
                                // Get a Bitmap object from the Drawable object
                                //Bitmap bitmap = drawable.getBitmap();
                            }
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                /*}else {
                    //Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(viewHolder.image);
                    viewHolder.image.setImageBitmap(params.get(a.get(holder.getAdapterPosition())).IMAGE);
                }*/
                viewHolder.text.setText(title);
            }
        }
        int pos = position;
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(a==null) {
                    a = folders.get(s[pos]);
                    notifyDataSetChanged();
                }else{
                    MainActivity.recClicked(a.get(pos),a,pos);
                }

                //ViewPagerAdapter.restart_folder((String) s[pos], "Folder_Opened");
            }
        });
    }


    @Override
    public int getItemCount() {
        if(a!=null){
            return a.size();
        }
        return folders.size();
    }

    public static int darken(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        RelativeLayout layout;
        TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.song_name);
            image = itemView.findViewById(R.id.icon);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
