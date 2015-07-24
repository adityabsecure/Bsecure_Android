package com.mail.bsecure.tasks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mail.bsecure.callbacks.IRequestCallback;
import com.mail.bsecure.common.Item;

public class HTTPost extends BackgroundTask {

	private HttpURLConnection conn = null;
	private String requestUrl = null;

	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary = "*****";

	DataOutputStream outputStream = null;
	
	private long length = 0;

	private String path = null;

	private Item header = null;

	private IRequestCallback callback = null;

	private Context context = null;

	private String title = "";

	private String imageId = "";
	
	private String imageName="";
	
	long mDownloadStartTime = 0;
	
	private boolean multipart = true;
	  private PrintWriter writer;
	  private static final String LINE_FEED = "\r\n";
	  String charset = "UTF-8";

	public HTTPost(IRequestCallback callback, Context context, Item headers) {
		this.header = headers;
		this.callback = callback;
		this.context = context;
	}
	
	public void enableMutipart(boolean multipart){
		this.multipart = multipart;
	}

	 public void addFormField(String name, String value) {
	        writer.append("--" + boundary).append(LINE_FEED);
	        writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
	        writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
	        writer.append(LINE_FEED);
	        writer.append(value).append(LINE_FEED);
	        writer.flush();
	    }
	 
