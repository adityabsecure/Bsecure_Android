package com.mail.bsecure.database;

import java.util.Enumeration;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mail.bsecure.common.Item;

public class App_Table {
	private Database database = null;
	Vector<Item> values = new Vector<Item>();

	public App_Table(Context context) {
		database = new Database(context);
	}

	public boolean isRecordExits(String id) {
		boolean isExits = false;
		try {
			if (database != null) {
				SQLiteDatabase db = database.getWritableDatabase();
				String iwhereClause = "CID=" + id;
				Cursor cursor = db.query("DOWNLOADQUEUE", null, iwhereClause,
						null, null, null, null);

				if (cursor.getCount() > 0)
					isExits = true;

				cursor.close();
				db.close();
			}
		} catch (Exception e) {

		}
		return isExits;
	}

	/**
	 * insert - This method is called to insert a record.
	 * 
	 * @param String
	 */
	@SuppressWarnings("unchecked")
	public long addTrack(Item item) {
		if (database != null) {
			SQLiteDatabase db = database.getWritableDatabase();

			ContentValues cv = new ContentValues();

			Enumeration<String> keys = item.getAllAttributes().keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement().toString();
				String value = item.getAttribValue(key).toString();
				cv.put(key, value);
			}

			long rawId = db.insert("DOWNLOADQUEUE", null, cv);
			db.close();

			return rawId;

		}
		return -1;
	}

	public Item getTrackFromQueue() {
		Item data = null;
		try {
			if (database != null) {
				SQLiteDatabase db = database.getWritableDatabase();

				Cursor cursor = db
						.rawQuery(
								"SELECT * FROM DOWNLOADQUEUE ORDER BY ROWID ASC LIMIT 1",
								null);
				if (cursor.moveToFirst()) {

					do {
						data = new Item("data");
						String[] resultsColumns = cursor.getColumnNames();
						for (int i = 0; i < resultsColumns.length; i++) {
							String key = resultsColumns[i];
							String value = cursor.getString(cursor
									.getColumnIndexOrThrow(resultsColumns[i]));
							if (value != null)
								data.setAttribute(key, value);
						}
						break;

					} while (cursor.moveToNext());
				}
				cursor.close();
				db.close();
			}
		} catch (Exception e) {

		}
		return data;
	}

	/**
	 * removeInstalledApp - This method is called to remove any particular table
	 * record base on 'CID'.
	 * 
	 * @param String
	 */
	public void removeTrack(String id) {
		try {
			if (database != null) {
				SQLiteDatabase db = database.getWritableDatabase();
				String iwhereClause = "CID = '" + id + "'";
				int i = db.delete("DOWNLOADQUEUE", iwhereClause, null);
				if (i > 0) {
					// Log.e("AIRTEL", "Deleted a record with ID : " + id);
				} else {
					// Log.e("AIRTEL", "Error while deleting a  record ");
				}
				db.close();
			}

		} catch (Exception e) {

		}
	}

	public int getCount() {
		int count = 0;
		try {
			if (database != null) {
				SQLiteDatabase db = database.getWritableDatabase();
				Cursor cursor = db
						.rawQuery("select * from DOWNLOADQUEUE", null);
				if (cursor != null) {
					cursor.moveToFirst();
					count = cursor.getCount();
				}
				db.close();
			}

		} catch (Exception e) {
		}
		return count;
	}

	/**
	 * delete - delete complete table from the database.
	 */
	public void deleteAllTracks() {

		if (database != null) {
			SQLiteDatabase db = database.getWritableDatabase();
			db.delete("DOWNLOADQUEUE", null, null);
			db.close();

		}
	}

	/**
	 * update - This method is called to update any particular table record base
	 * on 'CID'.
	 * 
	 * @param String
	 */
	public void update(String CID) {

		if (database != null) {
			String iwhereClause = "CID=" + CID;

			SQLiteDatabase db = database.getWritableDatabase();
			ContentValues cv = new ContentValues();
			db.update("app_detail", cv, iwhereClause, null);
			db.close();
		}
	}

	/**
	 * close - close the database
	 */
	public void close() {
		database.close();
	}

	public void insertDownloadedTrack(String query) {
		try {
			if (database != null) {
				SQLiteDatabase db = database.getWritableDatabase();
				db.execSQL(query);
				db.close();
			}

		} catch (Exception e) {

		}
	}

}
