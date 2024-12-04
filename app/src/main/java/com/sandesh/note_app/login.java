package com.sandesh.note_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class login extends AppCompatActivity {

    Button loginbtn;
    ImageView google_login_btn,back_img;
    EditText text_email,text_passord;
    TextView backtosign,forgot_passwd,error_info;
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
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
        google_login_btn = findViewById(R.id.google_login_btn);
        back_img = findViewById(R.id.back_img);

        //Inputs
        text_email=findViewById(R.id.email);
        text_passord=findViewById(R.id.password);
        error_info=findViewById(R.id.error_info);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this matches the client ID in Firebase
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google Sign-In button click listener
        google_login_btn.setOnClickListener(view -> signInWithGoogle());


        back_img.setOnClickListener(view -> onBackPressed());

//        Forgot password
        forgot_passwd.setOnClickListener(view -> showForgotPasswordDialog());



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
                errorInfo("Email is required");
                text_email.requestFocus();
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorInfo("please enter a valid email");
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
    private void errorInfo(String errorinfo) {
        error_info.setText(errorinfo);
        new Handler(Looper.getMainLooper()).postDelayed(() -> error_info.setText(""), 2000);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void loginUser(String password, String email) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Intent intent = new Intent(this, home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            showCustomToast(login.this,"Logged in successfully.");
                            startActivity(intent);
                            finish();
                        } else {
                            showCustomToast(login.this, "Please verify your email before logging in");
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            showCustomToast(login.this, "User with this email already exists.");
                        } else {
                            showCustomToast(login.this, "Authentication failed: " + task.getException().getMessage());
                        }
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.w("Google Sign-In", "Google sign in failed", e);
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-in successful, go to home activity
                        showCustomToast(login.this,"logged in successfully.");
                        Intent intent = new Intent(this, home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Sign-in failed
                        String errorCode = ((ApiException) task.getException()).getStatusCode() + "";
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            // Email already exists
                            showCustomToast(login.this, "An account with this email already exists.");
                        } else {
                            showCustomToast(login.this, "Authentication failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void showForgotPasswordDialog() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.forgot_password_dialog, null);

        // Initialize dialog components
        EditText resetMail = dialogView.findViewById(R.id.resetMail);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button sendButton = dialogView.findViewById(R.id.sendButton);

        // Create the dialog
        AlertDialog forgotPasswordDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        forgotPasswordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // Cancel button functionality
        cancelButton.setOnClickListener(v -> forgotPasswordDialog.dismiss());

        // Send button functionality
        sendButton.setOnClickListener(v -> {
            String email = resetMail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                resetMail.setError("Email is required!");
                return;
            }

            // Check if the email exists in Firebase
            firebaseAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    boolean emailExists = !task.getResult().getSignInMethods().isEmpty();

                    if (emailExists) {
                        // Send password reset email
                        firebaseAuth.sendPasswordResetEmail(email)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_SHORT).show();
                                    forgotPasswordDialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to send reset link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        resetMail.setError("Email not registered.");
                        Toast.makeText(this, "This email is not registered in our system.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error checking email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to fetch email data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        // Show the dialog
        forgotPasswordDialog.show();
    }



    private void showCustomToast(Context context, String message) {
        // Inflate the custom toast layout
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custm_toast));

        // Set the text and image in the custom toast layout
        TextView toastText = toastLayout.findViewById(R.id.toast_text);
        toastText.setText(message);

        // Create and display the toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout); // Set custom view
        toast.show();
    }
}