package com.mail.bsecure.common;

import android.os.Environment;

public class Constants {

	public static final int CONTACT_PICKER = 600;

	public static final int CONTENT_INSTALL = 601;

	public static final int CONTENT_FSHARE = 602;

	public static final String ITEM = "item.xml";

	public static String PATH = Environment.getExternalStorageDirectory()
			.toString();

	public static final String DWLPATH = PATH + "/airtel/downloads/";

	public static final String CACHEPATH = PATH + "/airtel/cache/";

	public static final String CACHEDATA = CACHEPATH + "data/";

	public static final String CACHETEMP = CACHEPATH + "temp/";

	public static final String CACHEIMAGE = CACHEPATH + "images/";

}
