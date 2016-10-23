package com.thomaskioko.podadddict.app.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.thomaskioko.podadddict.app.R;
import com.thomaskioko.podadddict.app.api.model.Item;
import com.thomaskioko.podadddict.app.interfaces.Listener;
import com.thomaskioko.podadddict.app.service.PlayerWidgetService;
import com.thomaskioko.podadddict.app.ui.fragments.PodCastEpisodesFragment;
import com.thomaskioko.podadddict.app.ui.views.ProgressBarCompat;
import com.thomaskioko.podadddict.app.util.ApplicationConstants;
import com.thomaskioko.podadddict.app.util.GoogleAnalyticsUtil;
import com.thomaskioko.podadddict.musicplayerlib.model.Track;
import com.thomaskioko.podadddict.musicplayerlib.player.PodAdddictPlayer;
import com.thomaskioko.podadddict.musicplayerlib.player.PodAdddictPlayerListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Activity used to display and play podcast episodes. This class also implements classes that
 * enable an episode to be played/streamed using {@link android.media.MediaPlayer} once selected.
 * <p>
 * This class also fetches podcast episodes and saves them locally adding offline viewing.
 *
 * @author kioko
 */
public class PodCastEpisodeActivity extends AppCompatActivity implements
        PodCastEpisodesFragment.EpisodeCallback, PodAdddictPlayerListener,
        SeekBar.OnSeekBarChangeListener, Listener {

    //Bind Views using {@link ButterKnife}
    @Bind(R.id.detail_relative_latout)
    FrameLayout mFrameLayout;
    @Bind(R.id.playback_view_artwork)
    ImageView mArtwork;
    @Bind(R.id.playback_view_track)
    TextView mTitle;
    @Bind(R.id.playback_view_current_time)
    TextView mCurrentTime;
    @Bind(R.id.playback_view_duration)
    TextView mDuration;
    @Bind(R.id.playback_view_toggle_play)
    ImageView mPlayPause;
    @Bind(R.id.playback_view_seekbar)
    SeekBar mSeekBar;
    @Bind(R.id.playback_view_loader)
    ProgressBarCompat mLoader;
    @Bind(R.id.fullPlayer)
    RelativeLayout mFullPlayerLayout;
    @Bind(R.id.player_album_art)
    ImageView mIvAlbumArt;
    @Bind(R.id.controller_close)
    ImageView mIvClose;
    @Bind(R.id.selected_track_image_sp)
    CircleImageView mImageArtWork;
    @Bind(R.id.selected_track_title_sp)
    TextView mTvArtistName;

    private boolean isFullPlayerShowing;
    private boolean mSeeking;
    private static PodAdddictPlayer mPodAdddictPlayer;
    public static final String LOG_TAG = PodCastEpisodeActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast_detail);
        ButterKnife.bind(this);


        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(PodCastEpisodesFragment.DETAIL_URI, getIntent().getData());
            PodCastEpisodesFragment fragment = new PodCastEpisodesFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.podcast_detail_container, fragment)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();

        }
        //Initialize the player
        mPodAdddictPlayer = new PodAdddictPlayer.Builder()
                .from(this)
                .notificationActivity(PodCastEpisodeActivity.class)
                .notificationIcon(R.drawable.ic_notification)
                .build();

        mSeekBar.setOnSeekBarChangeListener(this);

        // synchronize the player view with the current player (loaded track, playing state, etc.)
        synchronize(mPodAdddictPlayer);

    }

    /**
     * Method to handle click events using {#@link {@link ButterKnife}}
     *
     * @param views {@link View}
     */
    @OnClick({R.id.detail_relative_latout, R.id.controller_close})
    void onClickViews(View views) {
        switch (views.getId()) {
            case R.id.detail_relative_latout:
                if(isFullPlayerShowing){
                    isFullPlayerShowing = false;
                    mFullPlayerLayout.setVisibility(View.GONE);
                }else{
                    isFullPlayerShowing = true;
                    mFullPlayerLayout.setVisibility(VISIBLE);
                }
                break;
            case R.id.controller_close:
                mFullPlayerLayout.setVisibility(View.GONE);
                isFullPlayerShowing = false;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, PodCastListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPodAdddictPlayer.registerPlayerListener(this);
        mPodAdddictPlayer.registerPlayerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPodAdddictPlayer.unregisterPlayerListener(this);
        mPodAdddictPlayer.unregisterPlayerListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPodAdddictPlayer.destroy();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int[] secondMinute = getSecondMinutes(progress);
        mCurrentTime.setText(String.format(getResources().getString(R.string.playback_view_time), secondMinute[0], secondMinute[1]));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mSeeking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mSeeking = false;
        this.onSeekToRequested(seekBar.getProgress());
    }

    @Override
    public void onPlayerPlay(Track track, int position) {
        setTrack(track);
    }

    @Override
    public void onPlayerPause() {
        mPlayPause.setImageResource(R.drawable.ic_play_white);
        if (mPlayPause.getVisibility() == INVISIBLE) {
            mLoader.setVisibility(INVISIBLE);
            mPlayPause.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onPlayerSeekTo(int milli) {
        mSeekBar.setProgress(milli);
    }

    @Override
    public void onPlayerDestroyed() {
        mPlayPause.setImageResource(R.drawable.ic_play_white);
    }

    @Override
    public void onBufferingStarted() {
        mLoader.setVisibility(VISIBLE);
        mPlayPause.setVisibility(INVISIBLE);
    }

    @Override
    public void onBufferingEnded() {
        mLoader.setVisibility(INVISIBLE);
        mPlayPause.setVisibility(VISIBLE);
    }

    @Override
    public void onProgressChanged(int milli) {
        if (!mSeeking) {
            mSeekBar.setProgress(milli);
            int[] secondMinute = getSecondMinutes(milli);
            String duration = String.format(getResources().getString(R.string.playback_view_time),
                    secondMinute[0], secondMinute[1]);
            mCurrentTime.setText(duration);
        }
    }

    @Override
    public void onErrorOccurred() {
        mPlayPause.setImageResource(R.drawable.ic_play_white);
        if (mPlayPause.getVisibility() == INVISIBLE) {
            mLoader.setVisibility(INVISIBLE);
            mPlayPause.setVisibility(VISIBLE);
        }
    }


    @Override
    public void onEpisodeSelected(Uri uri, Item item) {

        mFrameLayout.setVisibility(VISIBLE);

        //Fetch episode details.
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                String imageUrl = cursor.getString(ApplicationConstants.COLUMN_SUBSCRIBED_PODCAST_FEED_IMAGE_URL);
                //create track object
                Track track = new Track();
                track.setTitle(item.getTitle());
                track.setStreamUrl(item.getEnclosure().getUrl());
                track.setArtist(item.getItunesAuthor());
                track.setArtworkUrl(imageUrl);
                track.setDurationInMilli(Long.parseLong(item.getItunesDuration()));

                setTrack(track);
            }
        }
    }


    /**
     * Helper method to populate the track on the player.
     *
     * @param track {@link Track} object
     */
    private void setTrack(Track track) {
        if (track == null) {
            mTitle.setText("");
            mArtwork.setImageDrawable(null);
            mPlayPause.setImageResource(R.drawable.ic_play_white);
            mSeekBar.setProgress(0);
            String none = String.format(getResources().getString(R.string.playback_view_time), 0, 0);
            mCurrentTime.setText(none);
            mDuration.setText(none);
        } else {

            isFullPlayerShowing = true;
            mFullPlayerLayout.setVisibility(VISIBLE);

            if (mPodAdddictPlayer.getTracks().contains(track)) {
                //TODO:: fix player issue
                // mPodAdddictPlayer.play(track);
            } else {
                boolean playNow = !mPodAdddictPlayer.isPlaying();
                mPodAdddictPlayer.addTrack(track, playNow);
            }

            //Log event to Google analytics
            GoogleAnalyticsUtil.trackEvent(
                    getResources().getString(R.string.action_category_stream),
                    getResources().getString(R.string.action_action_stream),
                    track.getTitle()
            );

            //Invoke service to update the widget.
            Intent active = new Intent(getApplicationContext(), PlayerWidgetService.class);
            active.setAction("ACTION_START_PLAYER");
            startService(active);

            //This will create a blur effect
            mArtwork.setColorFilter(new LightingColorFilter(0xff828282, 0x000000));

            //Load the art work
            Picasso.with(getApplicationContext())
                    .load(track.getArtworkUrl())
                    .fit()
                    .centerCrop()
                    .placeholder(R.color.placeholder)
                    .into(mImageArtWork);

            Picasso.with(getApplicationContext())
                    .load(track.getArtworkUrl())
                    .fit()
                    .centerCrop()
                    .placeholder(R.color.placeholder)
                    .into(mIvAlbumArt);

            mTitle.setText(track.getTitle());
            mPlayPause.setImageResource(R.drawable.ic_pause_white);
            mTvArtistName.setText(track.getArtist());

            mSeekBar.setMax(((int) track.getDurationInMilli()));
            String none = String.format(getResources().getString(R.string.playback_view_time), 0, 0);
            int[] secondMinute = getSecondMinutes(track.getDurationInMilli());
            String duration = String.format(getResources().getString(R.string.playback_view_time), secondMinute[0], secondMinute[1]);
            mCurrentTime.setText(none);
            mDuration.setText(duration);

        }
    }

    @OnClick({R.id.playback_view_next, R.id.playback_view_previous, R.id.playback_view_toggle_play})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playback_view_next:
                this.onNextPressed();
                break;
            case R.id.playback_view_previous:
                this.onPreviousPressed();
                break;
            case R.id.playback_view_toggle_play:
                this.onTogglePlayPressed();
                break;

        }
    }

    /**
     * Helper method that converts time in milliseconds to standard time hh:mm
     *
     * @param milli time in milliseconds
     * @return formatted time.
     */
    private int[] getSecondMinutes(long milli) {
        int inSeconds = (int) milli / 1000;
        return new int[]{inSeconds / 60, inSeconds % 60};
    }

    /**
     * Synchronize the player view with the current player state.
     * <p/>
     * Basically, check if a track is loaded as well as the playing state.
     *
     * @param player player currently used.
     */
    public void synchronize(PodAdddictPlayer player) {
        setTrack(player.getCurrentTrack());
        mLoader.setVisibility(INVISIBLE);
        mPlayPause.setVisibility(VISIBLE);
        setPlaying(player.isPlaying());
    }

    /**
     * Used to update the play/pause button.
     * <p/>
     * Should be synchronize with the player playing state.
     * See also : {@link PodAdddictPlayer#isPlaying()}.
     *
     * @param isPlaying true if a track is currently played.
     */
    private void setPlaying(boolean isPlaying) {
        if (isPlaying) {
            mPlayPause.setImageResource(R.drawable.ic_pause_white);
        } else {
            mPlayPause.setImageResource(R.drawable.ic_play_white);
        }
    }


    @Override
    public void onTogglePlayPressed() {
        mPodAdddictPlayer.togglePlayback();
    }

    @Override
    public void onPreviousPressed() {
        mPodAdddictPlayer.previous();
    }

    @Override
    public void onNextPressed() {
        mPodAdddictPlayer.next();
    }

    @Override
    public void onSeekToRequested(int milli) {
        mPodAdddictPlayer.seekTo(milli);
    }
}
