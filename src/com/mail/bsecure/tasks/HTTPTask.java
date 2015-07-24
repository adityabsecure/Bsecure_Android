package com.mail.bsecure.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.Item;
import com.mail.bsecure.utils.Utils;

public class HTTPTask {

	private Context context = null;
	private IItemHandler callback = null;
	private int requestId = -1;
	private boolean progressFlag = true;
	private int RESCODE = 0;
	static Item item = null;
	
	private String ids  = "";
		
	public HTTPTask(Context context, IItemHandler callback) {
		this.context = context;
		this.callback = callback;
	}
	
	public void setIds(String ids) {
		this.ids = ids;
	}

	public void disableProgress() {
		progressFlag = false;
	}

	public static void setHeaders(Item aItem) {
			item = aItem;
	}

	public static Item getHeaders() {
		return item;
	}

	public void userRequest(String progressMsg, int requestId, final String url) {
		this.requestId = requestId;
		
		if (progressFlag)
			Utils.showProgress(progressMsg, context);

		if (!isNetworkAvailable()) {
			showUserActionResult(-1, context.getString(R.string.nipcyns));
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				HttpURLConnection conn = null;
				InputStream inputStream = null;

				try {

					String link = urlEncode(url);
					
					//Log.e("link::: ", link+"");
					
					conn = getHTTPConnection(link);

					if (conn == null) {
						postUserAction(-1, context.getString(R.string.isr));
						return;
					}

					inputStream = conn.getInputStream();

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

					//Log.e("response link::: ", response+"");
					
					postUserAction(0, response);

				} catch (MalformedURLException me) {
					
					postUserAction(-1, context.getString(R.string.iurl));
				}

				catch (ConnectException e) {
					
					postUserAction(-1, context.getString(R.string.snr1));
				}

				catch (SocketException se) {
					
					postUserAction(-1, context.getString(R.string.snr2));
				}

				catch (SocketTimeoutException stex) {
					stex.printStackTrace();
					postUserAction(-1, context.getString(R.string.sct));
				}

				catch (Exception ex) {
					ex.printStackTrace();
					postUserAction(-1, context.getString(R.string.snr3));
				}

				finally {
					if (inputStream != null)
						try {
							inputStream.close();
							inputStream = null;
						} catch (IOException e) {
							
						}
					if (conn != null)
						conn.disconnect();
					conn = null;
				}
			}
		}).start();
	}

	private void postUserAction(final int status, final String response) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				
				showUserActionResult(status, response);
			}
		});
	}

	private void showUserActionResult(int status, String response) {

		Utils.dismissProgress();
		
		

		switch (status) {
		case 0:
			callback.onFinish(response, requestId);
			break;

		case -1:
			callback.onError(response, requestId);
			break;

		default:
			break;
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

	/**
	 * 
	 * @param url
	 *            - URL to establish connection
	 * @return HttpURLConnection
	 * @throws Exception
	 *             if URL Malformed or SocketTimeout or Any other Exception.
	 */
	@SuppressWarnings("rawtypes")
	public HttpURLConnection getHTTPConnection(String url) throws Exception {
		HttpURLConnection _conn = null;
		URL serverAddress = null;
		URL hostAddress = null;
		int socketExepCt = 0;
		int ExepCt = 0;
		int numRedirect = 0;

		hostAddress = new URL(url);
		String host = "http://" + hostAddress.getHost() + "/";

		for (int i = 0; i <= 5; i++) {

			try {

				serverAddress = new URL(url);

				_conn = (HttpURLConnection) serverAddress.openConnection();
				if (_conn != null) {
					_conn.setRequestMethod("GET");
					_conn.setDoOutput(false);
					_conn.setReadTimeout(30000);
					_conn.setConnectTimeout(10000);
					_conn.setInstanceFollowRedirects(false);

					if (item != null) {
						Enumeration keys = item.keys();
						while (keys.hasMoreElements()) {
							String key = keys.nextElement().toString();
							String value = item.get(key).toString();
							_conn.setRequestProperty(key, value);

						}
					}
					
					if(ids.length() > 0) {
						_conn.setRequestProperty("X-BSECURE-IDS", ids+"");	
					}
					

					RESCODE = 0;
					_conn.connect();

					RESCODE = _conn.getResponseCode();
					if (RESCODE == HttpURLConnection.HTTP_OK) {
						return _conn;
					} else if (RESCODE == HttpURLConnection.HTTP_MOVED_TEMP
							|| RESCODE == HttpURLConnection.HTTP_MOVED_PERM) {
						if (numRedirect > 15) {
							_conn.disconnect();
							_conn = null;
							break;
						}

						numRedirect++;
						i = 0;
						url = _conn.getHeaderField("Location");
						if (!url.startsWith("http")) {
							url = host + url;
						}
						// Log.e("redirection url : ", url);
						_conn.disconnect();
						_conn = null;
						continue;

					} else {
						_conn.disconnect();
						_conn = null;
					}
				}
			}

			catch (MalformedURLException me) {
				
				throw me;
			}

			catch (SocketTimeoutException se) {
				
				_conn = null;
				if (i >= 5)
					throw se;
			}

			catch (SocketException s) {
				s.printStackTrace();
				if (socketExepCt > 10) {
					_conn = null;
					throw s;
				}
				socketExepCt++;
				i = 0;
				continue;
			}

			catch (Exception e) {
				

				if (ExepCt > 10) {
					_conn = null;
					throw e;
				}
				ExepCt++;
				i = 0;
				continue;

			}
		}
		return null;
	}

	/**
	 * urlEncode -
	 * 
	 * @param String
	 * @return String
	 */
	public static String urlEncode(String sUrl) {
		int i = 0;
		String urlOK = "";
		while (i < sUrl.length()) {
			if (sUrl.charAt(i) == ' ') {
				urlOK = urlOK + "%20";
			} else {
				urlOK = urlOK + sUrl.charAt(i);
			}
			i++;
		}
		return (urlOK);
	}		

}
