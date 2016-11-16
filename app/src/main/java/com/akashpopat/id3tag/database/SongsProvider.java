package com.akashpopat.id3tag.database;

import android.net.Uri;
import android.net.Uri.Builder;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by akash on 11/17/16.
 */

@ContentProvider(authority = SongsProvider.AUTHORITY, database = SongDatabase.class)
public class SongsProvider {

    public static final String AUTHORITY = "com.akashpopat.id3tag";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + SongsProvider.AUTHORITY);

    interface Path{
        String SONGS = "songs";
    }

    private static Uri buildUri(String ... paths){
        Builder builder = SongsProvider.BASE_CONTENT_URI.buildUpon();
        for (String path : paths){
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = SongDatabase.SONGS) public static class Songs{
        @ContentUri(
                path = SongsProvider.Path.SONGS,
                type = "vnd.android.cursor.dir/songs")
        public static final Uri CONTENT_URI = SongsProvider.buildUri(SongsProvider.Path.SONGS);

        @InexactContentUri(
                name = "SONGS_ID",
                path = SongsProvider.Path.SONGS + "/#",
                type = "vnd.android.cursor.item/planet",
                whereColumn = SongColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id){
            return SongsProvider.buildUri(SongsProvider.Path.SONGS, String.valueOf(id));
        }
    }
}

