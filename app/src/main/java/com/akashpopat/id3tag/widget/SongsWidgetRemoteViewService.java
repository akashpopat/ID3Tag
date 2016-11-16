package com.akashpopat.id3tag.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.akashpopat.id3tag.R;
import com.akashpopat.id3tag.R.id;
import com.akashpopat.id3tag.R.layout;
import com.akashpopat.id3tag.database.SongColumns;
import com.akashpopat.id3tag.database.SongsProvider;
import com.akashpopat.id3tag.database.SongsProvider.Songs;

/**
 * Created by akash on 11/17/16.
 */

public class SongsWidgetRemoteViewService extends RemoteViewsService {

    public final String LOG_TAG = SongsWidgetRemoteViewService.class.getSimpleName();
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsService.RemoteViewsFactory() {
            private Cursor data;
            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (this.data != null) {
                    this.data.close();
                }
                this.data = SongsWidgetRemoteViewService.this.getContentResolver().query(Songs.CONTENT_URI,
                        new String[] {"Distinct "+ SongColumns.TITLE,SongColumns.ARTIST,SongColumns.DATA,SongColumns._ID},
                        null,
                        null,
                        null);
            }

            @Override
            public void onDestroy() {
                if (this.data != null) {
                    this.data.close();
                    this.data = null;
                }
            }

            @Override
            public int getCount() {
                return this.data == null ? 0 : this.data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        this.data == null || !this.data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(SongsWidgetRemoteViewService.this.getPackageName(),
                        layout.songs_widget_list);


                if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    //TODO
//                    setRemoteContentDescription(views, description);
                }
                views.setTextViewText(id.songTitleTextView, this.data.getString(0));
                views.setTextViewText(id.songArtistTextView, this.data.getString(1));

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(SongsWidgetRemoteViewService.this.getPackageName(), layout.songs_widget_list);
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
