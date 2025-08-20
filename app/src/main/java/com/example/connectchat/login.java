package com.example.connectchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
// If ProgressDialog is to be used, you'll need its import:
// import android.app.ProgressDialog;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    Button button, logSingupBut; // Added logSingupBut declaration
    EditText email, password;
    FirebaseAuth auth;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    // android.app.ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        button = findViewById(R.id.logButton); // Your existing login button
        email = findViewById(R.id.editTextLogEmail);
        password = findViewById(R.id.editTextLogPassword);
        logSingupBut = findViewById(R.id.logSingupBut); // Initialize the Signup button - REPLACE R.id.logSingupBut WITH YOUR ACTUAL ID

        // --- Check if login button and fields are found (Good Practice) ---
        if (button == null) {
            Toast.makeText(this, "Error: Login button (logButton) not found.", Toast.LENGTH_LONG).show();
            return;
        }
        if (email == null) {
            Toast.makeText(this, "Error: Email field (editTextLogEmail) not found.", Toast.LENGTH_LONG).show();
            return;
        }
        if (password == null) {
            Toast.makeText(this, "Error: Password field (editTextLogPassword) not found.", Toast.LENGTH_LONG).show();
            return;
        }
        // --- End of check for login button and fields ---


        // --- Check if Signup button is found (Crucial for its functionality) ---
        if (logSingupBut == null) {
            Toast.makeText(this, "Error: Signup button (logSingupBut) not found. Check ID in XML and findViewById.", Toast.LENGTH_LONG).show();
            // You might not want to 'return' here if the login functionality should still work
            // but the signup button will definitely not work.
        }
        // --- End of check for Signup button ---


        // OnClickListener for the LOGIN button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = email.getText().toString().trim();
                String passInput = password.getText().toString();

                if (TextUtils.isEmpty(emailInput)) {
                    email.setError("Enter The Email");
                    email.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(passInput)) {
                    password.setError("Enter The Password");
                    password.requestFocus();
                    return;
                }
                if (!emailInput.matches(emailPattern)) {
                    email.setError("Enter a valid Email address");
                    email.requestFocus();
                    return;
                }
                if (passInput.length() < 6) {
                    password.setError("Password must be at least 6 characters long");
                    password.requestFocus();
                    return;
                }

                auth.signInWithEmailAndPassword(emailInput, passInput)
                        .addOnCompleteListener(login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(login.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(login.this, "Authentication failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        // OnClickListener for the SIGNUP button
        // Ensure logSingupBut is not null before setting the listener
        if (logSingupBut != null) {
            logSingupBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Action to open the Registration page
                    Intent intent = new Intent(login.this, registration.class); // Ensure registration.class is your registration Activity
                    startActivity(intent);
                    // Optional: finish(); // if you want to close the login page when going to registration
                }
            });
        }
    }
}
