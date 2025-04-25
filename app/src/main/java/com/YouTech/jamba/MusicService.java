package com.YouTech.jamba;

import static androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

import com.YouTech.jamba.models.AudioParams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class MusicService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener {
    private static final int NOTIFICATION_ID = 787362328;
    private static final String CHANNEL_ID = "Music Player";
    private int lstIdx = -1;
    private static int finished = 0;
    private boolean finished1 = false;
    private static boolean running = false;
    private static boolean playing1 = false, p = false;
    public static ArrayList<String> mediaItems = new ArrayList<>();
    //public static Map<String, AudioParams> params = new HashMap<>();
    public static ArrayList<String> playing;
    private static Context context;
    private MediaSource mediaSource;
    private static ExoPlayer player;
    private static AudioManager mAudioManager;
    private static SharedPreferences sharedPreferences;
    public static int position = -1, position_in_queue = -1;
    private static MediaSessionCompat mediaSession;
    private boolean lstPlay = false;
    //private Cursor cursor;
    private Handler handler = new Handler(), handler1 = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (playing != null) {
                sharedPreferences = getSharedPreferences("musicData", Context.MODE_PRIVATE);
                sharedPreferences.edit().putLong("current", player.getCurrentPosition()).apply();
                //sharedPreferences.edit().putInt("position", position).apply();
                sharedPreferences.edit().putInt("position_in_queue", player.getCurrentMediaItemIndex()).apply();
                sharedPreferences.edit().putString("position_in_queue_path", playing.get(player.getCurrentMediaItemIndex())).apply();
            }
            handler.postDelayed(runnable, 100);
        }
    };
    private Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            if (playing != null && finished1) {
                if (!p || lstIdx != player.getCurrentMediaItemIndex() || lstPlay != player.isPlaying()) {
                    lstPlay = player.isPlaying();
                    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    String[] projection = {
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION
                    };
                    //p = true;
                    lstIdx = player.getCurrentMediaItemIndex();
                    if (player.isPlaying()) {
                        //if(params.get(playing.get(player.getCurrentMediaItemIndex())).IMAGE == null){
                            Cursor cursor = context.getContentResolver().query(uri,
                                    projection, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{playing.get(player.getCurrentMediaItemIndex())}, null);
                            if (cursor.moveToFirst()) {
                                int DATA = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                                int TITLE = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                                int ARTIST = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                                int _ID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                                int ALBUM = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                                int ALBUM_ID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                                int DURATION = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                                long albumId = cursor.getLong(ALBUM_ID);
                                Uri sArtworkUri = Uri
                                        .parse("content://media/external/audio/albumart");
                                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                                Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        //params.get(playing.get(player.getCurrentMediaItemIndex())).IMAGE = bitmap;
                                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MusicService.this, CHANNEL_ID);
                                        notificationBuilder.setSmallIcon(R.drawable.model);
                                        notificationBuilder.setContentTitle(cursor.getString(TITLE));
                                        notificationBuilder.setContentText(cursor.getString(ARTIST));
                                        notificationBuilder.setLargeIcon(bitmap);
                                        notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                                .setShowActionsInCompactView(0, 1, 2)
                                                .setMediaSession(mediaSession.getSessionToken()));
                                        notificationBuilder.addAction(new NotificationCompat.Action(
                                                R.drawable.ic_previous, getString(R.string.skip_previous),
                                                MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
                                        notificationBuilder.addAction(new NotificationCompat.Action(
                                                R.drawable.ic_pause, getString(R.string.pause),
                                                MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_PAUSE)));
                                        notificationBuilder.addAction(new NotificationCompat.Action(
                                                R.drawable.ic_next, getString(R.string.skip_next),
                                                MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
                                        startForeground(NOTIFICATION_ID, notificationBuilder.build());
                                        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                                                .setState(PlaybackStateCompat.STATE_PLAYING, player.getCurrentPosition(), 1.0f)
                                                .setActions(PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                                                .build());
                                        updateMetaData(new AudioParams(
                                                cursor.getString(ARTIST),
                                                cursor.getString(ALBUM),
                                                cursor.getString(TITLE),
                                                null,
                                                cursor.getLong(DURATION)
                                        ));
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                            }
                        /*}else {
                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MusicService.this, CHANNEL_ID);
                            notificationBuilder.setSmallIcon(R.drawable.model);
                            notificationBuilder.setContentTitle(params.get(playing.get(player.getCurrentMediaItemIndex())).TITLE);
                            notificationBuilder.setContentText(params.get(playing.get(player.getCurrentMediaItemIndex())).ARTIST);
                            notificationBuilder.setLargeIcon(params.get(playing.get(player.getCurrentMediaItemIndex())).IMAGE);
                            notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(0, 1, 2)
                                    .setMediaSession(mediaSession.getSessionToken()));
                            notificationBuilder.addAction(new NotificationCompat.Action(
                                    R.drawable.ic_previous, getString(R.string.skip_previous),
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
                            notificationBuilder.addAction(new NotificationCompat.Action(
                                    R.drawable.ic_pause, getString(R.string.pause),
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_PAUSE)));
                            notificationBuilder.addAction(new NotificationCompat.Action(
                                    R.drawable.ic_next, getString(R.string.skip_next),
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
                            startForeground(NOTIFICATION_ID, notificationBuilder.build());
                            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                                    .setState(PlaybackStateCompat.STATE_PLAYING, player.getCurrentPosition(), 1.0f)
                                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                                    .build());
                            updateMetaData();
                        }*/
                    } else {
                        //if(params.get(playing.get(player.getCurrentMediaItemIndex())).IMAGE == null){
                            Cursor cursor = context.getContentResolver().query(uri,
                                    projection, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{playing.get(player.getCurrentMediaItemIndex())}, null);
                            if (cursor.moveToFirst()) {
                                int DATA = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                                int TITLE = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                                int ARTIST = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                                int _ID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                                int ALBUM = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                                int ALBUM_ID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                                int DURATION = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                                long albumId = cursor.getLong(ALBUM_ID);
                                Uri sArtworkUri = Uri
                                        .parse("content://media/external/audio/albumart");
                                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                                Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        //params.get(playing.get(player.getCurrentMediaItemIndex())).IMAGE = bitmap;
                                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MusicService.this, CHANNEL_ID);
                                        notificationBuilder.setSmallIcon(R.drawable.model);
                                        notificationBuilder.setSmallIcon(R.drawable.model);
                                        notificationBuilder.setContentTitle(cursor.getString(TITLE));
                                        notificationBuilder.setContentText(cursor.getString(ARTIST));
                                        notificationBuilder.setLargeIcon(bitmap);
                                        notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                                .setShowActionsInCompactView(0, 1, 2)
                                                .setMediaSession(mediaSession.getSessionToken()));
                                        notificationBuilder.addAction(new NotificationCompat.Action(
                                                R.drawable.ic_previous, getString(R.string.skip_previous),
                                                MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
                                        notificationBuilder.addAction(new NotificationCompat.Action(
                                                R.drawable.ic_play, getString(R.string.play),
                                                MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_PLAY)));
                                        notificationBuilder.addAction(new NotificationCompat.Action(
                                                R.drawable.ic_next, getString(R.string.skip_next),
                                                MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
                                        startForeground(NOTIFICATION_ID, notificationBuilder.build());
                                        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                                                .setState(PlaybackStateCompat.STATE_PAUSED, player.getCurrentPosition(), 1.0f)
                                                .setActions(PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                                                .build());
                                        updateMetaData(new AudioParams(
                                                cursor.getString(ARTIST),
                                                cursor.getString(ALBUM),
                                                cursor.getString(TITLE),
                                                null,
                                                cursor.getLong(DURATION)
                                        ));
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                            }
                        /*}else {
                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MusicService.this, CHANNEL_ID);
                            notificationBuilder.setSmallIcon(R.drawable.model);
                            notificationBuilder.setContentTitle(params.get(playing.get(player.getCurrentMediaItemIndex())).TITLE);
                            notificationBuilder.setContentText(params.get(playing.get(player.getCurrentMediaItemIndex())).ARTIST);
                            notificationBuilder.setLargeIcon(params.get(playing.get(player.getCurrentMediaItemIndex())).IMAGE);
                            notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(0, 1, 2)
                                    .setMediaSession(mediaSession.getSessionToken()));
                            notificationBuilder.addAction(new NotificationCompat.Action(
                                    R.drawable.ic_previous, getString(R.string.skip_previous),
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
                            notificationBuilder.addAction(new NotificationCompat.Action(
                                    R.drawable.ic_play, getString(R.string.play),
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_PLAY)));
                            notificationBuilder.addAction(new NotificationCompat.Action(
                                    R.drawable.ic_next, getString(R.string.skip_next),
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
                            startForeground(NOTIFICATION_ID, notificationBuilder.build());
                            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                                    .setState(PlaybackStateCompat.STATE_PAUSED, player.getCurrentPosition(), 1.0f)
                                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                                    .build());
                            updateMetaData();
                        }*/

                    }
                }
            }

            handler1.postDelayed(runnable1, 100);
        }
    };


    private final MediaSessionCompat.Callback MySessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        @OptIn(markerClass = UnstableApi.class)
        public void onPlay() {
            super.onPlay();
            System.out.println("s");
            player.setPlayWhenReady(true);
            p = false;
            requestFocus();
        }

        @Override
        @OptIn(markerClass = UnstableApi.class)
        public void onPause() {
            super.onPause();
            System.out.println("sqs");
            p = false;
            player.pause();
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            player.setRepeatMode((repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL) ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_ONE);
            super.onSetRepeatMode(repeatMode);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            player.setShuffleModeEnabled(shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL);
            super.onSetShuffleMode(shuffleMode);
        }

        @Override
        @OptIn(markerClass = UnstableApi.class)
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            player.clearMediaItems();
            //mediaItemsDTO mediaItemsDTO = (com.YouTech.jamba.mediaItemsDTO) extras.get("playing");
            playing = extras.getStringArrayList("playing");
            for (int i = 0; i < playing.size(); i++) {
                if (Objects.equals(mediaId, playing.get(i))) {
                    position_in_queue = i;
                    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MusicService.this);
                    mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(playing.get(i)));
                    player.addMediaSource(mediaSource);
                    break;
                }
            }

            player.seekTo(position_in_queue, 0);
            player.prepare();
            MySessionCallback.onPlay();
            for (int i = position_in_queue + 1; i < playing.size(); i++) {
                DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MusicService.this);
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(playing.get(i)));
                player.addMediaSource(i, mediaSource);
            }
            for (int i = 0; i < position_in_queue; i++) {
                DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MusicService.this);
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(playing.get(i)));
                player.addMediaSource(i, mediaSource);
            }
            Gson gson = new GsonBuilder().serializeNulls().create();
            String json = gson.toJson(playing);
            sharedPreferences.edit().putString("playing", json).apply();
            super.onPlayFromMediaId(mediaId, extras);
        }


        @Override
        public void onSkipToNext() {
            if ((position_in_queue + 1) <= (playing.size() - 1)) {
                position_in_queue++;
            } else {
                position_in_queue = 0;
            }

            player.seekToNext();
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            if ((position_in_queue - 1) >= 0) {
                position_in_queue--;
            } else {
                position_in_queue = (playing.size() - 1);
            }
            player.seekToPrevious();
            super.onSkipToPrevious();
        }

        @Override
        public void onSeekTo(long pos) {
            player.seekTo(pos);
            p = false;
            super.onSeekTo(pos);
        }
    };

    public static boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        mediaItems.clear();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION
        };
        String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " ASC";

        Cursor cursor = getContentResolver().query(uri, projection, null, null, sortOrder);

        final CountDownLatch latch = new CountDownLatch(cursor.getCount());
        if (cursor != null && cursor.moveToFirst()) {
            int DATA = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int TITLE = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int ARTIST = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int _ID = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int ALBUM = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int ALBUM_ID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int DURATION = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            do {
                if (!Objects.equals(cursor.getString(TITLE), " ")) {
                    Cursor cursor1 = context.getContentResolver().query(uri,
                            projection, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{cursor.getString(DATA)}, null);
                    if (cursor1.moveToFirst()) {
                        long albumId = cursor1.getLong(ALBUM_ID);
                        Uri sArtworkUri = Uri
                                .parse("content://media/external/audio/albumart");
                        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                        mediaItems.add(cursor1.getString(DATA));
                        /*params.put(cursor1.getString(DATA),
                                new AudioParams(
                                        cursor1.getString(ARTIST),
                                        cursor1.getString(ALBUM),
                                        cursor1.getString(TITLE),
                                        null,
                                        cursor1.getLong(DURATION)
                                ));
                         */
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
            /*Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Your code to execute after the children have been loaded.
                    loadImages();
                }
            });*/
            try {
                //latch.await();
                finished1 = true;
                result.sendResult(new ArrayList<>());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void loadImages() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM_ID
        };
        for(String item : mediaItems) {
            Cursor cursor = context.getContentResolver().query(uri,
                    projection, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{item}, null);
            if (cursor.moveToFirst()) {
                int ALBUM_ID = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                long albumId = cursor.getLong(ALBUM_ID);
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                Picasso.get().load(albumArtUri).placeholder(R.drawable.head_model).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        //params.get(item).IMAGE = bitmap;
                        finished++;
                        System.out.println("Finished " + finished);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    @OptIn(markerClass = UnstableApi.class)
    public void onCreate() {
        super.onCreate();
        running = true;
        createNotificationChannel();
        Gson gson = new GsonBuilder().serializeNulls().create();
        context = this;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaSession = new MediaSessionCompat(this, "YouTech");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(MySessionCallback);
        mediaSession.setActive(true);
        mediaSession.setPlaybackToLocal(AudioManager.STREAM_MUSIC);
        setSessionToken(mediaSession.getSessionToken());
        player = new ExoPlayer.Builder(this).build();
        mediaSession.setActive(true);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                player.seekToNext();
                player.prepare();
                Player.Listener.super.onPlayerError(error);
            }
        });
        handler.postDelayed(runnable, 100);
        handler1.postDelayed(runnable1, 100);
        sharedPreferences = getSharedPreferences("musicData", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("playing", null);
        playing = gson.fromJson(json, new TypeToken<ArrayList<String>>() {
        });
        //playing = null;
        if (playing != null) {
            System.out.println(playing.size());
            long currentMilli = sharedPreferences.getLong("current", 0);
            //position = sharedPreferences.getInt("position", -1);
            position_in_queue = sharedPreferences.getInt("position_in_queue", -1);
            String position_in_queue_path = sharedPreferences.getString("position_in_queue_path", null);
            if (position_in_queue != -1 && playing.size() != 0 && position_in_queue_path != null) {
                player.clearMediaItems();
                //mediaItemsDTO mediaItemsDTO = (com.YouTech.jamba.mediaItemsDTO) extras.get("playing");
                for (int i = 0; i < playing.size(); i++) {
                    if (Objects.equals(position_in_queue_path, playing.get(i)))
                        position_in_queue = i;
                    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MusicService.this);
                    mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(playing.get(i)));
                    player.addMediaSource(mediaSource);
                }

                player.seekTo(position_in_queue, currentMilli);
                player.prepare();
                /*for (int i = position_in_queue + 1; i < playing.size(); i++) {
                    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MusicService.this);
                    mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(playing.get(i)));
                    player.addMediaSource(i, mediaSource);
                }
                for (int i = 0; i < position_in_queue; i++) {
                    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(MusicService.this);
                    mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(playing.get(i)));
                    player.addMediaSource(i, mediaSource);
                }*/
            }
        }
        /*final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] cursor_cols = {MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION};
        cursor = context.getContentResolver().query(uri,
                cursor_cols, MediaStore.Audio.Media.DATA + " LIKE ?", new String[]{playing.get(player.getCurrentMediaItemIndex())}, null);
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
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                        context.getContentResolver(), albumArtUri);

            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
                bitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.head_model);
            } catch (IOException e) {
                e.printStackTrace();
            }
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(MusicService.this, CHANNEL_ID);
            notificationBuilder.setSmallIcon(R.drawable.model);
            notificationBuilder.setContentTitle(getString(R.string.temp_title));
            notificationBuilder.setContentText("");
            notificationBuilder.setLargeIcon(bitmap);
            notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession.getSessionToken()));
           /* notificationBuilder.addAction(new NotificationCompat.Action(
                    R.drawable.ic_previous, getString(R.string.skip_previous),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)));
            notificationBuilder.addAction(new NotificationCompat.Action(
                    R.drawable.ic_pause, getString(R.string.pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_PAUSE)));
            notificationBuilder.addAction(new NotificationCompat.Action(
                    R.drawable.ic_next, getString(R.string.skip_next),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)));
            startForeground(NOTIFICATION_ID, notificationBuilder.build());
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0f)
                    //.setActions(PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build());
            //updateMetaData();
        }*/
    }

    public static List<String> getPlaying() {
        return playing;
    }

    public static void setPlaying(ArrayList<String> playing) {
        sharedPreferences = context.getSharedPreferences("musicData", Context.MODE_PRIVATE);
        /*FileOutputStream fos = null;
        try {
            fos = context.openFileOutput("playing", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(playing);
            oos.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error1");
            e.printStackTrace();
        }*/
        Gson gson = new GsonBuilder().serializeNulls().create();
        String json = gson.toJson(gson);
        sharedPreferences.edit().putString("playing", json).apply();
        MusicService.playing = playing;
    }

    public static long getPosition() {
        return player.getCurrentPosition();
    }

    public static long getDuration() {
        return player.getDuration();
    }

    private void updateMetaData(AudioParams params) {
        //replace with medias albumArt
        // Update the current metadata
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();


        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, params.IMAGE);
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, params.IMAGE);

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, params.IMAGE);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, params.ARTIST);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, params.TITLE);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, params.TITLE);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, params.ARTIST);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, params.ALBUM);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, position_in_queue + 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, params.DURATION);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, playing.size());

        mediaSession.setMetadata(metadataBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Current playing music", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean requestFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager
                .requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public static boolean isRunning() {
        return running;
    }

    @Override
    public void onDestroy() {
        running = false;
        player.pause();
        player.release();
        /*player.stop();
        player = null;*/
    }

    public static ExoPlayer getPlayer() {
        return player;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (player != null) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN || focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT || focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE || focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
                if (playing1) {
                    playing1 = false;
                    MySessionCallback.onPlay();
                }
            } else {
                if (isPlaying()) {
                    playing1 = true;
                    MySessionCallback.onPause();
                } else {
                    playing1 = false;
                }
            }

        }
    }
}
