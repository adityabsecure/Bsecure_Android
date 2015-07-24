package com.mail.bsecure.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.Item;

public class HTTPostJson extends BackgroundTask {

	private HttpURLConnection conn = null;
	private String requestUrl = null;
	private Item header = null;

	private IItemHandler callback = null;
	private String title = "";
	private Context context = null;
	private String path = null;
	private int length = 0;
	String response;
	String contentType = new String();

	private int requestId = 0;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary = "*****";

	DataOutputStream outputStream = null;

	private boolean multipart = true;

	private String postData;

	public HTTPostJson(IItemHandler callback, Context context, Item headers,
			String postData, int requestId) {
		this.header = headers;
		this.callback = callback;
		this.context = context;
		this.postData = postData;
		this.requestId = requestId;
	}

	public void enableMutipart(boolean multipart) {
		this.multipart = multipart;
	}

	@Override
	protected Integer doInBackground(String... params) {

		requestUrl = params[0];

		path = params[1];

		int bytesAvailable;

		requestUrl = urlEncode(requestUrl);

		InputStream fileInputStream = null;

		try {

			if (path != null && path.length() > 0) {
				if (path.startsWith("content://")) {

					Cursor cursor = context.getContentResolver().query(
							Uri.parse(path), null, null, null, null);

					if (cursor != null) {
						while (cursor.moveToNext()) {
							String[] resultsColumns = cursor.getColumnNames();
							for (int i = 0; i < resultsColumns.length; i++) {
								String key = resultsColumns[i];
								if (key.startsWith("_")) {
									key = key.substring(1, key.length());
								}
								String value = cursor
										.getString(cursor
												.getColumnIndexOrThrow(resultsColumns[i]));
								if (value != null) {
									
									if (key.equalsIgnoreCase("data"))
										path = value;
								}
							}
						}
						title = path.substring(path.lastIndexOf("/") + 1,
								path.length());
						cursor.close();
					}
				}

				fileInputStream = new FileInputStream(new File(path));

				bytesAvailable = fileInputStream.available();

				length = bytesAvailable / 1024;
			} else {
				fileInputStream = new ByteArrayInputStream(postData.getBytes());
				
				bytesAvailable = fileInputStream.available();

				length = bytesAvailable / 1024;
			}

			conn = getHTTPConnection(requestUrl, postData, contentType);
			
			if (conn == null)
				return 1;	
			
			if (fileInputStream != null) {
				outputStream = new DataOutputStream(conn.getOutputStream());
				if (multipart) {
					outputStream.writeBytes(twoHyphens + boundary + lineEnd);
					outputStream
							.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\""
									+ path + "\"" + lineEnd);
					outputStream.writeBytes(lineEnd);
					// outputStream.writeBytes("image=");
				}
					byte[] data = new byte[1024];
					int bytesRead = 0, totalRead = 0;

					while ((bytesRead = fileInputStream.read(data)) != -1)

					{
						// update progress bar
						totalRead += bytesRead;
						int totalReadInKB = totalRead / 1024;
						publishProgress((long)totalReadInKB);
						outputStream.write(data, 0, bytesRead);
					}

					if (multipart) {
						outputStream.writeBytes(lineEnd);
						outputStream.writeBytes(twoHyphens + boundary
								+ twoHyphens + lineEnd);
					}
				
			}

			int serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

			if (serverResponseCode == 200) {

				InputStream inputStream = conn.getInputStream();

				byte[] bytebuf = new byte[0x1000];
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				for (;;) {
					int len = inputStream.read(bytebuf);
					if (len < 0)
						break;
					baos.write(bytebuf, 0, len);
				}
				bytebuf = baos.toByteArray();
				response = new String(bytebuf, "UTF-8");

				response = response.replaceAll("\\\\/", "/");
				response = response.toLowerCase(Locale.ENGLISH);
				
				//Log.e("response:::: ", response+"");

			} else {
				return 8;
			}
			return 0;

		} catch (MalformedURLException me) {
			//e.printStackTrace();
			return 4;
		}

		catch (ConnectException e) {
			//e.printStackTrace();
			return 3;
		}

