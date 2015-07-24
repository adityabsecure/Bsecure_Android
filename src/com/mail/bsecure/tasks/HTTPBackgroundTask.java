package com.mail.bsecure.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.bsecure.mail.R;
import com.mail.bsecure.cache.CacheManager;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.Constants;
import com.mail.bsecure.common.Item;
import com.mail.bsecure.parsers.JSONParser;
import com.mail.bsecure.utils.Utils;

public class HTTPBackgroundTask extends AsyncTask<String, Integer, Integer>
		implements IItemHandler {

	private HttpURLConnection conn = null;

	private InputStream inputStream = null;

	private IItemHandler handler = null;

	private Context context = null;

	private int length = 0;

	static Item item = null;

	private int parserType = 0;

	private int requestId = 0;

	private boolean state = false;

	private String errorMsg = "";

	private Object obj = null;

	private CacheManager cManager = null;

	private int cacheType = 0;

	private int deleteFileCount = -1;

	private boolean offline = false;

	private final int CACHE_ITEMS_EXTN = 1;
	private final int CACHE_STREAM_EXTN = 2;

	private final int CACHE_ITEMS_INTRN = 3;
	private final int CACHE_STREAM_INTRN = 4;

	private final int CACHE_ITEMS_EXTN_P = 5;
	private final int CACHE_STREAM_INTRN_P = 6;

	private final int CACHE_ITEMS_INTRN_ONLY = 7;
	private final int CACHE_ITEMS_EXTN_ONLY = 8;

	public HTTPBackgroundTask(IItemHandler callback, Context context,
			int parserType, int requestId) {
		this.handler = callback;
		this.context = context;
		this.parserType = parserType;
		this.requestId = requestId;
	}

	public void setDeleteCacheFiles(int pageCount) {
		deleteFileCount = pageCount;
	}

	public void setCacheType(int cache) {
		cacheType = cache;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public static void setHeaders(Item aItem) {
		if (item == null)
			item = aItem;
	}

	@Override
	protected void onCancelled() {
		// handler.onError("You have cancelled request to server", requestId);
		try {
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		super.onCancelled();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {
		String requestUrl = "";
		try {

			requestUrl = params[0];
			requestUrl = Utils.urlEncode(requestUrl);

			cManager = CacheManager.getInstance(context);

			if (deleteFileCount > -1) {
				cManager.deleteCacheFiles(deleteFileCount, requestUrl,
						Constants.CACHETEMP);
			}

			if (!isNetworkAvailable()) {
				if (offline) {
					obj = getOfflineCache(requestUrl);
					if (obj != null)
						return 0;
				}
				return 5;
			}

			obj = getCache(requestUrl);

			if (obj != null)
				return 0;

			//Log.e("requestUrl : ", requestUrl);

			conn = HTTPGetTask.getHTTPConnection(requestUrl);

			if (conn == null) {
				return 1;
			}

			length = conn.getContentLength();
			length = length / 1024;
			inputStream = conn.getInputStream();

			/*if (inputStream != null)
				writeFileData();*/

			parseData(parserType);

			if (state) {
				if (cacheType != 0 && obj != null) {
					if (obj instanceof Item && ((Item) obj).size() > 0) {
						setCache(requestUrl);
					}
				}
				return 0;
			} else
				return 10;

		} catch (MalformedURLException me) {
			return 4;
		}

		catch (ConnectException e) {
			return 3;
		}

		catch (SocketException se) {
			return 6;
		}

		catch (SocketTimeoutException stex) {
			return 2;
		}

		catch (Exception ex) {
			return 1;
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			if (conn != null)
				conn.disconnect();
			conn = null;

		}

	}

	@Override
	protected void onPostExecute(Integer result) {

		try {

			if (result != 0) {
				if (result == 3)
					handler.onError(context.getString(R.string.snr1), requestId);
				if (result == 2)
					handler.onError(context.getString(R.string.sct), requestId);
				if (result == 6)
					handler.onError(context.getString(R.string.snr2), requestId);
				if (result == 4)
					handler.onError(context.getString(R.string.iurl), requestId);
				if (result == 1)
					handler.onError(context.getString(R.string.cerr), requestId);
				if (result == 5)
					handler.onError(context.getString(R.string.nipcyns),
							requestId);
				if (result == 10)
					handler.onError(errorMsg, requestId);				

				return;
			}

			if (obj != null) {
				handler.onFinish(obj, requestId);
				return;
			}
			return;

		} catch (Exception e) {
			
		} 
	}

	/**
	 * checkConnectivity - Checks Whether Internet connection is available or
	 * not.
	 */
	private boolean isNetworkAvailable() {

		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}

		NetworkInfo net = manager.getActiveNetworkInfo();
		if (net != null) {
			if (net.isConnected()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	}

	/*
	 * public void onFinish(Vector<Item> results, int currentPage, int
	 * totalPages, int requestType) { state = true; this.requestId =
	 * requestType; }
	 */

	public void onError(String errorCode, int requestType) {
		state = false;
		errorMsg = errorCode;
		this.requestId = requestType;
	}

	@Override
	public void onFinish(Object results, int requestType) {
		state = true;
		this.requestId = requestType;
		this.obj = results;
	}

	private void parseData(int parserType) throws Exception {
		try {
			switch (parserType) {

			case 1:
				JSONParser jsonParser = new JSONParser(this, requestId);
				jsonParser.parseXmlData(inputStream);
				break;

			default:
				break;
			}

		} catch (Exception e) {
			// e.printStackTrace();
			throw e;
		}
	}

	public Vector<Item> sort(Vector<Item> vector) {

		Vector<Item> temp1 = new Vector<Item>();
		Vector<Item> temp2 = new Vector<Item>();
		Vector<Item> temp3 = new Vector<Item>();

		int count = vector.size();
		for (int i = 1; i <= count; i++) {
			temp1.add(vector.get(count - i));
		}

		for (int i = 0; i < temp1.size(); i++) {
			if ((i % 2) == 0)
				temp2.add(temp1.get(i));
			else
				temp3.add(temp1.get(i));
		}

		temp2.addAll(temp3);

		vector = temp2;
		return vector;
	}

	public int getCACHE_ITEMS_EXTN() {
		return CACHE_ITEMS_EXTN;
	}

	public int getCACHE_STREAM_EXTN() {
		return CACHE_STREAM_EXTN;
	}

	public int getCACHE_ITEMS_INTRN() {
		return CACHE_ITEMS_INTRN;
	}

	public int getCACHE_ITEMS_INTRN_ONLY() {
		return CACHE_ITEMS_INTRN_ONLY;
	}

	public int getCACHE_ITEMS_EXTN_ONLY() {
		return CACHE_ITEMS_EXTN_ONLY;
	}

	public int getCACHE_STREAM_INTRN() {
		return CACHE_STREAM_INTRN;
	}

	public int getCACHE_ITEMS_EXTN_P() {
		return CACHE_ITEMS_EXTN_P;
	}

	public int getCACHE_STREAM_INTRN_P() {
		return CACHE_STREAM_INTRN_P;
	}

	private void setCache(String requestUrl) {
		switch (cacheType) {

		case CACHE_ITEMS_EXTN:
			cManager.cacheObjExtn(requestUrl, obj, Constants.CACHETEMP);
			break;

		case CACHE_STREAM_EXTN:

			break;

		case CACHE_ITEMS_INTRN:
			cManager.cacheObjIntrn(requestUrl, obj);
			break;

		case CACHE_STREAM_INTRN:

			break;

		case CACHE_ITEMS_EXTN_P:
			cManager.cacheObjExtn(requestUrl, obj, Constants.CACHEDATA);
			break;

		case CACHE_STREAM_INTRN_P:

			break;

		case CACHE_ITEMS_INTRN_ONLY:
			cManager.cacheObjIntrn(requestUrl, obj);
			break;

		case CACHE_ITEMS_EXTN_ONLY:
			cManager.cacheObjExtn(requestUrl, obj, Constants.CACHEDATA);
			break;

		default:
			break;
		}
	}

	private Object getCache(String requestUrl) {
		Object object = null;
		try {

			switch (cacheType) {

			case CACHE_ITEMS_EXTN:
				object = cManager
						.getExtnObject(requestUrl, Constants.CACHETEMP);
				break;

			case CACHE_STREAM_EXTN:
				break;

			case CACHE_ITEMS_INTRN:
				object = cManager.getIntrnObj(requestUrl);
				break;

			case CACHE_STREAM_INTRN:
				break;

			case CACHE_ITEMS_EXTN_P:
				object = cManager
						.getExtnObject(requestUrl, Constants.CACHEDATA);
				break;

			case CACHE_STREAM_INTRN_P:
				break;

			default:
				break;
			}
			return object;
		} catch (Exception e) {
			// e.printStackTrace();
			return object;
		}
	}

	private Object getOfflineCache(String requestUrl) {
		Object object = null;
		try {

			switch (cacheType) {

			case CACHE_ITEMS_EXTN_ONLY: // no required
				object = cManager
						.getExtnObject(requestUrl, Constants.CACHEDATA);
				break;

			case CACHE_ITEMS_INTRN:
				object = cManager.getIntrnObj(requestUrl);
				break;

			default:
				break;
			}
			return object;
		} catch (Exception e) {
			// e.printStackTrace();
			return object;
		}
	}

	/*private void writeFileData() { // /////////////////////////////////////////////
		
		try {
			byte[] bytebuf = new byte[0x1000];

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (;;) {
				int len = inputStream.read(bytebuf);
				if (len < 0)
					break;
				baos.write(bytebuf, 0, len);
			}
			bytebuf = baos.toByteArray();
			String response = new String(bytebuf, "UTF-8");
			
			Log.e("response : : :", response+"");

			inputStream = new ByteArrayInputStream(baos.toByteArray());

		} catch (Exception e) {
			e.printStackTrace();
		}
		 
	}*/

	public String getCustomSystemTime() {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
		java.util.Date date = new java.util.Date();
		return dateFormat.format(date);
	}

}