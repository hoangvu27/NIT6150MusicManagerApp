package com.example.musicmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button loginButton, registerButton;
    private FirebaseAuth mAuth;
    private TextView forgotPasswordTextView;
    private ImageButton showPasswordButton;
    private boolean isPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        showPasswordButton = findViewById(R.id.showPasswordButton);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // Check if a logout message was passed
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("LOGOUT_REASON")) {
            String logoutReason = intent.getStringExtra("LOGOUT_REASON");
            if (logoutReason != null) {
                // Display the logout message to the user
                Toast.makeText(this, logoutReason, Toast.LENGTH_LONG).show();
            }
        }

        loginButton.setOnClickListener(view -> loginUser());

        registerButton.setOnClickListener(view -> {
            Intent tempIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(tempIntent);
        });

        showPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility();
            }
        });

        forgotPasswordTextView.setOnClickListener(view -> {
            showForgotPasswordDialog();
        });
    }

    private void loginUser() {
        String emailStr = email.getText().toString();
        String passwordStr = password.getText().toString();
        mAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(LoginActivity.this, MusicListActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        // Create an AlertDialog to prompt the user for their email
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText emailInput = new EditText(this);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Enter your email");
        builder.setView(emailInput);

        builder.setPositiveButton("Reset Password", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                resetPassword(email);  // Send the password reset email
            } else {
                Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error sending reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            showPasswordButton.setImageResource(R.drawable.show_password); // Show password icon
        } else {
            // Show password
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            showPasswordButton.setImageResource(R.drawable.show_password); // Hide password icon
        }
        // Move the cursor to the end of the text
        password.setSelection(password.length());
        isPasswordVisible = !isPasswordVisible;
    }
}