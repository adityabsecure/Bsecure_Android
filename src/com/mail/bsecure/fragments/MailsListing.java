package com.mail.bsecure.fragments;

import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnCloseListener;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.support.v7.widget.SearchView.SearchAutoComplete;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bsecure.mail.R;
import com.mail.bsecure.Bsecure;
import com.mail.bsecure.adapter.MailsListingAdapter;
import com.mail.bsecure.adapter.SearchFeedResultsAdaptor;
import com.mail.bsecure.callbacks.IItemHandler;
import com.mail.bsecure.common.AppSettings;
import com.mail.bsecure.common.Item;
import com.mail.bsecure.helper.RecyclerOnScrollListener;
import com.mail.bsecure.tasks.HTTPBackgroundTask;
import com.mail.bsecure.tasks.HTTPTask;

public class MailsListing extends ParentFragment implements IItemHandler {

	private View layout = null;

	private Bsecure bsecure = null;

	private RecyclerView mRecyclerView = null;

	private MailsListingAdapter adapter = null;
	
	private String total_pages = "0";
	
	private boolean isRefresh =  false;
	
	private int tempId = -1;
	
	private String comid  = "";
	
	private SearchFeedResultsAdaptor mSearchViewAdapter = null;
	
	public static String[] columns = new String[]{"_id", "TITLE"};
	
	private SearchView searchView = null;
	
	private SearchAutoComplete autoCompleteTextView = null; 

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

		layout = inflater.inflate(R.layout.tempalte_recyclerview, container, false);
		View addButton = layout.findViewById(R.id.add_button);
						
		addButton.setOnClickListener(bsecure);
		
		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		LinearLayoutManager layoutManager = new LinearLayoutManager(bsecure);

