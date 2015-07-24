package com.mail.bsecure.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.Resources;

import com.bsecure.mail.R;

public class AppSettings {
	
	static AppSettings settings = null;
	
	private Context mContext = null;

	private Properties properties = null;
	
	public static AppSettings getInstance(Context context) {
		if (settings == null)
			settings = new AppSettings(context);
		return settings;
	}
	
	public AppSettings(Context context) {
		this.mContext = context;
		loadProperties();
	}
	
	/**
	 * loadProperties - Loads properties file from raw folder.
	 */
	private void loadProperties() {
		try {
			InputStream rawResource = mContext.getResources().openRawResource(
					R.raw.settings);
			properties = new Properties();
			properties.load(rawResource);
			rawResource.close();
			rawResource = null;
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPropertyValue(String key) {
		return properties.getProperty(key);
	}
	
}
