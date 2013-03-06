package cc.hughes.droidchatty;

import com.slidingmenu.lib.SlidingMenu;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;


/**
 * An activity representing a list of Threads. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ThreadDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ThreadListFragment} and the item details
 * (if present) is a {@link ThreadDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ThreadListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MainActivity extends FragmentActivity
        implements ThreadListFragment.Callbacks, ViewPager.OnPageChangeListener, MenuListFragment.Callbacks, MessageListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    
    ThreadPageAdapter mThreadPageAdapter;
    ViewPager mViewPager;
    SlidingMenu mSlidingMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTheme(android.R.style.Theme_Holo);
        setContentView(R.layout.activity_thread_list);

        ThreadListFragment mainFragment = new ThreadListFragment();
        
        if (findViewById(R.id.thread_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            
        	getSupportFragmentManager().beginTransaction()
				.replace(R.id.thread_list_container, mainFragment)
				.commit();
            
            //mainFragment.setActivateOnItemClick(true);
        }
        else
        {
        	
        	mViewPager = (ViewPager)findViewById(R.id.pager);
        	mViewPager.setOnPageChangeListener(this);
        	mThreadPageAdapter = new ThreadPageAdapter(getSupportFragmentManager(), mViewPager);
        	
        	// default to thread list fragment
        	mThreadPageAdapter.setMainFragment(mainFragment);
        }

        // TODO: If exposing deep links into your app, handle intents here.
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_width);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        mSlidingMenu.setMenu(R.layout.menu);
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
    			if (!mTwoPane && mViewPager.getCurrentItem() == 1)
    	    		mViewPager.setCurrentItem(0, true);
    			else
    				mSlidingMenu.toggle(true);
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    /**
     * Callback method from {@link ThreadListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onThreadListItemSelected(String id) {

        Bundle arguments = new Bundle();
        arguments.putString(ThreadDetailFragment.ARG_ITEM_ID, id);
    	
        ThreadDetailFragment fragment = new ThreadDetailFragment();
        fragment.setArguments(arguments);
        
        setDetailFragment(fragment);
    }
        
    /**
     * Callback method from {@link MessageListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onMessageListItemSelected(String id) {

        Bundle arguments = new Bundle();
        arguments.putString(ThreadDetailFragment.ARG_ITEM_ID, id);
    	
        MessageDetailFragment fragment = new MessageDetailFragment();
        fragment.setArguments(arguments);
       
        setDetailFragment(fragment);       
    }  
    
    /**
     * Callback method from {@link MenuListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
	@Override
	public void onMenuItemSelected(String id) {
		
		Fragment fragment = null;
		
		// determine the correct main fragment to use
		if (id == MenuListFragment.ID_HOME) {
			fragment = new ThreadListFragment();
		}
		else if (id == MenuListFragment.ID_MESSAGES) {
			fragment = new MessageListFragment();
		}
		else if (id == MenuListFragment.ID_SETTINGS) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return;
		}

		// close the menu
		mSlidingMenu.toggle(true);
		
		setMainFragment(fragment);
	}
	
	private void setMainFragment(Fragment fragment)
	{
		if (mTwoPane) {			
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.thread_list_container, fragment)
				.commit();
		}
		else {
			mThreadPageAdapter.setMainFragment(fragment);
		}
	}
	
	private void setDetailFragment(Fragment fragment)
	{
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.           
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.thread_detail_container, fragment)
                    .commit();

        } else {
        	// In single-pane mode, show the detail in the second pane of
        	// the view pager, and disable the sliding menu.
        	mThreadPageAdapter.setDetailFragment(fragment);
        	mViewPager.setCurrentItem(1);
        	mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        }
	}
    
	@Override
	public void onPageScrollStateChanged(int state) {
		// When the thread list is selected, remove the thread detail
		// fragment from the View Pager, and re-enable the sliding menu.
		if (state == ViewPager.SCROLL_STATE_IDLE && mViewPager.getCurrentItem() == 0)
		{
			mThreadPageAdapter.removeDetailFragment();
			mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		}
	
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) { }

	@Override
	public void onPageSelected(int position) { }
    
    @Override
	public void onBackPressed() {
    	
    	if (mSlidingMenu.isMenuShowing()) {
    		// If the sliding menu is showing, close it.
    		mSlidingMenu.toggle(true);
    	}
    	else if (!mTwoPane && mViewPager.getCurrentItem() == 1) {
    		// If the thread details are showing, close it
    		mViewPager.setCurrentItem(0, true);
    	}
    	else {
    		super.onBackPressed();
    	}
	}

	public class ThreadPageAdapter extends FragmentStatePagerAdapter  {

		Fragment mMainFragment;
		Fragment mDetailFragment;
		
    	ViewPager mViewPager;
    	
		public ThreadPageAdapter(FragmentManager fm, ViewPager pager) {
			super(fm);
			mViewPager = pager;
			mViewPager.setAdapter(this);
		}
		
		public void setMainFragment(Fragment fragment)
		{
			mMainFragment = fragment;
			mDetailFragment = null;
			super.notifyDataSetChanged();
		}
		
		public void setDetailFragment(Fragment fragment)
		{
			mDetailFragment = fragment;
			super.notifyDataSetChanged();
		}
		
		public void removeDetailFragment() {
			mDetailFragment = null;
			super.notifyDataSetChanged();			
		}
		
		@Override
		public Fragment getItem(int i)
		{
			if (i == 0)
				return mMainFragment;
			if (i == 1)
				return mDetailFragment;
			
			return null;
		}
		
		@Override 
		public int getItemPosition(Object object)
		{
			if (object == mMainFragment)
				return PagerAdapter.POSITION_UNCHANGED;
			return PagerAdapter.POSITION_NONE;
		}
		
		@Override
		public int getCount()
		{
			return mDetailFragment == null ? 1 : 2;
		}
    }

}
