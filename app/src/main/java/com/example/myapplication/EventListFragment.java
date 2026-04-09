package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class EventListFragment extends Fragment {

    ListView eventList;
    ArrayList<Event> events;
    EventAdapter adapter;
    AppDatabase db;

    public EventListFragment() {
        // empty public constructor required for fragment
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventList = view.findViewById(R.id.eventList);

        db = Room.databaseBuilder(requireContext(),
                        AppDatabase.class, "event_database")
                .allowMainThreadQueries()
                .build();

        events = new ArrayList<>();
        adapter = new EventAdapter();
        eventList.setAdapter(adapter);

        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();

        // loading again whenever user returns to this screen
        loadEvents();
        adapter.notifyDataSetChanged();
    }

    private void loadEvents() {
        List<Event> savedEvents = db.eventDao().getAllEvents();
        events.clear();
        events.addAll(savedEvents);
    }

    class EventAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return events.size();
        }

        @Override
        public Object getItem(int position) {
            return events.get(position);
        }

        @Override
        public long getItemId(int position) {
            return events.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = getLayoutInflater().inflate(R.layout.list_item, null);

            TextView eventText = view.findViewById(R.id.eventText);
            Button updateBtn = view.findViewById(R.id.updateBtn);
            Button deleteBtn = view.findViewById(R.id.deleteBtn);

            Event event = events.get(position);

            String text = event.getTitle() + "\n"
                    + event.getCategory() + "\n"
                    + event.getLocation() + "\n"
                    + event.getDate() + " " + event.getTime();

            eventText.setText(text);

            // sending selected event data to add event fragment for edit mode
            updateBtn.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("id", event.getId());
                bundle.putString("title", event.getTitle());
                bundle.putString("category", event.getCategory());
                bundle.putString("location", event.getLocation());
                bundle.putString("date", event.getDate());
                bundle.putString("time", event.getTime());

                getParentFragmentManager().setFragmentResult("edit_event_request", bundle);

                BottomNavigationView bottomNav =
                        requireActivity().findViewById(R.id.bottomNavigationView);

                bottomNav.setSelectedItemId(R.id.addEventFragment);
            });

            // deleting selected event from room database
            deleteBtn.setOnClickListener(v -> {
                db.eventDao().delete(event);
                loadEvents();
                notifyDataSetChanged();
                Toast.makeText(requireContext(), "event deleted", Toast.LENGTH_SHORT).show();
            });

            return view;
        }
    }
}