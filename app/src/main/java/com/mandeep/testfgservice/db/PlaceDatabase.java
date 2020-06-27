package com.mandeep.testfgservice.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Places.class}, version = 1, exportSchema = false)
abstract public class PlaceDatabase extends RoomDatabase {

    private static PlaceDatabase instance;

    public abstract PlaceDao getPlaceDao();

    public static synchronized PlaceDatabase getInstance(Context context) {
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    PlaceDatabase.class, "place_database" )
                    .build();
        }
        return instance;
    }

}
