package cc.hughes.droidchatty2.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;

import cc.hughes.droidchatty2.FragmentContextActivity;
import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.fragment.ThreadListFragment;


/**
 * An activity representing a list of Threads. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link cc.hughes.droidchatty2.fragment.ThreadDetailFragment} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link cc.hughes.droidchatty2.fragment.ThreadListFragment} and the item details
 * (if present) is a {@link cc.hughes.droidchatty2.fragment.ThreadDetailFragment}.
 */
public class MainActivity extends ActionBarActivity
        implements ViewPager.OnPageChangeListener, FragmentContextActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    
    ThreadPageAdapter mThreadPageAdapter;
    DrawerLayout mDrawerLayout;
    View mDrawerMenu;
    ViewPager mViewPager;

    ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //setTheme(android.R.style.Theme_Holo_Light);

        setContentView(R.layout.main);

        ThreadListFragment mainFragment = new ThreadListFragment();
        
        if (findViewById(R.id.thread_list_container) != null) {
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

            mViewPager = (ViewPager)findViewById(R.id.pager);
            mViewPager.setOnPageChangeListener(this);
            mThreadPageAdapter = new ThreadPageAdapter(getSupportFragmentManager(), mViewPager);
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

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        mDrawerMenu = findViewById(R.id.sliding_menu);
        // TODO: If exposing deep links into your app, handle intents here.
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close )
        {
            public void onDrawerClosed(View view) { }
            public void onDrawerOpened(View view) { }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /** Sets whether the drawer is enabled or not */
    void setDrawerEnabled(boolean enabled) {
        mDrawerLayout.setDrawerLockMode(enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle.setDrawerIndicatorEnabled(enabled);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case android.R.id.home:
    			if (!mTwoPane && mViewPager.getCurrentItem() >= 1)
    	    		mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
    			else if (mDrawerLayout.isDrawerOpen(mDrawerMenu))
                    mDrawerLayout.closeDrawer(mDrawerMenu);
                else
                    mDrawerLayout.openDrawer(mDrawerMenu);
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
                setDrawerEnabled(false);
	    }

        if (mDrawerLayout.isDrawerOpen(mDrawerMenu))
            mDrawerLayout.closeDrawer((mDrawerMenu));

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
                setDrawerEnabled(true);
		}
	
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) { }

	@Override
	public void onPageSelected(int position) { }
    
    @Override
	public void onBackPressed() {
    	
    	if (mDrawerLayout.isDrawerOpen(mDrawerMenu)) {
    		// If the sliding menu is showing, close it.
            mDrawerLayout.closeDrawer(mDrawerMenu);
    	}
    	else if (!mTwoPane && mViewPager.getCurrentItem() >= 1) {
    		// If the thread details are showing, close it
    		mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
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
            for (Fragment mFragment : mFragments) {
                if (object == mFragment) {
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
