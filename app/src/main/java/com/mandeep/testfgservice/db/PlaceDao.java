package com.mandeep.testfgservice.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaceDao {

    // this will replace if any data is available with same place
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Places places);

    @Query("SELECT * FROM places_table")
    List<Places> getAllPlaces();

}
