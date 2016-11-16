package com.akashpopat.id3tag;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


/**
 * An activity representing a list of Songs. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SongDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SongListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_MUSIC_ID = 9;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 123;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    SimpleItemRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        requestPermissions();

        if (findViewById(R.id.song_detail_container) != null) {
            mTwoPane = true;
        }
    }

    private void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL);
            }
        else
            restOfSetUp();
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    restOfSetUp();

                } else {
                    new AlertDialog.Builder(this).setMessage("Cant use app without permission!")
                            .setPositiveButton("Rety", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissions();
                                }
                            })
                            .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .show();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void restOfSetUp() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.song_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);
        setupAds();
        getSupportLoaderManager().initLoader(LOADER_MUSIC_ID, null, this);
    }

    private void setupAds() {
        MobileAds.initialize(getApplicationContext(),"ca-app-pub-8969848292746786~7300074754");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {

        mAdapter = new SimpleItemRecyclerViewAdapter(this,null);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if(data.getCount() == 0)
        {
            Snackbar snackbar = Snackbar
                    .make(((CoordinatorLayout)findViewById(R.id.coordLayout)), "No songs on Device :(", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        Cursor mCursor;
        Context mContext;

        public SimpleItemRecyclerViewAdapter(Activity context, Cursor cursor) {
            mCursor = cursor;
            mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.song_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            mCursor.moveToPosition(position);

            holder.mTitleTextView.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            holder.mArtistTextView.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

            String albumID =  mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            Cursor c = managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID+ "=?",
                    new String[] {String.valueOf(albumID)},
                    null);

            if (c.moveToFirst()) {
                String path = c.getString(c.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                Drawable img = Drawable.createFromPath(path);
                holder.mAlbumImage.setImageDrawable(img);
            }

            holder.mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCursor.moveToPosition(position);
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(SongDetailFragment.SONG_DATA, mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        SongDetailFragment fragment = new SongDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.song_detail_container, fragment)
                                .commit();
                    } else {
                        Intent intent = new Intent(mContext, SongDetailActivity.class);
                        intent.putExtra(SongDetailFragment.SONG_DATA, mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));

                        mContext.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mCursor == null) ? 0 : mCursor.getCount();
        }

        public void swapCursor(Cursor cursor) {
            if (mCursor == cursor) {
                return;
            }
            if (cursor != null) {
                mCursor = null;
                mCursor = cursor;
                mAdapter.notifyDataSetChanged();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final CardView mCard;
            final ImageView mAlbumImage;
            final TextView mArtistTextView;
            final TextView mTitleTextView;

            ViewHolder(View view) {
                super(view);
                mCard = (CardView) view.findViewById(R.id.cardView);
                mAlbumImage = (ImageView) view.findViewById(R.id.albumImage);
                mArtistTextView = (TextView) view.findViewById(R.id.artistText);
                mTitleTextView = (TextView) view.findViewById(R.id.titleText);
            }
        }
    }
}
