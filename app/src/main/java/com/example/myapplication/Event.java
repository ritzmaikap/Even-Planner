package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// this class represents one event record that will be stored inside the room database table
// every object of this class becomes one row in the "events" table
@Entity(tableName = "events")
public class Event {

    // this is the unique identifier for each event
    // room automatically generates the id value whenever a new event is inserted
    @PrimaryKey(autoGenerate = true)
    private int id;

    // these fields represent the actual data columns stored for each event
    private String title;
    private String category;
    private String location;
    private String date;
    private String time;

    // this constructor is used when creating a new event before saving it into the database
    public Event(String title, String category, String location, String date, String time) {
        this.title = title;
        this.category = category;
        this.location = location;
        this.date = date;
        this.time = time;
    }

    // getter used to read the generated id value
    public int getId() {
        return id;
    }

    // setter used by room when the id value needs to be assigned or updated
    public void setId(int id) {
        this.id = id;
    }

    // getters used to read each stored field value
    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    // setters used when an existing event is edited before calling the update query
    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }
}