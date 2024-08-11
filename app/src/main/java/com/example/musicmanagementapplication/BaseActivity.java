package com.example.musicmanagementapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.musicmanagementapplication.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity {

    private static final long INACTIVITY_TIMEOUT = 12 * 1000; // 15 minutes
    private boolean shouldLogout = true;
    private final Handler inactivityHandler = new Handler();
    private final Runnable logoutRunnable = new Runnable() {
        @Override
        public void run() {
            logout();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetInactivityTimer();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        resetInactivityTimer();  // Reset the timer on any touch event
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetInactivityTimer();  // Reset the timer on any interaction
    }

    private void resetInactivityTimer() {
        inactivityHandler.removeCallbacks(logoutRunnable); // Remove any pending executions
        inactivityHandler.postDelayed(logoutRunnable, INACTIVITY_TIMEOUT);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("LOGOUT_REASON", "You have been logged out due to inactivity.");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();  // Ensure the current activity is finished
    }

    @Override
    protected void onPause() {
        super.onPause();
        inactivityHandler.removeCallbacks(logoutRunnable);
        // Optionally, you could also stop the timer here to save resources
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetInactivityTimer(); // Reset the timer when the user returns to the app
    }

    protected void onStop() {
        super.onStop();
//        resetInactivityTimer(); // Reset the timer when the user returns to the app
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        inactivityHandler.removeCallbacks(logoutRunnable); // Ensure no memory leaks
    }
}
