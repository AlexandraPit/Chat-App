package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatapp.MainActivity;
import com.example.chatapp.SignUpActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";

    private EditText userEmailEditText;
    private EditText userPasswordEditText;
    private TextView signInButton;
    private TextView signUpButton;
    private String email;
    private String password;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        initializeViews();
        setupClickListeners();
        setupWindowInsets();
    }

    private void initializeViews() {
        userEmailEditText = findViewById(R.id.emailText);
        userPasswordEditText = findViewById(R.id.passwordText);
        signInButton = findViewById(R.id.login);
        signUpButton = findViewById(R.id.signup);
    }

    private void setupClickListeners() {
        signInButton.setOnClickListener(v -> {
            if (validateInput()) {
                signIn();
            }
        });

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean validateInput() {
        email = userEmailEditText.getText().toString().trim();
        password = userPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            userEmailEditText.setError("Please enter your email");
            userEmailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            userPasswordEditText.setError("Please enter your password");
            userPasswordEditText.requestFocus();
            return false;
        }
        return true;
    }

    private void signIn() {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        String username = user.getDisplayName();
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        intent.putExtra("name", username);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "User is null after successful sign-in");
                        Toast.makeText(SignInActivity.this, "Sign-in successful, but user data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Sign-in failed", e);
                    if (e instanceof FirebaseAuthInvalidUserException) {
                        Toast.makeText(SignInActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(SignInActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignInActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            finish();
        }
    }
}