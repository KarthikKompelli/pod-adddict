package com.thomaskioko.podadddict.app.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the PodAdddict database.
 *
 * @author Thomas Kioko
 */

public class PodCastContract {

    public static final String CONTENT_AUTHORITY = "com.thomaskioko.podadddict";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible paths (appended to base content URI for possible URI's)
     * For instance, content://com.thomaskioko.podadddict/podCastFeed/ is a valid path for
     * looking at podCastFeed data.
     */
    public static final String PATH_PODCAST_FEED = "podCastFeed";
    public static final String PATH_PODCAST_FEED_PLAYLIST = "podCastFeedPlaylist";
    public static final String PATH_PODCAST_FEED_SUBSCRIBED = "podCastFeedSubscribed";


    /**
     * PodCastFeed class which defines the columns and URI's for the table
     */
    public static final class PodCastFeedEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PODCAST_FEED).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PODCAST_FEED;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PODCAST_FEED;

        public static final String TABLE_NAME = "podCastFeed";

        /**
         * Columns
         */
        public static final String COLUMN_PODCAST_FEED_ID = "feed_id";
        public static final String COLUMN_PODCAST_FEED_TITLE = "title";
        public static final String COLUMN_PODCAST_FEED_IMAGE_URL = "image_url";
        public static final String COLUMN_PODCAST_FEED_SUMMARY = "summary";
        public static final String COLUMN_PODCAST_FEED_ARTIST = "artist";
        public static final String COLUMN_PODCAST_FEED_CATEGORY = "category";

        /**
         * Helper method for building the ContentProvider query.
         *
         * @param id Id of the feed row int PodCastFeed table
         * @return URI
         */
        public static Uri buildPodCastFeedUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Helper method to build base URI
         * @return Uri
         */
        public static Uri buildPodCastFeedUri() {
            return CONTENT_URI.buildUpon().build();
        }

        /**
         * Helper method for building the ContentProvider query. It gets the feedId from the URI
         * content://com.thomaskioko.podadddict/podCastFeed/20167113
         *
         * @param uri {@link Uri}
         * @return FeedId
         */
        public static String getPodcastFeedIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    /**
     * PodCastFeedPlayList class which defines the columns and URI's for the table
     */
    public static final class PodCastFeedPlaylistEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PODCAST_FEED_PLAYLIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PODCAST_FEED_PLAYLIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PODCAST_FEED_PLAYLIST;

        // Table name
        public static final String TABLE_NAME = "podCastFeedPlaylist";

        /**
         * Columns
         */
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_ID = "podCast_feedId";
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_TRACK_ID = "track_id";
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_ARTIST_NAME = "artist_name";
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_TRACK_NAME = "track_name";
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_URL = "feed_url";
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_ART_WORK_URL_100 = "artwork_100";
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_ART_WORK_URL_600 = "artwork_600";
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_TRACK_COUNT = "track_count";
        public static final String COLUMN_PODCAST_FEED_PLAYLIST_GENRE = "genre_name";

