package com.akashpopat.id3tag.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.akashpopat.id3tag.R;
import com.akashpopat.id3tag.database.SongColumns;
import com.akashpopat.id3tag.database.SongsProvider;

/**
 * Created by akash on 11/17/16.
 */

public class SongsWidgetRemoteViewService extends RemoteViewsService {

    public final String LOG_TAG = SongsWidgetRemoteViewService.class.getSimpleName();
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                data = getContentResolver().query(SongsProvider.Songs.CONTENT_URI,
                        new String[] {"Distinct "+ SongColumns.TITLE,SongColumns.ARTIST,SongColumns.DATA,SongColumns._ID},
                        null,
                        null,
                        null);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.songs_widget_list);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    //TODO
//                    setRemoteContentDescription(views, description);
                }
                views.setTextViewText(R.id.songTitleTextView, data.getString(0));
                views.setTextViewText(R.id.songArtistTextView, data.getString(1));

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.songs_widget_list);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
