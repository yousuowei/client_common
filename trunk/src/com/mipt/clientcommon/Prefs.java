package com.mipt.clientcommon;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {
    private static Prefs INSTANCE = null;
    private SharedPreferences sharedPreference;
    private Context context;

    public static final int TYPE_INT = 0;
    public static final int TYPE_FLOAT = TYPE_INT + 1;
    public static final int TYPE_STRING = TYPE_INT + 2;
    public static final int TYPE_LONG = TYPE_INT + 3;
    public static final int TYPE_BOOLEAN = TYPE_INT + 4;

    private Prefs(Context context) {
	this.context = context;
	sharedPreference = PreferenceManager
		.getDefaultSharedPreferences(this.context);
    }

    public static Prefs getInstance(Context context) {
	if (INSTANCE == null) {
	    synchronized (Prefs.class) {
		if (INSTANCE == null) {
		    INSTANCE = new Prefs(context);
		}
	    }
	}
	return INSTANCE;
    }

    public void save(int type, String key, Object value) {
	switch (type) {
	case TYPE_INT:
	    sharedPreference.edit().putInt(key, (Integer) value).commit();
	    break;
	case TYPE_FLOAT:
	    sharedPreference.edit().putFloat(key, (Float) value).commit();
	    break;
	case TYPE_STRING:
	    sharedPreference.edit().putString(key, (String) value).commit();
	    break;
	case TYPE_LONG:
	    sharedPreference.edit().putLong(key, (Long) value).commit();
	    break;
	case TYPE_BOOLEAN:
	    sharedPreference.edit().putBoolean(key, (Boolean) value).commit();
	    break;
	default:
	    throw new IllegalArgumentException(
		    "check your object type first !!!");
	}
    }

    public Object get(int type, String key, Object defValue) {
	Object ret = null;
	switch (type) {
	case TYPE_INT:
	    ret = sharedPreference.getInt(key, (Integer) defValue);
	    break;
	case TYPE_FLOAT:
	    ret = sharedPreference.getFloat(key, (Float) defValue);
	    break;
	case TYPE_STRING:
	    ret = sharedPreference.getString(key, (String) defValue);
	    break;
	case TYPE_LONG:
	    ret = sharedPreference.getLong(key, (Long) defValue);
	    break;
	case TYPE_BOOLEAN:
	    ret = sharedPreference.getBoolean(key, (Boolean) defValue);
	    break;
	default:
	    throw new IllegalArgumentException(
		    "check your object type first !!!");
	}
	return ret;
    }
}
