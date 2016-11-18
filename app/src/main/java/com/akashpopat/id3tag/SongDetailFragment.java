package com.akashpopat.id3tag;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog.Builder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.akashpopat.id3tag.R.id;
import com.akashpopat.id3tag.R.layout;
import com.akashpopat.id3tag.R.string;
import com.akashpopat.id3tag.database.SongColumns;
import com.akashpopat.id3tag.database.SongsProvider.Songs;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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

    public static final String ACTION_DATA_UPDATED =
            "com.akashpopat.id3tag.ACTION_DATA_UPDATED";

    String mTitle;
    String mArtist;
    String mLocation;
    Bitmap mArt;

    TextView mTitleTextView;
    TextView mArtistTextView;
    TextView mLocationTextView;
    ImageView mArtImageView;
    View rootView;

    Button searchButton;
    ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getArguments().containsKey(SongDetailFragment.SONG_DATA)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
//            mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            try {
                TagOptionSingleton.getInstance().setAndroid(true);
                AudioFile file = AudioFileIO.read(new File(this.getArguments().getString(SongDetailFragment.SONG_DATA)));
                this.mLocation = this.getArguments().getString(SongDetailFragment.SONG_DATA);
                this.mTitle = file.getTag().getFirst(FieldKey.TITLE);
                if(this.mTitle.equals("")){
                    this.mTitle = file.getFile().getName();
                }
                this.mArtist = file.getTag().getFirst(FieldKey.ARTIST);
                this.mArt = BitmapFactory.decodeByteArray(file.getTag().getFirstArtwork().getBinaryData(), 0, file.getTag().getFirstArtwork().getBinaryData().length);
            } catch (Exception e) {
                e.printStackTrace();
                this.mTitle = new File(this.getArguments().getString(SongDetailFragment.SONG_DATA)).getName();
                this.mLocation = this.getArguments().getString(SongDetailFragment.SONG_DATA);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(layout.song_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (this.mTitle != null) {
            this.mTitleTextView = (TextView) rootView.findViewById(id.titleDetailText);
            this.mTitleTextView.setText(this.mTitle);
            this.mArtistTextView = (TextView) rootView.findViewById(id.artistDetailText);
            this.mArtistTextView.setText(this.mArtist);
            this.mLocationTextView = (TextView) rootView.findViewById(id.locationDetailText);
            this.mLocationTextView.setText(this.mLocation);
            this.mArtImageView = (ImageView) rootView.findViewById(id.imageDetailArt);
            this.mArtImageView.setImageBitmap(this.mArt);
            this.searchButton = (Button) rootView.findViewById(id.searchButton);
            this.searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SongDetailFragment.this.searchOnline(SongDetailFragment.this.mTitle, SongDetailFragment.this.mArtist);
                }
            });
        }
        this.rootView = rootView;

        return rootView;
    }

    private void searchOnline(String mTitle, String mArtist) {
        this.mProgress = new ProgressDialog(this.mContext);
        this.mProgress.setMessage(this.getString(string.searching_dialog_msg));
        this.mProgress.setIndeterminate(true);
        this.mProgress.show();
        new getShazamInfo().execute(mTitle + " " + mArtist);
    }

    class getShazamInfo extends AsyncTask<String,Void,String[]> {

        String shazamBaseUrl = SongDetailFragment.this.getString(string.shazam_api_base_url);

        @Override
        protected void onPostExecute(String[] strings) {
            SongDetailFragment.this.mProgress.dismiss();
            if(strings == null)
            {
                Snackbar snackbar = Snackbar
                        .make(SongDetailFragment.this.rootView.findViewById(id.detailLinear), string.no_internet_snackbar_msg, Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
                return;
            }
            SongDetailFragment.this.postExecuteStuff(strings[0],strings[1],strings[2]);
        }

        @Override
        protected String[] doInBackground(String... params) {

            String source = null;

            final HttpUrl[] cookUrl = new HttpUrl[1];
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(new CookieJar() {
                        private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                            this.cookieStore.put(url, cookies);
                            if(url.toString().equals(SongDetailFragment.this.getString(string.shazam_website_link)))
                                cookUrl[0] = url;
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url) {
                            List<Cookie> cookies = this.cookieStore.get(cookUrl[0]);
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    })
                    .build();

            Request Frequest = new Request.Builder()
                    .url(SongDetailFragment.this.getString(string.shazam_website_link))
                    .build();

            try {
                Response r = client.newCall(Frequest).execute();
                if(!r.isSuccessful()){
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            Request request = new Request.Builder()
                    .url(this.shazamBaseUrl + params[0].replaceAll("[^A-Za-z0-9]", "").substring(0,20))
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                source = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("SongDetail", SongDetailFragment.this.getString(string.connection_error_log_msg));
            }
            String data[] = new String[3];
            if(source != null) {
                try {
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
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
            File file = null;
            try {
                file = new File(
                        SongDetailFragment.this.mContext.getCacheDir().getAbsolutePath()
                                + "/." + SongDetailFragment.this.mArtist + ".jpg");
            }catch (Exception e){
            }
            try {
                file.createNewFile();
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(CompressFormat.JPEG,100,ostream);
                ostream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private void postExecuteStuff(final String title, final String artist, String albumArt) {
        this.mTitleTextView.setText(title);
        this.mArtistTextView.setText(artist);

        Picasso.with(this.mContext).load(albumArt).into(this.mArtImageView);
        Picasso.with(this.mContext).load(albumArt).into(this.target);
        final File artFile = new File(
                this.mContext.getCacheDir().getAbsolutePath()
                        + "/." + this.mArtist + ".jpg");

        new Builder(this.mContext).setTitle(string.write_to_file_msg)
                .setMessage(getString(string.correct_info_msg) + title + getString(string.line_break) + getString(string.artist_msg) + artist)
                .setPositiveButton(string.yes_msg, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SongDetailFragment.this.mProgress.setTitle(SongDetailFragment.this.getString(string.writing_msg));
                        SongDetailFragment.this.mProgress.show();

                        try {

//                            Mp3File mp3file = new Mp3File(mLocation);
//                            ID3v2 id3v2Tag;
//                            if(mp3file.hasId3v1Tag())
//                                mp3file.removeId3v1Tag();
//                            if (mp3file.hasId3v2Tag()) {
//                                id3v2Tag = mp3file.getId3v2Tag();
//                            } else {
//                                // mp3 does not have an ID3v2 tag, let's create one..
//                                id3v2Tag = new ID3v24Tag();
//                                mp3file.setId3v2Tag(id3v2Tag);
//                            }
//                            id3v2Tag.setTitle(mTitle);
//                            id3v2Tag.setArtist(mArtist);
//                            id3v2Tag.setAlbum("ID3TAG_"+mArtist);
//                            Artwork artwork = ArtworkFactory.createArtworkFromFile(artFile);
//                            id3v2Tag.setAlbumImage(artwork.getBinaryData(),"image/jpg");
//                            mp3file.save(mLocation);

//                            MP3File f = new MP3File(new File(mLocation));
//                            MP3File f = (MP3File) AudioFileIO.read(new File(mLocation));

                            Tag tag;
                            if(SongDetailFragment.this.mLocation.endsWith("mp3")) {
                                MP3File f = new MP3File(new File(SongDetailFragment.this.mLocation));
                                tag = f.getTagAndConvertOrCreateAndSetDefault();
                                tagThem(tag);
//                            AudioFileIO.write(f);
                                f.commit();
                            }
                            else {
                                AudioFile f = AudioFileIO.read(new File(SongDetailFragment.this.mLocation));
                                tag = f.getTag();
                                tagThem(tag);
//                            AudioFileIO.write(f);
                                f.commit();
                            }
                        } catch (Exception e) {
                            new Builder(SongDetailFragment.this.mContext)
                                    .setTitle(string.data_not_written_msg)
                                    .setMessage(string.file_corrupted_msg)
                                    .setPositiveButton(string.ok_msg,null)
                                    .show();
                            e.printStackTrace();
                        }
                        SongDetailFragment.this.mProgress.dismiss();
                        Snackbar snackbar = Snackbar
                                .make(SongDetailFragment.this.rootView.findViewById(id.detailLinear), string.done_msg, Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        SongDetailFragment.scanFile(SongDetailFragment.this.mContext, SongDetailFragment.this.mLocation);
                        SongDetailFragment.this.addToDB(title,artist, SongDetailFragment.this.mLocation);
                    }

                    private void tagThem(Tag tag) throws FieldDataInvalidException, IOException {
                        try {
                            tag.deleteField(FieldKey.ARTIST);
                            tag.deleteField(FieldKey.TITLE);
                            tag.deleteField(FieldKey.ALBUM);
                        }catch (Exception ignored){}
                        tag.setField(FieldKey.ARTIST,artist);
                        tag.setField(FieldKey.TITLE,title);
                        tag.setField(FieldKey.ALBUM, SongDetailFragment.this.getString(string.ID3TAG_album_extra)+artist);
                        if(artFile.exists()) {
                            Artwork artwork = ArtworkFactory.createArtworkFromFile(artFile);
                            tag.addField(artwork);
                            tag.setField(artwork);
                        }
                    }
                })
                .setNegativeButton(string.no_msg,null)
                .show();


    }

    private void addToDB(String title, String artist, String mLocation) {

        ContentValues cv = new ContentValues();
        cv.put(SongColumns.TITLE,title);
        cv.put(SongColumns.ARTIST,artist);
        cv.put(SongColumns.DATA,mLocation);

        this.mContext.getContentResolver().insert(Songs.CONTENT_URI,cv);
        this.updateWidgets();
    }

    private void updateWidgets() {
        Context context = this.mContext;
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(SongDetailFragment.ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }


    public static void scanFile(Context context, String file) {
        MediaScannerConnection.scanFile(context,
                new String[]{file},
                null,
                null);

        new Builder(context).setTitle(string.media_refreshing_msg)
                .setMessage(string.restart_app_see_change_msg)
                .setPositiveButton( string.ok_msg,null)
                .show();
    }
}
