package com.mail.bsecure;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Stack;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.bsecure.mail.R;
import com.mail.bsecure.callbacks.IDownloadCallback;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.AppSettings;
import com.mail.bsecure.common.Constants;
import com.mail.bsecure.common.Item;
import com.mail.bsecure.dialogfragments.BlockUnblockDialog;
import com.mail.bsecure.dialogfragments.ReplyDialog;
import com.mail.bsecure.fragments.BsecureFilters;
import com.mail.bsecure.fragments.ComposeMail;
import com.mail.bsecure.fragments.DraftMailsListing;
import com.mail.bsecure.fragments.MailViews;
import com.mail.bsecure.fragments.MailsListing;
import com.mail.bsecure.fragments.NavigationDrawerFragment;
import com.mail.bsecure.fragments.ParentFragment;
import com.mail.bsecure.fragments.SentMailViews;
import com.mail.bsecure.fragments.SentMailsListing;
import com.mail.bsecure.fragments.Settings;
import com.mail.bsecure.fragments.TrashMailsListing;
import com.mail.bsecure.tasks.HTTPTask;

public class Bsecure extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks, OnClickListener,
		IItemHandler, IDownloadCallback {

	private FragmentManager manager = null;

	private MailsListing mailListing = null;
	
	private SentMailsListing sentMailListing = null;
	
	private TrashMailsListing trashMailListing = null;
	
	private DraftMailsListing draftMailListing = null;
	
	private MailViews mailViews = null;
	
	private SentMailViews sentMailViews = null;
	
	private Settings settings = null;

	private Properties properties = null;

	private String cid = "0";
	
	private ComposeMail composeMail = null;
	
	private BsecureFilters filters = null;
	
	//private UploadManager uManager = null;

	private ReplyDialog rpyDialog = null;
	
	private BlockUnblockDialog bunbDialog = null;
		
	private NavigationDrawerFragment mNavigationDrawerFragment;

	private Stack<ParentFragment> fragStack = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.bsecure);

		loadProperties();

		fragStack = new Stack<ParentFragment>();

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);

		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		getSupportActionBar().getThemedContext();

		mailListing = new MailsListing();

		Item item = new Item("");
		item.setAttribute("TITLE", "Home");
		item.setAttribute("EVENT", "odp_home");
		item.setAttribute("fname", "home");

		Bundle bundle = new Bundle();
		bundle.putSerializable("item", item);
		mailListing.setArguments(bundle);

		manager = getSupportFragmentManager();

		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(R.id.container, mailListing, "home");

		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.commit();
		fragStack.push(mailListing);
		
		/*if (isServiceRunning(UploadService.class.getName())) {
			if (uManager == null)
				uManager = new UploadManager(this);
			uManager.setCallbackListener(this);
			uManager.onStart();
		}
		
		uManager = getUploadManager();*/

	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if(filters != null && filters.isVisible()) {
			MenuItem item = menu.findItem(R.id.cm_send);
			if(item != null)
				item.setVisible(false);
		}
		   
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {

		if (position == 8) {

			addToStore("fromExit", "exit");		
			return;
		}
				
		int count = fragStack.size();
		while (count > 1) {

			ParentFragment pf = fragStack.remove(count - 1);

			FragmentTransaction trans = manager.beginTransaction();
			trans.remove(pf);
			trans.commit();					
			
			sentMailListing = null;
			draftMailListing = null;
			trashMailListing = null;
			mailViews = null;
			sentMailViews = null;

			count--;
		}
		
		switch (position) {

		case 0: //Inbox

			swiftFragments(mailListing, "mailListing");

			break;

		case 1: //Sent
			
			if(sentMailListing == null)
				sentMailListing = new SentMailsListing();
			
			swiftFragments(sentMailListing, "sentMailListing");
									
			break;		

		case 2: //Drafts
			
			if(draftMailListing == null)
				draftMailListing = new DraftMailsListing();
			
			swiftFragments(draftMailListing, "draftMailListing");
			
			break;

		case 3: //Trash
			
			if(trashMailListing == null)
				trashMailListing = new TrashMailsListing();
			
			swiftFragments(trashMailListing, "trashMailListing");
			
			break;

		case 4: //Settings
						
			if(settings == null)
				settings = new Settings();
			
			swiftFragments(settings, "settings");
			
			break;

		case 5: //Logout
			
			 addToStore("email", "");
			 addToStore("authpin", "");
			 Bsecure.this.finish();
			
			break;

		case 6:
			break;

		default:
			break;
		}

	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);				
		
        actionBar.setDisplayHomeAsUpEnabled(true);        
        actionBar.setDisplayShowHomeEnabled(true);        
        actionBar.setHomeButtonEnabled(true);                     		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * loadProperties - Loads properties file from raw folder.
	 */
	private void loadProperties() {
		try {
			InputStream rawResource = getResources().openRawResource(
					R.raw.settings);
			properties = new Properties();
			properties.load(rawResource);
			rawResource.close();
			rawResource = null;
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Properties getProperties() {
		return properties;
	}

	public String getPropertyValue(String key) {
		return properties.getProperty(key);
	}

	public void showToast(String value) {
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}

	public void showToast(int text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * addToStore - This method is used to persist data in shared preferences.
	 * It accept two parameter, 1st parameter is key and second parameter is
	 * value
	 * 
	 * @param String
	 * @param String
	 */
	public void addToStore(String key, String value) {
		SharedPreferences pref = this.getSharedPreferences("bsecure", MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * getFromStore - Returns value associated with a key in the shared
	 * preference.
	 * 
	 * @param String
	 * @return String
	 */
	public String getFromStore(String key) {
		SharedPreferences pref = this.getSharedPreferences("bsecure", MODE_PRIVATE);
		String res = pref.getString(key, "");
		return res;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		
		case R.id.bn_bu_cancel:
			closeDialogs();
			break;
			
		case R.id.bn_bu_submit:
			makeBlockUnblockUser((Integer)view.getTag());
			break;
		
		case R.id.tv_blockusers:
			showBlockUnDialog(view.getId(), R.string.blockusers);			
			break;
			
		case R.id.tv_unblockusers:
			showBlockUnDialog(view.getId(), R.string.unblockusers);
			break;
			
		case R.id.tv_rating:
			break;
			
		case R.id.tv_shareapp:
			break;
		
		case R.id.tv_about:
			break;
		
		case R.id.tvCommentSubmit/*submit_comment*/:
			submitReply((Item) view.getTag());
			break;
		
		case R.id.bn_dg_cancel:
			closeDialogs();
			break;
		
		case R.id.tv_attachmentAction:
			composeMail.removeAttachment((Integer)view.getTag());
			break;
		
		case R.id.tv_addattachment:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("*/*");
		    startActivityForResult(intent, 2798);
			break;

		case R.id.add_button:
			
			if(composeMail == null)
				composeMail = new ComposeMail();
			
				swiftFragments(composeMail, "composeMail");
			
			break;
		
		case R.id.rv_row_lisiting_layout:
			fragStack.peek().onFragmentChildClick(view);
			break;
			
		case R.id.tv_filterdone:
			
			JSONObject object = filters.getFilterVlaues();
						
			String pin = object.optString("pin");
			if(pin.length() != 0) {
				if(pin.length() < 4) {
					showToast(R.string.psbdl);
					return;
				}
			}
								
			composeMail.showFilterValues(object);
			onKeyDown(4, null);
			filters = null;
			
			break;
			
		case R.id.tv_reply:
			
			Item itemp = (Item) view.getTag();
			String value = itemp.getAttribute("replyid").trim();
			if(value.length() > 0) {
				
				if(composeMail == null)
					composeMail = new ComposeMail();
				
				Bundle bundle = new Bundle();
				bundle.putSerializable("item", (Item) view.getTag());
				composeMail.setArguments(bundle);
				
			   swiftFragments(composeMail, "composeMail");
				
				return;
			}
						
			showReplyDialog(itemp);
								
			break;
			
		case R.id.tv_ccText:
			composeMail.onCCClick();
			break;
			
		case R.id.tv_bccText:
			composeMail.onBCCClick();
			break;	
		
		default:
			break;
		}
	}

	private void swiftFragments(ParentFragment frag, String tag) {
		
		FragmentTransaction trans = manager.beginTransaction();
		if (frag.isAdded() && frag.isVisible())
			return;
		else if (frag.isAdded() && frag.isHidden()) {

			trans.hide(fragStack.get(fragStack.size() - 1));
			trans.show(frag);

		} else if (!frag.isAdded()) {

			ParentFragment pf = fragStack.get(fragStack.size() - 1);
			trans.hide(pf);

			trans.add(R.id.container, frag, tag);
			trans.show(frag);
		}

		trans.commitAllowingStateLoss();
		trans = null;
		
		getSupportActionBar().setTitle(frag.getFragmentName());
		
		/*getSupportActionBar().setBackgroundDrawable(new ColorDrawable(frag.getFragmentActionBarColor()));
		updateStatusBarColor(frag.getFragmentStatusBarColor());*/

		if(!(frag instanceof MailsListing))
		    fragStack.push((ParentFragment) frag);
	}	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == 4) {

			if (mNavigationDrawerFragment.isDrawerOpen()) {
				mNavigationDrawerFragment.close();
				return true;
			}

			if (fragStack.size() > 1) {

				ParentFragment pf = fragStack.peek();

				if (pf.back() == true)
					return true;

				fragStack.pop();													

				FragmentTransaction trans = manager.beginTransaction();
				trans.remove(pf);
				
				if(pf instanceof ComposeMail)
					composeMail = null;

				ParentFragment pf1 = fragStack.get(fragStack.size() - 1);
				trans.show(pf1);
				trans.commit();
				
				getSupportActionBar().setTitle(pf1.getFragmentName());
				
				/*getSupportActionBar().setBackgroundDrawable(new ColorDrawable(pf1.getFragmentActionBarColor()));
				updateStatusBarColor(pf1.getFragmentStatusBarColor());*/

				return true;
			}
			
			return super.onKeyDown(keyCode, event);

		}
		return super.onKeyDown(keyCode, event);
	}
	
	OnClickListener onclick = new OnClickListener() {

		@Override
		public void onClick(View view) {

			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(view.getWindowToken(), 0);

			switch (view.getId()) {
		
			case R.id.home:
				swiftFragments(mailListing, "mailListing");

				break;							

			default:
				break;
			}

		}
	};

	@Override
	public void onFinish(Object results, int requestType) {
		
		try {					
		
		switch (requestType) {
		
		case 1:
			
			if(results != null) {
				JSONObject object = new JSONObject(results.toString());

				showToast(object.optString("statusdescription"));
				onKeyDown(4, null);	
				object = null;				
			}			
			
			break;
			
		case 2:
			parseBlockUnBlockResponse((String) results, requestType);
		break;
		
		default:
			break;
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onError(String errorCode, int requestType) {
		showToast(errorCode);
	}

	public FragmentManager getFragManager() {
		return manager;
	}

	@Override
	protected void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {

		case 1001:
			break;
			
		case 2001:
			
			if(resultCode == RESULT_CANCELED) {
				onKeyDown(4, null);
				return;
			}
			
			if(resultCode == RESULT_OK) {
				if(mailViews != null && mailViews.isVisible()) {
					mailViews.startShowTimeTask();
				}
				return;
			}			
			
			break;
			
		case 2798:
			if(resultCode == RESULT_OK) {
												
				Uri uri = data.getData();
				
				if (uri == null) {
					showToast(R.string.fnf);					
					return;
				}

				Item item  = processData(uri);
				if(item == null) {
					showToast(R.string.fnf);					
					return;
				}
				
				if(!item.getAttribute("mimetype").contains("image")) {
					showToast(R.string.cwansaff);					
					return;
				}
				
				String size = item.getAttribute("size");
				if(size.length() > 0) {
					long sizeLong = Long.parseLong(size);
					
					long mb = sizeLong / (1024 * 1024) ;
					logE("mb::: ", mb+"");
					if(mb > 5) {
						showToast(R.string.fsitbmia);
						return;
					}
				}

				String id = item.getAttribute("documentid").split(":")[1];
				
				String[] column = { MediaStore.Images.Media.DATA };     

				String sel = MediaStore.Images.Media._ID + "=?";

				Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{ id }, null);
				
				String filePath = "";

				int columnIndex = cursor.getColumnIndex(column[0]);

				if (cursor.moveToFirst()) {
				    filePath = cursor.getString(columnIndex);				    
				}  
				
				cursor.close();
				cursor = null;								
				
				item.setAttribute("DOWNLOADURL", filePath);
				
				composeMail.addAttachmentView(item);
				
			}
			break;

		}
	}
	
	
	public void launchMailPin(Intent intent) {
		startActivityForResult(intent, 2001);
	}
	
	protected String getContactInfo(Intent intent) {

		String number = "";

		if (intent == null)
			return number;

		Cursor cursor = getContentResolver().query(
				intent.getData(),
				new String[] { ContactsContract.Contacts._ID,
						ContactsContract.Contacts.HAS_PHONE_NUMBER }, null,
				null, null);

		while (cursor != null && cursor.moveToNext()) {
			String contactId = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));

			String hasPhone = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone.equalsIgnoreCase("1"))
				hasPhone = "true";
			else
				hasPhone = "false";

			if (Boolean.parseBoolean(hasPhone)) {
				Cursor phones = this
						.getContentResolver()
						.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
								new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID
										+ " = " + contactId, null, null);

				while (phones.moveToNext()) {
					number = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				}
				phones.close();
			}

		}
		cursor.close();
		return number;
	}	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		addToStore("inactivity", "no");

		clearCache();

	}
	
	@Override
	protected void onStart() {
		super.onStart();
		addToStore("inactivity", "yes");
	}

	private void clearCache() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				deleteAppFiles(new File(Constants.CACHETEMP));
			}
		}).start();
	}

	/**
	 * deleteAppFiles - This method is called on canceling a request.
	 * 
	 * @param aName
	 *            - file name to be deleted
	 **/
	private boolean deleteAppFiles(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteAppFiles(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}


	public String getCid() {
		return cid;
	}

	public boolean emailValidation(String email) {

		if (email == null || email.length() == 0 || email.indexOf("@") == -1
				|| email.indexOf(" ") != -1) {
			return false;
		}
		int emailLenght = email.length();
		int atPosition = email.indexOf("@");

		String beforeAt = email.substring(0, atPosition);
		String afterAt = email.substring(atPosition + 1, emailLenght);

		if (beforeAt.length() == 0 || afterAt.length() == 0) {
			return false;
		}
		if (email.charAt(atPosition - 1) == '.') {
			return false;
		}
		if (email.charAt(atPosition + 1) == '.') {
			return false;
		}
		if (afterAt.indexOf(".") == -1) {
			return false;
		}
		char dotCh = 0;
		for (int i = 0; i < afterAt.length(); i++) {
			char ch = afterAt.charAt(i);
			if ((ch == 0x2e) && (ch == dotCh)) {
				return false;
			}
			dotCh = ch;
		}
		if (afterAt.indexOf("@") != -1) {
			return false;
		}
		int ind = 0;
		do {
			int newInd = afterAt.indexOf(".", ind + 1);

			if (newInd == ind || newInd == -1) {
				String prefix = afterAt.substring(ind + 1);
				if (prefix.length() > 1 && prefix.length() < 6) {
					break;
				} else {
					return false;
				}
			} else {
				ind = newInd;
			}
		} while (true);
		dotCh = 0;
		for (int i = 0; i < beforeAt.length(); i++) {
			char ch = beforeAt.charAt(i);
			if (!((ch >= 0x30 && ch <= 0x39) || (ch >= 0x41 && ch <= 0x5a)
					|| (ch >= 0x61 && ch <= 0x7a) || (ch == 0x2e)
					|| (ch == 0x2d) || (ch == 0x5f))) {
				return false;
			}
			if ((ch == 0x2e) && (ch == dotCh)) {
				return false;
			}
			dotCh = ch;
		}
		return true;
	}
	
	public boolean isDrawerOpen() {
		return mNavigationDrawerFragment.isDrawerOpen();
	}
	
	public void showFilterFragment() {
		
		if(filters == null)
			filters = new BsecureFilters();
		
		FragmentTransaction transaction = manager.beginTransaction();
		
		if (filters.isAdded() && filters.isVisible()) {
			
			transaction.remove(filters);
			fragStack.pop();
			filters = null;
			
		} else if (filters.isAdded() && filters.isHidden()) {
			
			transaction.show(filters);
			
		} else {

			transaction.add(R.id.container, filters, "filters");
			transaction.show(filters);
			fragStack.push((ParentFragment) filters);

		}
		
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.commit();
		manager.executePendingTransactions();

		
	}
	
	public void updateMailCounts(Item item) {
		mNavigationDrawerFragment.updateMailCounts(item);
	}
	
	public void showComposeViewMessagePage(Item item) {
		
		if(mailViews == null)
			mailViews = new MailViews();
		
		Bundle bundle = new Bundle();
		bundle.putSerializable("item", item);
		mailViews.setArguments(bundle);
		
		swiftFragments(mailViews, "mailViews");
		
	}
	
	public void showSentViewMessagePage(Item item) {
		
		if(sentMailViews == null)
			sentMailViews = new SentMailViews();
		
		Bundle bundle = new Bundle();
		bundle.putSerializable("item", item);
		sentMailViews.setArguments(bundle);
		
		swiftFragments(sentMailViews, "sentMailViews");
		
	}
	
	public void logE(String key, String value) {
		//Log.e(key, value);
	}
	
	public void sendMailListRefresh () {
		if(mailListing != null) {
			mailListing.refresh();
		}
	}
	
	public void lockMode() {
		mNavigationDrawerFragment.lockMode();
	}
	
	public void unLockMode() {
		mNavigationDrawerFragment.unLockMode();
	}

	/*private boolean checkSDCard() {

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;

		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			showToast(R.string.sdcardmsg2);
			return false;
		} else {
			showToast(R.string.sdcardmsg1);
			return false;
		}
	}*/
	
	/*private DownloadManager getDownloadManager() {
		if (dManager == null) {
			dManager = new DownloadManager(Bsecure.this);
			dManager.setUserId(getFromStore("userid"));
			dManager.setCallbackListener(this);
		}

		return dManager;
	}*/
	
	/*private UploadManager getUploadManager() {
		if (uManager == null) {
			uManager = new UploadManager(Bsecure.this);
			uManager.setUserId(getFromStore("userid"));
			uManager.setCallbackListener(this);
		}

		return uManager;
	}*/
	
	/*private boolean isServiceRunning(String name) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (name.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}*/
	
	/*public void initDownload(String id, String fileName, String path) {

		if (!checkSDCard())
			return;		

		Item item = new Item("");
		item.setAttribute("CID", id);
		item.setAttribute("TITLE", fileName);
		item.setAttribute("CTYPE", "");
		item.setAttribute("DOWNLOADURL", path);		
		
		addTrackToDownloadQueue(item);		
	}*/
	
	/*public void initUpload(String id, String fileName, String path) {

		if (!checkSDCard())
			return;		

		Item item = new Item("");
		item.setAttribute("CID", id);
		item.setAttribute("TITLE", fileName);
		item.setAttribute("CTYPE", "");
		item.setAttribute("DOWNLOADURL", path);		
		
		addTrackToDownloadQueue(item);		
	}
	
	private void addTrackToDownloadQueue(Item item) {		

		getUploadManager().addTrackToDownloadQueue(item);
		//getDownloadManager().addTrackToDownloadQueue(item);
	}*/
	
	@Override
	public void onStateChange(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		
	}
	
	private Item processData(Uri uri) {

		try {									

			Cursor cursor = getContentResolver().query(uri, null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				Item item = new Item("");
				
				do {
					String[] resultsColumns = cursor.getColumnNames();
					for (int i = 0; i < resultsColumns.length; i++) {
						String key = resultsColumns[i];
						String value = cursor.getString(cursor
								.getColumnIndexOrThrow(resultsColumns[i]));

						if (value != null) {
							if (key.contains("_"))
								key = key.replace("_", "");
							item.setAttribute(key, value);	
							
						}
					}

				} while (cursor.moveToNext());

				cursor.close();
				cursor = null;				
			
				return item;
			}


		} catch (Exception e) {
			
		}
		
		return null;

	}
	
	private void closeDialogs() {
		if(rpyDialog != null)
			rpyDialog.dismiss();
		rpyDialog = null;
		
		if(bunbDialog != null)
			bunbDialog.dismiss();
		bunbDialog = null;		
	}
	
	private void showReplyDialog(Item item) {

		if (rpyDialog == null)
			rpyDialog = new ReplyDialog();

		Bundle conatcts = new Bundle();		
		conatcts.putSerializable("item", item);
		rpyDialog.setArguments(conatcts);
		rpyDialog.show(Bsecure.this.getSupportFragmentManager(), "dialog");

	}
	
	private void showBlockUnDialog(int id, int titleId) {

		if (bunbDialog == null)
			bunbDialog = new BlockUnblockDialog();

		Bundle conatcts = new Bundle();
		conatcts.putInt("id", id);
		conatcts.putInt("titleId", titleId);
		bunbDialog.setArguments(conatcts);		
		bunbDialog.show(Bsecure.this.getSupportFragmentManager(), "dialog");

	}		
	
	public void submitReply(Item item) {
		
		try {					

		String replyMsg = ((EditText) rpyDialog.getView().findViewById(R.id.cmttxt))
				.getText().toString();

		replyMsg = replyMsg.trim();
		
		if(replyMsg.length() == 0) {
			showToast(R.string.pwyr);
			return;
		}
		
		String link = AppSettings.getInstance(this).getPropertyValue("reply_receiver");
		link  = link.replace("(COMID)", item.getAttribute("composeid"));
		link  = link.replace("(TO)", getFromStore("email"));
		link  = link.replace("(FROM)", item.getAttribute("from"));
		link  = link.replace("(MSG)", replyMsg);
				
		HTTPTask task = new HTTPTask(this, this);
		task.userRequest(getString(R.string.pwait), 1, link);
		
		closeDialogs();
				
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void makeBlockUnblockUser(Integer id) {
		
		
		String email = ((EditText) bunbDialog.getView().findViewById(R.id.cmttxt))
				.getText().toString();

		email = email.trim();
		
		if (email.length() == 0) {
			showToast(R.string.peei);
			return;
		}
		
		if (!emailValidation(email)) {
			showToast(R.string.peavei);
			return;
		}
		
		String link = "";
		
		switch (id) {
		
		case R.id.tv_blockusers:
			link = getPropertyValue("unsubUser");
			break;
			
		case R.id.tv_unblockusers:
			link = getPropertyValue("unblockUser");
			break;

		default:
			break;
		}				
		
		link =  link.replace("(EMAIL)", getFromStore("email"));
		
		link =  link.replace("(UNEMAIL)", email);
		
		HTTPTask task = new HTTPTask(this, this);
		task.userRequest(getString(R.string.pwait), 2, link);		
		
	}
	
	private void parseBlockUnBlockResponse(String response, int requestId)
			throws Exception {

		if (response != null && response.length() > 0) {

			JSONObject jsonObject = new JSONObject(response);
			
			if (jsonObject.optString("status").equalsIgnoreCase("0")) {
				closeDialogs();
			}			
			showToast(jsonObject.optString("statusdescription"));			

		}
	}
	
	/*private void initCrashLog() {
		mUEHandler = new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				try {					
				
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/bsecure.log", true)));
					e.printStackTrace(pw);
					pw.flush();
					pw.close();
					
					e.printStackTrace();
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				Bsecure.this.finish();
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(mUEHandler);
	}*/
	
	private void updateStatusBarColor(int color) {
		if (Build.VERSION.SDK_INT >= 21) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			//window.setStatusBarColor(color);
		}
	}
	
	public void openDraftMail (Item item) {
		if(composeMail == null)
			composeMail = new ComposeMail();
		
		item.setAttribute("drafts_values", "yes");
		Bundle conatcts = new Bundle();		
		conatcts.putSerializable("item", item);
		composeMail.setArguments(conatcts);
		
			swiftFragments(composeMail, "composeMail");
	}
	
}
