package com.YouTech.jamba;

import static com.YouTech.jamba.MusicService.getDuration;
import static com.YouTech.jamba.MusicService.getPlayer;
import static com.YouTech.jamba.MusicService.getPlaying;
import static com.YouTech.jamba.MusicService.getPosition;
import static com.YouTech.jamba.MusicService.isPlaying;
import static com.YouTech.jamba.MusicService.isRunning;
import static com.YouTech.jamba.MusicService.mediaItems;
import static com.YouTech.jamba.MusicService.position;
import static com.YouTech.jamba.MusicService.position_in_queue;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.YouTech.jamba.adapters.AlbumRecycleAdapter;
import com.YouTech.jamba.adapters.ArtistRecycleAdapter;
import com.YouTech.jamba.adapters.FolderRecycleAdapter;
import com.YouTech.jamba.adapters.ViewPagerAdapter;
import com.YouTech.jamba.models.PageModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    // All needed variables between methods
    private MediaBrowserCompat mediaBrowser;
    private ArrayList<String> tracks;
    private Map<String, ArrayList<String>> folders;
    private Map<String, ArrayList<String>> albums;
    private Map<String, ArrayList<String>> artists;
    private ViewPager viewPager;
    private static MediaControllerCompat.TransportControls controls;
    private static Context context;
    private SharedPreferences preferences;
    private SeekBar seekBar;
    private TabLayout tabLayout;
    private ImageButton sPrevious, sPlay, sNext, bPrevious, bPlay, bNext, close, shuffle, repeat;
    private ImageView album_cover;
    private TextView sName, bName, bArtist, current, duration;
    private float x1, x2, y1, y2;
    private int alpha = (int) (255 / 0.5);
    private float a1, a2, peekHeight;
    private boolean playi = false;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View mBottomSheet;
    private final Handler handler = new Handler();
    private static Cursor cursor;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (position_in_queue != -1 && getPlaying() != null && getPlayer() != null) {
                bottomSheetBehavior.setDraggable(true);
                mBottomSheet.setClickable(true);
                final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                final String[] cursor_cols = {MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION};
                cursor = context.getContentResolver().query(uri,
                        cursor_cols, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{getPlaying().get(getPlayer().getCurrentMediaItemIndex())}, null);
                if (cursor.moveToFirst()) {
                    String title = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    long albumId = cursor.getLong(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(album_cover);
                    sName.setText(title);
                    bName.setText(title);
                    bArtist.setText((artist != null) ? artist : getString(R.string.unknown_artist));
                }
                if (isPlaying()) {
                    sPlay.setImageResource(R.drawable.ic_pause);
                    bPlay.setImageResource(R.drawable.ic_pause);
                } else {
                    sPlay.setImageResource(R.drawable.ic_play);
                    bPlay.setImageResource(R.drawable.ic_play);
                }
                sPlay.setAlpha(alpha);
                seekBar.setMax(Integer.MAX_VALUE);
                seekBar.setProgress((int) (((double) getPosition() / (double) getDuration()) * Integer.MAX_VALUE));
                current.setText(getTimeString(getPosition()));
                duration.setText(getTimeString(getDuration()));
                cursor.close();
            } else {
                bottomSheetBehavior.setDraggable(false);
                mBottomSheet.setClickable(false);
            }
            handler.postDelayed(runnable, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setting up views and variables
        invalidate();
        // Calling required permissions
        // check battery optimization
        //checkBatteryOptimization();
    }

    private static String getTimeString(long millis) {
        String returned;

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        if (hours != 0) {
            returned = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            returned = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }

        return returned;
    }

    private void checkBatteryOptimization() {
        // Create an alert dialog
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        String packageName = getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                // Set the title of the dialog
                builder.setTitle(getString(R.string.battery_optimization_title));

                // Set the message of the dialog
                builder.setMessage(getString(R.string.battery_optimization_message));

                // Add a button to the dialog that allows the user to disable battery optimization
                builder.setPositiveButton("Disable Battery Optimization", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Disable battery optimization for the app
                    /*Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);*/
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent();


                            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            startActivity(intent);

                        }
                    }
                });

                // Create the dialog
                AlertDialog dialog = builder.create();

                // Show the dialog
                dialog.show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setTheme(R.style.Theme_Jamba);
        setContentView(R.layout.activity_main);
        // Setting up views and variables
        invalidate();
    }

    private void measurements() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        float xx1 = (float) (width * 0.8);
        float xx2 = (float) (height * 0.5);
        a2 = Math.min(xx1, xx2);
        float yy1 = (float) (width * 0.10);
        float yy2;
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // code for portrait mode
            yy2 = (float) (height * (1.2 / 19.4));
        } else {
            // code for landscape mode
            yy2 = (float) (height * (1.2 / 11.6));
        }
        peekHeight = yy2;
        yy2 *= 0.9;
        a1 = Math.min(yy1, yy2);
        x1 = (float) ((width * 0.05) + ((yy1 - a1) / 2));
        y1 = (float) ((peekHeight - a1) / 2.0);
        x2 = (float) ((width * 0.1) + ((xx1 - a2) / 2));
        y2 = (float) ((height * 0.06) + ((xx2 - a2) / 2));
    }

    private void invalidate() {
        // Setting up views
        preferences = getSharedPreferences("MusicApp.com", Context.MODE_PRIVATE);
        mBottomSheet = findViewById(R.id.bottom_sheet);
        sPrevious = findViewById(R.id.sPrevious);
        sPlay = findViewById(R.id.sPlay);
        bName = findViewById(R.id.song_title);
        bArtist = findViewById(R.id.song_artist);
        sName = findViewById(R.id.sName);
        sNext = findViewById(R.id.sNext);
        bPrevious = findViewById(R.id.previous);
        bPlay = findViewById(R.id.play_pause);
        bNext = findViewById(R.id.next);
        close = findViewById(R.id.close);
        current = findViewById(R.id.current);
        duration = findViewById(R.id.duration);
        shuffle = findViewById(R.id.shuffle);
        repeat = findViewById(R.id.repeat);
        album_cover = findViewById(R.id.album_cover);
        viewPager = findViewById(R.id.pages);
        seekBar = findViewById(R.id.seek_bar);
        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
        shuffle.setImageResource((preferences.getBoolean("Shuffle", false)) ? R.drawable.ic_shuffle_on : R.drawable.ic_shuffle);
        repeat.setImageResource((preferences.getBoolean("Repeat", false)) ? R.drawable.ic_repeat_one : R.drawable.ic_repeat);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int selectedTabIndex = tab.getPosition();
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    if (i != selectedTabIndex) {
                        tabLayout.getTabAt(i).view.setAlpha((((float) tabLayout.getTabCount()) - Math.abs(((float) selectedTabIndex - i))) / ((float) tabLayout.getTabCount()));
                    } else {
                        tab.view.setAlpha(1);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // Needed for recycler view
        /*LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);*/
        measurements();
        context = this;
        // Setting up mediaBrowser
        if (!isRunning()) {
            mediaBrowser = new MediaBrowserCompat(this,
                    new ComponentName(this, MusicService.class),
                    connectionCallbacks,
                    null);
            mediaBrowser.connect();
            callPermissions();
        } else {
            /*tracks = saved.getStringArrayList("tracks");
            folders = new HashMap<>();
            albums = new HashMap<>();
            artists = new HashMap<>();
            try {
                folders = retrieveMapFromBundleAsSerializable(saved.getByteArray("folders"));
                albums = retrieveMapFromBundleAsSerializable(saved.getByteArray("albums"));
                artists = retrieveMapFromBundleAsSerializable(saved.getByteArray("artists"));
            } catch (IOException | ClassNotFoundException | NullPointerException e) {
                e.printStackTrace();
            }*/
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(MainActivity.this, new PageModel(tracks, folders, albums, artists));
            viewPager.setAdapter(viewPagerAdapter);
        }
        int width1 = (int) a1;
        album_cover.setX(x1);
        album_cover.setY(y1);
        sPrevious.setVisibility(View.VISIBLE);
        sPlay.setVisibility(View.VISIBLE);
        sNext.setVisibility(View.VISIBLE);
        bPrevious.setVisibility(View.GONE);
        bNext.setVisibility(View.GONE);
        bPlay.setVisibility(View.GONE);
        float ne = 25;
        GradientDrawable drawable = (GradientDrawable) mBottomSheet.getBackground();
        drawable.setCornerRadii(new float[]{ne, ne, ne, ne, 0, 0, 0, 0});
        album_cover.setLayoutParams(new ViewGroup.LayoutParams(width1, width1));
        // Function for setting up bottomSheetBehavior
        setUpBottomSheets();
        handler.postDelayed(runnable, 100);
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferences.getBoolean("Shuffle", false)) {
                    controls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                    shuffle.setImageResource(R.drawable.ic_shuffle);
                    preferences.edit().putBoolean("Shuffle", false).apply();
                } else {
                    controls.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                    shuffle.setImageResource(R.drawable.ic_shuffle_on);
                    preferences.edit().putBoolean("Shuffle", true).apply();
                }
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferences.getBoolean("Repeat", false)) {
                    controls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                    repeat.setImageResource(R.drawable.ic_repeat);
                    preferences.edit().putBoolean("Repeat", false).apply();
                } else {
                    controls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                    repeat.setImageResource(R.drawable.ic_repeat_one);
                    preferences.edit().putBoolean("Repeat", true).apply();
                }
            }
        });

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position_in_queue != -1) {
                    controls.skipToNext();
                }
            }
        });
        sNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position_in_queue != -1) {
                    controls.skipToNext();
                }
            }
        });
        bPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position_in_queue != -1) {
                    controls.skipToPrevious();
                }
            }
        });
        sPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position_in_queue != -1) {
                    controls.skipToPrevious();
                }
            }
        });

        sPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position_in_queue != -1) {
                    if (isPlaying()) {
                        controls.pause();
                    } else {
                        controls.play();
                    }
                }
            }
        });
        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position_in_queue != -1) {
                    if (isPlaying()) {
                        sPlay.setImageResource(R.drawable.ic_play);
                        controls.pause();
                    } else {
                        sPlay.setImageResource(R.drawable.ic_pause);
                        controls.play();
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    controls.seekTo((long) (((double) progress) / ((double) Integer.MAX_VALUE) * getDuration()));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                playi = isPlaying();
                controls.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(playi) controls.play();
            }
        });

        mBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
    }

    private void setUpBottomSheets() {
        bottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight((int) peekHeight);
        bottomSheetBehavior.setHideable(false);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    int width = (int) a1;
                    album_cover.setX(x1);
                    album_cover.setY(y1);
                    bottomSheetBehavior.setDraggable(true);
                    sPrevious.setVisibility(View.VISIBLE);
                    sPlay.setVisibility(View.VISIBLE);
                    sNext.setVisibility(View.VISIBLE);
                    bPrevious.setVisibility(View.GONE);
                    bNext.setVisibility(View.GONE);
                    bPlay.setVisibility(View.GONE);
                    sName.setVisibility(View.VISIBLE);
                    bName.setVisibility(View.GONE);
                    shuffle.setVisibility(View.GONE);
                    repeat.setVisibility(View.GONE);
                    current.setVisibility(View.GONE);
                    duration.setVisibility(View.GONE);
                    seekBar.setVisibility(View.GONE);
                    bArtist.setVisibility(View.GONE);
                    float ne = 25;
                    GradientDrawable drawable = (GradientDrawable) mBottomSheet.getBackground();
                    drawable.setCornerRadii(new float[]{ne, ne, ne, ne, 0, 0, 0, 0});
                    album_cover.setLayoutParams(new ViewGroup.LayoutParams(width, width));
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setDraggable(false);
                    int width = (int) a2;
                    album_cover.setX(x2);
                    album_cover.setY(y2);
                    close.setVisibility(View.VISIBLE);
                    sPrevious.setVisibility(View.GONE);
                    sPlay.setVisibility(View.GONE);
                    sNext.setVisibility(View.GONE);
                    bPrevious.setVisibility(View.VISIBLE);
                    bNext.setVisibility(View.VISIBLE);
                    sName.setVisibility(View.GONE);
                    bName.setVisibility(View.VISIBLE);
                    bArtist.setVisibility(View.VISIBLE);

                    shuffle.setVisibility(View.VISIBLE);
                    repeat.setVisibility(View.VISIBLE);
                    current.setVisibility(View.VISIBLE);
                    duration.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);
                    float ne = 0;
                    GradientDrawable drawable = (GradientDrawable) mBottomSheet.getBackground();
                    drawable.setCornerRadii(new float[]{ne, ne, ne, ne, 0, 0, 0, 0});
                    bPlay.setVisibility(View.VISIBLE);
                    album_cover.setLayoutParams(new ViewGroup.LayoutParams(width, width));
                }
                if (newState != BottomSheetBehavior.STATE_EXPANDED) {
                    close.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (position_in_queue != -1) {
                    if (slideOffset < 0.5) {
                        float newOff = (float) Math.abs(slideOffset - 0.5);
                        alpha = (int) (newOff * 255 / 0.5);
                        sPrevious.getDrawable().setAlpha((int) (newOff * 255 / 0.5));
                        sPlay.getDrawable().setAlpha((int) (newOff * 255 / 0.5));
                        sNext.getDrawable().setAlpha((int) (newOff * 255 / 0.5));
                        sPrevious.setVisibility(View.VISIBLE);
                        sPlay.setVisibility(View.VISIBLE);
                        sNext.setVisibility(View.VISIBLE);
                        bPrevious.setVisibility(View.GONE);
                        bNext.setVisibility(View.GONE);
                        bPlay.setVisibility(View.GONE);
                        sName.setVisibility(View.GONE);
                        bName.setVisibility(View.GONE);
                        bArtist.setVisibility(View.GONE);
                        shuffle.setVisibility(View.GONE);
                        repeat.setVisibility(View.GONE);
                        current.setVisibility(View.GONE);
                        duration.setVisibility(View.GONE);
                        seekBar.setVisibility(View.GONE);
                    } else if (slideOffset > 0.5) {
                        float newOff = (float) Math.abs(slideOffset);
                        bPrevious.getDrawable().setAlpha((int) (newOff * 255 / 0.5));
                        bPlay.getDrawable().setAlpha((int) (newOff * 255 / 0.5));
                        bNext.getDrawable().setAlpha((int) (newOff * 255 / 0.5));
                        bName.setAlpha((int) (newOff * 255 / 0.5));
                        bArtist.setAlpha((int) (newOff * 255 / 0.5));
                        shuffle.setAlpha((int) (newOff * 255 / 0.5));
                        repeat.setAlpha((int) (newOff * 255 / 0.5));
                        current.setAlpha((int) (newOff * 255 / 0.5));
                        duration.setAlpha((int) (newOff * 255 / 0.5));
                        seekBar.setAlpha((int) (newOff * 255 / 0.5));
                        bPrevious.setVisibility(View.VISIBLE);
                        bNext.setVisibility(View.VISIBLE);
                        bPlay.setVisibility(View.VISIBLE);
                        sPrevious.setVisibility(View.GONE);
                        sNext.setVisibility(View.GONE);
                        sPlay.setVisibility(View.GONE);
                        sName.setVisibility(View.GONE);
                        bName.setVisibility(View.VISIBLE);
                        bArtist.setVisibility(View.VISIBLE);
                        shuffle.setVisibility(View.VISIBLE);
                        repeat.setVisibility(View.VISIBLE);
                        current.setVisibility(View.VISIBLE);
                        duration.setVisibility(View.VISIBLE);
                        seekBar.setVisibility(View.VISIBLE);
                    } else {
                        sPrevious.setVisibility(View.GONE);
                        sPlay.setVisibility(View.GONE);
                        sNext.setVisibility(View.GONE);
                        bPrevious.setVisibility(View.GONE);
                        bNext.setVisibility(View.GONE);
                        bPlay.setVisibility(View.GONE);
                        shuffle.setVisibility(View.GONE);
                        repeat.setVisibility(View.GONE);
                        current.setVisibility(View.GONE);
                        duration.setVisibility(View.GONE);
                        seekBar.setVisibility(View.GONE);
                    }
                    int width = (int) (((a2 - a1) * slideOffset) + a1);
                    float x = (((x2 - x1) * slideOffset) + x1);
                    float y = (((y2 - y1) * slideOffset) + y1);
                    album_cover.setX(x);
                    album_cover.setY(y);
                    album_cover.setLayoutParams(new ViewGroup.LayoutParams(width, width));
                    float ne = 25 * Math.abs(slideOffset - 1);
                    GradientDrawable drawable = (GradientDrawable) mBottomSheet.getBackground();
                    drawable.setCornerRadii(new float[]{ne, ne, ne, ne, 0, 0, 0, 0});
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Map<String, ArrayList<String>> folders = new HashMap<>();
        Map<String, ArrayList<String>> albums = new HashMap<>();
        Map<String, ArrayList<String>> artists = new HashMap<>();
        outState.putStringArrayList("tracks", tracks);
        try {
            outState.putByteArray("folders", saveMapToBundleAsSerializable(folders));
            outState.putByteArray("albums", saveMapToBundleAsSerializable(albums));
            outState.putByteArray("artists", saveMapToBundleAsSerializable(artists));
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onSaveInstanceState(outState);
    }

    public static void recClicked(int pos) {
        //position = pos;
        //position_in_queue = pos;
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("playing", mediaItems);
        controls.playFromMediaId(mediaItems.get(pos), bundle);
    }

    public static void recClicked(String path, ArrayList<String> items, int pos) {
        position_in_queue = pos;
        position = pos;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> medias = new ArrayList<>();
                for (int i = 0; i < items.size(); i++) {
                    String[] projection = {
                            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST
                    };

                    final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    cursor = context.getContentResolver().query(uri,
                            projection, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{items.get(i)}, null);
                    if (cursor.moveToFirst()) {
                        int location = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                        int title = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                        int singer = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                        if (!Objects.equals(cursor.getString(title), " ")) {
                            medias.add(cursor.getString(location));
                        }
                    }
                    cursor.close();
                }

                Bundle bundle = new Bundle();
                bundle.putStringArrayList("playing", medias);
                for (int i = 0; i < mediaItems.size(); i++) {
                    if (mediaItems.get(i).equals(path)) {
                        position = i;
                        controls.playFromMediaId(mediaItems.get(i), bundle);
                        break;
                    }
                }
            }
        });

        thread.start();


    }

    private void callPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.POST_NOTIFICATIONS}, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mediaBrowser.subscribe("root", subscriptionCallback);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            tracks = new ArrayList<>();
            folders = new HashMap<>();
            albums = new HashMap<>();
            artists = new HashMap<>();
            String externalStorageDirectory = Environment.getExternalStorageDirectory().getPath();

            // Get the external storage directory.
            String internalStorageDirectory = getFilesDir().getPath();

            // Get the removable storage directories.
            File[] removableStorageFiles = ContextCompat.getExternalFilesDirs(MainActivity.this, null);
            ArrayList<String> removableStorageDirectories = new ArrayList<>();
            for (int i = 0; i < removableStorageFiles.length; i++) {
                removableStorageDirectories.add(removableStorageFiles[i].getPath());
            }

            for (int i = 0; i < mediaItems.size(); i++) {
                final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                final String[] cursor_cols = {MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DURATION};
                cursor = context.getContentResolver().query(uri,
                        cursor_cols, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{mediaItems.get(i)}, null);
                if (cursor.moveToFirst()) {
                    String title = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String album = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    long albumId = cursor.getLong(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                    Uri sArtworkUri = Uri
                            .parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    Bitmap bitmap = null;
                    String[] artistsArr;
                    if (Objects.equals(artist, MediaStore.UNKNOWN_STRING)) {
                        artistsArr = new String[]{getString(R.string.unknown_artist)};
                    } else {
                        artistsArr = artist.split("/");
                    }
                    /*try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                context.getContentResolver(), albumArtUri);

                    } catch (FileNotFoundException exception) {
                        bitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.head_model);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(mediaItems.get(i).getDescription().getMediaId());
                    byte [] data = mmr.getEmbeddedPicture();
                    if(data !=null) {
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }else{
                        bitmap = BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.head_model);
                    }*/
                    /*bitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.head_model);*/
                    File file = new File(mediaItems.get(i));

                    String folder = file.getParentFile().getName();
                    //String artist1 = (artist != null) ? artist : getString(R.string.unknown_artist);
                    String album1 = (album != null) ? album : getString(R.string.unknown_album);
                    int index = removableStorageDirectories.indexOf(file.getParent());
                    if (Objects.equals(file.getParent(), internalStorageDirectory))
                        folder = getString(R.string.Internal_Storage);
                    else if (Objects.equals(file.getParent(), externalStorageDirectory))
                        folder = getString(R.string.External_Storage);
                    else if (index != -1) folder = getString(R.string.Removable_Storage);
                    tracks.add(file.getAbsolutePath());
                    if (folders.containsKey(folder)) {
                        folders.get(folder).add(file.getAbsolutePath());
                    } else {
                        folders.put(folder, new ArrayList<>());
                        folders.get(folder).add(file.getAbsolutePath());
                    }
                    if (albums.containsKey(album1)) {
                        albums.get(album1).add(file.getAbsolutePath());
                    } else {
                        albums.put(album1, new ArrayList<>());
                        albums.get(album1).add(file.getAbsolutePath());
                    }
                    for (String artist1 : artistsArr) {
                        if (artists.containsKey(artist1)) {
                            artists.get(artist1).add(file.getAbsolutePath());
                        } else {
                            artists.put(artist1, new ArrayList<>());
                            artists.get(artist1).add(file.getAbsolutePath());
                        }
                    }
                }
                cursor.close();
            }
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(MainActivity.this, new PageModel(tracks, folders, albums, artists));
            viewPager.setAdapter(viewPagerAdapter);
            super.onChildrenLoaded(parentId, children);
        }
    };

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            if (viewPager.getCurrentItem() == 1 && FolderRecycleAdapter.a != null) {
                FolderRecycleAdapter.back();
            } else if (viewPager.getCurrentItem() == 2 && AlbumRecycleAdapter.a != null) {
                AlbumRecycleAdapter.back();
            } else if (viewPager.getCurrentItem() == 3 && ArtistRecycleAdapter.a != null) {
                ArtistRecycleAdapter.back();
            } else {
                super.onBackPressed();
            }
        }
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    MediaControllerCompat mediaController =
                            new MediaControllerCompat(MainActivity.this, // Context
                                    token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);

                    // Finish building the UI
                    MediaControllerCompat mediaController1 = MediaControllerCompat.getMediaController(MainActivity.this);

                    // Register a Callback to stay in sync
                    mediaController1.registerCallback(controllerCallback);
                    controls = MediaControllerCompat.getMediaController((Activity) context).getTransportControls();
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                    System.out.println("Suspended");
                }


                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                    System.out.println("Failed");
                }
            };
    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                }

                @Override
                public void onSessionDestroyed() {
                    mediaBrowser.disconnect();
                    // maybe schedule a reconnection using a new MediaBrowser instance
                }
            };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (service != null) {
            stopService(service);
        }*/
        mediaBrowser.disconnect();
        if (MediaControllerCompat.getMediaController(MainActivity.this) != null) {
            MediaControllerCompat.getMediaController(MainActivity.this).unregisterCallback(controllerCallback);
        }

    }

    private Map<String, ArrayList<String>> retrieveMapFromBundleAsSerializable(byte[] bundle) throws IOException, ClassNotFoundException, NullPointerException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bundle);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Map<String, ArrayList<String>> map = (Map<String, ArrayList<String>>) objectInputStream.readObject();
        objectInputStream.close();
        return map;
    }

    public static byte[] saveMapToBundleAsSerializable(Map<String, ArrayList<String>> map) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(map);
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}