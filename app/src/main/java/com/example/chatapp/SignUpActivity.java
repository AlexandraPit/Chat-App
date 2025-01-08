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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private EditText userNameEditText, userEmailEditText, userPasswordEditText;
    private TextView signInTextView, signUpTextView;
    private String name, email, password;

    private DatabaseReference usersDatabaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI elements
        userNameEditText = findViewById(R.id.usernameText);
        userEmailEditText = findViewById(R.id.emailText);
        userPasswordEditText = findViewById(R.id.passwordText);
        signInTextView = findViewById(R.id.login);
        signUpTextView = findViewById(R.id.signup);

        // Set up listeners
        signUpTextView.setOnClickListener(v -> {
            // Validate input fields
            if (validateInputFields()) {
                signUp();
            }
        });

        signInTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
        }
    }

    private boolean validateInputFields() {
        name = userNameEditText.getText().toString().trim();
        email = userEmailEditText.getText().toString().trim();
        password = userPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            userNameEditText.setError("Please enter your name");
            userNameEditText.requestFocus();
            return false;
        }

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
        // Add more validation if needed (e.g., email format, password strength)
        return true;
    }

    private void signUp() {
        firebaseAuth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener(authResult -> {
                    // User creation successful
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        firebaseUser.updateProfile(userProfileChangeRequest)
                                .addOnSuccessListener(unused -> {
                                    Log.d(TAG, "User profile updated.");
                                    saveUserDataToDatabase(firebaseUser);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update user profile.", e);
                                    Toast.makeText(SignUpActivity.this, "Failed to update user profile.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // User creation failed
                    Log.w(TAG, "createUserWithEmail:failure", e);
                    handleSignUpFailure(e);
                });
    }

    private void saveUserDataToDatabase(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        UserModel userModel = new UserModel(userId, name, email, password);
        usersDatabaseReference.child(userId).setValue(userModel)
                .addOnSuccessListener(unused -> {
                    // Data saved successfully
                    Log.d(TAG, "User data saved to database.");
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    // Data save failed
                    Log.e(TAG, "Failed to save user data to database.", e);
                    Toast.makeText(SignUpActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleSignUpFailure(Exception e) {
        String errorMessage = "Signup Failed";
        if (e instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "The password is too weak.";
            userPasswordEditText.setError(errorMessage);
            userPasswordEditText.requestFocus();
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "The email address is badly formatted.";
            userEmailEditText.setError(errorMessage);
            userEmailEditText.requestFocus();
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "An account already exists with this email address.";
            userEmailEditText.setError(errorMessage);
            userEmailEditText.requestFocus();
        }
        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }
}