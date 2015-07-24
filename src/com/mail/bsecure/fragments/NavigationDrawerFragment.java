package com.mail.bsecure.fragments;

import java.util.Vector;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bsecure.mail.R;
import com.mail.bsecure.Bsecure;
import com.mail.bsecure.adapter.MenuAdapter;
import com.mail.bsecure.common.Item;

public class NavigationDrawerFragment extends Fragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private NavigationDrawerCallbacks mCallbacks;
    
    private Bsecure bsecure;

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    
    private View mLayout = null;
    
    private ListView mDrawerListView;    
    
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    
    private MenuAdapter mMenuAdpt = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {    	    	
    	
    	mLayout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    	
        mDrawerListView = (ListView)mLayout.findViewById(R.id.lv_sidemenu);
        
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        
        mMenuAdpt = new MenuAdapter(bsecure); 
        
        mDrawerListView.setAdapter(mMenuAdpt);
        
		String[] titles = getResources().getStringArray(R.array.menuArray);
		TypedArray icons = getResources().obtainTypedArray(R.array.menuicons);

		Vector<Item> items = new Vector<Item>();
		for (int i = 0; i < titles.length; i++) {
			Item item = new Item("");
			item.setAttribute("TITLE", titles[i]);
			item.setAttribute("IMAGE", icons.getResourceId(i, 0) + "");
			items.add(item);
		}
		
		icons.recycle();
		icons = null;

		mMenuAdpt.setItems(items);
		mMenuAdpt.notifyDataSetChanged();              
        
        return mLayout;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
    
    public void close() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        actionBar.setDisplayShowHomeEnabled(true);                
        actionBar.setHomeButtonEnabled(true);
        //actionBar.setIcon(R.drawable.ic_launcher);
        
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                /*R.drawable.ic_drawer, */            /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);                                
                
                if (!isAdded()) {
                    return;
                }                

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                
            }           
            
        };
        
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        mDrawerToggle.setDrawerIndicatorEnabled(true);                
                
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	bsecure = (Bsecure)activity;
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	    	
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }        
        
        /*if (item.getItemId() == R.id.action_example) {
            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }    

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
    
    public void updateMailCounts(Item item) {
    	
    	
    	
    	Object object =  item.get("inboxcount");
    	if(object != null) {
    		    		    		
    		Vector<Item> items =  (Vector<Item>)object;
    		Item temp = items.get(0);
    		String cnt  = temp.getAttribute("icount");
    		if(!cnt.equalsIgnoreCase("0")) {
    			mMenuAdpt.getItems().get(0).setAttribute("COUNT", "("+cnt+")");
    		}
    	}
    	
    	object =  item.get("sentmailcount");
    	if(object != null) {
    		Vector<Item> items =  (Vector<Item>)object;
    		Item temp = items.get(0);
    		String cnt  = temp.getAttribute("scount");
    		if(!cnt.equalsIgnoreCase("0") ) {
    			mMenuAdpt.getItems().get(1).setAttribute("COUNT", "("+cnt+")");
    		}
    	}
    	
    	object =  item.get("draftscount");
    	if(object != null) {
    		Vector<Item> items =  (Vector<Item>)object;
    		Item temp = items.get(0);
    		String cnt  = temp.getAttribute("dcount");
    		if(!cnt.equalsIgnoreCase("0") ) {
    			mMenuAdpt.getItems().get(2).setAttribute("COUNT", "("+cnt+")");
    		}
    	}
    	
    	object =  item.get("trashcount");
    	if(object != null) {
    		Vector<Item> items =  (Vector<Item>)object;
    		Item temp = items.get(0);
    		String cnt  = temp.getAttribute("tcount");
    		if(!cnt.equalsIgnoreCase("0") ) {
    			mMenuAdpt.getItems().get(3).setAttribute("COUNT", "("+cnt+")");
    		}
    	}
    	
    	mMenuAdpt.notifyDataSetChanged();
    	
	}
    
    public void lockMode() {
    	mDrawerToggle.setDrawerIndicatorEnabled(false);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    
    public void unLockMode() {
    	mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
	
}
