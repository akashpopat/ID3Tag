package com.akashpopat.id3tag.database;


import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.DataType.Type;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by akash on 11/17/16.
 */

public class SongColumns {

    @DataType(Type.INTEGER) @PrimaryKey
    public static final String _ID = "_id";
    public static final int COL_ID = 0;

    @DataType(Type.TEXT) @NotNull
    public static final String TITLE = "title";
    public static final int COL_TITLE = 1;

    @DataType(Type.TEXT) @NotNull
    public static final String ARTIST = "artist";
    public static final int COL_ARTIST = 2;

    @DataType(Type.TEXT) @NotNull
    public static final String DATA = "data";
    public static final int COL_DATA = 3;
}
