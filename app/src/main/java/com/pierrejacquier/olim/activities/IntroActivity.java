package com.pierrejacquier.olim.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.github.paolorotolo.appintro.AppIntro;
import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.fragments.AllSetFragment;
import com.pierrejacquier.olim.fragments.GoogleConnectFragment;
import com.pierrejacquier.olim.fragments.PermissionsFragment;
import com.pierrejacquier.olim.helpers.DriveSyncController;

public class IntroActivity
        extends AppIntro
        implements DriveSyncController.GoogleApiClientCallbacks,
                    GoogleConnectFragment.OnFragmentInteractionListener {

    private Olim app;
    private GoogleConnectFragment googleConnectFragment;
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getApplicationContext();
        googleConnectFragment = GoogleConnectFragment.newInstance();

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            addSlide(PermissionsFragment.newInstance());
            askForPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }

        addSlide(googleConnectFragment);
        addSlide(AllSetFragment.newInstance());
        showSkipButton(false);
        showDoneButton(true);

        pager.setNextPagingEnabled(true);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onGoogleConnected(Olim app) {
        moveToAllSet();
    }

    @Override
    public void onGoogleDisconnected(Olim app) {
        moveToGoogleConnect();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d("auie", "auinearusienuires");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    app.setReadContactsAllowed(true);
                } else {
                    app.setReadContactsAllowed(false);
                }
            }
        }
    }

    private void moveToAllSet() {
        googleConnectFragment.setGoogleConnected();
        pager.setNextPagingEnabled(true);
        pager.setCurrentItem(2);
        showDoneButton(true);
    }

    private void moveToGoogleConnect() {
        pager.setNextPagingEnabled(false);
        pager.setCurrentItem(0);
        showDoneButton(false);
    }
}