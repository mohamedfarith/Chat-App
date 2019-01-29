package com.google.firebase.udacity.friendlychat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginScreen extends AppCompatActivity implements View.OnClickListener {
    private EditText userName;
    private EditText password;
    private Button loginButton;
    private Button signUpButton;
    private static final String TAG = "LoginScreen";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    FirebaseAuth firebaseAuth;
    ProgressDialog dialog;
    private EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        initiateViews();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            Intent intent = new Intent(LoginScreen.this, MainActivity.class);
            intent.putExtra("EMAIL", user.getEmail());
            intent.putExtra("USER_ID", user.getUid());
            finish();
            startActivity(intent);
        }
        loginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    private void initiateViews() {
        dialog = new ProgressDialog(this);
        userName = findViewById(R.id.edit_user_name);
        email = findViewById(R.id.edit_email_id);
        password = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.button_login);
        signUpButton = findViewById(R.id.button_sign_up);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login:
                if (checkForInternetConnectivity()) {
                    login();
                } else {
                    Toast.makeText(LoginScreen.this, "Check the Internet Connection and Try Again", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.button_sign_up:
                if (checkForInternetConnectivity()) {
                    signUp();
                } else {
                    Toast.makeText(LoginScreen.this, "Check the Internet Connection and Try Again", Toast.LENGTH_LONG).show();
                }
                break;
        }


    }

    //sign up the user
    private void signUp() {
        if (checkForInvalidInputs()) {
            return;
        } else {
            dialog.setMessage("Signing up ...");
            dialog.setCancelable(false);
            dialog.show();
            firebaseAuth.fetchSignInMethodsForEmail(email.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    if (task.isSuccessful()) {
                        boolean check = task.getResult().getSignInMethods().isEmpty();
                        if (!check) {
                            dialog.dismiss();
                            Toast.makeText(LoginScreen.this, "Email Address is already Registered", Toast.LENGTH_SHORT).show();
                        } else {
                            dialog.setMessage("Registering User");
                            dialog.show();
                            firebaseAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        final FirebaseUser user = firebaseAuth.getCurrentUser();
                                        Log.d(TAG, "onComplete: inside sign in method:user id is " + user.getUid());
                                        mDatabaseReference = mFirebaseDatabase.getReference().child("messages");
                                        Map<String, Object> updates = new HashMap<String, Object>();
                                        updates.put("uid", user.getUid());
                                        mDatabaseReference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mDatabaseReference = mFirebaseDatabase.getReference().child("messages").child(user.getUid());
                                                    mDatabaseReference.setValue(userName.getText().toString());
                                                    Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                                                    intent.putExtra("USERNAME", userName.getText().toString().trim());
                                                    intent.putExtra("EMAIL", user.getEmail());
                                                    intent.putExtra("USER_ID", user.getUid());
                                                    dialog.dismiss();
                                                    finish();
                                                    startActivity(intent);
                                                }
                                            }
                                        });

                                    } else {
                                        dialog.dismiss();
                                        email.setError("Email id may be inCorrect");
                                        password.setError("Password may be incorrect");
                                    }
                                }
                            });
                        }

                    } else {
                        dialog.dismiss();
                        Toast.makeText(LoginScreen.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
    }

    //Check for the user, whether he has signed in before and logs him in
    private void login() {
        if (checkForInvalidInputs()) {
            return;
        } else {
            dialog.setMessage("Logging in ...");
            dialog.setCancelable(false);
            dialog.show();
            firebaseAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim()).addOnCompleteListener(LoginScreen.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Log.d(TAG, "onComplete: user email" + user.getEmail());
                        Log.d(TAG, "onComplete: user uid " + user.getUid());
                        Toast.makeText(LoginScreen.this, "Successfully Loged In", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                        intent.putExtra("USERNAME", userName.getText().toString().trim());
                        intent.putExtra("EMAIL", user.getEmail());
                        intent.putExtra("USER_ID", user.getUid());
                        finish();
                        startActivity(intent);
                    } else {
                        dialog.dismiss();
                        Toast.makeText(LoginScreen.this, "User name or Password is incorrect, if you don't have an account then sign up", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }

    private boolean checkForInternetConnectivity() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private boolean checkForInvalidInputs() {
        Log.d(TAG, " inside checkForInvalidInputs: ");
        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("this field is required");
            return true;
        } else if (TextUtils.isEmpty(userName.getText().toString().trim())) {
            userName.setError("this field is required");
            return true;
        } else if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("this field is required");
            return true;
        }
        return false;
    }

}
