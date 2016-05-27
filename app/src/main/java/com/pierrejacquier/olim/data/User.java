package com.pierrejacquier.olim.data;

import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import im.delight.android.ddp.db.Document;
import com.pierrejacquier.olim.helpers.Tools;

public class User {
    private String id;
    private String username;
    private String fullName;
    private String email;

    public User(Document userDoc) {
        HashMap<String, Object> user = Tools.getMap(userDoc);
        ArrayList<Object> emails = (ArrayList<Object>) user.get("emails");
        HashMap<String, Object> profile = (HashMap<String, Object>) Tools.getObject(user, "profile", true);
        HashMap<String, Object> email = (HashMap<String, Object>) emails.get(0);

        this.id = Tools.getString(user, "_id");
        this.username = Tools.getString(user, "username");
        this.fullName = Tools.getString(profile, "fullName");
        this.email = Tools.getString(email, "address");
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        result.append( this.getClass().getName() );
        result.append( " Object {" );
        result.append(newLine);
        Field[] fields = this.getClass().getDeclaredFields();
        for ( Field field : fields  ) {
            result.append("  ");
            try {
                result.append( field.getName() );
                result.append(": ");
                result.append( field.get(this) );
            } catch ( IllegalAccessException ex ) {  }
            result.append(newLine);
        }
        result.append("}");
        return result.toString();
    }
}
