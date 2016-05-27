package com.pierrejacquier.olim.helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import im.delight.android.ddp.db.Document;

public final class Tools {

    public static HashMap<String, Object> getMap(Document doc) {
        HashMap<String, Object> obj = new HashMap<>();
        if(doc != null) {
            for (String f : doc.getFieldNames()) {
                obj.put(f, doc.getField(f));
            }
            obj.put("_id", doc.getId());
        }
        return obj;
    }

    public static HashMap<String, Object> getMap(Object doc) {
        HashMap<String, Object> obj = new HashMap<>();
        obj = (HashMap<String, Object>) doc;
        return obj;
    }

    public static String dispDuration(long sec) {
        return String.format("%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60);
    }

    public static String dispDuration(Object str) {
        int sec = 0;
        try{
            sec = Integer.parseInt(str.toString());
        } catch (Exception e) {
            return "";
        }
        return String.format("%02d:%02d:%02d", sec / 3600, (sec % 3600) / 60, sec % 60);
    }

    public static String dispDate(Object str) {
        HashMap<String, Long> t = (HashMap<String, Long>) str;
        long timestamp = 0;
        try {
            timestamp = t.get("$date");
        } catch (Exception e ) {}
        Date date = new Date(timestamp);
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static String dispDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    public static Date getDate(Object dateObj) {
        try {
            Long t = (Long) dateObj;
            return new Date(t);
        } catch (Exception e) {}
        HashMap<String, Long> t = (HashMap<String, Long>) dateObj;
        long timestamp = 0;
        try {
            timestamp = t.get("$date");
        } catch (Exception e ) {}
        return new Date(timestamp);
    }

    public static String dispType(String t) {
        switch (t) {
            case "wk": return "Entrainement";
            case "rc": return "Compétition";
            case "nth": return "Repos";
            default: return "";
        }
    }

    public static String dispSupport(String s) {
        try {
            switch (s) {
                case "mtb":
                    return "VTT";
                case "road":
                    return "Route";
                case "run":
                    return "Course à pied";
                case "ht":
                    return "Home Trainer";
                case "swim":
                    return "Natation";
                case "skix":
                    return "Ski de fond";
                case "endr":
                    return "Enduro";
                case "othr":
                    return "Autre";
                default:
                    return "";
            }
        } catch(Exception e ) { return "";}
    }

    public static String getString(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? String.valueOf(obj.get(key)) : "";
    }

    public static int getInt(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? Integer.valueOf(obj.get(key).toString()) : 0;
    }

    public static float getFloat(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? Float.valueOf(obj.get(key).toString()) : 0;
    }

    public static long getLong(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? Long.valueOf(String.valueOf(obj.get(key).toString())) : 0;
    }

    public static Date getDate(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? getDate(obj.get(key)) : new Date();
    }

    public static boolean getBoolean(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) && Boolean.valueOf(obj.get(key).toString());
    }

    public static Object getObject(HashMap<String, ?> obj, String key) {
        return obj.containsKey(key) ? obj.get(key) : new Object();
    }

    public static String[] getStringsArray(HashMap<String, ?> obj, String key) {
        String[] r = {};
        if(obj.containsKey(key)) {
            ArrayList<String> a = (ArrayList<String>) obj.get(key);
            return a.toArray(new String[a.size()]);
        } else {
          return r;
        }
    }

    public static Object getObject(HashMap<String, ?> obj, String key, boolean json) {
        if(obj.containsKey(key)) {
            return obj.get(key);
        } else if(json) {
            return new HashMap<String, Object>();
        } else {
            return new Object();
        }
    }

    public static void setStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}