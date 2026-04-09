package com.example.myapplication;

import android.app.DatePickerDialog;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

    // temporary storage for events (in-memory list)
    ArrayList<String> events;

    // adapter to display events in ListView
    EventAdapter adapter;

    // variables to hold selected date and time
    String selectedDate = "", selectedTime = "";

    // keeps track of selected item index for updating
    int selectedPosition = -1;

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

        // initializing event storage list
        events = new ArrayList<>();

        // setting up custom adapter for ListView
        adapter = new EventAdapter();
        eventList.setAdapter(adapter);

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

            String event = getEventText();

            if (!event.isEmpty()) {

                // if no item is selected, create new event
                if (selectedPosition == -1) {
                    events.add(event);
                }
                // if item is selected, update existing event
                else {
                    events.set(selectedPosition, event);
                    selectedPosition = -1;
                }

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

    // collects input data and validates fields
    private String getEventText() {

        String title = titleInput.getText().toString();
        String category = categoryInput.getText().toString();
        String location = locationInput.getText().toString();

        // checking if any required field is empty
        if (title.isEmpty() || category.isEmpty() || location.isEmpty()
                || selectedDate.isEmpty() || selectedTime.isEmpty()) {

            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return "";
        }

        // formatting event as a single string
        return title + "\n" + category + "\n" + location + "\n" + selectedDate + " " + selectedTime;
    }

    // clears input fields and resets date/time
    private void clearFields() {
        titleInput.setText("");
        categoryInput.setText("");
        locationInput.setText("");
        dateTimeText.setText("Select Date & Time");

        selectedDate = "";
        selectedTime = "";
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
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup parent) {

            // inflating custom layout for each row
            view = getLayoutInflater().inflate(R.layout.list_item, null);

            TextView text = view.findViewById(R.id.eventText);
            Button update = view.findViewById(R.id.updateBtn);
            Button delete = view.findViewById(R.id.deleteBtn);

            // setting event details to textview
            text.setText(events.get(i));

            // edit button loads selected event into input fields
            update.setOnClickListener(v -> {

                selectedPosition = i;

                String[] parts = events.get(i).split("\n");

                titleInput.setText(parts[0]);
                categoryInput.setText(parts[1]);
                locationInput.setText(parts[2]);

                String[] dt = parts[3].split(" ");
                selectedDate = dt[0];
                selectedTime = dt[1];

                updateDateTime();

                Toast.makeText(MainActivity.this, "Edit and press Add to update", Toast.LENGTH_SHORT).show();
            });

            // delete button removes selected event
            delete.setOnClickListener(v -> {
                events.remove(i);
                notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            });

            return view;
        }
    }
}