package com.mail.bsecure.fragments;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bsecure.mail.R;
import com.mail.bsecure.Bsecure;
import com.mail.bsecure.MailPin;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.AppSettings;
import com.mail.bsecure.common.Item;
import com.mail.bsecure.tasks.HTTPBackgroundTask;
import com.mail.bsecure.utils.Utils;

public class MailViews extends ParentFragment implements IItemHandler {

	private View layout = null;

	private Bsecure bsecure = null;

	private Item item = null;

	private int mailType = -1;

	private TextView tv_msg = null;

	private TextView tv_reply = null;

	private TextView tv_from = null;

	private TextView tv_subject = null;
	
	private TextView tv_timeleft = null, tv_validupto = null;

	private int isShowTime = 0;

	private Timer timer = null, secTimer = null;

	private TimerTask timerTask = null;
	
	final Handler myHandler = new Handler();
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.bsecure = (Bsecure) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle mArgs = getArguments();

		item = (Item) mArgs.getSerializable("item");

		layout = inflater.inflate(R.layout.mailview, container, false);

		tv_msg = (TextView) layout.findViewById(R.id.tv_msg);
		tv_reply = (TextView) layout.findViewById(R.id.tv_reply);
		tv_from = (TextView) layout.findViewById(R.id.tv_from);
		
		tv_timeleft = (TextView) layout.findViewById(R.id.tv_timeleft);
		
		tv_validupto = (TextView) layout.findViewById(R.id.tv_validupto);

		tv_subject = (TextView) layout.findViewById(R.id.tv_subject);

		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		tv_reply.setOnClickListener(bsecure);

		getData();

