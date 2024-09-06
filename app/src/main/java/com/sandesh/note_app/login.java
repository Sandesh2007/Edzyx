package com.sandesh.note_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;


public class login extends AppCompatActivity {

    Button loginbtn,google_login_btn;
    EditText text_email,text_passord;
    TextView backtosign,forgot_passwd;
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
        forgot_passwd = findViewById(R.id.forgotPassword);
//        google_login_btn = findViewById(R.id.googleLoginButton);

        //Inputs
        text_email=findViewById(R.id.email);
        text_passord=findViewById(R.id.password);

//        Forgot password
        forgot_passwd.setOnClickListener(view -> {

            EditText resetMail = new EditText(view.getContext());
            resetMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            resetMail.setHint("someone@gmail.com");
            final AlertDialog.Builder passwordForgotDialog = new AlertDialog.Builder(view.getContext());
            passwordForgotDialog.setTitle("Forgot Password?");
            passwordForgotDialog.setMessage("Enter your email to reset password.");
            passwordForgotDialog.setView(resetMail);

            // Buttons for dialog
            passwordForgotDialog.setPositiveButton("Send", (dialogInterface, i) -> {
                String email = resetMail.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    resetMail.setError("Email is required!");
                    Toast.makeText(login.this, "Email is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if email is registered in Firebase
                firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean emailExists = !task.getResult().getSignInMethods().isEmpty();

                        if (emailExists) {
                            // If email exists, send the password reset email
                            firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
                                Toast.makeText(login.this, "Reset link is sent to " + email + ".", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(login.this, "Failed to send reset link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Email does not exist in Firebase
                            resetMail.setError("Email not registered.");
                            Toast.makeText(login.this, "Email not found in Server. Make sure you enter a valid email.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle error
                        Toast.makeText(login.this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });

            passwordForgotDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
                // Close dialog
            });

            passwordForgotDialog.create().show();
        });



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