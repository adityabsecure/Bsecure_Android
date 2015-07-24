package com.mail.bsecure.imageloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ImageDatabase extends SQLiteOpenHelper {

	private final static String APP_DATABASE_NAME = "imagedatabase.db";
	private final static int APP_DATABASE_VERSION = 1;

	final String CREATE_TABLE = "CREATE TABLE IMAGES(IMAGEURL TEXT, TIMESTAMP TEXT);";

	/**
	 * Database - Constructor with context as parameter.
	 */
	public ImageDatabase(Context context) {
		super(context, APP_DATABASE_NAME, null, APP_DATABASE_VERSION);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
	 *      int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

}
