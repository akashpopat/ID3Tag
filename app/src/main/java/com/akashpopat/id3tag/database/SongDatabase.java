package com.akashpopat.id3tag.database;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by akash on 11/17/16.
 */
@Database(version = SongDatabase.VERSION)
final class SongDatabase {

    private SongDatabase(){}

    static final int VERSION = 1;

    @Table(SongColumns.class) public static final String SONGS = "songs";
}
