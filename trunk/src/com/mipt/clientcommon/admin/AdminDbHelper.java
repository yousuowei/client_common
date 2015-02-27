package com.mipt.clientcommon.admin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class AdminDbHelper extends SQLiteOpenHelper {
	private static final String TAG = "AdminDbHelper";

	private static final String DB_NAME = "user";
	private static final int DB_VERSION = 1;

	private static final String TABLE_ADMIN = "t_admin";

	private Context context;
	private SQLiteDatabase db;
	private static AdminDbHelper instance = null;

	public static AdminDbHelper getInstance(Context context) {
		if (instance == null) {
			synchronized (AdminDbHelper.class) {
				if (instance == null) {
					instance = new AdminDbHelper(context);
				}
			}
		}
		return instance;
	}

	private AdminDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
		this.db = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_ADMIN + "(" + UserColumns._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT," + UserColumns.USER
				+ " TEXT NOT NULL," + UserColumns.PASSWORD + " TEXT,"
				+ UserColumns.PASSPORT + " TEXT NOT NULL" + ")");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMIN);
			onCreate(db);
		}

	}

	private static class UserColumns implements BaseColumns {
		public static final String USER = "user";
		public static final String PASSWORD = "password";
		public static final String PASSPORT = "passport";
	}

	public void saveUser(String user, String password, String passport) {
		clearOldUser();
		ContentValues values = new ContentValues();
		values.put(UserColumns.USER, user);
		values.put(UserColumns.PASSWORD, password);
		values.put(UserColumns.PASSPORT, passport);
		db.insert(TABLE_ADMIN, UserColumns.USER, values);
	}

	private void clearOldUser() {
		db.delete(TABLE_ADMIN, null, null);
	}

	public void deleteUser() {
		clearOldUser();
	}

	public String getPassport() {
		Cursor c = db.query(TABLE_ADMIN, new String[] { UserColumns.PASSPORT },
				null, null, null, null, null);
		if (c == null) {
			return null;
		}
		boolean exist = c.moveToFirst();
		if (!exist) {
			c.close();
			return null;
		}
		String passport = c.getString(0);
		c.close();
		return passport;
	}

	public String getPassword() {
		Cursor c = db.query(TABLE_ADMIN, new String[] { UserColumns.PASSWORD },
				null, null, null, null, null);
		if (c == null) {
			return null;
		}
		boolean exist = c.moveToFirst();
		if (!exist) {
			c.close();
			return null;
		}
		String password = c.getString(0);
		c.close();
		return password;
	}

	public boolean isPassportExist() {
		String passport = getPassport();
		if (passport == null || passport.trim().length() <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * {user,password,passport}
	 * 
	 * @return
	 * @author: herry
	 * @date: 2014年12月29日 下午3:17:29
	 */
	public String[] getUser() {
		Cursor c = db.query(TABLE_ADMIN, null, null, null, null, null, null);
		if (c == null) {
			return null;
		}
		boolean exist = c.moveToFirst();
		if (!exist) {
			c.close();
			return null;
		}
		int indexOfUser = c.getColumnIndex(UserColumns.USER);
		int indexOfPassword = c.getColumnIndex(UserColumns.PASSWORD);
		int indexOfPassport = c.getColumnIndex(UserColumns.PASSPORT);
		String user = c.getString(indexOfUser);
		String password = c.getString(indexOfPassword);
		String passport = c.getString(indexOfPassport);
		return new String[] { user, password, passport };
	}

	public void updatePassport(String newPassport) {
		ContentValues values = new ContentValues(1);
		values.put(UserColumns.PASSPORT, newPassport);
		// this table *ONLY* has one record
		db.update(TABLE_ADMIN, values, null, null);
	}

	public void updatePassword(String newPassword) {
		ContentValues values = new ContentValues(1);
		values.put(UserColumns.PASSWORD, newPassword);
		// this table *ONLY* has one record
		db.update(TABLE_ADMIN, values, null, null);
	}

	public void updateUser(String newUser) {
		ContentValues values = new ContentValues(1);
		values.put(UserColumns.USER, newUser);
		// this table *ONLY* has one record
		db.update(TABLE_ADMIN, values, null, null);
	}

}
