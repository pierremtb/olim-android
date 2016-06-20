package com.pierrejacquier.olim;

import android.app.Application;

import com.pierrejacquier.olim.data.User;

public class Olim extends Application {
    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}