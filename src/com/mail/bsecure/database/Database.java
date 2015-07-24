package com.mail.bsecure.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

	private final static String APP_DATABASE_NAME = "glo.db";
	private final static int APP_DATABASE_VERSION = 1;

	final String DOWNLOAD_QUEUE = "CREATE TABLE DOWNLOADQUEUE(CID INTEGER, TITLE TEXT, CTYPE TEXT, DOWNLOADURL TEXT);";

	/**
	 * Database - Constructor with context as parameter.
	 */
	public Database(Context context) {
		super(context, APP_DATABASE_NAME, null, APP_DATABASE_VERSION);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DOWNLOAD_QUEUE);
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
