package com.pierrejacquier.olim.activities;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getApplicationContext();
        googleConnectFragment = GoogleConnectFragment.newInstance();

        addSlide(googleConnectFragment);
        addSlide(PermissionsFragment.newInstance());
        addSlide(AllSetFragment.newInstance());
        showSkipButton(false);
        showDoneButton(false);
        askForPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 2);

        pager.setNextPagingEnabled(false);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onGoogleConnected(Olim app) {
        moveToPermissions();
    }

    @Override
    public void onGoogleDisconnected(Olim app) {
        moveToGoogleConnect();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    private void moveToPermissions() {
        googleConnectFragment.setGoogleConnected();
        pager.setNextPagingEnabled(true);
        pager.setCurrentItem(1);
        showDoneButton(true);
    }

    private void moveToGoogleConnect() {
        pager.setNextPagingEnabled(false);
        pager.setCurrentItem(0);
        showDoneButton(false);
    }
}