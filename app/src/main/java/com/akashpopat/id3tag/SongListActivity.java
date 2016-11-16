package com.akashpopat.id3tag;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Media;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.akashpopat.id3tag.R.id;
import com.akashpopat.id3tag.R.layout;
import com.akashpopat.id3tag.R.string;
import com.akashpopat.id3tag.SongListActivity.SimpleItemRecyclerViewAdapter.ViewHolder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;


/**
 * An activity representing a list of Songs. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SongDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SongListActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>{

    private static final int LOADER_MUSIC_ID = 9;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 123;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    SongListActivity.SimpleItemRecyclerViewAdapter mAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(layout.activity_song_list);

        Toolbar toolbar = (Toolbar) this.findViewById(id.toolbar);
        this.setSupportActionBar(toolbar);
        toolbar.setTitle(this.getTitle());

        // Obtain the FirebaseAnalytics instance.
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        this.requestPermissions();

        if (this.findViewById(id.song_detail_container) != null) {
            this.mTwoPane = true;
        }
    }

    private void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this,
                        new String[]{permission.WRITE_EXTERNAL_STORAGE},
                        SongListActivity.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL);
            }
        else
            this.restOfSetUp();
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SongListActivity.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    this.restOfSetUp();

                } else {
                    new AlertDialog.Builder(this).setMessage(string.permission_dialog_msg)
                            .setPositiveButton(string.retry_msg, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SongListActivity.this.requestPermissions();
                                }
                            })
                            .setNegativeButton(string.quit_msg, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SongListActivity.this.finish();
                                }
                            })
                            .show();

                }
                return;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void restOfSetUp() {
        RecyclerView recyclerView = (RecyclerView) this.findViewById(id.song_list);
        assert recyclerView != null;
        this.setupRecyclerView(recyclerView);
        this.setupAds();
        this.getSupportLoaderManager().initLoader(SongListActivity.LOADER_MUSIC_ID, null, this);
    }

    private void setupAds() {
        MobileAds.initialize(this.getApplicationContext(), this.getString(string.admob_adview_key));
        AdView mAdView = (AdView) this.findViewById(id.adView);
        AdRequest adRequest = new Builder()
                .build();
        mAdView.loadAd(adRequest);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {

        this.mAdapter = new SongListActivity.SimpleItemRecyclerViewAdapter(this,null);
        recyclerView.setAdapter(this.mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Media.EXTERNAL_CONTENT_URI,null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.mAdapter.swapCursor(data);
        if(data.getCount() == 0)
        {
            Snackbar snackbar = Snackbar
                    .make(this.findViewById(id.coordLayout), string.no_songs_msg, Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.mAdapter.swapCursor(null);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        Cursor mCursor;
        Context mContext;

        public SimpleItemRecyclerViewAdapter(Activity context, Cursor cursor) {
            this.mCursor = cursor;
            this.mContext = context;
        }

        @Override
        public SongListActivity.SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layout.song_list_content, parent, false);
            return new SongListActivity.SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SongListActivity.SimpleItemRecyclerViewAdapter.ViewHolder holder, final int position) {

            this.mCursor.moveToPosition(position);

            holder.mTitleTextView.setText(this.mCursor.getString(this.mCursor.getColumnIndex(Media.TITLE)));
            holder.mArtistTextView.setText(this.mCursor.getString(this.mCursor.getColumnIndex(Media.ARTIST)));

            String albumID = this.mCursor.getString(this.mCursor.getColumnIndex(Media.ALBUM_ID));
            Cursor c = SongListActivity.this.managedQuery(Albums.EXTERNAL_CONTENT_URI,
                    new String[] {Albums._ID, Albums.ALBUM_ART},
                    Albums._ID+ "=?",
                    new String[] {String.valueOf(albumID)},
                    null);

            if (c.moveToFirst()) {
                String path = c.getString(c.getColumnIndex(Albums.ALBUM_ART));
                Drawable img = Drawable.createFromPath(path);
                holder.mAlbumImage.setImageDrawable(img);
            }

            holder.mCard.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    SongListActivity.SimpleItemRecyclerViewAdapter.this.mCursor.moveToPosition(position);
                    if (SongListActivity.this.mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(SongDetailFragment.SONG_DATA, SongListActivity.SimpleItemRecyclerViewAdapter.this.mCursor.getString(SongListActivity.SimpleItemRecyclerViewAdapter.this.mCursor.getColumnIndex(Media.DATA)));
                        SongDetailFragment fragment = new SongDetailFragment();
                        fragment.setArguments(arguments);
                        SongListActivity.this.getSupportFragmentManager().beginTransaction()
                                .replace(id.song_detail_container, fragment)
                                .commit();
                    } else {
                        Intent intent = new Intent(SongListActivity.SimpleItemRecyclerViewAdapter.this.mContext, SongDetailActivity.class);
                        intent.putExtra(SongDetailFragment.SONG_DATA, SongListActivity.SimpleItemRecyclerViewAdapter.this.mCursor.getString(SongListActivity.SimpleItemRecyclerViewAdapter.this.mCursor.getColumnIndex(Media.DATA)));

                        SongListActivity.SimpleItemRecyclerViewAdapter.this.mContext.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.mCursor == null ? 0 : this.mCursor.getCount();
        }

        public void swapCursor(Cursor cursor) {
            if (this.mCursor == cursor) {
                return;
            }
            if (cursor != null) {
                this.mCursor = null;
                this.mCursor = cursor;
                SongListActivity.this.mAdapter.notifyDataSetChanged();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final CardView mCard;
            final ImageView mAlbumImage;
            final TextView mArtistTextView;
            final TextView mTitleTextView;

            ViewHolder(View view) {
                super(view);
                this.mCard = (CardView) view.findViewById(id.cardView);
                this.mAlbumImage = (ImageView) view.findViewById(id.albumImage);
                this.mArtistTextView = (TextView) view.findViewById(id.artistText);
                this.mTitleTextView = (TextView) view.findViewById(id.titleText);
            }
        }
    }
}
