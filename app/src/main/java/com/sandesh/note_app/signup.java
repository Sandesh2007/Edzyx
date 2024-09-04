package com.sandesh.note_app;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Patterns;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.sandesh.note_app.databinding.ActivityMainBinding;

import java.util.Objects;

public class signup extends AppCompatActivity {

//    ActivityMainBinding binding;

//    Button google_signup;
    Button signupbtn;
    TextView backtolog;
    EditText text_registername,getText_registeremail,getText_registerpasswd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);
        EdgeToEdge.enable(this);


        //INputs
        text_registername=findViewById(R.id.username);
        getText_registeremail=findViewById(R.id.email);
        getText_registerpasswd=findViewById(R.id.password);
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        //Buttons
        signupbtn=findViewById(R.id.signUpButton);
//        google_signup=findViewById(R.id.googleSignUpButton);



        //This button is to sent user back to signup page/activity
        backtolog = findViewById(R.id.backtolog);

        if (backtolog != null) {
            backtolog.setOnClickListener(view -> {
                Intent intent = new Intent(signup.this, login.class);
                startActivity(intent);
            });
        } else {

            throw new NullPointerException("Button with ID backlogin not found.");
        }


        //This button is to go to the login page/activity
        if (signupbtn!= null) {
            signupbtn.setOnClickListener(view -> {
                Intent intent = new Intent(signup.this,
                        home.class);
                startActivity(intent);
            });
        } else {
            throw new NullPointerException("Button with ID signin not found.");
        }


        signupbtn.setOnClickListener(view -> {

            String username =text_registername.getText().toString();
            String email =getText_registeremail.getText().toString();
            String password =getText_registerpasswd.getText().toString();
            checkUsernameExists(username);

            if (TextUtils.isEmpty(username)){
                text_registername.setError("Username is required");
                text_registername.requestFocus();
            }
            else if (TextUtils.isEmpty(email)){
                getText_registeremail.setError("Email is also required");
                getText_registeremail.requestFocus();
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                getText_registeremail.setError("please enter a valid email");
                getText_registeremail.requestFocus();
            }
            else if (TextUtils.isEmpty(password)) {
                getText_registerpasswd.setError("Password is required");
                getText_registerpasswd.requestFocus();
            }
            else if (password.length() <7 ) {
                getText_registerpasswd.setError("Password should have more than 7 letters");
                getText_registerpasswd.requestFocus();
            }
            else {
                registerUser(username,password,email);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //This method registers the user after all the user details are correct
    private void registerUser(String username, String password, String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(signup.this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();

                if (firebaseUser != null) {
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();
                    firebaseUser.updateProfile(profileChangeRequest);

                    ReadWriteUserDetails readWriteUserDetails = new ReadWriteUserDetails(email);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");

                    reference.child(firebaseUser.getUid()).setValue(readWriteUserDetails).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            // Send verification email
                            firebaseUser.sendEmailVerification();

                            Toast.makeText(signup.this, "User registered successfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(signup.this, home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(signup.this, "Error while registering user", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // Check if the exception is due to the user already existing
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(signup.this, "User already exists. Please log in.", Toast.LENGTH_LONG).show();
                } else {
                    // Handle other errors
                    Toast.makeText(signup.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void checkUsernameExists(String username) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users");

        // check on firebase to see if the username already exists
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    Toast.makeText(signup.this, "Username already used", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
                Toast.makeText(signup.this, "Failed to check username", Toast.LENGTH_SHORT).show();
            }
        });
    }

}