		mRecyclerView = (RecyclerView) layout.findViewById(R.id.content_list);

		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());

		mRecyclerView.setOnScrollListener(new RecyclerOnScrollListener(
				layoutManager) {

			@Override
			public void onLoadMoreData(int currentPage) {
				if (currentPage <= Integer.parseInt(total_pages) - 1) {
					getData(2, currentPage);
				}
			}
		});						
		
		getData(1, 0);
		
		setHasOptionsMenu(true);
		
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(!bsecure.isDrawerOpen()) {
			inflater.inflate(R.menu.mailslisting_menu, menu);
						
			searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
			
			if(mSearchViewAdapter == null) {
				mSearchViewAdapter = new SearchFeedResultsAdaptor(this.getActivity(), R.layout.abc_search_dropdown_item_icons_2line, null, columns,null, -1000);
				
				autoCompleteTextView = (SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
				
			     searchView.setSuggestionsAdapter(mSearchViewAdapter);	
			}			 
			
			searchView.setOnQueryTextListener(new OnQueryTextListener() { 

				@Override
				public boolean onQueryTextSubmit(String searchKey) {
					searchContent(searchKey);
					return false;
				}

				@Override
				public boolean onQueryTextChange(String searchKey) {
					if(searchKey.length() >= 1/*3*/ || searchKey.contains("@")) {
						getSearchContacts(searchKey);
					}
					return false;
				} 							

	        });
			
			searchView.setOnCloseListener(new OnCloseListener() {
				
				@Override
				public boolean onClose() {
					
					return false;
				}
			});
			
			searchView.setOnSuggestionListener(new OnSuggestionListener() {
				
				@Override
				public boolean onSuggestionSelect(int position) {
					
					Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
			        String feedName = cursor.getString(1);
			        searchView.setQuery(feedName, false);
			        searchView.clearFocus();
					
					return false;
				}
				
				@Override
				public boolean onSuggestionClick(int position) {
					
					
					Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
			        String feedName = cursor.getString(1);
			        searchView.setQuery(feedName, false);
			        searchView.clearFocus();
					
					return false;
				}
			});						

		}				
		
        super.onCreateOptionsMenu(menu, inflater);
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
	public void onHiddenChanged(boolean hidden) {		
		super.onHiddenChanged(hidden);
		if(!hidden)
			if(isRefresh) {
				isRefresh = false;
				getData(1, 0);
			}
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		
		adapter.clear();
		adapter.notifyDataSetChanged();
		adapter.release();
		adapter = null;
		
		mRecyclerView.removeAllViews();
		mRecyclerView = null;
		
		layout = null;
		
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void getData(int requestId, int currentNo) {

		getView().findViewById(R.id.catgr_pbar).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.catgr_txt).setVisibility(View.GONE);
		
		if (adapter == null) {
			adapter = new MailsListingAdapter(bsecure, "");
			mRecyclerView.setAdapter(adapter);			
		}

		HTTPBackgroundTask task = new HTTPBackgroundTask(this, getActivity(), 1, requestId);

		String link = bsecure.getPropertyValue("getmaillist");		
		link = link.replace("(EMAIL)", bsecure.getFromStore("email"));

		task.execute(link);
	}
	
	private void searchContent(String searchKey) {
		getView().findViewById(R.id.catgr_pbar).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.catgr_txt).setVisibility(View.GONE);
		
		if (adapter == null) {
			adapter = new MailsListingAdapter(bsecure, "");
			mRecyclerView.setAdapter(adapter);			
		}
		adapter.clear();
		adapter.notifyDataSetChanged();

		HTTPBackgroundTask task = new HTTPBackgroundTask(this, getActivity(), 1, 1);
		
		String link = bsecure.getPropertyValue("searchInInbox");		
		link = link.replace("(EMAIL)", bsecure.getFromStore("email"));
		link = link.replace("(SEMAIL)", searchKey);

		task.execute(link);
	}
	
	private void getSearchContacts(String searchKey) {
		
		HTTPTask task = new HTTPTask(bsecure, this);
		
		String link = bsecure.getPropertyValue("searchContacts");		
		link = link.replace("(EMAIL)", bsecure.getFromStore("email"));
		link = link.replace("(KEY)", searchKey);
		task.disableProgress();
		task.userRequest("", 4, link);
	}

	@Override
	public void onFinish(Object results, int requestType) {

		try {

			getView().findViewById(R.id.catgr_pbar).setVisibility(View.GONE);

			switch (requestType) {

			case 1:

				if (results != null) {
					
					Item item = (Item) results;
					bsecure.updateMailCounts(item);
					
					Vector<Item> items = (Vector<Item>)item.get("mail_detail");
					if(items != null && items.size() > 0) {
						
						adapter.setItems(items);
						adapter.notifyDataSetChanged();
						return;
					}
					
				}

				adapter.clear();
				adapter.notifyDataSetChanged();
				getView().findViewById(R.id.catgr_txt).setVisibility(
						View.VISIBLE);

				break;

			case 2:

				if (results != null) {
					Vector<Vector<Object>> object = (Vector<Vector<Object>>) results;
					if (object != null && object.size() > 0) {
						Vector<Object> section = object.get(0);

						Item item = (Item) section.remove(0);
						
						total_pages = item.getAttribute("TGS");

						Vector<Item> items = (Vector<Item>) section.clone();
						Vector<Item> oldCContain = adapter.getItems();
						oldCContain.addAll(items);
						adapter.setItems(oldCContain);
						adapter.notifyDataSetChanged();
						return;
					}

				}

				break;
				
			case 3:
				
				parseMoveToTrashResponse(results);
				comid = "";
				
				break;
				
			case 4:
				
				if(results != null) {
					JSONObject object = new JSONObject(results.toString());
					if(object.has("status") && object.optString("status").equalsIgnoreCase("0")) {
						JSONArray array = object.getJSONArray("emails_det");
						if(array != null && array.length() > 0) {
							ArrayList<String> list = new ArrayList<String>();
							for(int i = 0; i < array.length(); i++) {
								JSONObject jObject = array.getJSONObject(i);
								String useremails = jObject.optString("useremails");
								useremails = useremails.trim();
								if(useremails.length() > 0) {
									list.add(useremails);
								}
							}
							
							 MatrixCursor matrixCursor = convertToCursor(list);
				             mSearchViewAdapter.changeCursor(matrixCursor);
				             mSearchViewAdapter.notifyDataSetChanged();				             
				             autoCompleteTextView.showDropDown();
						}
					}
				}
				
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

		bsecure.showToast(errorCode);

		getView().findViewById(R.id.catgr_pbar).setVisibility(View.GONE);
		
		comid = "";

	}

	@Override
	public void onFragmentChildClick(View view) {
		
		int itemPosition = mRecyclerView.getChildLayoutPosition(view);
		
		Item item = adapter.getItems().get(itemPosition);

		bsecure.showComposeViewMessagePage(item);
		
		
		super.onFragmentChildClick(view);
	}	

	public void removeItem(String cid) {
		if (adapter == null)
			return;

		adapter.removeItem(cid);

		if (adapter.getCount() == 0) {
			getView().findViewById(R.id.catgr_txt).setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		
		
		if(tempId == 1) {
			MenuItem item = menu.findItem(R.id.cm_delete);			
			item.setVisible(false);			
			
			MenuItem cm_delete_done = menu.findItem(R.id.cm_delete_done);
			cm_delete_done.setVisible(true);
		} else if(tempId == 2) {
			MenuItem item = menu.findItem(R.id.cm_delete);
			if(item != null)
			   item.setVisible(true);			
			
			MenuItem cm_delete_done = menu.findItem(R.id.cm_delete_done);
			if(cm_delete_done != null)
				cm_delete_done.setVisible(false);
		}				

		super.onPrepareOptionsMenu(menu);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        
		switch (item.getItemId()) {		
				
		case android.R.id.home:
			
			bsecure.unLockMode();
			adapter.showCheckBoxs(false);
			adapter.notifyDataSetChanged();				
			
			getActivity().supportInvalidateOptionsMenu();
			tempId = 2;
			
			break;
		
		
		case R.id.cm_delete:
			
			adapter.showCheckBoxs(true);
			adapter.notifyDataSetChanged();						
			bsecure.lockMode();
			
			getActivity().supportInvalidateOptionsMenu();
			tempId = 1;
			
			break;
			
		case R.id.cm_delete_done:
			
			moveMailToTrash();
			
			bsecure.unLockMode();
			adapter.showCheckBoxs(false);
			adapter.notifyDataSetChanged();
			
			getActivity().supportInvalidateOptionsMenu();
			tempId = 2;
			
			break;			
			

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}


	@Override
	public String getFragmentName() {
		return "Inbox";//getString(R.string.inbox);
	}

	public void updateView(String cid, String packageId) {
		removeItem(cid);
	}
	
	public void refresh() {
		isRefresh =  true;
	}
	
	private void moveMailToTrash() {
		
		int count = adapter.getItems().size();
		
		for(int i = 0; i < count; i++) {
			Item item = adapter.getItems().get(i);
			if(item.getAttribute("checked").equalsIgnoreCase("y")) {		
				comid = comid+","+item.getAttribute("composeid");
			}
		}
		
		if(comid.trim().length() == 0)
			return;

		comid =  comid.substring(1);
		
		String url  = AppSettings.getInstance(bsecure).getPropertyValue("inboxTotrash");		
		url = url.replace("(EMAIL)", bsecure.getFromStore("email"));
		HTTPTask task = new HTTPTask(bsecure, this);
		task.setIds(comid);
		task.userRequest(getString(R.string.pwait), 3, url);	
		
	}
	
	private void parseMoveToTrashResponse(Object object) throws Exception{
		
		if(object != null) {
			JSONObject jsonObject = new JSONObject(object.toString());
			if (jsonObject.optString("status").equalsIgnoreCase("0")) {
				
				String temp[] = comid.split(",");
				
				for(int i  = 0; i < temp.length; i++) {
					adapter.removeItem(temp[i]);	
				}	
				adapter.notifyDataSetChanged();
				
			}
			bsecure.showToast(jsonObject.optString("statusdescription"));
		}
		
	}
	
	private MatrixCursor convertToCursor(ArrayList<String> feedlyResults) {
	       MatrixCursor cursor = new MatrixCursor(columns);
	       int i = 0;
	       
	       for (int j = 0; j < feedlyResults.size(); j++) {
	           String[] temp = new String[2];
	           i = i + 1;
	           temp[0] = Integer.toString(i);
	 
	           String feedUrl = feedlyResults.get(j);	           
	           temp[1] = feedUrl;           
	           cursor.addRow(temp);
	       }
	       return cursor;
	   }

}
