package com.akashpopat.id3tag.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.akashpopat.id3tag.R;
import com.akashpopat.id3tag.R.id;
import com.akashpopat.id3tag.R.layout;
import com.akashpopat.id3tag.SongDetailFragment;
import com.akashpopat.id3tag.SongListActivity;

/**
 * Created by akash on 11/17/16.
 */

public class SongWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {


            Intent intent = new Intent(context, SongsWidgetRemoteViewService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);


            RemoteViews views = new RemoteViews(context.getPackageName(), layout.songs_widget);
            views.setRemoteAdapter(id.widget_list,intent);
            views.setEmptyView(id.widget_list, id.widget_empty);


            // Create an Intent to launch MainActivity
            Intent pIntent = new Intent(context, SongListActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, pIntent, 0);
            views.setOnClickPendingIntent(id.widget, pendingIntent);



            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(SongDetailFragment.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, this.getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, id.widget_list);
        }
    }
}