		setHasOptionsMenu(true);
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (!bsecure.isDrawerOpen())
			inflater.inflate(R.menu.viewmail_menu, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void getData() {

		/*
		 * "subject": "Trying", "to": "pradeep.poonam@gmail.com", "datetime":
		 * "01/Jun", "replyid": "06%3F%3B%3F", "recomposeid": "",
		 * "subcomposeid": "", "memberid": "12", "composeid": "9", "sessid": "",
		 * "attachment": "no", "mailcount": "1", "flag": "read"
		 */

		String link = "";
		String value = item.getAttribute("composeid");
		if (value.length() > 0) {
			mailType = 0;
			link = bsecure.getPropertyValue("compose_v_m_p");
			link = link.replace("(COMID)", item.getAttribute("composeid"));
			link = link.replace("(MID)", item.getAttribute("memberid"));
			link = link.replace("(SID)", item.getAttribute("sessid"));
		}

		value = item.getAttribute("recomposeid");
		if (value.length() > 0) {
			mailType = 1;
			link = bsecure.getPropertyValue("re_compose_v_m_p");
			link = link.replace("(REID)", item.getAttribute("recomposeid"));
			link = link.replace("(COMID)", item.getAttribute("composeid"));
			link = link.replace("(MID)", item.getAttribute("memberid"));
			link = link.replace("(SID)", item.getAttribute("sessid"));
		}

		value = item.getAttribute("subcomposeid");
		if (value.length() > 0) {
			mailType = 2;
			link = bsecure.getPropertyValue("sub_compose_v_m_p");
			link = link.replace("(SUBID)", item.getAttribute("subcomposeid"));
			link = link.replace("(COMID)", item.getAttribute("composeid"));
			link = link.replace("(MID)", item.getAttribute("memberid"));
			link = link.replace("(SID)", item.getAttribute("sessid"));
		}

		value = item.getAttribute("replyid");
		if (value.length() > 0) {
			mailType = 3;
			link = bsecure.getPropertyValue("repy_v_m_p");
			link = link.replace("(RID)", item.getAttribute("replyid"));
		}

		HTTPBackgroundTask task = new HTTPBackgroundTask(this, bsecure, 1, 1);
		task.execute(link);
		Utils.showProgress(getString(R.string.pleasewait), bsecure);
		/*
		 * HTTPTask task = new HTTPTask(bsecure, this);
		 * task.userRequest(getString(R.string.pleasewait), 1, link);
		 */

	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {

		layout = null;

		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		
		if(secTimer != null)
			secTimer.cancel();
		
		secTimer = null;
		
		isShowTime = 0;
		
		stoptimertask();
		
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onFinish(Object results, int requestType) {

		try {

			Utils.dismissProgress();

			switch (mailType) {

			case 0:
				showComposeMessage(results);
				break;

			case 1:
				showReComposeMessage(results);
				break;
				
			case 2:
				showComposeMessage(results);
				break;

			case 3:
				showReplyMessage(results);
				break;

			default:
				break;
			}

			bsecure.logE("-=-=-=-=-=-=-"+mailType, results + "");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(String errorCode, int requestType) {

		bsecure.showToast(errorCode);

	}

	@Override
	public void onFragmentChildClick(View view) {
		super.onFragmentChildClick(view);
	}

	@Override
	public String getFragmentName() {
		return "";
	}

	private void showReComposeMessage(Object results) {
		if (results != null) {

			Item item = (Item) results;

			if (item.getAttribute("status").equalsIgnoreCase("0")) {
				
				tv_msg.setText(Html.fromHtml("<h3>Message:</h3>"+ item.getAttribute("message")));	
				
				Vector<Item> items = (Vector<Item>) item.get("recompose_detail");
				
				String composeid = (String)item.getAttribValue("composeid");
				String reid = (String)item.getAttribValue("reid");

				Item msgItem = items.get(0);

				tv_subject.setText(Html.fromHtml(msgItem.getAttribute("subject")));				

				//tv_msg.setText(Html.fromHtml("Message:\n"+ msgItem.getAttribute("message")));

				if (msgItem.getAttribute("reply").equalsIgnoreCase("0")) { /*1 = No, 0 =Yes, pin 0 = no, 1 = yes*/
					tv_reply.setVisibility(View.VISIBLE);
					tv_reply.setTag(this.item);
				}								
				
				if(msgItem.getAttribute("validuntil").length() > 0 && !msgItem.getAttribute("validuntil").equalsIgnoreCase("01-01-1970")) {
					tv_validupto.setText("Valid Until: " + msgItem.getAttribute("validuntil"));	
				}

				tv_from.setText("From: " + msgItem.getAttribute("from"));

				String showtime = msgItem.getAttribute("showtime");
				showtime = showtime.replace("Sec", "");
				showtime = showtime.replace("sec", "");
				showtime = showtime.trim();
				if (showtime.length() > 0
						&& !showtime.equalsIgnoreCase("Unlimited")) {
					isShowTime = 10;

					try {
						isShowTime = Integer.parseInt(showtime);
					} catch (Exception e) {
					}

				}

				if (msgItem.getAttribute("autodelete").equalsIgnoreCase("yes")) {
					sendMailListRefresh();
				}

				if (msgItem.getAttribute("readonly").equalsIgnoreCase("yes")) {
					sendMailListRefresh();
				}

				if (msgItem.getAttribute("pin").equalsIgnoreCase("1")) {
					
					String url = AppSettings.getInstance(bsecure).getPropertyValue("re_mailpin");
					url = url.replace("COMID", composeid);
					url = url.replace("(RID)", reid);
					
					Intent intent = new Intent(bsecure, MailPin.class);										
					intent.putExtra("url", url);										
					
					/*intent.putExtra("COMID", composeid);//msgItem.getAttribute("composeid")+ "");
					intent.putExtra("REID", reid);//msgItem.getAttribute("composeid")+ "");					
					intent.putExtra("MID", msgItem.getAttribute("memberid")+ "");
					intent.putExtra("SID", msgItem.getAttribute("sessid") + "");*/
					bsecure.launchMailPin(intent);
					return;
				}

				startShowTimeTask();

			} else if (item.getAttribute("status").equalsIgnoreCase("1")) {
				//tv_subject.setText(item.getAttribute("statusdescription"));
				tv_msg.setText(item.getAttribute("statusdescription"));
			}
		}
	}

	private void showReplyMessage(Object results) throws Exception {
		if (results != null) {

			Item item = (Item) results;

			if (item.getAttribute("status").equalsIgnoreCase("0")) {
				Vector<Item> items = (Vector<Item>) item.get("reply_detail");

				Item msgItem = items.get(0);

				tv_msg.setText(Html.fromHtml("<h3>Message:</h3>"+ msgItem.getAttribute("message")));				
				
				tv_reply.setVisibility(View.VISIBLE);
				tv_reply.setTag(this.item);

				tv_from.setText("From: " + msgItem.getAttribute("frommsg"));
				
				/*String showtime = "60";
				showtime = showtime.replace("Sec", "");
				showtime = showtime.trim();
				if (showtime.length() > 0
						&& !showtime.equalsIgnoreCase("Unlimited")) {
					isShowTime = 10;

					try {
						isShowTime = Integer.parseInt(showtime);
					} catch (Exception e) {
					}

				}

				startShowTimeTask();*/

			} else if (item.getAttribute("status").equalsIgnoreCase("1")) {
				//tv_subject.setText(item.getAttribute("statusdescription"));
				tv_msg.setText(item.getAttribute("statusdescription"));
			}
			
		}

	}

	private void showComposeMessage(Object results) throws Exception {
		if (results != null) {

			Item item = (Item) results;

			if (item.getAttribute("status").equalsIgnoreCase("0")) {
				
				tv_msg.setText(Html.fromHtml("<h3>Message:</h3>"+ item.getAttribute("message")));				
				
				Vector<Item> items = (Vector<Item>) item.get("compose_detail");
				
				String composeid = (String)item.getAttribValue("composeid");
				String subcomposeid = "";
				
				if(item.getAttribValue("subid") != null)
				  subcomposeid = (String)item.getAttribValue("subid");				

				Item msgItem = items.get(0);

				tv_subject.setText(Html.fromHtml(msgItem.getAttribute("subject")));

				if (msgItem.getAttribute("reply").equalsIgnoreCase("0")) { /*1 = No, 0 =Yes, pin 0 = no, 1 = yes*/
					tv_reply.setVisibility(View.VISIBLE);
					tv_reply.setTag(this.item);
				}				
				
				if(msgItem.getAttribute("validuntil").length() > 0 && !msgItem.getAttribute("validuntil").equalsIgnoreCase("01-01-1970")) {
					tv_validupto.setText("Valid Until: " + msgItem.getAttribute("validuntil"));	
				}
				
				/*if(!msgItem.getAttribute("validuntil").equalsIgnoreCase("01-01-1970")) {
					tv_validupto.setText("Valid Until: Unlimited");	
				}*/

				tv_from.setText("From: " + msgItem.getAttribute("from"));

				String showtime = msgItem.getAttribute("showtime");
				showtime = showtime.replace("Sec", "");
				showtime = showtime.replace("sec", "");
				showtime = showtime.trim();
				
				if (showtime.length() > 0
						&& !showtime.equalsIgnoreCase("Unlimited")) {
					isShowTime = 10;

					try {
						isShowTime = Integer.parseInt(showtime);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

				if (msgItem.getAttribute("autodelete").equalsIgnoreCase("yes")) {
					sendMailListRefresh();
				}

				if (msgItem.getAttribute("readonly").equalsIgnoreCase("yes")) {
					sendMailListRefresh();
				}
//06-30 15:38:53.901: E/-=-=-=-=-=-=-(19056): {status=1, statusdescription=Message Viewing Permission Restricted}

				//if (msgItem.getAttribute("pin").equalsIgnoreCase("1")) {
				if (!msgItem.getAttribute("pin").equalsIgnoreCase("0")) {
					
					//?composeid=(COMID)&memberid=(MID)&(PIN)
					String url = AppSettings.getInstance(bsecure).getPropertyValue("mailpin");
					
					if(subcomposeid.length() > 0) {
						url = AppSettings.getInstance(bsecure).getPropertyValue("sub_mailpin");
					}
					
					url = url.replace("(COMID)", composeid);
					url = url.replace("(MID)", msgItem.getAttribute("memberid")+ "");	
					url = url.replace("(SUBID)", subcomposeid+"");	
					
					/*Intent intent = new Intent(bsecure, MailPin.class);
					intent.putExtra("COMID", composeid);//msgItem.getAttribute("composeid")+ "");
					intent.putExtra("MID", msgItem.getAttribute("memberid")+ "");
					intent.putExtra("SID", msgItem.getAttribute("sessid") + "");*/
					
					Intent intent = new Intent(bsecure, MailPin.class);
					intent.putExtra("url", url);					
					bsecure.launchMailPin(intent);
					return;
				}

				startShowTimeTask();

			} else if (item.getAttribute("status").equalsIgnoreCase("1")) {
				//tv_subject.setText(item.getAttribute("statusdescription"));
				tv_msg.setText(item.getAttribute("statusdescription"));
			}
		}
	}

	public void startShowTimeTask() {

		if (isShowTime == -1)
			return;
		
		if (isShowTime == 0)
			return;
	
		final long milliseconds = isShowTime * 1000;

		startTimer(milliseconds);
		
		/*mUpdateTimeTask = new Runnable() {
			public void run() {				
				
				bsecure.onKeyDown(4, null);
				stoptimertask();
				
			}
		};
		handler.postDelayed(mUpdateTimeTask, milliseconds);*/
				
		int delay = 1000; 
	    int period = 1000;

	    secTimer = new Timer();
	    
	    secTimer.schedule(new TimerTask()
	        {	    	
	    	   long ms = milliseconds;
	            public void run()
	            {
	            	myHandler.post(new Runnable() {
						public void run() {
							ms = ms - 1000;
			            	tv_timeleft.setText("Time left: "+milliSecondsToTime(ms));
						}
					});
	            	
	            }
	        }, delay, period);

	}

	public void startTimer(long time) {

		try {

			timer = new Timer();

			initializeTimerTask();

			timer.schedule(timerTask, time); 

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stoptimertask() {

		try {

			if (timer != null)
				timer.cancel();

			timer = null;

			if (timerTask != null)
				timerTask.cancel();

			timerTask = null;

			//handler = null;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void initializeTimerTask() {
		
		try {					
		
		timerTask = new TimerTask() {
			public void run() {
				
				Handler h = new Handler(Looper.getMainLooper());
				h.post(new Runnable() {
				  public void run() {
					  bsecure.onKeyDown(4, null);
					  bsecure.showToast(R.string.ymnlefv);
					  bsecure.showToast(R.string.ymnlefv);
				  }
				});
				
				/*bsecure.runOnUiThread(new Runnable() {
					  public void run() {
						  
						  bsecure.onKeyDown(4, null);
						  stoptimertask();
					    
					  }
					});*/
				
				/*new Handler().post(new Runnable() {
					public void run() {						
						bsecure.onKeyDown(4, null);
						stoptimertask();
					}
				});*/
			}
		};
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMailListRefresh() {
		bsecure.sendMailListRefresh();
	}

	public static String milliSecondsToTime(long milliseconds)
	{
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int)(milliseconds / (1000 * 60 * 60));
		int minutes = (int)(milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int)((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if( hours > 0 )
		{
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if( seconds < 10 )
		{
			secondsString = "0" + seconds;
		}
		else
		{
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}
	
}
