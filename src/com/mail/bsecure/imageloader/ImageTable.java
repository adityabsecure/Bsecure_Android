package com.mail.bsecure.imageloader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ImageTable {
	private ImageDatabase database = null;
	//private String TAG = "Application-DB";
	private SQLiteDatabase db = null;

	public ImageTable(Context context) {
		database = new ImageDatabase(context);
	}

	/**
	 * getInstalledApp - This method will return a record based on 'CID' in the
	 * form of Item .
	 * 
	 * @param String
	 * @return Vector<Item>
	 */
	public String getImageInfo(String imageurl) {
		String value = "";
		try {
			if (database != null) {
				SQLiteDatabase db = database.getWritableDatabase();
				String iwhereClause = "IMAGEURL=" + imageurl;
				Cursor cursor = db.query("IMAGES", null, iwhereClause, null,
						null, null, null);

				if (cursor != null && cursor.moveToFirst()) {
					do {
						value = cursor.getString(cursor
								.getColumnIndexOrThrow("TIMESTAMP"));
					} while (cursor.moveToNext());
					cursor.close();
					db.close();
					return value;
				}
				if (db.isOpen())
					db.close();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return value;
	}

	public Vector<ImageItem> getAllImagesInfo() {
		Vector<ImageItem> values = new Vector<ImageItem>();
		try {

			if (database != null) {
				SQLiteDatabase db = database.getWritableDatabase();
				Cursor cursor = db.query("IMAGES", null, null, null, null,
						null, null);

				if (cursor != null && cursor.moveToFirst()) {

					do {
						ImageItem item = new ImageItem("");
						String[] resultsColumns = cursor.getColumnNames();
						for (int i = 0; i < resultsColumns.length; i++) {
							String key = resultsColumns[i];
							String value = cursor.getString(cursor
									.getColumnIndexOrThrow(resultsColumns[i]));
							if (value != null)
								item.setAttribute(key, value);
						}
						values.add(item);
					} while (cursor.moveToNext());
					cursor.close();
					db.close();
					return values;
				}
				cursor.close();
				db.close();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return values;
	}

	/**
	 * insert - This method is called to insert a record.
	 * 
	 * @param String
	 */
	public long insertORUpdate(String imageurl) {
		long id = -1;
		try {

			if (db == null)
				db = database.getWritableDatabase();

			if (database != null) {
				ContentValues cv = new ContentValues();
				cv.put("IMAGEURL", imageurl);
				cv.put("TIMESTAMP", getDateTime());

				String WHERECLAUSE = "IMAGEURL = " + imageurl;

				long result = db.update("IMAGES", cv, WHERECLAUSE, null);

				if (result == 0) {
					id = db.insert("IMAGES", null, cv);
					if (id != -1) {
						//Log.e(TAG, "Insert a new record");
					} else {
						//Log.e(TAG, "Error while insert a new hash record ");
					}
				} else if (result > 0) {
					// Log.e(TAG, "record updated");
				}
				// db.close();
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return id;
	}

	/**
	 * update - This method is called to update any particular table record base
	 * on 'CID'.
	 * 
	 * @param String
	 */
	public void update(String imageurl) {

		if (database != null) {
			String iwhereClause = "IMAGEURL=" + imageurl;

			SQLiteDatabase db = database.getWritableDatabase();
			ContentValues cv = new ContentValues();
			cv.put("TIMESTAMP", getDateTime());
			db.update("IMAGES", cv, iwhereClause, null);
			db.close();
		}
	}

	public int deleteImage(String imageurl) {
		int i = -1;
		try {
			if (database != null) {
				SQLiteDatabase db = database.getWritableDatabase();
				String iwhereClause = "IMAGEURL = '" + imageurl + "'";
				i = db.delete("IMAGES", iwhereClause, null);
				if (i > 0) {
					//Log.e(TAG, "Deleted a record with SESSION ID : " + imageurl+ " : : " + i);
				} else {
					//Log.e(TAG, "Error while deleting a  record ");
				}
				db.close();
			}
			return i;

		} catch (Exception e) {
			//e.printStackTrace();
			return -1;
		}
	}

	/**
	 * delete - delete complete table from the database.
	 */
	public void delete() {

		if (database != null) {
			SQLiteDatabase db = database.getWritableDatabase();
			db.delete("IMAGES", null, null);
			db.close();
			//Log.d(TAG, "Delete hash record.");
		}
	}

	/**
	 * close - close the database
	 */
	public void close() {
		database.close();
		if (db != null)
			if (db.isOpen())
				db.close();
		db = null;

	}

	private String getDateTime() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
				Locale.US);
		return format.format(date);
	}

}