		catch (SocketException se) {
			//e.printStackTrace();
			return 2;
		}

		catch (SocketTimeoutException stex) {
			stex.printStackTrace();
			return 2;
		}

		catch (Exception ex) {
			ex.printStackTrace();
			return 1;
		} finally {

			try {

				if (conn != null)
					conn.disconnect();
				conn = null;

				if (fileInputStream != null)
					fileInputStream.close();
				fileInputStream = null;

				if (outputStream != null)
					outputStream.close();
				outputStream = null;

			} catch (Exception e) {
			
			}

		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (conn != null) {
			conn.disconnect();
			conn = null;
		}

		if (result != 0) {
			if (result == 3)
				callback.onError(context.getString(R.string.snr1), requestId);
			if (result == 2)
				callback.onError(context.getString(R.string.sct), requestId);
			if (result == 6)
				callback.onError(context.getString(R.string.snr2), requestId);
			if (result == 4)
				callback.onError(context.getString(R.string.iurl), requestId);
			if (result == 1)
				callback.onError(context.getString(R.string.isres), requestId);
			if (result == 5)
				callback.onError(context.getString(R.string.nipcyns),requestId);
			if (result == 7)
				callback.onError(context.getString(R.string.snr3), requestId);
			if (result == 8)
				callback.onError(context.getString(R.string.ftu), requestId);			
			
			return;
		}				
		
		callback.onFinish(response, requestId);

	}

	@Override
	protected void onProgressUpdate(Long... values) {

	}

	@Override
	protected void onCancelled() {
		// callback.onRequestCancelled("Your Cancelled");
		super.onCancelled();
	}

	/**
	 * 
	 * @param url
	 *            - URL to establish connection
	 * @return HttpURLConnection
	 * @throws Exception
	 *             if URL Malformed or SocketTimeout or Any other Exception.
	 */
	public HttpURLConnection getHTTPConnection(String url, String postData,
			String contentType) throws Exception {

		HttpURLConnection _conn = null;
		URL serverAddress = null;
		for (int i = 0; i <= 1; i++) {

			try {
				serverAddress = new URL(url);
				_conn = (HttpURLConnection) serverAddress.openConnection();
				if (_conn != null) {

					_conn.setDoInput(true);
					_conn.setDoOutput(true);
					_conn.setUseCaches(false);
					_conn.setRequestProperty("connection", "close");
					_conn.setRequestMethod("POST");

					if (header != null) {						
						Enumeration keys = header.keys();
						while (keys.hasMoreElements()) {
							String key = keys.nextElement().toString();
							String value = header.get(key).toString();
							_conn.setRequestProperty(key, value);
						}
					}

					_conn.setRequestProperty("Connection", "Keep-Alive");
					if (multipart)
						_conn.setRequestProperty("Content-Type",
								"multipart/form-data;boundary=" + boundary);
					else
						_conn.setRequestProperty("Content-Type", contentType);
					
					_conn.setRequestProperty("X-IMI-AUTHKEY", "eZ92W8E8J5");
					
					/*if (postData == null) {
						_conn = null;
						return _conn;
					}
					if (postData.length() > 0) {
						byte[] outputInBytes = postData.getBytes("UTF-8");
						OutputStream os = _conn.getOutputStream();
						os.write(outputInBytes);
					}*/
					// os.close();

					_conn.setReadTimeout(120000);
					_conn.setConnectTimeout(120000);

					_conn.connect();

					return _conn;
				}
			}

			catch (MalformedURLException me) {
				//e.printStackTrace();
				throw me;
			}

			catch (SocketTimeoutException se) {
				//e.printStackTrace();
				_conn = null;
				if (i != 0)
					throw se;
			}

			catch (Exception e) {
				//e.printStackTrace();
				throw e;
			}
		}
		return null;
	}

	/**
	 * urlEncode -
	 * 
	 * @param String
	 *            sUrl
	 * @return String - encoded url
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

	public String getCustomSystemTime() {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
		java.util.Date date = new java.util.Date();
		return dateFormat.format(date);
	}
	
}
