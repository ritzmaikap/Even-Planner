package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// dao means data access object
// this interface contains the database actions that can be performed on the events table
@Dao
public interface EventDao {

    // this method inserts a new event row into the events table
    @Insert
    void insert(Event event);

    // this method updates an already existing event row
    @Update
    void update(Event event);

    // this method deletes the selected event row from the database
    @Delete
    void delete(Event event);

    // this query reads all saved events from the database
    // order by id desc shows the latest inserted event near the top
    @Query("SELECT * FROM events ORDER BY id DESC")
    List<Event> getAllEvents();
}