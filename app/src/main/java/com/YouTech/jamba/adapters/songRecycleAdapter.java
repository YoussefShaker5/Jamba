package com.YouTech.jamba.adapters;


import static com.YouTech.jamba.MainActivity.recClicked;
//import static com.YouTech.jamba.MusicService.params;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
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

import com.YouTech.jamba.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class songRecycleAdapter extends RecyclerView.Adapter {
    ArrayList<String> tracks;
    ArrayList<Bitmap> bitmaps;
    Object[] s;
    Context context;

    public songRecycleAdapter(ArrayList<String> tracks, Context context) {
        this.tracks = tracks;
        this.context = context;
        bitmaps = new ArrayList<>();
        for (int i = 0; i < tracks.size(); i++) {
            bitmaps.add(null);
        }
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
        //if (icons.get(position) != null) {

          /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bitmaps.get(holder.getAbsoluteAdapterPosition()) == null) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                context.getContentResolver(), Uri.parse(tracks.get(s[holder.getAbsoluteAdapterPosition()]).second));

                    } catch (FileNotFoundException exception) {
                        bitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.head_model);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmaps.set(holder.getAbsoluteAdapterPosition(),bitmap);

                    viewHolder.image.setImageBitmap(bitmap);
                }else viewHolder.image.setImageBitmap(bitmaps.get(holder.getAbsoluteAdapterPosition()));
            }
        },0);*/
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] cursor_cols = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM_ID};
        Cursor cursor = context.getContentResolver().query(uri,
                cursor_cols, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{tracks.get(holder.getAdapterPosition())}, null);
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            Long albumId = cursor.getLong(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
           // if(params.get(tracks.get(holder.getAdapterPosition())).IMAGE == null) {
                Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(viewHolder.image, new Callback() {
                    @Override
                    public void onSuccess() {
                        Drawable drawable = viewHolder.image.getDrawable();

// Check if the Drawable object is a BitmapDrawable
                        if (drawable instanceof BitmapDrawable) {
                            // Cast the Drawable object to a Bitmap object
                            //params.get(tracks.get(holder.getAdapterPosition())).IMAGE = ((BitmapDrawable) drawable).getBitmap();
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
                viewHolder.image.setImageBitmap(params.get(tracks.get(holder.getAdapterPosition())).IMAGE);
            }*/
            viewHolder.text.setText(title);
        }
        /*} else {
            android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, Uri.parse(arrayList.get(position).getAbsolutePath()));
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            byte[] bytes = mmr.getEmbeddedPicture();
            //coverart is an Imageview object
            if (bytes != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                viewHolder.image.setImageBitmap(bitmap);
                icons.set(position, bitmap);
            } else {
                Bitmap image = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.y_music);
                viewHolder.image.setImageBitmap(image);
                icons.set(position, image);
            }
            if (title != null) {
                viewHolder.text.setText(title);
                names.set(position, title);
            } else {
                String fileName = arrayList.get(position).getName();
                int lastPeriodPos = fileName.lastIndexOf('.');
                if (lastPeriodPos > 0) {
                    fileName = fileName.substring(0, lastPeriodPos);
                }
                viewHolder.text.setText(fileName);
                names.set(position, fileName);
            }
            try {
                mmr.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recClicked(holder.getAdapterPosition());
            }
        });
        /*viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){

                    //viewHolder.layout.setBackgroundDrawable(darken(viewHolder.itemView.getSolidColor(),5f));
                    viewHolder.layout.setBackgroundColor(context.getResources().getColor(R.color.darken));
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    Drawable background =  viewHolder.layout.getBackground();
                    background.setColorFilter(null);
                    //viewHolder.layout.setBackgroundDrawable(darken(viewHolder.itemView.getSolidColor(),-5f));
                    //viewHolder.layout.setBackground(background);
                    viewHolder.layout.setBackgroundColor(Color.TRANSPARENT);
                    recClicked(pos);
                    System.out.println(pos);
                }
                return true;
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return tracks.size();
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
