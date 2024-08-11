package com.example.musicmanagementapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateProfileActivity extends BaseActivity {
    private static final String EMAIL_TEXT = "email_text";
    private static final String PHONE_TEXT = "phone_text";

    private EditText emailEditText;
    private EditText phoneEditText;
    private Button updateButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView customBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.updateEmailEditText);
        phoneEditText = findViewById(R.id.updatePhoneEditText);
        updateButton = findViewById(R.id.updateButton);
        customBackButton = findViewById(R.id.customBackButton);

        updateButton.setOnClickListener(v -> updateProfile());

        customBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (savedInstanceState != null) {
            emailEditText.setText(savedInstanceState.getString(EMAIL_TEXT, ""));
            phoneEditText.setText(savedInstanceState.getString(PHONE_TEXT, ""));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EMAIL_TEXT, emailEditText.getText().toString());
        outState.putString(PHONE_TEXT, phoneEditText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            emailEditText.setText(savedInstanceState.getString(EMAIL_TEXT, ""));
            phoneEditText.setText(savedInstanceState.getString(PHONE_TEXT, ""));
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

    private void updateProfile() {
        String newEmail = emailEditText.getText().toString().trim();
        String newPhone = phoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newEmail) || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPhone)) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        updateEmail(newEmail);
        updatePhoneNumber(newPhone);
    }


    private void updateEmail(String newEmail) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateProfileActivity.this, "Email updated", Toast.LENGTH_SHORT).show();
                            navigateBackToMusicList();
                        } else {
                            Toast.makeText(UpdateProfileActivity.this, "Failed to update email", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updatePhoneNumber(String newPhone) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Assuming you are using Firestore to store additional user information
            db.collection("users").document(userId)
                    .update("phone", newPhone)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateProfileActivity.this, "Phone number updated", Toast.LENGTH_SHORT).show();
                            navigateBackToMusicList();
                        } else {
                            Toast.makeText(UpdateProfileActivity.this, "Failed to update phone number", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void navigateBackToMusicList() {
        Intent intent = new Intent(UpdateProfileActivity.this, MusicListActivity.class);
        startActivity(intent);
        finish(); // Close the UpdateProfileActivity
    }
}

