package com.pierrejacquier.olim.data;

import com.pierrejacquier.olim.helpers.Tools;

import java.lang.reflect.Field;
import java.util.HashMap;

import im.delight.android.ddp.db.Document;

public class Tag {
    private String id;
    private String owner;
    private String name;
    private String comments;
    private String color;
    private String icon;

    public Tag(Document tagDoc) {
        HashMap<String, Object> tag = Tools.getMap(tagDoc);

        this.id = Tools.getString(tag, "_id");
        this.owner = Tools.getString(tag, "owner");
        this.name = Tools.getString(tag, "name");
        this.comments = Tools.getString(tag, "comments");
        this.color = Tools.getString(tag, "color");
        this.icon = Tools.getString(tag, "icon");
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getComments() {
        return comments;
    }

    public String getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
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
