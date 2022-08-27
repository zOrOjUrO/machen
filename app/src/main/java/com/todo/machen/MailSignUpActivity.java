package com.todo.machen;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class MailSignUpActivity extends Activity {

    private static final String ETAG = "EmailLogin";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        EditText email = (EditText)findViewById(R.id.uName);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

                Pattern pattern = Pattern.compile(regex);
                if(!pattern.matcher(email.getText()).matches()) email.setError("Enter Valid Email Address");
            }
        });
    }

    private void updateUI(FirebaseUser currentUser) {
        if(currentUser != null) Toast.makeText(MailSignUpActivity.this.getApplicationContext(), "User: "  + currentUser.getDisplayName(), Toast.LENGTH_LONG).show();
    }

    private String checkMail(View view){

        EditText mail = ((EditText)view.getRootView().findViewById(R.id.uName));
        if( TextUtils.isEmpty(mail.getText())){
            Toast.makeText(this, "Please Enter Your E-Mail", Toast.LENGTH_SHORT).show();
            mail.setError( "Email is Required!" );
        }
        return String.valueOf(mail.getText());
    }

    private String checkPass(View view){
        EditText pass = ((EditText)view.getRootView().findViewById(R.id.uPass));
        if( TextUtils.isEmpty(pass.getText())){
            Toast.makeText(this, "Enter a Password", Toast.LENGTH_SHORT).show();
            pass.setError( "Enter Password" );
        }
        return String.valueOf(pass.getText());
    }

    private void Verify() {
        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        if(!user.isEmailVerified()) {
            user.sendEmailVerification()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Email sent
                        }
                    });
        }
        // [END send_email_verification]
    }

    public void MailSignUp(View view) {

        String email = checkMail(view);
        String password = checkPass(view);

        if(!email.isEmpty() && !password.isEmpty()){
            try{
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(ETAG, "createUserWithEmail:success");
                                    Verify();
                                    Toast.makeText(MailSignUpActivity.this, "Account Created Successfully",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(mAuth.getCurrentUser());
                                    saveCreds(email, password, mAuth.getCurrentUser().getDisplayName());
                                    mAuth.signOut();
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(ETAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(MailSignUpActivity.this, "Account Could not be created",
                                            Toast.LENGTH_SHORT).show();
                                    ((EditText)view.getRootView().findViewById(R.id.uName)).setError(task.getException().getMessage());
                                }
                            }
                        });
            }
            catch (Exception e){
                Log.e(ETAG, e.toString());
                EditText pass = ((EditText)view.getRootView().findViewById(R.id.uPass));
                pass.setError("Try A Stronger Password [len>6]");
            }

        }
    }

    private void saveCreds(String email, String password, String username){
        SharedPreferences sharedPref = MailSignUpActivity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.email_saved), email);
        editor.putString(getString(R.string.password_saved), password);
        editor.putString(getString(R.string.username_saved), username);
        editor.apply();
    }

    public void MailLogin(View view) {
        super.onBackPressed();
    }
}
