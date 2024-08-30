package com.example.musicmanagementapplication;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

public class MusicListActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener  {
    private static final int MUSIC_MANAGEMENT_REQUEST_CODE = 1;
    private static final String QUERY_KEY = "query_key";

    private ListView musicListView;
    private MusicAdapter adapter;
    private List<Music> musicList;
    private List<String> musicKeys;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button addMusicButton, logoutButton, updateProfileButton ;

    private SearchView searchView;
    private String currentQuery = "";
    private Spinner sortSpinner;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        musicListView = findViewById(R.id.musicListView);
        addMusicButton = findViewById(R.id.addMusicButton);
        searchView = findViewById(R.id.searchView);
//        logoutButton = findViewById(R.id.logoutButton);
//        updateProfileButton = findViewById(R.id.updateProfileButton);

        musicList = new ArrayList<>();
        musicKeys = new ArrayList<>();
        adapter = new MusicAdapter(this, musicList, musicKeys);
//        adapter = new MusicAdapter(this, filteredMusicList, filteredKeys);
        sortSpinner = findViewById(R.id.sortSpinner);
        musicListView.setAdapter(adapter);

        // In your MainActivity or wherever the user should be able to update their profile

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set the navigation view listener
        navigationView.setNavigationItemSelectedListener(this);

        // Set up the toggle for opening and closing the drawer
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle the navigation button click
        ImageButton navButton = findViewById(R.id.nav_button);
        navButton.setOnClickListener(view -> drawerLayout.openDrawer(navigationView));

//        updateProfileButton.setOnClickListener(v -> {
//            Intent intent = new Intent(MusicListActivity.this, UpdateProfileActivity.class);
//            startActivity(intent);
//        });
//
//        logoutButton.setOnClickListener(view -> {
//            mAuth.signOut();
//            Intent intent = new Intent(MusicListActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();  // Close the current activity
//        });


        addMusicButton.setOnClickListener(view -> {
            Intent intent = new Intent(MusicListActivity.this, MusicManagementActivity.class);
            startActivityForResult(intent, MUSIC_MANAGEMENT_REQUEST_CODE);  // Use startActivityForResult instead of startActivity
//            startActivity(intent);
//            finish();  // this line really causes the problem
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMusic(newText);
                return false;
            }
        });

        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString(QUERY_KEY, "");
        }
        setupSortSpinner();
        loadMusic();
    }

    private void setupSortSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                if ("Sort by Title".equals(selectedItem)) {
                    sortByTitle();
                } else if ("Sort by Artist".equals(selectedItem)) {
                    sortByArtist();
                }
                filterMusic(searchView.getQuery().toString());  // Apply sorting to the filtered list
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    private void sortByTitle() {
        Collections.sort(musicList, Comparator.comparing(music -> music.getTitle().toLowerCase()));
    }

    private void sortByArtist() {
        Collections.sort(musicList, Comparator.comparing(music -> music.getArtist().toLowerCase()));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY_KEY, currentQuery);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString(QUERY_KEY, "");
            filterMusic(currentQuery);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadMusic();  // Reload music list every time the activity resumes
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("tag" , String.valueOf(requestCode));
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MUSIC_MANAGEMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            loadMusic();  // Reload music list when returning from MusicManagementActivity
        }
    }

    private void loadMusic() {
        db.collection("music").get();
        db.collection("music").document(mAuth.getCurrentUser().getUid())
                .collection("userMusic")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        musicList.clear();
                        musicKeys.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Music music = document.toObject(Music.class);
                            musicList.add(music);
                            musicKeys.add(document.getId());
                            Log.d("tag", String.valueOf(musicList.size()));
                        }
//                        filterMusic(searchView.getQuery().toString());
                        filterMusic(searchView.getQuery().toString());
                    } else {
                        Toast.makeText(MusicListActivity.this, "Failed to load music.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterMusic(String query) {
        List<Music> filteredMusicList = new ArrayList<>();
        List<String> filteredKeys = new ArrayList<>();

        if (query.isEmpty()) {
            filteredMusicList.addAll(musicList);
            filteredKeys.addAll(musicKeys);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (int i = 0; i < musicList.size(); i++) {
                Music music = musicList.get(i);
                if (music.title.toLowerCase().contains(lowerCaseQuery) || music.artist.toLowerCase().contains(lowerCaseQuery)) {
                    filteredMusicList.add(music);
                    filteredKeys.add(musicKeys.get(i));
                }
            }
        }
        adapter.updateData(filteredMusicList, filteredKeys);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int itemId = item.getItemId();
        if (itemId == R.id.nav_edit_profile) {
            Intent profileIntent = new Intent(MusicListActivity.this, UpdateProfileActivity.class);
            startActivity(profileIntent);
        } else if (itemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent logoutIntent = new Intent(MusicListActivity.this, LoginActivity.class);
            startActivity(logoutIntent);
            finish(); // Close MusicListActivity
        } else {
            Toast.makeText(this, "Unknown Option", Toast.LENGTH_SHORT).show();
        }
        drawerLayout.closeDrawer(navigationView); // Close drawer after selection
        return true;
    }
}
