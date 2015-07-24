package com.mail.bsecure.imageloader;

import java.io.File;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class ImageService extends IntentService {

	private int cacheDays = 1;
	private int folderSize = 2;
	private int filterMode = 1; // 0 = all & 1 = individual
	private ImageTable table = null;

	public ImageService() {
		super("shadab");
	}

	@Override
	public void onCreate() {
		getAttr();
		super.onCreate();
	}

	private void getAttr() {
		try {
			table = new ImageTable(this);

			ApplicationInfo ai = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			if (bundle == null) {
				return;
			}

			cacheDays = (Integer) bundle.get("cacheDays");
			folderSize = (Integer) bundle.get("folderSize");
			filterMode = (Integer) bundle.get("filterMode");

		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		cacheDays();
		onFolderSize();
	}

	private void cacheDays() {
		switch (filterMode) {
		case 0:
			ifAll();
			break;

		case 1:
			ifIndividual();
			break;

		default:
			break;
		}
	}

	private void ifAll() {

		try {
			File file = new File(FileCache.CACHEPATH);
			if (!file.exists())
				return;

			String timestamp = convertTime(file.lastModified());
			String curtimestamp = getDateTime();

			String diff = dateDiff(curtimestamp, timestamp); // Current date,
																// Timestamp
			if (diff.contains("days")) {
				diff = diff.replace("day", "");
				diff = diff.trim();
				int diffInt = Integer.parseInt(diff);
				if (diffInt >= cacheDays) {
					deleteFolder(file);
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}

	}

	private void ifIndividual() {
		try {
			Vector<ImageItem> vector = table.getAllImagesInfo();
			for (int i = 0; i < vector.size(); i++) {
				ImageItem item = vector.get(i);
				String timestamp = item.getAttribute("TIMESTAMP");
				String curtimestamp = getDateTime();

				String diff = dateDiff(curtimestamp, timestamp); // Current
																	// date,
																	// Timestamp
				if (diff.contains("days")) {
					diff = diff.replace("day", "");
					diff = diff.trim();
					int diffInt = Integer.parseInt(diff);
					if (diffInt >= cacheDays) {
						deleteImage(item);
					}
				} else if ((diff.contains("mins") || diff.contains("secs"))
						&& cacheDays == 0) {
					deleteImage(item);
				}
			}

		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	private void deleteImage(ImageItem item) {
		String imagePath = item.getAttribute("IMAGEURL");
		delete(FileCache.CACHEPATH + imagePath);
		table.deleteImage(imagePath);
	}

	private void delete(String path) {
		File file = new File(path);
		file.delete();
	}

	private boolean deleteFolder(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteFolder(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private void onFolderSize() {

		File file = new File(FileCache.CACHEPATH);

		long size = getDirectorySize(file);

		double sizeGB = (double) size / (1024 * 1024 * 1024);

		if (sizeGB > folderSize)
			deleteFolder(new File(FileCache.CACHEPATH));

	}

	private long getDirectorySize(File directory) {
		long size = 0;
		if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				if (file.isFile()) {
					size += file.length();
				} else
					size += getDirectorySize(file);
			}
		} else if (directory.isFile()) {
			size += directory.length();
		}
		return size;
	}

	private String dateDiff(String curtimestamp, String timestamp) {

		String m_strStatusAt = "";

		try {

			long diff = 0;
			long diffSeconds = 0;
			long diffMinutes = 0;
			long diffHours = 0;
			long diffDays = 0;

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"dd/MM/yyyy HH:mm:ss", Locale.US);

			Date d = dateFormat.parse(timestamp);
			Date d1 = dateFormat.parse(curtimestamp);

			Calendar calendar = Calendar.getInstance();
			Calendar calendar1 = Calendar.getInstance();

			calendar.setTime(d);
			calendar1.setTime(d1);

			long milliseconds = calendar.getTimeInMillis();
			long milliseconds1 = calendar1.getTimeInMillis();

			diff = milliseconds1 - milliseconds;
			diffDays = diff / (24 * 60 * 60 * 1000);
			diffHours = diff / (60 * 60 * 1000);
			diffMinutes = diff / (60 * 1000);
			diffSeconds = diff / 1000;

			if (diffDays > 0) {
				m_strStatusAt = diffDays + " day";
			} else if (diffHours > 0) {
				m_strStatusAt = diffHours + " hours";
			} else if (diffMinutes > 0) {
				m_strStatusAt = diffMinutes + " mins";
			} else if (diffSeconds > 0) {
				m_strStatusAt = diffSeconds + " secs";
			}
			return m_strStatusAt;
		} catch (ParseException e) {
			//e.printStackTrace();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return m_strStatusAt;
	}

	private String getDateTime() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
				Locale.US);
		return format.format(date);
	}

	private String convertTime(long time) {
		Date date = new Date(time);
		Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
		return format.format(date).toString();
	}

}
