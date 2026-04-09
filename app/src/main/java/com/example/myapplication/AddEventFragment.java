package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.Locale;

public class AddEventFragment extends Fragment {

    EditText titleInput, categoryInput, locationInput;
    TextView dateTimeText;
    Button pickDateBtn, pickTimeBtn, addBtn;

    String selectedDate = "";
    String selectedTime = "";

    AppDatabase db;
    int editEventId = -1;

    // this calendar stores the selected event date so it can be compared with today's date
    Calendar selectedCalendar = null;

    public AddEventFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleInput = view.findViewById(R.id.titleInput);
        categoryInput = view.findViewById(R.id.categoryInput);
        locationInput = view.findViewById(R.id.locationInput);
        dateTimeText = view.findViewById(R.id.dateTimeText);
        pickDateBtn = view.findViewById(R.id.pickDateBtn);
        pickTimeBtn = view.findViewById(R.id.pickTimeBtn);
        addBtn = view.findViewById(R.id.addBtn);

        db = Room.databaseBuilder(requireContext(),
                        AppDatabase.class, "event_database")
                .allowMainThreadQueries()
                .build();

        // receiving old event data when user clicks edit from event list
        getParentFragmentManager().setFragmentResultListener(
                "edit_event_request",
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    editEventId = bundle.getInt("id", -1);

                    String title = bundle.getString("title", "");
                    String category = bundle.getString("category", "");
                    String location = bundle.getString("location", "");
                    selectedDate = bundle.getString("date", "");
                    selectedTime = bundle.getString("time", "");

                    titleInput.setText(title);
                    categoryInput.setText(category);
                    locationInput.setText(location);

                    // rebuilding calendar object from the old date string
                    if (!selectedDate.isEmpty()) {
                        String[] parts = selectedDate.split("/");
                        if (parts.length == 3) {
                            int day = Integer.parseInt(parts[0]);
                            int month = Integer.parseInt(parts[1]) - 1;
                            int year = Integer.parseInt(parts[2]);

                            selectedCalendar = Calendar.getInstance();
                            selectedCalendar.set(year, month, day, 0, 0, 0);
                            selectedCalendar.set(Calendar.MILLISECOND, 0);
                        }
                    }

                    if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                        updateDateTimeText();
                    } else {
                        dateTimeText.setText("Select Date and Time");
                    }

                    addBtn.setText("Update Event");
                }
        );

        // date picker
        pickDateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (datePicker, year, month, day) -> {
                        selectedDate = day + "/" + (month + 1) + "/" + year;

                        // storing selected date in calendar for later past-date validation
                        selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, day, 0, 0, 0);
                        selectedCalendar.set(Calendar.MILLISECOND, 0);

                        updateDateTimeText();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });

        // keyboard based time picker
        pickTimeBtn.setOnClickListener(v -> {
            MaterialTimePicker picker =
                    new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .setHour(12)
                            .setMinute(0)
                            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                            .setTitleText("Enter Time")
                            .build();

            picker.show(getParentFragmentManager(), "TIME_PICKER");

            picker.addOnPositiveButtonClickListener(dialog -> {
                int hour = picker.getHour();
                int minute = picker.getMinute();

                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                updateDateTimeText();
            });
        });

        addBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();

            // input validation for title
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // input validation for date
            if (selectedDate.isEmpty()) {
                Toast.makeText(requireContext(), "date cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // optional extra check to keep the whole form safe
            if (category.isEmpty() || location.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(requireContext(), "fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // logic validation to block past dates only for new events
            if (editEventId == -1 && isPastDate()) {
                Toast.makeText(requireContext(), "past dates are not allowed for new events", Toast.LENGTH_SHORT).show();
                return;
            }

            Event event = new Event(title, category, location, selectedDate, selectedTime);

            if (editEventId == -1) {
                db.eventDao().insert(event);
                Toast.makeText(requireContext(), "event added successfully", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                event.setId(editEventId);
                db.eventDao().update(event);
                Toast.makeText(requireContext(), "event updated successfully", Toast.LENGTH_SHORT).show();
                clearFields();

                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.eventListFragment);
            }
        });
    }

    private void updateDateTimeText() {
        dateTimeText.setText(selectedDate + " " + selectedTime);
    }

    // this method compares the selected date with today's date
    // it ignores time and checks date only
    private boolean isPastDate() {
        if (selectedCalendar == null) {
            return false;
        }

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return selectedCalendar.before(today);
    }

    private void clearFields() {
        titleInput.setText("");
        categoryInput.setText("");
        locationInput.setText("");
        selectedDate = "";
        selectedTime = "";
        selectedCalendar = null;
        editEventId = -1;
        dateTimeText.setText("Select Date and Time");
        addBtn.setText("Add Event");
    }
}