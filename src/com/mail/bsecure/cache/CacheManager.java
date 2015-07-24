package com.mail.bsecure.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;

public class CacheManager {

	static CacheManager mCacheManager = null;
	static Context mContext;

	/**
	 * CacheManager - Constructor
	 */
	private CacheManager() {
	}

	/**
	 * getInstance -
	 * 
	 * @param Context
	 * @return CacheManager
	 */
	public static CacheManager getInstance(Context context) {
		if (mCacheManager == null)
			mCacheManager = new CacheManager();
		mContext = context;
		return mCacheManager;
	}

	/**
	 * cacheObjExtn
	 * 
	 * This method saves the Object of type Vector<Object> into file system.
	 * This method is using to save temporary as well as permanently. Data will
	 * be saved in sdcard.
	 * 
	 * @param String
	 *            fileName
	 * @param Object
	 *            object
	 * @param String
	 *            path
	 */
	public void cacheObjExtn(String fileName, Object object, String path) {

		fileName = String.valueOf(fileName.hashCode());

		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(path + "/" + fileName);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			oos.flush();
			oos.close();
		} catch (Exception e) {
			
		}
	}

	/**
	 * cacheObjIntrn
	 * 
	 * This method saves the Object into file system(internally).
	 * 
	 * @param String
	 *            fileName
	 * @param Oject
	 *            object
	 * 
	 */
	public void cacheObjIntrn(String fileName, Object object) {

		fileName = String.valueOf(fileName.hashCode());

		FileOutputStream fileOS = null;

		ObjectOutputStream objectOS = null;

		try {
			fileOS = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			objectOS = new ObjectOutputStream(fileOS);
			objectOS.writeObject(object);
			objectOS.flush();
		} catch (Exception e) {
			
		} finally {

			try {
				if (fileOS != null)
					fileOS.close();
				fileOS = null;

				if (objectOS != null)
					objectOS.close();
				objectOS = null;

			} catch (Exception e2) {
				
			}
		}
	}

	/**
	 * cacheStreamExtn
	 * 
	 * This method saves the InputStream into file system. This method is using
	 * to save temporary as well as permanently. Data will be saved in sdcard.
	 * 
	 * @param String
	 *            fileName
	 * @param InputStream
	 *            inputStream
	 * @param String
	 *            path
	 */
	public void cacheStreamExtn(String fileName, InputStream inputStream,
			String path) {
		FileOutputStream fileOut = null;
		fileName = String.valueOf(fileName.hashCode());
		try {

			String line = "";
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					inputStream));

			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
			}
			file = new File(path + "/" + fileName);

			fileOut = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fileOut, "UTF-8");

			while ((line = rd.readLine()) != null) {
				osw.append(line);
			}

			osw.flush();
			osw.close();

			if (rd != null) {
				rd.close();
				rd = null;
			}

			file = null;

		} catch (Exception e) {
			
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				inputStream = null;

				if (fileOut != null)
					fileOut.close();
				fileOut = null;

			} catch (Exception e) {
				
			}
		}

	}

	public void cacheStreamIntrn(String fileName, InputStream inputStream) {

		FileOutputStream fileOut = null;
		fileName = String.valueOf(fileName.hashCode());
		try {

			String line = "";
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					inputStream));
			fileOut = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fileOut, "UTF-8");

			while ((line = rd.readLine()) != null) {
				osw.append(line);
			}

			osw.flush();
			osw.close();

			if (rd != null) {
				rd.close();
				rd = null;
			}

		} catch (Exception e) {
			
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				inputStream = null;

				if (fileOut != null)
					fileOut.close();
				fileOut = null;

			} catch (Exception e) {
				
			}
		}
	}

	public Object getIntrnObj(String fileName) throws Exception {

		FileInputStream fileIS = null;
		ObjectInputStream objectIS = null;
		Object object = null;

		try {
			fileName = String.valueOf(fileName.hashCode());
			File iFile = mContext.getFileStreamPath(fileName);
			if (iFile.exists()) {
				fileIS = mContext.openFileInput(fileName);
				objectIS = new ObjectInputStream(fileIS);
				object = objectIS.readObject();
				objectIS.close();
				return object;
			}

		} catch (Exception e) {
			
			throw e;
		} finally {

			try {

				if (fileIS != null)
					fileIS.close();
				fileIS = null;

				if (objectIS != null)
					objectIS.close();
				objectIS = null;

			} catch (Exception e) {
				
				throw e;
			}

		}

		return object;
	}

	public Object getExtnObject(String fileName, String path) throws Exception {

		Object object = null;
		FileInputStream fileInputStream = null;
		ObjectInputStream objectIS = null;

		fileName = String.valueOf(fileName.hashCode());

		File file = new File(path + "/" + fileName);
		if (!file.exists()) {
			return object;
		}

		try {
			fileInputStream = new FileInputStream(path + "/" + fileName);
			objectIS = new ObjectInputStream(fileInputStream);
			object = objectIS.readObject();
			objectIS.close();

		} catch (Exception e) {
			
		} finally {

			try {

				if (fileInputStream != null)
					fileInputStream.close();
				fileInputStream = null;

				if (objectIS != null)
					objectIS.close();
				objectIS = null;

			} catch (Exception e) {
				
				throw e;
			}

		}

		return object;
	}

	public InputStream getExtnStream(String fileName, String path) {

		InputStream inputStream = null;
		FileInputStream fileInputStream = null;
		try {

			fileName = String.valueOf(fileName.hashCode());

			File file = new File(path + "/" + fileName);
			if (!file.exists()) {
				return inputStream;
			}
			fileInputStream = new FileInputStream(file);
			inputStream = (InputStream) fileInputStream;

			return inputStream;

		} catch (Exception e) {
			
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				inputStream = null;

				if (fileInputStream != null)
					fileInputStream.close();
				fileInputStream = null;

			} catch (Exception e) {
				
			}
		}
		return inputStream;
	}

	public InputStream getIntrnStream(String fileName) {

		InputStream inputStream = null;
		FileInputStream fileInputStream = null;
		try {

			fileName = String.valueOf(fileName.hashCode());

			File iFile = mContext.getFileStreamPath(fileName);
			if (iFile.exists()) {
				fileInputStream = mContext.openFileInput(fileName);
				inputStream = (InputStream) fileInputStream;
			}

			return inputStream;

		} catch (Exception e) {
			
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				inputStream = null;

				if (fileInputStream != null)
					fileInputStream.close();
				fileInputStream = null;

			} catch (Exception e) {
				
			}
		}
		return inputStream;
	}
	
	public void deleteCacheFiles(int pageCount, String fileName, String path) {

		try {
			
			fileName = fileName.substring(0, fileName.lastIndexOf("pno=")+4);
			
			while (pageCount > -1) {
			
				String fileNameEncode = fileName+pageCount;
				
				fileNameEncode = String.valueOf(fileNameEncode.hashCode());

				File file = new File(path + "/" + fileNameEncode);
				if (file.exists()) {
					file.delete();
				}
				pageCount --;
			}

		} catch (Exception e) {

		}
	}

}