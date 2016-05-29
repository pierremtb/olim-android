package com.pierrejacquier.olim.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import butterknife.ButterKnife;
import butterknife.InjectView;
import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.db.Document;

import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.User;

public class LoginActivity extends AppCompatActivity implements MeteorCallback {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    Olim app;
    private Meteor mMeteor;
    public MaterialDialog loadingDialog;

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.btn_go_signup) Button _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        app = (Olim) getApplicationContext();

        _emailText.clearFocus();

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        loadingDialog = new MaterialDialog.Builder(this)
                .content(R.string.please_wait)
                .progress(true, 0)
                .theme(Theme.LIGHT)
                .show();

        final String email = _emailText.getText().toString();
        final String password = _passwordText.getText().toString();


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if(email.contains("@") && email.contains(".")) {
                            MeteorSingleton.getInstance().loginWithEmail(email, password, new ResultListener() {
                                @Override
                                public void onSuccess(String result) {
                                    onLoginSuccess();
                                }

                                @Override
                                public void onError(String error, String reason, String details) {
                                    onLoginFailed();
                                }
                            });
                        } else {
                            MeteorSingleton.getInstance().loginWithUsername(email, password, new ResultListener() {
                                @Override
                                public void onSuccess(String result) {
                                    onLoginSuccess();
                                }

                                @Override
                                public void onError(String error, String reason, String details) {
                                    onLoginFailed();
                                }
                            });
                        }
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                // Finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    private void onLoginSuccess() {
        if (MeteorSingleton.getInstance().isLoggedIn()) {
            Document userDoc = MeteorSingleton.getInstance().getDatabase()
                    .getCollection("users")
                    .getDocument(MeteorSingleton.getInstance().getUserId());
            if (userDoc != null) {
                app.setCurrentUser(new User(userDoc));
            }
        }
        _loginButton.setEnabled(true);
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        loadingDialog.dismiss();
        finish();
    }

    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
        loadingDialog.dismiss();
    }

    private boolean validate() {
        boolean valid = true;

        return valid;
    }

    @Override
    public void onConnect(boolean signedInAutomatically) {

    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {

    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {

    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {

    }
}