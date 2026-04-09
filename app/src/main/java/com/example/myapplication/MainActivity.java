package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// this activity only acts as the host for the fragments
// the actual screens are shown inside the fragment container using the navigation component
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setting the main layout which contains the nav host fragment and bottom navigation
        setContentView(R.layout.activity_main);

        // getting the nav host fragment from the layout
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        // getting the nav controller from the nav host fragment
        NavController navController = navHostFragment.getNavController();

        // connecting bottom navigation with the nav controller
        // this automatically switches between add event and event list fragments
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }
}