        /**
         * Helper method for building the ContentProvider query.
         *
         * @param id id
         * @return URI
         */
        public static Uri buildPodCastFeedPlaylistUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Helper method for building the ContentProvider query.
         *
         * @param feedId PodCast Feed Id
         * @return URI
         */
        public static Uri buildPodCastFeedPlaylist(long feedId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(feedId)).build();
        }


        /**
         * Helper method for building the ContentProvider query.
         *
         * @param uri {@link Uri}
         * @return URI
         */
        public static String getFeedIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Helper method for getting the id of a record
         *
         * @param uri {@link Uri}
         * @return long id
         */
        public static long getPlaylistFeedIdFromUri(Uri uri) {
            String playlistFeedString = uri.getQueryParameter(COLUMN_PODCAST_FEED_PLAYLIST_ID);
            if (null != playlistFeedString && playlistFeedString.length() > 0)
                return Long.parseLong(playlistFeedString);
            else
                return 0;
        }
    }

    /**
     * Podcast Feed Subscription Columns and table .
     */
    public static final class PodcastFeedSubscriptionEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PODCAST_FEED_SUBSCRIBED).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PODCAST_FEED_SUBSCRIBED;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PODCAST_FEED_SUBSCRIBED;

        public static final String TABLE_NAME = "podCastFeedSubscription";

        public static final String COLUMN_SUBSCRIBED_PODCAST_FEED_ID = "feed_id";
        public static final String COLUMN_SUBSCRIBED_PODCAST_TRACK_ID = "track_id";
        public static final String COLUMN_SUBSCRIBED_PODCAST_ARTIST_NAME = "artist_name";
        public static final String COLUMN_SUBSCRIBED_PODCAST_TRACK_NAME = "track_name";
        public static final String COLUMN_SUBSCRIBED_PODCAST_URL = "feed_url";
        public static final String COLUMN_SUBSCRIBED_PODCAST_ART_WORK_URL_100 = "artwork_100";
        public static final String COLUMN_SUBSCRIBED_PODCAST_ART_WORK_URL_600 = "artwork_600";
        public static final String COLUMN_SUBSCRIBED_PODCAST_TRACK_COUNT = "track_count";
        public static final String COLUMN_SUBSCRIBED_PODCAST_GENRE = "genre_name";

        /**
         * Helper method for building the ContentProvider query.
         *
         * @param id id
         * @return URI
         */
        public static Uri buildSubscriptionUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * This method retrieves the Feed Id from the Uri
         *
         * @param uri Query Uri
         * @return Feed Id
         */
        public static String getPodcastFeedIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Helper method to build base URI
         * @return Uri
         */
        public static Uri buildSubscriptionUri() {
            return CONTENT_URI.buildUpon().build();
        }
    }

    /**
     * PodCastFeedPlayList class which defines the columns and URI's for the table
     */
    public static final class PodCastEpisodeEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PODCAST_FEED_PLAYLIST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PODCAST_FEED_PLAYLIST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PODCAST_FEED_PLAYLIST;

        // Table name
        public static final String TABLE_NAME = "podCastEpisode";

        /**
         * Columns
         */
        public static final String COLUMN_PODCAST_FEED_ID = "podCast_feedId";
        public static final String COLUMN_PODCAST_EPISODE_TITLE = "episode_title";
        public static final String COLUMN_PODCAST_EPISODE_AUTHOR = "episode_author";
        public static final String COLUMN_PODCAST_EPISODE_SUMMARY = "episode_summary";
        public static final String COLUMN_PODCAST_EPISODE_DURATION = "episode_duration";
        public static final String COLUMN_PODCAST_EPISODE_STREAM_URL = "episode_stream_url";
        public static final String COLUMN_PODCAST_EPISODE_PUBLISH_DATE = "episode_publish_date";

        /**
         * Helper method to build base URI
         * @return Uri
         */
        public static Uri buildPodCastEpisodeUri() {
            return CONTENT_URI.buildUpon().build();
        }
        /**
         * Helper method for building the ContentProvider query.
         *
         * @param id id
         * @return URI
         */
        public static Uri buildPodCastEpisodePlaylistUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /**
         * Helper method for building the ContentProvider query.
         *
         * @param feedId PodCast Feed Id
         * @return URI
         */
        public static Uri buildPodCastEpisode(long feedId) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(feedId)).build();
        }


        /**
         * Helper method for building the ContentProvider query.
         *
         * @param uri {@link Uri}
         * @return URI
         */
        public static String getEpsiodeIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Helper method for getting the id of a record
         *
         * @param uri {@link Uri}
         * @return long id
         */
        public static long getPlaylistFeedIdFromUri(Uri uri) {
            String playlistFeedString = uri.getQueryParameter(COLUMN_PODCAST_FEED_ID);
            if (null != playlistFeedString && playlistFeedString.length() > 0)
                return Long.parseLong(playlistFeedString);
            else
                return 0;
        }
    }

}
