package com.example.myapplication;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// this class acts as the main database holder for the app
// it connects the Event entity with the EventDao so room knows what table and operations exist
@Database(entities = {Event.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // this method gives access to the dao methods such as insert, update, delete, and fetch
    public abstract EventDao eventDao();
}