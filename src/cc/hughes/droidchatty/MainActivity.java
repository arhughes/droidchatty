package cc.hughes.droidchatty;

import java.util.ArrayList;
import java.util.List;

import com.slidingmenu.lib.SlidingMenu;

import android.app.ActionBar;
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
        implements ViewPager.OnPageChangeListener, FragmentContextActivity {

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
        	//mThreadPageAdapter.setMainFragment(mainFragment);
        	mThreadPageAdapter.setFragment(mainFragment, 0);
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

    @Override
    public void changeContext(Fragment fragment, int level) {
        
	    if (mTwoPane && level == 0)
	    {
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.thread_list_container, fragment)
				.commit();
	    }
	    else
	    {
	        // in two pane view the ViewPanel doesn't contain level 0.
	        if (mTwoPane)
	            level--;
	        
	        mThreadPageAdapter.setFragment(fragment, level);
	        mViewPager.setCurrentItem(level);
	        
	        if (level > 0 && !mTwoPane)
	            mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
	    }
	    
	    if (mSlidingMenu.isMenuShowing())
	        mSlidingMenu.toggle();
	    
	}
	
	@Override
	public void onPageScrollStateChanged(int state) {
		// When the thread list is selected, remove the thread detail
		// fragment from the View Pager, and re-enable the sliding menu.
		if (state == ViewPager.SCROLL_STATE_IDLE)
		{
			//mThreadPageAdapter.removeDetailFragment();
		    mThreadPageAdapter.removeFragmentsAfter(mViewPager.getCurrentItem());
		    if (mViewPager.getCurrentItem() == 0)
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

	    List<Fragment> mFragments;
    	ViewPager mViewPager;
    	
		public ThreadPageAdapter(FragmentManager fm, ViewPager pager) {
			super(fm);
			mFragments = new ArrayList<Fragment>();
			mViewPager = pager;
			mViewPager.setAdapter(this);
		}
		
		public void setFragment(Fragment fragment, int level)
		{
		    mFragments.add(level, fragment);
		    removeFragmentsAfter(level);
		    mViewPager.setCurrentItem(level);
		}
		
		public void removeFragmentsAfter(int level)
		{
		    for (int i = level + 1; i < mFragments.size(); i++)
    		    mFragments.remove(i);
			super.notifyDataSetChanged();			
		}
		
		@Override
		public Fragment getItem(int i)
		{
		    return mFragments.get(i);
		}
		
		@Override 
		public int getItemPosition(Object object)
		{
		    for (int i = 0; i < mFragments.size(); i++) {
		        if (object == mFragments.get(i)) {
    				return PagerAdapter.POSITION_UNCHANGED;
		        }
		    }
			return PagerAdapter.POSITION_NONE;
		}
		
		@Override
		public int getCount()
		{
		    return mFragments.size();
		}
    }

}
