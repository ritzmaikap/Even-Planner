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

    // this stores the id of the event being edited
    // if it remains -1, it means a new event is being added
    int editEventId = -1;

    public AddEventFragment() {
        // empty public constructor required for fragment
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

        // listening for edit request sent from event list fragment
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

                    if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                        updateDateTimeText();
                    } else {
                        dateTimeText.setText("Select Date and Time");
                    }

                    addBtn.setText("Update Event");
                }
        );

        // opening date picker dialog
        pickDateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (datePicker, year, month, day) -> {
                        selectedDate = day + "/" + (month + 1) + "/" + year;
                        updateDateTimeText();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });

        // opening keyboard-based material time picker
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

        // adding a new event or updating an existing one
        addBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String category = categoryInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();

            if (title.isEmpty() || category.isEmpty() || location.isEmpty()
                    || selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(requireContext(), "fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Event event = new Event(title, category, location, selectedDate, selectedTime);

            // if editEventId is -1, this is a new event
            if (editEventId == -1) {
                db.eventDao().insert(event);
                Toast.makeText(requireContext(), "event added", Toast.LENGTH_SHORT).show();

                // keeping user on add event page after insert
                clearFields();
            } else {
                // if editEventId has a real id, update that row
                event.setId(editEventId);
                db.eventDao().update(event);
                Toast.makeText(requireContext(), "event updated", Toast.LENGTH_SHORT).show();

                clearFields();

                // after update, returning user to event list page
//                NavController navController = Navigation.findNavController(v);
//                navController.navigate(R.id.eventListFragment);
            }
        });
    }

    private void updateDateTimeText() {
        dateTimeText.setText(selectedDate + " " + selectedTime);
    }

    private void clearFields() {
        titleInput.setText("");
        categoryInput.setText("");
        locationInput.setText("");
        selectedDate = "";
        selectedTime = "";
        editEventId = -1;
        dateTimeText.setText("Select Date and Time");
        addBtn.setText("Add Event");
    }
}