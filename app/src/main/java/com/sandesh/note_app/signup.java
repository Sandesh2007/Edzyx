package com.sandesh.note_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
//import com.sandesh.note_app.databinding.ActivityMainBinding;

import java.util.List;
import java.util.Objects;

public class signup extends AppCompatActivity {

//    ActivityMainBinding binding;

    ImageView google_signup,back_img;
    Button signupbtn;
    TextView backtolog,error_info;
    EditText text_registername,getText_registeremail,getText_registerpasswd;

    FirebaseAuth auth;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);
        EdgeToEdge.enable(this);


        //INputs
        text_registername=findViewById(R.id.username);
        getText_registeremail=findViewById(R.id.email);
        getText_registerpasswd=findViewById(R.id.password);
        error_info=findViewById(R.id.error_info);
        back_img = findViewById(R.id.back_img);
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        //Buttons
        signupbtn=findViewById(R.id.signUpButton);
        google_signup = findViewById(R.id.google_signup_btn);

        auth = FirebaseAuth.getInstance();


        back_img.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        google_signup.setOnClickListener(view -> signInWithGoogle());
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
                errorInfo("Username is required");
                text_registername.requestFocus();
            }
            else if (TextUtils.isEmpty(email)){
                errorInfo("Email is also required");
                getText_registeremail.requestFocus();
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorInfo("please enter a valid email");
                getText_registeremail.requestFocus();
            }
            else if (TextUtils.isEmpty(password)) {
                errorInfo("Password is required");
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

    private void errorInfo(String errorinfo) {
        error_info.setText(errorinfo);
        new Handler(Looper.getMainLooper()).postDelayed(() -> error_info.setText(""), 2000);
    }


    private void signInWithGoogle() {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                String errorMessage;

                switch (statusCode) {
                    case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                        errorMessage = "Sign-in was cancelled by the user.";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                        errorMessage = "Sign-in failed. Please try again.";
                        break;
                    case GoogleSignInStatusCodes.NETWORK_ERROR:
                        errorMessage = "Network error occurred. Please check your internet connection.";
                        break;
                    case GoogleSignInStatusCodes.INVALID_ACCOUNT:
                        errorMessage = "Invalid account. Please use a valid Google account.";
                        break;
                    case GoogleSignInStatusCodes.DEVELOPER_ERROR:
                        errorMessage = "[Contact San Desh]Developer error. Please check your Google Sign-In configuration.";
                        break;
                    default:
                        errorMessage = "Google sign-in failed with error code: " + statusCode;
                        break;
                }

                showCustomToast(this, errorMessage);
            }

        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
                    reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                ReadWriteUserDetails userDetails = new ReadWriteUserDetails(user.getEmail());
                                reference.child(user.getUid()).setValue(userDetails);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showCustomToast(signup.this, "Error: " + error.getMessage());
                        }
                    });
                }
                showCustomToast(signup.this, "Signed in with Google");
                startActivity(new Intent(signup.this, home.class));
            } else {
                showCustomToast(signup.this, "Authentication Failed.");
            }
        });
    }


    //This method registers the user after all the user details are correct
    private void registerUser(String username, String password, String email) {
        // First, check if the email is already used by an existing user
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> signInMethods = task.getResult().getSignInMethods();
                if (signInMethods != null && !signInMethods.isEmpty()) {
                    // If the user already exists, check if they used Google Sign-In
                    if (signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) {
                        showCustomToast(signup.this, "This email is already associated with a Google account. Please log in using Google.");
                    } else {
                        showCustomToast(signup.this, "User with this email already exists. Please log in.");
                    }
                } else {
                    createNewUser(username, password, email);
                }
            } else {
                // Handle failure in checking email existence
                showCustomToast(signup.this, "Failed to check if user exists. Try again.");
            }
        });
    }

    private void createNewUser(String username, String password, String email) {
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
                            showCustomToast(signup.this, "User registered successfully");

                            // Subscribe to topic
                            FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                                    .addOnCompleteListener(task2 -> {
                                        String msg = "Subscribed to topic successfully!";
                                        if (!task2.isSuccessful()) {
                                            msg = "Subscription to topic failed.";
                                        }
                                        showCustomToast(signup.this, msg);
                                    });

                            Intent intent = new Intent(signup.this, home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            showCustomToast(signup.this, "Error while registering user");
                        }
                    });
                }
            } else {
                // Check if the exception is due to the user already existing
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    showCustomToast(signup.this, "User already exists. Please log in.");
                } else {
                    // Handle other errors
                    showCustomToast(signup.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage());
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
                    showCustomToast(signup.this,"Username already used");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showCustomToast(signup.this,"Failed to check username");
            }
        });
    }
    private void showCustomToast(Context context, String message) {
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custm_toast));
        TextView toastText = toastLayout.findViewById(R.id.toast_text);
        toastText.setText(message);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

}