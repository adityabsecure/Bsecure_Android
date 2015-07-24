package com.mail.bsecure.imageloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bsecure.mail.R;

public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService executorService;
	private boolean resize = false;

	private ImageTable table = null;

	private int stub_id = R.drawable.ic_launcher;

	public ImageLoader(Context context, boolean save) {
		fileCache = new FileCache(context, save);
		executorService = Executors.newFixedThreadPool(3);

		table = new ImageTable(context);
	}

	public void setDefaultDrawbale(int drawable) {
		stub_id = drawable;
	}

	public void setResize(boolean resize) {
		this.resize = resize;
	}

	public void DisplayImage(String url, ImageView imageView) {
		DisplayImage(url, imageView, 0);
	}

	public void DisplayImage(String url, ImageView imageView, int defaultSize) { // 0
																					// =
																					// default;
																					// 1
																					// =
																					// 100.
																					// 2
																					// =
																					// 200
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {

			if (resize)
				bitmap = setCustomImage(bitmap);

			imageView.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageView, defaultSize);
			imageView.setImageResource(stub_id);
		}
	}

	/*
	 * Dnt remove below code /*public void displayImageDrwable(String url,
	 * ImageView imageView) { imageViews.put(imageView, url); Bitmap bitmap =
	 * memoryCache.get(url); if (bitmap != null) {
	 * 
	 * Resources resources = context.getResources(); Drawable drawable = new
	 * BitmapDrawable(resources, bitmap);
	 * 
	 * imageView.setImageDrawable(drawable); } else { queuePhoto(url,
	 * imageView); imageView.setImageResource(stub_id); } }
	 * 
	 * private void queuePhoto(String url, ImageView imageView) {
	 * queuePhoto(url, imageView, false); }
	 */

	private void queuePhoto(String url, ImageView imageView, int defaultSize) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		p.setDefaultSize(defaultSize);
		executorService.execute(new PhotosLoader(p));// submit(new
														// PhotosLoader(p));
	}

	private Bitmap getBitmap(String url, int defaultSize) {

		File f = fileCache.getFile(url);
		Bitmap b = decodeFile(f, defaultSize);
		if (b != null)
			return b;

		HttpURLConnection conn = null;

		try {
			Bitmap bitmap = null;
			// String link = url.replaceAll("%26", "&");
			URL imageUrl = new URL(url);

			conn = (HttpURLConnection) imageUrl.openConnection();

			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);

			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			CopyStream(is, os);
			os.close();
			is.close();
			os = null;
			is = null;
			bitmap = decodeFile(f, defaultSize);

			return bitmap;
		} catch (Exception ex) {
			return null;
		} finally {
			if (conn != null)
				conn.disconnect();
			conn = null;

		}
	}

	private Bitmap decodeFile(File f, int photoToLoad) {
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			int REQUIRED_SIZE = 70;

			if (photoToLoad == 1)
				REQUIRED_SIZE = 120;

			if (photoToLoad == 2)
				REQUIRED_SIZE = 200;

			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;

			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);

		} catch (FileNotFoundException e) {
		}
		return null;
	}

	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public int defaultSize = 0;

		private void setDefaultSize(int defaultSize) {
			this.defaultSize = defaultSize;
		}

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}

		/*
		 * private boolean isDefaultSize() { return defaultSize; }
		 */
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url, photoToLoad.defaultSize);

			if (resize)
				bmp = setCustomImage(bmp);

			if (bmp != null) {
				saveImageInfo(photoToLoad.url);
			}

			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	private void saveImageInfo(final String image) {
		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					String link = String.valueOf(image.hashCode());
					if (table != null)
						table.insertORUpdate(link);
				}
			}).start();
		} catch (Exception e) {

		}

	}

	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageResource(stub_id);
		}
	}

	public void clearCache() {
		memoryCache.clear();
		clearThreads();
	}

	public void clearThreads() {

		if (!executorService.isShutdown()) {
			List<Runnable> list = executorService.shutdownNow();
			for (int i = 0; i < list.size(); i++) {
				PhotosLoader runnable = (PhotosLoader) list.get(i);
				imageViews.remove(runnable.photoToLoad.imageView);
				/*
				 * BitmapDisplayer runnable = (BitmapDisplayer) list.get(i);
				 * imageViews.remove(runnable.photoToLoad.imageView);
				 */
			}
		}

		table.close();
		table = null;
		executorService.shutdown();
		executorService = null;

	}

	private Bitmap setCustomImage(Bitmap bitmap) {

		if (bitmap.getWidth() < 100) {
			return Bitmap.createScaledBitmap(bitmap, 200, 200, true);
		} else if (bitmap.getWidth() < 140) {
			return Bitmap.createScaledBitmap(bitmap, 150, 150, true);
		} else if (bitmap.getWidth() < 200) {
			return Bitmap.createScaledBitmap(bitmap, 200, 200, true);
		}
		return bitmap;
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

}