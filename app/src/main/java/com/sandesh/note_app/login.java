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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class login extends AppCompatActivity {

    Button loginbtn,google_login_btn;
    EditText text_email,text_passord;
    TextView backtosign;
  FirebaseAuth firebaseAuth;

//    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        //Buttons
        loginbtn = findViewById(R.id.loginButton);
//        google_login_btn = findViewById(R.id.googleLoginButton);

        //Inputs
        text_email=findViewById(R.id.email);
        text_passord=findViewById(R.id.password);

        backtosign = findViewById(R.id.tosign);
        if (backtosign != null) {
            backtosign.setOnClickListener(view -> onBackPressed());
        } else {
            // Handle the case where the button is not found
            throw new NullPointerException("Button with ID getStartedButton not found.");
        }


        //Login if user details are positive
        loginbtn.setOnClickListener(view -> {

            String email =text_email.getText().toString();
            String password =text_passord.getText().toString();

            if (TextUtils.isEmpty(email)){
                text_email.setError("Email is also required");
                text_email.requestFocus();
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                text_email.setError("please enter a valid email");
                text_email.requestFocus();
            }
            else if (TextUtils.isEmpty(password)) {
                text_passord.setError("Password is required");
                text_passord.requestFocus();
            }
            else {
                loginUser(password,email);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void loginUser(String password, String email) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // User is signed in and email is verified
                            Intent intent = new Intent(this, home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // User email is not verified
                            Toast.makeText(login.this, "Please verify your email before logging in", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

}