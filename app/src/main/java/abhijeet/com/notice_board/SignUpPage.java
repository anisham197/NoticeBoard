// Class for sign up activity

package abhijeet.com.notice_board;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpPage extends AppCompatActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

      // Size of list
    final int size = MainActivity.Departments.length;

    ProgressDialog progressDialog;
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_page);
        progressDialog = new ProgressDialog(SignUpPage.this);

        Log.d("Anisha", " Size " + MainActivity.Departments.length );

        // Views
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_create_account_button).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // Creates an account and authenticates it
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        progressDialog.show();
        progressDialog.setMessage("Loading");

        // Create user with email
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful())
                        {
                            String error;
                            try
                            {
                                String errorCode = ((FirebaseAuthException)task.getException()).getErrorCode();
                                error = MainActivity.getError(errorCode);
                            }
                            catch (ClassCastException e)
                            {
                                error = "Please check your internet connection.";
                            }
                            Log.d(TAG, "signInWithEmail " + error);//task.getException()
                            Toast.makeText(SignUpPage.this, error,
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                        else
                        {
                            addPreferences();

                            /*pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                            editor = pref.edit();
                            editor.putString("uid", user.getUid());
                            editor.apply();*/

                            //Intent notificationIntent = new Intent(SignUpPage.this, NotificationService.class);
                            //startService(notificationIntent);

                            // Shifts to initial change preference activity if form is validated
                            Intent intent = new Intent();
                            intent.setClass(SignUpPage.this, SetPreferences.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    // Checking if username and password are entered
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    // On clicking sign up button, the account is created (Added to database)
    @Override
    public void onClick(View v) {
        createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
    }

    // Function to initially store preferences in database
    public void addPreferences()
    {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        HashMap<String, Boolean> hm = new HashMap<>();

        for ( int i = 0; i < size; i++)
        {
            hm.put(MainActivity.Departments[i], false);
        }

        mDatabase.child("ID's").child(uid).setValue(hm);

        pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        editor.putString("uid", user.getUid());
        editor.apply();

        Intent notificationIntent = new Intent(SignUpPage.this, NotificationService.class);
        startService(notificationIntent);
    }
}
