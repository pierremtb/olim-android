package com.pierrejacquier.olim;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.plus.Plus;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.pierrejacquier.olim.data.User;
import com.pierrejacquier.olim.helpers.DbHelper;
import com.pierrejacquier.olim.helpers.DriveSyncController;
import com.pierrejacquier.olim.helpers.NewerDatabaseCallback;
import com.google.android.gms.auth.api.Auth;

public class Olim extends Application implements NewerDatabaseCallback {
    private User currentUser;
    private DriveSyncController googleSync;
    private DbHelper database;
    private boolean readContactsAllowed;

    public DriveSyncController getGoogleSync() {
        return googleSync;
    }

    public void setGoogleSync(Context context, boolean googleAuthorized) {
        this.googleSync = DriveSyncController
                .get(context, this.database, this, this, googleAuthorized)
                .setDebug(true);
    }

    public boolean isReadContactsAllowed() {
        return readContactsAllowed;
    }

    public void setReadContactsAllowed(boolean readContactsAllowed) {
        this.readContactsAllowed = readContactsAllowed;
    }

    public DbHelper getDatabase() {
        return database;
    }

    public void setDatabase(Context context) {
        this.database = new DbHelper(context);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void wipeData() {
        setCurrentUser(null);
        Plus.AccountApi.clearDefaultAccount(getGoogleSync().getGoogleApiClient());
        getGoogleSync().getGoogleApiClient().disconnect();
        this.googleSync = null;
        this.database.clearDatabase();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }

                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()

                return super.placeholder(ctx, tag);
            }
        });
    }

    @Override
    public void driveNewer() {

    }

    @Override
    public void localNewer() {

    }
}