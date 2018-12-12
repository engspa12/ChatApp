package com.example.dbm.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";

    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER=2;

    //private ListView mMessageListView;
    private RecyclerView mMessagesRecyclerView;
    //private MessageAdapter mMessageAdapter;
    private DividerItemDecoration mDividerItemDecoration;

    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;



    private FirebaseRecyclerAdapter adapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        //mMessageListView = (ListView) findViewById(R.id.messageListView);
        mMessagesRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        //List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        //mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        //mMessageListView.setAdapter(mMessageAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMessagesRecyclerView.setLayoutManager(layoutManager);

        mMessagesRecyclerView.setHasFixedSize(true);

        mDividerItemDecoration = new DividerItemDecoration(mMessagesRecyclerView.getContext(), layoutManager.getOrientation());
        mDividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        mMessagesRecyclerView.addItemDecoration(mDividerItemDecoration);


        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
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
                // TODO: Send messages on click
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd - h:mm a");
                String currentDateAndTime = sdf.format(new Date());
                ChatMessage friendlyMessage = new ChatMessage(mMessageEditText.getText().toString(), mUsername, null, currentDateAndTime);
                mMessagesDatabaseReference.push().setValue(friendlyMessage);
                // Clear input box
                mMessageEditText.setText("");
            }
        });


        // Check sign-in state
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    onSignedInInitialize(user.getDisplayName());
                    // User is signed in
                    Toast.makeText(MainActivity.this, "You're now signed in. Welcome to the Chat App.", Toast.LENGTH_SHORT).show();
                } else {
                    onSignedOutCleanup();
                    // User is signed out
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build()
                            //new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            //new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                    );
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .setTheme(R.style.ChatTheme)
                                    .setLogo(R.drawable.ic_chat)
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);

        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this,"Signed in!",Toast.LENGTH_SHORT).show();

            } else if(resultCode == RESULT_CANCELED){
                Toast.makeText(this,"Sign in canceled!",Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            final StorageReference photoRef =
                    mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask = photoRef.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd - h:mm a");
                        String currentDateAndTime = sdf.format(new Date());
                        ChatMessage friendlyMessage =
                                new ChatMessage(null, mUsername, downloadUri.toString(), currentDateAndTime);
                        mMessagesDatabaseReference.push().setValue(friendlyMessage);
                    } else {
                        // Handle failures
                        // ...
                        Log.e(TAG,"An error occurred during the getDownloadUrl process");
                    }
                }
            });

//            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd - h:mm a");
//                    String currentDateAndTime = sdf.format(new Date());
//                    FriendlyMessage friendlyMessage =
//                            new FriendlyMessage(null, mUsername, downloadUrl.toString(), currentDateAndTime);
//                    mMessagesDatabaseReference.push().setValue(friendlyMessage);
//                }
//            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        //mMessageAdapter.clear();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void onSignedInInitialize(String username){
        mUsername = username;
        attachDatabaseReadListener();

    }

    private void onSignedOutCleanup(){
        mUsername = ANONYMOUS;
        //mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessage friendlyMessage = dataSnapshot.getValue(ChatMessage.class);
                    //mMessageAdapter.add(friendlyMessage);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }

        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(mMessagesDatabaseReference, ChatMessage.class)
                        .build();


        adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder>(options) {
            @Override
            public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);

                return new ChatMessageViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(ChatMessageViewHolder holder, int position, ChatMessage message) {
                // Bind the Chat object to the ChatHolder
                // ...
                boolean isPhoto = message.getPhotoUrl() != null;
                if (isPhoto) {
                    holder.messageTextView.setVisibility(View.GONE);
                    holder.photoImageView.setVisibility(View.VISIBLE);
                    Glide.with(holder.photoImageView.getContext())
                            .load(message.getPhotoUrl())
                            .apply(new RequestOptions().centerCrop())
                            .into(holder.photoImageView);
                } else {
                    holder.messageTextView.setVisibility(View.VISIBLE);
                    holder.photoImageView.setVisibility(View.GONE);
                    holder.messageTextView.setText(message.getText());
                }
                holder.authorTextView.setText(message.getName());
                holder.dateTextView.setText(message.getCurrentDateAndTime());
            }

            @Override
            public void onDataChanged() {
                // Called each time there is a new data snapshot. You may want to use this method
                // to hide a loading spinner or check for the "no documents" state and update your UI.
                // ...
                mMessagesRecyclerView.scrollToPosition(mMessagesRecyclerView.getAdapter().getItemCount() - 1);
            }

            @Override
            public void onError(DatabaseError e) {
                // Called when there is an error getting data. You may want to update
                // your UI to display an error message to the user.
                // ...
            }
        };

        mMessagesRecyclerView.setAdapter(adapter);
        if(adapter != null) {
            adapter.startListening();
        }
    }

    private void detachDatabaseReadListener(){
        if(mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    public void fetchConfig(){
        long cacheExpiration = 3600;

        if(mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()){
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        applyRetrievedLengthLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Error fetching config",e);
                        applyRetrievedLengthLimit();
                    }
                });

    }

    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = mFirebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG,FRIENDLY_MSG_LENGTH_KEY + " = " + friendly_msg_length);
    }
}
