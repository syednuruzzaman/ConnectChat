package com.example.connectchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class registration extends AppCompatActivity {

    TextView loginBut;
    EditText rg_userName, rg_email, rg_password, rg_rePassword;
    Button rg_signup;
    CircleImageView rg_profileImage; // Corrected variable name consistency
    FirebaseAuth auth;
    Uri imageUri; // Used consistently
    String imageuri; // This is for the download URL string
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseDatabase database;
    FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        loginBut = findViewById(R.id.loginBut);
        rg_userName = findViewById(R.id.rgUserName);
        rg_email = findViewById(R.id.rgEmail);
        rg_password = findViewById(R.id.rgPassword);
        rg_rePassword = findViewById(R.id.rgRePassword);
        rg_signup = findViewById(R.id.signupButton);
        rg_profileImage = findViewById(R.id.profil0); // Make sure R.id.profil0 is correct
        // rg_signup = findViewById(R.id.signupButton); // Duplicate initialization


        loginBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(registration.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        rg_profileImage.setOnClickListener(new View.OnClickListener() { // Listener should be set in onCreate
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        rg_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String namee = rg_userName.getText().toString().trim();
                String emaill = rg_email.getText().toString().trim();
                String Password = rg_password.getText().toString(); // Passwords usually aren't trimmed
                String cPassword = rg_rePassword.getText().toString();
                String status = "Hey I'm using this application!";

                if (TextUtils.isEmpty(namee) || TextUtils.isEmpty(emaill) ||
                        TextUtils.isEmpty(Password) || TextUtils.isEmpty(cPassword)) {
                    Toast.makeText(registration.this, "Please enter valid information", Toast.LENGTH_SHORT).show();
                    return; // Added return to stop further execution
                }

                if (!emaill.matches(emailPattern)) {
                    rg_email.setError("Type a valid email Here");
                    Toast.makeText(registration.this, "Type a valid email", Toast.LENGTH_SHORT).show(); // Added Toast
                    return; // Added return
                }

                if (Password.length() < 6) {
                    rg_password.setError("Password must be Six character or more");
                    Toast.makeText(registration.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show(); // Added Toast
                    return; // Added return
                }

                if (!Password.equals(cPassword)) {
                    rg_rePassword.setError("The Password does not match"); // Set error on rePassword field
                    Toast.makeText(registration.this, "Passwords do not match", Toast.LENGTH_SHORT).show(); // Added Toast
                    return; // Added return
                }

                // If imageUri is null, prompt the user to select an image or handle accordingly
                if (imageUri == null) {
                    Toast.makeText(registration.this, "Please select a profile image", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(emaill, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String id = task.getResult().getUser().getUid();
                            DatabaseReference reference = database.getReference().child("user").child(id);
                            StorageReference storageReference = storage.getReference().child("Upload").child(id);

                            // imageUri should not be null here due to the check above
                            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskSnapshotTask) { // Renamed variable
                                    if (taskSnapshotTask.isSuccessful()) {
                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                imageuri = uri.toString(); // Store download URL string
                                                Users users = new Users(id,namee,emaill,Password,imageuri,status); // Adjusted Users constructor based on common patterns
                                                reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> voidTask) { // Renamed variable
                                                        if (voidTask.isSuccessful()) {
                                                            Intent intent = new Intent(registration.this, MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(registration.this, "Error in creating the user profile in database", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(e -> { // Handle failure to get download URL
                                            Toast.makeText(registration.this, "Failed to get image download URL", Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                                        Toast.makeText(registration.this, "Image upload failed: " + taskSnapshotTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(registration.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    } // End of onCreate


    // onActivityResult MUST be outside of any other method, directly in the class
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK) { // Also check resultCode
            if (data != null && data.getData() != null) { // Check data and data.getData()
                imageUri = data.getData(); // Use the class member variable
                rg_profileImage.setImageURI(imageUri); // Use the correct ImageView variable
            } else {
                Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
            }
        }
    }
} // End of class
