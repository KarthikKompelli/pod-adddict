package com.thomaskioko.podadddict.app.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.thomaskioko.podadddict.app.R;
import com.thomaskioko.podadddict.app.interfaces.onPlayPauseListener;
import com.thomaskioko.podadddict.app.ui.PodCastListActivity;
import com.thomaskioko.podadddict.app.ui.fragments.PlayerFragment;
import com.thomaskioko.podadddict.app.util.ApplicationConstants;

/**
 * @author kioko
 */
public class MediaPlayerService extends Service implements onPlayPauseListener {


private MediaSessionManager m_objMediaSessionManager;
private MediaSession m_objMediaSession;
private MediaController m_objMediaController;
private MediaPlayer m_objMediaPlayer;
private NotificationManager notificationManager;

        PlayerFragment pFragment;

        onCallbackListener callback;

private boolean isSwipable = false;

public interface onCallbackListener {
    public PlayerFragment getPlayerFragmentFromHome();
}

    @Override
    public IBinder onBind(Intent intent) {
        if (pFragment != null)
            pFragment.mCallback7 = this;
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ApplicationConstants.ACTION_PLAY)) {
            m_objMediaController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ApplicationConstants.ACTION_PAUSE)) {
            m_objMediaController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ApplicationConstants.ACTION_FAST_FORWARD)) {
            m_objMediaController.getTransportControls().fastForward();
        } else if (action.equalsIgnoreCase(ApplicationConstants.ACTION_REWIND)) {
            m_objMediaController.getTransportControls().rewind();
        } else if (action.equalsIgnoreCase(ApplicationConstants.ACTION_PREVIOUS)) {
            m_objMediaController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ApplicationConstants.ACTION_NEXT)) {
            m_objMediaController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ApplicationConstants.ACTION_STOP)) {
            m_objMediaController.getTransportControls().stop();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildNotification(Notification.Action action) {

        Notification.MediaStyle style = new Notification.MediaStyle();
        style.setShowActionsInCompactView(1);
        style.setMediaSession(m_objMediaSession.getSessionToken());

        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(ApplicationConstants.ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        String artist;
        if (PlayerFragment.localIsPlaying) {
            artist = PlayerFragment.localTrack.getArtist();
        } else {
            artist = "";
        }

        Intent notificationIntent = new Intent(this, PodCastListActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setStyle(style)
                .setSmallIcon(R.drawable.ic_notification)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDeleteIntent(pendingIntent)
                .addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ApplicationConstants.ACTION_PREVIOUS))
                .addAction(action)
                .addAction(generateAction(android.R.drawable.ic_media_next, "Next", ApplicationConstants.ACTION_NEXT))
                .setContentTitle(PlayerFragment.selected_track_title.getText())
                .setContentText(artist)
                .setLargeIcon(((BitmapDrawable) PlayerFragment.selected_track_image.getDrawable()).getBitmap())
                .build();

        notification.contentIntent = pendingNotificationIntent;
        notification.priority = Notification.PRIORITY_MAX;

        if (isSwipable || (PlayerFragment.mMediaPlayer != null && PlayerFragment.mMediaPlayer.isPlaying())) {
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        }


        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

//        updateMediaSession();

    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private Notification.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(icon, title, pendingIntent).build();
    }

    private PendingIntent retreivePlaybackAction(int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(this, MediaPlayerService.class);
        switch (which) {
            case 1:
                // Play and pause
                action = new Intent(ApplicationConstants.ACTION_PLAY);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 1, action, 0);
                return pendingIntent;
            case 2:
                // Skip tracks
                action = new Intent(ApplicationConstants.ACTION_NEXT);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 2, action, 0);
                return pendingIntent;
            case 3:
                // Previous tracks
                action = new Intent(ApplicationConstants.ACTION_PREVIOUS);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 3, action, 0);
                return pendingIntent;
            case 4:
                //fast forward tracks
                action = new Intent(ApplicationConstants.ACTION_FAST_FORWARD);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 4, action, 0);
                return pendingIntent;
            case 5:
                //rewind tracks
                action = new Intent(ApplicationConstants.ACTION_REWIND);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 5, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pFragment = PodCastListActivity.getPlayerFragment();
        if (pFragment != null)
            pFragment.mCallback7 = this;

        if (m_objMediaSessionManager == null) {
            initMediaSessions();
        }
        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void updateMediaSession() {
        m_objMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        if (PlayerFragment.localIsPlaying) {
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, PlayerFragment.localTrack.getTitle());
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, PlayerFragment.localTrack.getArtist());
        } else {
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, PlayerFragment.track.getTitle());
            metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, "");
        }
        if (((BitmapDrawable) PlayerFragment.selected_track_image.getDrawable()).getBitmap() != null) {

            metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, ((BitmapDrawable) PlayerFragment.selected_track_image.getDrawable()).getBitmap());
        }

        m_objMediaSession.setMetadata(metadataBuilder.build());
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder();
        stateBuilder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_REWIND | PlaybackState.ACTION_FAST_FORWARD);
        stateBuilder.setState(!PlayerFragment.mMediaPlayer.isPlaying() ? PlaybackState.STATE_PAUSED : PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        m_objMediaSession.setPlaybackState(stateBuilder.build());
        m_objMediaSession.setActive(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSessions() {

        if (pFragment != null) {
            pFragment.mCallback7 = this;
        }
        m_objMediaPlayer = PlayerFragment.mMediaPlayer;
        m_objMediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        m_objMediaSession = new MediaSession(getApplicationContext(), "sample session");

        m_objMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        /*MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        if (PlayerFragment.localIsPlaying) {
            if (PlayerFragment.localTrack != null) {
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, PlayerFragment.localTrack.getTitle());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, PlayerFragment.localTrack.getArtist());
            }
        } else {
            if (PlayerFragment.track != null) {
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, PlayerFragment.track.getTitle());
                metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, "");
            }
        }
        if (PlayerFragment.selected_track_image != null) {
            if (((BitmapDrawable) PlayerFragment.selected_track_image.getDrawable()).getBitmap() != null) {
                metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, ((BitmapDrawable) PlayerFragment.selected_track_image.getDrawable()).getBitmap());
            }
        }


        m_objMediaSession.setMetadata(metadataBuilder.build());
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder();
        stateBuilder.setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_FAST_FORWARD | PlaybackState.ACTION_REWIND);
        stateBuilder.setState(!PlayerFragment.mMediaPlayer.isPlaying() ? PlaybackState.STATE_PAUSED : PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f);
        m_objMediaSession.setPlaybackState(stateBuilder.build());*/

        m_objMediaSession.setActive(true);
        m_objMediaController = m_objMediaSession.getController();
        m_objMediaSession.setCallback(new MediaSession.Callback() {

            @Override
            public void onPlay() {
                super.onPlay();
                try {
                    Log.d(ApplicationConstants.LOG_TAG, "onPlay");
                    PlayerFragment pFrag = pFragment;
                    if (!pFrag.isStart) {
                        pFrag.togglePlayPause();
                    }
                    pFrag.isStart = false;
                    buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ApplicationConstants.ACTION_PAUSE));
                } catch (Exception e) {
                    notificationManager.cancel(1);
                }
            }

            @Override
            public void onPause() {
                super.onPause();
                try {
                    PlayerFragment pFrag = pFragment;
                    pFrag.togglePlayPause();
                    buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ApplicationConstants.ACTION_PLAY));
                } catch (Exception e) {

                }
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                try {
                    if (pFragment != null)
                        pFragment.mCallback2.onComplete();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ApplicationConstants.ACTION_PAUSE));
                        }
                    }, 100);
                } catch (Exception e) {

                }

            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                try {
                    if (pFragment != null)
                        pFragment.mCallback3.onPreviousTrack();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ApplicationConstants.ACTION_PAUSE));
                        }
                    }, 100);
                } catch (Exception e) {

                }

            }

            @Override
            public void onFastForward() {
                super.onFastForward();
                Log.d(ApplicationConstants.LOG_TAG, "onFastForward");
                if (pFragment != null)
                    pFragment.mCallback2.onComplete();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ApplicationConstants.ACTION_PAUSE));
                    }
                }, 100);
            }

            @Override
            public void onRewind() {
                super.onRewind();
                Log.d(ApplicationConstants.LOG_TAG, "onRewind");
                if (pFragment != null)
                    pFragment.mCallback3.onPreviousTrack();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ApplicationConstants.ACTION_PAUSE));
                    }
                }, 100);
            }

            @Override
            public void onStop() {
                super.onStop();
//                if (PlayerFragment.mMediaPlayer != null) {
//                    if (PlayerFragment.mMediaPlayer.isPlaying()) {
//                        buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ApplicationConstants.ACTION_PAUSE));
//                    } else {
//                        buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ApplicationConstants.ACTION_PLAY));
//                    }
//                }
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }

            @Override
            public void onSetRating(Rating rating) {
                super.onSetRating(rating);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onUnbind(Intent intent) {
        m_objMediaSession.release();
        return super.onUnbind(intent);
    }

    @Override
    public void onPlayPause() {
        if (PlayerFragment.mMediaPlayer.isPlaying()) {
            buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ApplicationConstants.ACTION_PAUSE));
        } else {
            buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ApplicationConstants.ACTION_PLAY));
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        try {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        } catch (Exception e) {

        }
    }
}