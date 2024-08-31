package com.example.musicmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;


public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPhone, registerPassword, registerRetypePassword;
    private Button registerButton, backToLoginButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private ImageView customBackButton;
    private ImageButton showPasswordButton;
    private ImageButton showRetypePasswordButton;
    private boolean isPasswordVisible = false;
    private boolean isRetypePasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        registerEmail = findViewById(R.id.registerEmail);
        registerPhone = findViewById(R.id.registerPhone);
        registerPassword = findViewById(R.id.registerPassword);
        registerRetypePassword = findViewById(R.id.retypePassword);

        showPasswordButton = findViewById(R.id.showPasswordButton);
        showRetypePasswordButton = findViewById(R.id.showRetypePasswordButton);
        registerButton = findViewById(R.id.registerButton);
        customBackButton = findViewById(R.id.customBackButton);
        showPasswordButton = findViewById(R.id.showPasswordButton);

        registerButton.setOnClickListener(view -> registerUser());

        customBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        showPasswordButton.setOnClickListener(v -> togglePasswordVisibility(registerPassword, showPasswordButton));
        showRetypePasswordButton.setOnClickListener(v -> togglePasswordVisibility(registerRetypePassword, showRetypePasswordButton));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button click
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Register users
     */
    private void registerUser() {
        String emailStr = registerEmail.getText().toString();
        String phoneStr = registerPhone.getText().toString();
        String passwordStr = registerPassword.getText().toString();

        if (emailStr.isEmpty() || phoneStr.isEmpty() || passwordStr.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateAndRegister()) {
            return;
        };

        mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser(); // Get the currently signed-in user

                        if (user != null) {
                            User newUser = new User(emailStr, phoneStr);

                            db.collection("users").document(user.getUid()).set(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Register successful", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * validate register credentials
     * @return false if any field of credential is invalid. Otherwise, true
     */
    private boolean validateAndRegister() {
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String retypePassword = registerRetypePassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at most 18 characters & contain both numbers and letters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(retypePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Proceed with registration logic, such as saving the user's details in the database
//        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void togglePasswordVisibility(EditText passwordField, ImageButton toggleButton) {
        if (passwordField.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {
            // Show password
            passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.show_password); // Hide password icon
        } else {
            // Hide password
            passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.show_password); // Show password icon
        }
        // Move the cursor to the end of the text
        passwordField.setSelection(passwordField.length());
    }

    /**
     * check if password is valid
     * @param password the password that needs to be validated
     * @return true if password is valid. Otherwise, false
     */
    private boolean isValidPassword(String password) {
        if (password.length() > 18) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasLetter && hasDigit;
    }
}

