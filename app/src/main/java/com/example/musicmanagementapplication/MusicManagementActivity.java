package com.example.musicmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MusicManagementActivity extends BaseActivity  {
    private static final String MUSIC_TITLE_KEY = "music_title_key";
    private static final String MUSIC_ARTIST_KEY = "music_artist_key";

    private EditText musicTitle, musicArtist;
    private Button saveButton, deleteButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String musicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_management);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // what the line above does ???
        musicTitle = findViewById(R.id.musicTitle);
        musicArtist = findViewById(R.id.musicArtist);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        Button viewMusicListButton = findViewById(R.id.viewMusicListButton);

        Intent intent = getIntent();
        musicId = intent.getStringExtra("musicId");
        if (musicId != null) {
            loadMusicData(musicId);
            deleteButton.setVisibility(View.VISIBLE);
        }

        saveButton.setOnClickListener(view -> saveMusic());

        deleteButton.setOnClickListener(view -> {
            // Show confirmation dialog before deletion
            new AlertDialog.Builder(MusicManagementActivity.this)
                    .setTitle("Delete Music")
                    .setMessage("Are you sure you want to delete this music?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        // User confirmed deletion
                        deleteMusic(); // Call the method to delete music
                    })
                    .setNegativeButton(android.R.string.no, null) // User canceled, no action
                    .show();
        });

        viewMusicListButton.setOnClickListener(view -> {
            Intent listIntent = new Intent(MusicManagementActivity.this, MusicListActivity.class);
            startActivity(listIntent);
            finish();
        });

        if (savedInstanceState != null) {
            musicTitle.setText(savedInstanceState.getString(MUSIC_TITLE_KEY, ""));
            musicArtist.setText(savedInstanceState.getString(MUSIC_ARTIST_KEY, ""));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MUSIC_TITLE_KEY, musicTitle.getText().toString());
        outState.putString(MUSIC_ARTIST_KEY, musicArtist.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            musicTitle.setText(savedInstanceState.getString(MUSIC_TITLE_KEY, ""));
            musicArtist.setText(savedInstanceState.getString(MUSIC_ARTIST_KEY, ""));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }  // when navigate to different page,
//    it should be onStop(), but the logic here uses onPause when another activity comes to foreground

    private void loadMusicData(String musicId) {
        DocumentReference docRef = db.collection("music").document(mAuth.getCurrentUser().getUid())
                .collection("userMusic").document(musicId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            Music music = documentSnapshot.toObject(Music.class);
            if (music != null) {
                musicTitle.setText(music.title);
                musicArtist.setText(music.artist);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(MusicManagementActivity.this, "Failed to load music.", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Save a new music
     */
    private void saveMusic() {
        String title = musicTitle.getText().toString();
        String artist = musicArtist.getText().toString();

        if (title.isEmpty() || artist.isEmpty()) {
            Toast.makeText(this, "Title and Artist cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Music music = new Music(title, artist);
        if (musicId == null) {
            musicId = db.collection("music").document(mAuth.getCurrentUser().getUid())
                    .collection("userMusic").document().getId();
        }

        db.collection("music").document(mAuth.getCurrentUser().getUid())
                .collection("userMusic").document(musicId).set(music)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MusicManagementActivity.this, "Music saved.", Toast.LENGTH_SHORT).show();
//                    setResult(RESULT_OK);  // Set result to OK
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MusicManagementActivity.this, "Error saving music.", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteMusic() {
        if (musicId != null) {
            db.collection("music").document(mAuth.getCurrentUser().getUid())
                    .collection("userMusic").document(musicId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MusicManagementActivity.this, "Music deleted.", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MusicManagementActivity.this, "Error deleting music.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

}

