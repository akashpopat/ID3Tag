package com.akashpopat.id3tag;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;


/**
 * A fragment representing a single Song detail screen.
 * This fragment is either contained in a {@link SongListActivity}
 * in two-pane mode (on tablets) or a {@link SongDetailActivity}
 * on handsets.
 */
public class SongDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String SONG_DATA = "song_data";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SongDetailFragment() {
    }

    String mTitle;
    String mArtist;
    String mLocation;
    Bitmap mArt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(SONG_DATA)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
//            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            try {
                AudioFile file = new AudioFileIO().readFile(new File(getArguments().getString(SONG_DATA)));
                mLocation = getArguments().getString(SONG_DATA);
                mTitle = file.getTag().getFirst(FieldKey.TITLE);
                mArtist = file.getTag().getFirst(FieldKey.ARTIST);
                mArt  = BitmapFactory.decodeByteArray(file.getTag().getFirstArtwork().getBinaryData(), 0, file.getTag().getFirstArtwork().getBinaryData().length);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mTitle);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.song_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mTitle != null) {
            ((TextView) rootView.findViewById(R.id.titleDetailText)).setText(mTitle);
            ((TextView) rootView.findViewById(R.id.artistDetailText)).setText(mArtist);
            ((TextView) rootView.findViewById(R.id.locationDetailText)).setText(mLocation);
            ((ImageView) rootView.findViewById(R.id.imageDetailArt)).setImageBitmap(mArt);

        }

        return rootView;
    }
}
