package com.mail.bsecure.fragments;

import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class SentMailViews extends ParentFragment implements IItemHandler {

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

		String link = "";
		String value = item.getAttribute("composeid");
		if (value.length() > 0) {
			mailType = 0;
			link = bsecure.getPropertyValue("sentview_compose");
			link = link.replace("(COMID)", item.getAttribute("composeid"));
			link = link.replace("(MID)", item.getAttribute("memberid"));
		}

		value = item.getAttribute("recomposeid");
		if (value.length() > 0) {
			mailType = 1;
			link = bsecure.getPropertyValue("sentview_recompose");
			link = link.replace("(REID)", item.getAttribute("recomposeid"));
			link = link.replace("(COMID)", item.getAttribute("composeid"));
			link = link.replace("(MID)", item.getAttribute("memberid"));
		}

		value = item.getAttribute("subcomposeid");
		if (value.length() > 0) {
			mailType = 2;
			link = bsecure.getPropertyValue("sentview_subcompose");
			link = link.replace("(SUBID)", item.getAttribute("subcomposeid"));
			link = link.replace("(COMID)", item.getAttribute("composeid"));
			link = link.replace("(MID)", item.getAttribute("memberid"));
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

	}

	@Override
	public void onDestroyView() {

		layout = null;

		super.onDestroyView();
	}

	@Override
	public void onDestroy() {

		isShowTime = 0;

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

			bsecure.logE("-=-=-=-=-=-=-", results + "");

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

				tv_msg.setText(Html.fromHtml("<h3>Message:</h3>"
						+ item.getAttribute("message")));

				Vector<Item> items = (Vector<Item>) item
						.get("recompose_detail");

				String composeid = (String) item.getAttribValue("composeid");
				String reid = (String) item.getAttribValue("reid");

				Item msgItem = items.get(0);

				tv_subject.setText(Html.fromHtml(msgItem
						.getAttribute("subject")));

				// tv_msg.setText(Html.fromHtml("Message:\n"+
				// msgItem.getAttribute("message")));

				if (msgItem.getAttribute("reply").equalsIgnoreCase("0")) { /*
																			 * 1
																			 * =
																			 * No
																			 * ,
																			 * 0
																			 * =
																			 * Yes
																			 * ,
																			 * pin
																			 * 0
																			 * =
																			 * no
																			 * ,
																			 * 1
																			 * =
																			 * yes
																			 */
					tv_reply.setVisibility(View.VISIBLE);
					tv_reply.setTag(this.item);
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

					String url = AppSettings.getInstance(bsecure)
							.getPropertyValue("re_mailpin");
					url = url.replace("COMID", composeid);
					url = url.replace("(RID)", reid);

					Intent intent = new Intent(bsecure, MailPin.class);
					intent.putExtra("url", url);

					/*
					 * intent.putExtra("COMID",
					 * composeid);//msgItem.getAttribute("composeid")+ "");
					 * intent.putExtra("REID",
					 * reid);//msgItem.getAttribute("composeid")+ "");
					 * intent.putExtra("MID", msgItem.getAttribute("memberid")+
					 * ""); intent.putExtra("SID",
					 * msgItem.getAttribute("sessid") + "");
					 */
					bsecure.launchMailPin(intent);
					return;
				}

			} else if (item.getAttribute("status").equalsIgnoreCase("1")) {
				tv_subject.setText(item.getAttribute("statusdescription"));
			}
		}
	}

	private void showReplyMessage(Object results) throws Exception {
		if (results != null) {

			Item item = (Item) results;

			if (item.getAttribute("status").equalsIgnoreCase("0")) {
				Vector<Item> items = (Vector<Item>) item.get("reply_detail");

				Item msgItem = items.get(0);

				tv_msg.setText(Html.fromHtml("<h3>Message:</h3>"
						+ msgItem.getAttribute("message")));

				tv_reply.setVisibility(View.VISIBLE);
				tv_reply.setTag(this.item);

				tv_from.setText("From: " + msgItem.getAttribute("frommsg"));

			} else if (item.getAttribute("status").equalsIgnoreCase("1")) {
				tv_subject.setText(item.getAttribute("statusdescription"));
			}

		}

	}

	private void showComposeMessage(Object results) throws Exception {
		if (results != null) {

			Item item = (Item) results;

			if (item.getAttribute("status").equalsIgnoreCase("0")) {

				tv_msg.setText(Html.fromHtml("<h3>Message:</h3>"
						+ item.getAttribute("message")));

				Vector<Item> items = (Vector<Item>) item.get("compose_detail");

				String composeid = (String) item.getAttribValue("composeid");
				String subcomposeid = "";

				if (item.getAttribValue("subid") != null)
					subcomposeid = (String) item.getAttribValue("subid");

				Item msgItem = items.get(0);

				tv_subject.setText(Html.fromHtml(msgItem
						.getAttribute("subject")));

				if (msgItem.getAttribute("reply").equalsIgnoreCase("0")) { /*
																			 * 1
																			 * =
																			 * No
																			 * ,
																			 * 0
																			 * =
																			 * Yes
																			 * ,
																			 * pin
																			 * 0
																			 * =
																			 * no
																			 * ,
																			 * 1
																			 * =
																			 * yes
																			 */
					tv_reply.setVisibility(View.VISIBLE);
					tv_reply.setTag(this.item);
				}

				/*
				 * if(!msgItem.getAttribute("validuntil").equalsIgnoreCase(
				 * "01-01-1970")) { tv_validupto.setText("Valid Until: " +
				 * msgItem.getAttribute("validuntil")); }
				 */

				if (!msgItem.getAttribute("validuntil").equalsIgnoreCase(
						"Unlimited")) {
					tv_validupto.setText("Valid Until: Unlimited");
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
						e.printStackTrace();
					}

				}

				if (msgItem.getAttribute("autodelete").equalsIgnoreCase("yes")) {
					sendMailListRefresh();
				}

				if (msgItem.getAttribute("readonly").equalsIgnoreCase("yes")) {
					sendMailListRefresh();
				}

				// if (msgItem.getAttribute("pin").equalsIgnoreCase("1")) {
				if (!msgItem.getAttribute("pin").equalsIgnoreCase("0")) {

					// ?composeid=(COMID)&memberid=(MID)&(PIN)
					String url = AppSettings.getInstance(bsecure)
							.getPropertyValue("mailpin");

					if (subcomposeid.length() > 0) {
						url = AppSettings.getInstance(bsecure)
								.getPropertyValue("sub_mailpin");
					}

					url = url.replace("(COMID)", composeid);
					url = url.replace("(MID)", msgItem.getAttribute("memberid")
							+ "");
					url = url.replace("(SUBID)", subcomposeid + "");

					Intent intent = new Intent(bsecure, MailPin.class);
					intent.putExtra("url", url);
					bsecure.launchMailPin(intent);
					return;
				}

			} else if (item.getAttribute("status").equalsIgnoreCase("1")) {
				tv_subject.setText(item.getAttribute("statusdescription"));
			}
		}
	}

	private void sendMailListRefresh() {
		bsecure.sendMailListRefresh();
	}

	public static String milliSecondsToTime(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}

}
