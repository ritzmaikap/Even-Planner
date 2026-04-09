package com.example.myapplication;

import android.app.DatePickerDialog;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    // UI components connected from XML layout
    EditText titleInput, categoryInput, locationInput;
    TextView dateTimeText;
    Button addBtn, pickDateBtn, pickTimeBtn;
    ListView eventList;

    // this list is still used by the adapter, but now it temporarily holds Event objects loaded from room
    ArrayList<Event> events;

    // adapter to display events in ListView
    EventAdapter adapter;

    // variables to hold selected date and time
    String selectedDate = "", selectedTime = "";

    // keeps track of selected item index for updating
    int selectedPosition = -1;

    // this stores the currently selected event object during edit mode
    // it helps identify which database row should be updated when the user edits an event
    Event selectedEvent = null;

    // this is the room database reference used to perform insert, update, delete, and fetch operations
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // connecting UI elements with their IDs
        titleInput = findViewById(R.id.titleInput);
        categoryInput = findViewById(R.id.categoryInput);
        locationInput = findViewById(R.id.locationInput);
        dateTimeText = findViewById(R.id.dateTimeText);

        addBtn = findViewById(R.id.addBtn);
        pickDateBtn = findViewById(R.id.pickDateBtn);
        pickTimeBtn = findViewById(R.id.pickTimeBtn);
        eventList = findViewById(R.id.eventList);

        // creating the room database object
        // "event_database" is the local database file name that will be stored on the device
        // allowMainThreadQueries is used here to keep the assignment easier to understand and implement
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "event_database")
                .allowMainThreadQueries()
                .build();

        // initializing event storage list
        events = new ArrayList<>();

        // setting up custom adapter for ListView
        adapter = new EventAdapter();
        eventList.setAdapter(adapter);

        // loading previously saved events from room when the app opens
        // this makes the saved data appear again even after closing and reopening the app
        loadEvents();

        // date picker dialog to select event date
        pickDateBtn.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = d + "/" + (m + 1) + "/" + y;
                updateDateTime();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // time picker dialog to select event time
        pickTimeBtn.setOnClickListener(v -> {

            MaterialTimePicker picker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(13)
                            .setMinute(48)
                            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                            .setTitleText("Set time")
                            .build();

            picker.show(getSupportFragmentManager(), "TIME_PICKER");

            picker.addOnPositiveButtonClickListener(view -> {
                int hour = picker.getHour();
                int minute = picker.getMinute();

                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                updateDateTime();
            });
        });

        // handling add and update functionality using same button
        addBtn.setOnClickListener(v -> {

            Event event = getEventObject();

            if (event != null) {

                // if no item is selected, create new event
                if (selectedPosition == -1) {
                    // inserting the new event into room so that it remains saved permanently
                    db.eventDao().insert(event);
                }
                // if item is selected, update existing event
                else {
                    // copying the edited field values into the selected existing event object
                    // this keeps the same database row id and only changes the edited content
                    selectedEvent.setTitle(event.getTitle());
                    selectedEvent.setCategory(event.getCategory());
                    selectedEvent.setLocation(event.getLocation());
                    selectedEvent.setDate(event.getDate());
                    selectedEvent.setTime(event.getTime());

                    // updating the edited event inside room database
                    db.eventDao().update(selectedEvent);

                    selectedPosition = -1;

                    // clearing the selected event reference because update mode is finished
                    selectedEvent = null;
                }

                // reading all latest data again from room after insert or update
                // this refreshes the list so the screen shows the newest saved data
                loadEvents();

                // refresh ListView after data change
                adapter.notifyDataSetChanged();

                // clear input fields after operation
                clearFields();
            }
        });
    }

    // updates the date and time display text
    private void updateDateTime() {
        dateTimeText.setText(selectedDate + " " + selectedTime);
    }

    // this method creates an Event object instead of a single formatted string
    // room needs an object so it can store each field in separate database columns
    private Event getEventObject() {

        String title = titleInput.getText().toString();
        String category = categoryInput.getText().toString();
        String location = locationInput.getText().toString();

        // checking if any required field is empty
        if (title.isEmpty() || category.isEmpty() || location.isEmpty()
                || selectedDate.isEmpty() || selectedTime.isEmpty()) {

            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return null;
        }

        // creating and returning a proper Event object with all the entered values
        return new Event(title, category, location, selectedDate, selectedTime);
    }

    // this method fetches all events from the room database
    // the adapter list is cleared and filled again so the latest saved database data is shown
    private void loadEvents() {
        List<Event> savedEvents = db.eventDao().getAllEvents();
        events.clear();
        events.addAll(savedEvents);
    }

    // clears input fields and resets date/time
    private void clearFields() {
        titleInput.setText("");
        categoryInput.setText("");
        locationInput.setText("");
        dateTimeText.setText("Select Date & Time");

        selectedDate = "";
        selectedTime = "";

        // clearing this reference ensures the next button press behaves like a fresh add operation
        selectedEvent = null;
    }

    // custom adapter for displaying events in ListView
    class EventAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public Object getItem(int i) {
            return events.get(i);
        }

        @Override
        public long getItemId(int i) {
            // returning the real database id makes each row correspond to its stored room record
            return events.get(i).getId();
        }

        @Override
        public View getView(int i, View view, ViewGroup parent) {

            // inflating custom layout for each row
            view = getLayoutInflater().inflate(R.layout.list_item, null);

            TextView text = view.findViewById(R.id.eventText);
            Button update = view.findViewById(R.id.updateBtn);
            Button delete = view.findViewById(R.id.deleteBtn);

            // converting the Event object into the same multi-line text format used in the list row
            // this keeps the display style similar to the original version of the app
            Event event = events.get(i);
            String eventText = event.getTitle() + "\n"
                    + event.getCategory() + "\n"
                    + event.getLocation() + "\n"
                    + event.getDate() + " " + event.getTime();

            // setting event details to textview
            text.setText(eventText);

            // edit button loads selected event into input fields
            update.setOnClickListener(v -> {

                selectedPosition = i;

                // storing the selected object so the correct database row can be updated later
                selectedEvent = events.get(i);

                // filling the input fields directly from the selected event object fields
                // this avoids splitting a text string and is more reliable for database-backed data
                titleInput.setText(selectedEvent.getTitle());
                categoryInput.setText(selectedEvent.getCategory());
                locationInput.setText(selectedEvent.getLocation());

                selectedDate = selectedEvent.getDate();
                selectedTime = selectedEvent.getTime();

                updateDateTime();

                Toast.makeText(MainActivity.this, "Edit and press Add to update", Toast.LENGTH_SHORT).show();
            });

            // delete button removes selected event
            delete.setOnClickListener(v -> {
                // deleting the selected event from room removes it permanently from local storage
                db.eventDao().delete(events.get(i));

                // loading the fresh remaining data again after deletion
                loadEvents();

                notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            });

            return view;
        }
    }
}