	 public void addFilePart(String fieldName, File uploadFile)
	            throws IOException {
	        String fileName = uploadFile.getName();
	        writer.append("--" + boundary).append(LINE_FEED);
	        writer.append("Content-Disposition: form-data; name=\"" + fieldName+ "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
	        writer.append("Content-Type: "+ URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
	        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
	        writer.append(LINE_FEED);
	        writer.flush();
	 
	        FileInputStream inputStream = new FileInputStream(uploadFile);
	        byte[] buffer = new byte[4096];
	        int bytesRead = -1;
	        while ((bytesRead = inputStream.read(buffer)) != -1) {
	            outputStream.write(buffer, 0, bytesRead);
	        }
	        outputStream.flush();
	        inputStream.close();
	         
	        writer.append(LINE_FEED);
	        writer.flush();    
	    }
	 
	@Override
	protected Integer doInBackground(String... params) {

		requestUrl = params[0];

		path = params[1];
		try {
			if(multipart){
			
			imageName=params[3];
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		int bytesAvailable;

		requestUrl = urlEncode(requestUrl);

		FileInputStream fileInputStream = null;

		try {

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
							String value = cursor.getString(cursor
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

			//length = bytesAvailable / 1024;

			conn = getHTTPConnection(requestUrl);

			if (conn == null)
				return 1;
			
			mDownloadStartTime = System.currentTimeMillis();

			outputStream = new DataOutputStream(conn.getOutputStream());
			writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
			if(multipart){
				
	            addFormField("imagename", imageName);
	            
				outputStream.writeBytes(twoHyphens + boundary + lineEnd);
				String data="Content-Disposition: form-data; name=\"uploadedfile\";filename=\""+ path + "\"" + lineEnd;
				outputStream.writeBytes(data);
				outputStream.writeBytes(lineEnd);	
			}			

			byte[] data = new byte[1024];
			int bytesRead = 0;
			long totalRead = 0;

			while ((bytesRead = fileInputStream.read(data)) != -1)

			{
				totalRead += bytesRead;
				long totalReadInKB = totalRead / 1024;
				//publishProgress((long)totalReadInKB);
				
				outputStream.write(data, 0, bytesRead);
				
				if( this.length > 0 )
			    {
					Long[] progress = new Long[5];
					progress[0] = (long)((double)totalRead/(double)this.length * 100.0);
				    progress[1] = totalRead;
				    progress[2] = this.length;
				    
				    double elapsedTimeSeconds = (System.currentTimeMillis() - mDownloadStartTime) / 1000.0;
				     
				     // Compute the avg speed up to now
				     double bytesPerSecond = totalRead / elapsedTimeSeconds; 
				     
				     // How many bytes left?
				     long bytesRemaining = this.length - totalRead;
				     
				     double timeRemainingSeconds;
				     
				     if( bytesPerSecond > 0 )
				     {
				      timeRemainingSeconds = bytesRemaining / bytesPerSecond;
				     }
				     else
				     {
				      // Infinity so set to -1
				      timeRemainingSeconds = -1.0;
				     }
				     
				     progress[3] = (long)(elapsedTimeSeconds * 1000);
				     
				     progress[4] =  (long)(timeRemainingSeconds * 1000);
				     
				     publishProgress(progress);
			    }
				
			}

			if(multipart){
				outputStream.writeBytes(lineEnd);
				outputStream.writeBytes(twoHyphens + boundary + twoHyphens+ lineEnd);	
			}			

			// Responses from the server (code and message)
			int serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

			//Log.e("serverResponseCode", serverResponseCode + "");			
			//Log.e("serverResponseMessage", serverResponseMessage+"");

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
				String response = new String(bytebuf, "UTF-8");

				//Log.e("response : ", response + "");

				response = response.toLowerCase();
				//<job><success>true</success></job>
				if(response.contains("<image>")){
				if (response.contains("<image><id>")) {													
					int index = response.indexOf("<image><id>") + 11;
					int lastIndex = response.indexOf("</id></image>");
					imageId = response.substring(index, lastIndex);
					// callback.onRequestComplete("success", imageId);
//				} else if(response.contains("<success>true</success>")){
				} }else if(response.contains("<success>")){
					int index = response.indexOf("<success>") + 9;
					int lastIndex = response.indexOf("</success>");
					imageId = response.substring(index, lastIndex);
					
				} else {
					return 8;
					// callback.onRequestFailed("Failed to upload");
				}
			} else {
				return 8;
				// callback.onRequestFailed("Failed to upload");
			}

			outputStream.flush();

			return 0;

		} catch (MalformedURLException me) {
			me.printStackTrace();
			return 4;
		}

		catch (ConnectException e) {
			e.printStackTrace();
			return 3;
		}

		catch (SocketException se) {
			se.printStackTrace();
			return 2;
		}

		catch (SocketTimeoutException stex) {
			stex.printStackTrace();
			return 2;
		}

		catch (Exception ex) {
			// Toast.makeText(context, ex.getMessage(),
			// Toast.LENGTH_LONG).show();
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
				e.printStackTrace();
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
				callback.onRequestFailed("Server not reachable.!");
			if (result == 2)
				callback.onRequestFailed("Server connection timeout.!");
			if (result == 6)
				callback.onRequestFailed("Server not reachable.!!");
			if (result == 4)
				callback.onRequestFailed("Invalid URL");
			if (result == 1)
				callback.onRequestFailed("Invalid server response : ");
			if (result == 5)
				callback.onRequestFailed("No Internet, Please check your network settings");
			if (result == 7)
				callback.onRequestFailed("Server not reachable.!!!");
			if (result == 8)
				callback.onRequestFailed("Failed to upload.!!!");
			return;
		}
		callback.onRequestComplete("success", imageId);
		// callback.onRequestComplete("", "");

	}

	@Override
	protected void onProgressUpdate(Long... values) {
		callback.onRequestProgress(values);
	}

	@Override
	protected void onCancelled() {
		callback.onRequestCancelled("Your Cancelled");
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
	public HttpURLConnection getHTTPConnection(String url) throws Exception {

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
					_conn.setRequestMethod("POST");
					_conn.setRequestProperty("Connection", "Keep-Alive");
					if(multipart)
					_conn.setRequestProperty("Content-Type",
							"multipart/form-data;boundary=" + boundary);
					else
						_conn.setRequestProperty("Content-Type",
								"application/xml");
						
					_conn.setReadTimeout(120000);
					_conn.setConnectTimeout(120000);

					if (header != null) {
						
//						if(!(header.containKey("jobId") || header.containKey("image"))){
						Hashtable requestProperty = header.getAllAttributes();
						Enumeration keys = requestProperty.keys();
						while (keys.hasMoreElements()) {
							String key = keys.nextElement().toString();
							String value = requestProperty.get(key).toString();
							_conn.setRequestProperty(key, value);
//						}
					}
					}
					_conn.connect();

					return _conn;
				}
			}

			catch (MalformedURLException me) {
				me.printStackTrace();
				throw me;
			}

			catch (SocketTimeoutException se) {
				se.printStackTrace();
				_conn = null;
				if (i != 0)
					throw se;
			}

			catch (Exception e) {
				e.printStackTrace();
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

}
