package com.mail.bsecure.controls;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bsecure.mail.R;

public class ProgressControl extends LinearLayout implements OnClickListener {

	private Context mContext = null;
	private TextView textView/*, dwn_time_left*/ = null;
	private ProgressBar pbar = null;
	private String text = "";

	//private ImageView start, stop, pause = null;

	protected WeakReference<DownloadActionsListener> callback = null;

	private int currentCount = 0;

	private int totalCount = 0;

	private String downloadCount = "";

	/**
	 * ApplicationDownloader - Constructor which accept Context as its parameter
	 * 
	 * @param Context
	 */
	public ProgressControl(Context context) {
		super(context);
		mContext = context;
		init();
	}

	/**
	 * ApplicationDownloader - Constructor which accept Context and AttirbuteSet
	 * as its parameters
	 * 
	 * @param Context
	 */
	public ProgressControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public interface DownloadActionsListener {

		public void onDwnStart();

		public void onDwnStop();

		public void onDwnPause();

	}

	/**
	 * init - initialize the view.
	 * 
	 * @param String
	 * @param String
	 */
	public void init() {

		this.setOrientation(LinearLayout.VERTICAL);

		textView = new TextView(mContext);
		textView.setText(R.string.downloading);
		textView.setTextSize(12f);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.setMargins(2, 0, 0, 0);
		textView.setLayoutParams(params);
		this.addView(textView);

		pbar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
		params = new LayoutParams(LayoutParams.MATCH_PARENT, 10);
		params.setMargins(2, 0, 2, 0);
		pbar.setLayoutParams(params);
		this.addView(pbar);
		
		pbar.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.test_seek_bar));

		/*View view = View.inflate(mContext, R.layout.template_download_actiosn, null);

		dwn_time_left = (TextView) view.findViewById(R.id.dwn_time_left);

		start = (ImageView) view.findViewById(R.id.dwn_start);
		stop = (ImageView) view.findViewById(R.id.dwn_stop);
		pause = (ImageView) view.findViewById(R.id.dwn_pause);

		start.setOnClickListener(this);
		stop.setOnClickListener(this);
		pause.setOnClickListener(this);*/

		//this.addView(view);

	}

	public void setText(String text) {
		this.text = text;
		textView.setText(text);
	}

	public void setCurrentCount(int count) {
		this.currentCount = count;
		downloadCount = currentCount + "/" + totalCount;
	}

	public void setTotalCount(int total) {
		this.totalCount = total;
		downloadCount = currentCount + "/" + totalCount;
	}

	private void setMaxProgress(int max) {
		pbar.setMax(max);
	}

	private void setCurrentProgress(int current) {
		pbar.setProgress(current);
	}

	public void updateProgressState(Long... values) {

		double total = values[2] / 1024.0;
		setMaxProgress((int) total);
		total = total / 1024.0;

		double received = values[1] / 1024.0;
		setCurrentProgress((int) received);
		received = received / 1024.0;
		
		String rec = (String.format("%.02f", received));
		String tot = (String.format("%.02f", total));
		String recieved = rec.replace(",", ".");
		String total_ = tot.replace(",", ".");

		textView.setText(text + recieved + " MB read / " + total_ + " MB" + "     "+ downloadCount+ "     "+msToString(values[4]));
	}

	public void clear() {
		//textView.setText(R.string.downloading);
		pbar.setMax(0);
		pbar.setProgress(0);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {

		case R.id.dwn_start:
			callback.get().onDwnStart();
			break;

		case R.id.dwn_stop:
			callback.get().onDwnStop();
			break;

		case R.id.dwn_pause:
			callback.get().onDwnPause();
			break;

		default:
			break;
		}
	}

	public void setListener(DownloadActionsListener aCallback) {
		callback = new WeakReference<DownloadActionsListener>(aCallback);
	}

	/*public void pauseClicked() {
		start.setVisibility(View.VISIBLE);
		pause.setVisibility(View.GONE);
	}

	public void downloadClicked() {
		start.setVisibility(View.GONE);
		pause.setVisibility(View.VISIBLE);
	}*/

	public void setState() {

	}

	private String msToString(long ms) {
		long totalSecs = ms / 1000;
		long hours = (totalSecs / 3600);
		long mins = (totalSecs / 60) % 60;
		long secs = totalSecs % 60;
		String minsString = (mins == 0) ? "00" : ((mins < 10) ? "0" + mins : ""
				+ mins);
		String secsString = (secs == 0) ? "00" : ((secs < 10) ? "0" + secs : ""
				+ secs);
		if (hours > 0)
			return hours + ":" + minsString + ":" + secsString;
		else if (mins > 0)
			return mins + ":" + secsString +" "+ mContext.getString(R.string.minlft);
		else
			return secsString +" "+ mContext.getString(R.string.seclft);
	}
}
