package com.akashpopat.id3tag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


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
    private Context mContext;

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

    TextView mTitleTextView;
    TextView mArtistTextView;
    TextView mLocationTextView;
    ImageView mArtImageView;

    Button searchButton;
    ProgressDialog mProgress;

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
                if(mTitle.equals("")){
                    mTitle = file.getFile().getName();
                }
                mArtist = file.getTag().getFirst(FieldKey.ARTIST);
                mArt  = BitmapFactory.decodeByteArray(file.getTag().getFirstArtwork().getBinaryData(), 0, file.getTag().getFirstArtwork().getBinaryData().length);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Activity activity = this.getActivity();
//            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
//            if (appBarLayout != null) {
//                appBarLayout.setTitle(mTitle);
//            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.song_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mTitle != null) {
            mTitleTextView = (TextView) rootView.findViewById(R.id.titleDetailText);
            mTitleTextView.setText(mTitle);
            mArtistTextView = (TextView) rootView.findViewById(R.id.artistDetailText);
            mArtistTextView.setText(mArtist);
            mLocationTextView = (TextView) rootView.findViewById(R.id.locationDetailText);
            mArtImageView = (ImageView) rootView.findViewById(R.id.imageDetailArt);
            mArtImageView.setImageBitmap(mArt);
            searchButton = (Button) rootView.findViewById(R.id.searchButton);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchOnline(mTitle,mArtist);
                }
            });
        }

        return rootView;
    }

    private void searchOnline(String mTitle, String mArtist) {
        mProgress = new ProgressDialog(mContext);
        mProgress.setTitle("Searching!");
        mProgress.setIndeterminate(true);
        mProgress.show();
        new getShazamInfo().execute(mTitle + " " + mArtist);
    }

    class getShazamInfo extends AsyncTask<String,Void,String[]> {

        String shazamBaseUrl = "http://www.shazam.com/fragment/search/";
        String fileName;

        @Override
        protected void onPostExecute(String[] strings) {
            postExecuteStuff(strings[0],strings[1],strings[2]);
        }

        @Override
        protected String[] doInBackground(String... params) {

            fileName = params[0].replaceAll("%20", " ");
            Log.d("hey","searchin shazam "+ params[0].substring(0,10));
            String source = null;

            final HttpUrl[] cookUrl = new HttpUrl[1];
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new CookieJar() {
                        private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            cookieStore.put(url, cookies);
                            if(url.toString().equals("http://www.shazam.com/"))
                                cookUrl[0] = url;
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            List<Cookie> cookies = cookieStore.get(cookUrl[0]);
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    })
                    .build();

            Request Frequest = new Request.Builder()
                    .url("http://www.shazam.com")
                    .build();

            try {
                client.newCall(Frequest).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Request request = new Request.Builder()
                    .url(shazamBaseUrl + params[0].substring(0,10))
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                source = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String data[] = new String[3];
            if(source != null) {
                try {
                    Log.d("hey",source);
                    JSONObject sourceObj = new JSONObject(source);

                    JSONObject trackResults = sourceObj.getJSONObject("tracksresult");
                    JSONArray tracks = trackResults.getJSONArray("tracks");


                        JSONObject obj = tracks.getJSONObject(0);
                        data[0] = obj.getString("trackName");
                        data[1] = obj.getString("artist");
                        data[2] = obj.getString("image400");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return data;
        }
    }

    private void postExecuteStuff(String title, String artist, String albumArt) {
        mProgress.dismiss();
        mTitleTextView.setText(title);
        mArtistTextView.setText(artist);

        Picasso.with(mContext).load(albumArt).into(mArtImageView);

    }
}
