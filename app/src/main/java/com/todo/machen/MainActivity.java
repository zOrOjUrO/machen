package com.todo.machen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.todo.machen.listHelper.AddBottomSheetFragment;
import com.todo.machen.listHelper.listAdapter;
import com.todo.machen.listHelper.setData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final String MTAG = "Main Activity";

    public static SharedPreferences sharedPref;
    private ListView listView;
    public static listAdapter adapter;
    public static List<setData> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(getApplicationContext());
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //check for logged-in sessions
        accountCheck();
        listInit();
        checkList();
    }

    private void listInit(){
        listView = findViewById(R.id.list);
        data = new ArrayList<>();
        adapter = new listAdapter(MainActivity.this, R.id.list, data);
        listView.setAdapter(adapter);
        listView.refreshDrawableState();
    }

    public void checkList() {
        if(data.size() > 0){
            (findViewById(R.id.noTasks)).setVisibility(View.GONE);
            (findViewById(R.id.list)).setVisibility(View.VISIBLE);
        }
        else{
            (findViewById(R.id.noTasks)).setVisibility(View.VISIBLE);
            (findViewById(R.id.list)).setVisibility(View.GONE);
        }
    }

    private void accountCheck(){
        //[GOOGLE]
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        Log.i("Google Login: ", String.valueOf(account != null ? account.toString() : null));
        //[FACEBOOK]
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        Log.d("Google Check: ", String.valueOf((account == null || account.isExpired())));
        Log.d("FB Check: ", String.valueOf(!isLoggedIn));
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String userMail = sharedPref.getString(getString(R.string.email_saved), "");
        String password = sharedPref.getString(getString(R.string.password_saved), "");
        String username = sharedPref.getString(getString(R.string.username_saved), "");
        Log.i("SharedPreferences: ", username + password + userMail);
        boolean isLocalSigned = LocalMailLogin(userMail, password, username);

        if(!isLoggedIn && (account == null || account.isExpired()) && !isLocalSigned)
            //LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("public_profile"));
            startActivity(new Intent(this.getApplicationContext(), LoginActivity.class));
    }

    private boolean LocalMailLogin(String userMail, String password, String username) {
        mAuth = FirebaseAuth.getInstance();
        final boolean[] status = {false};
        if(!userMail.isEmpty() && !password.isEmpty()){
            mAuth.signInWithEmailAndPassword(userMail, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(MTAG, username + " LoggedIn WithEmail");
                            status[0] = true;
                            Toast.makeText(MainActivity.this, username + " logged in",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(MTAG, "LoginWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Please Login Again to Continue")
                                    .setTitle("Login Failed")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                            status[0] = false;
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
        }
        return status[0];
    }

    public void addTask(View view) {

        new Thread(() -> {
            AddBottomSheetFragment bottomSheetFragment = AddBottomSheetFragment.newInstance();
            bottomSheetFragment.show(getSupportFragmentManager(),
                    "task_dialog_fragment");
        }).start();
        (findViewById(R.id.noTasks)).setVisibility(View.GONE);
        (findViewById(R.id.list)).setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(data.size() > 0) adapter.notifyDataSetChanged();
                listView.refreshDrawableState();
                Log.i(MTAG, "List Refreshed\n List has: "+ data.toArray().toString());
            }
        }, 40000);
    }

    public void logOut(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Are You Sure You Want to Log Out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mAuth != null) mAuth.signOut();
                        if(LoginManager.getInstance() != null) LoginManager.getInstance().logOut();
                        GoogleSignInOptions gso = new GoogleSignInOptions.
                                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                                build();
                        GoogleSignInClient googleSignInClient=GoogleSignIn.getClient(MainActivity.this,gso);
                        googleSignInClient.signOut();
                        sharedPref.edit().clear().apply();
                        MainActivity.super.onStart();
                    }
                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}

