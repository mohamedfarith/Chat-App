/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.LoginFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "Farith";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private RecyclerView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    ProgressBar progressBar;
    private String mUsername;
    FirebaseAuth firebaseAuth;
    private String userId;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getting the intent values
        if (getIntent().getStringExtra("EMAIL") != null) {
            email = getIntent().getStringExtra("EMAIL");
//            mUsername  = getIntent().getStringExtra("USERNAME");
            userId = getIntent().getStringExtra("USER_ID");
            Log.d(TAG, "onCreate: get intent " + userId);
            mUsername = getIntent().getStringExtra("USERNAME");
        } else {
            mUsername = ANONYMOUS;
        }
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("messages").child(userId);
        userId = mDatabaseReference.getKey();


        // Initialize references to views
        mProgressBar = findViewById(R.id.progressBar);
        mMessageListView = findViewById(R.id.messageListView);
        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);
        mProgressBar.setVisibility(View.VISIBLE);
        //database refrence intialization
        mDatabaseReference = mFirebaseDatabase.getReference().child("messages").child(userId);
        // Initialize message ListView and its adapter
        final ArrayList<FriendlyMessage> friendlyMessages = new ArrayList<>();
        final ArrayList<FriendlyMessage> newMessage = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, friendlyMessages);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mMessageListView.setLayoutManager(manager);
        mMessageListView.scrollToPosition(friendlyMessages.size() - 1);
        mMessageListView.setAdapter(mMessageAdapter);
        mMessageListView.addItemDecoration(new DividerItemDecoration(MainActivity.this, DividerItemDecoration.HORIZONTAL));

        //Getting the last 10 messages for the current user
        Query query = mDatabaseReference.orderByKey().limitToLast(10);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue().toString());
                FriendlyMessage message = dataSnapshot.getValue(FriendlyMessage.class);
                if (TextUtils.isEmpty(message.getText())) {
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "No messages to load", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onDataChange: contents of message object " + message.getText() + " " + message.getName());
                    friendlyMessages.add(message);
                    mUsername = message.getName();
                    mProgressBar.setVisibility(View.GONE);
                    mMessageAdapter.notifyDataSetChanged();
                    mMessageListView.scrollToPosition(friendlyMessages.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: is called");

            }
        });
        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FriendlyMessage message = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);
                //Posting the value to the Firebase Database
                mDatabaseReference.push().setValue(message);
                // Clear input box
                mMessageEditText.setText("");
                mDatabaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.d(TAG, "onChildAdded: child added " + dataSnapshot.toString());
                        FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                        Log.d(TAG, "onChildAdded: " + friendlyMessage.getText());
                        mUsername = friendlyMessage.getName();
                        newMessage.add(friendlyMessage);
                        //updating the value in the adapter
                        Log.d(TAG, "onChildAdded: " + dataSnapshot.getKey());
                        mMessageAdapter.notifyItemChanged(friendlyMessages.size(), newMessage);
                        mMessageListView.scrollToPosition(friendlyMessages.size() - 1);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.d(TAG, "onChildChanged: child changed in the firebase database " + dataSnapshot.toString());
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: child cancelled");
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sign_out_menu) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(MainActivity.this, LoginScreen.class));
        }
        return super.onOptionsItemSelected(item);

    }
}
