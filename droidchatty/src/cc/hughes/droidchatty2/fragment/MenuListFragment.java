package cc.hughes.droidchatty2.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import cc.hughes.droidchatty2.FragmentContextActivity;
import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.activity.SettingsActivity;

public class MenuListFragment extends ListFragment {

	public interface Callbacks {
		public void onMenuItemSelected(String id);
	}
	
	public static final String ID_HOME = "home";
	public static final String ID_SEARCH = "search";
	public static final String ID_MESSAGES = "messages";
    public static final String ID_TAGS = "tags";
	public static final String ID_SETTINGS = "settings";
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)	{
		super.onActivityCreated(savedInstanceState);
		
		getListView().setBackgroundResource(R.color.menu_background);
		getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.menu_divider)));
		getListView().setDividerHeight(2);
				
		MenuAdapter adapter = new MenuAdapter(getActivity());
		adapter.add(new MenuListItem("Home", android.R.drawable.ic_menu_info_details, ID_HOME));
		adapter.add(new MenuListItem("Search", R.drawable.ic_action_search, ID_SEARCH));
		adapter.add(new MenuListItem("Messages", R.drawable.ic_action_email, ID_MESSAGES));
		adapter.add(new MenuListItem("View LOLs", R.drawable.ic_action_labels, ID_TAGS));
		adapter.add(new MenuListItem("Settings", R.drawable.ic_action_settings, ID_SETTINGS));
		setListAdapter(adapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		MenuListItem item = (MenuListItem)getListAdapter().getItem(position);
		
		Fragment fragment = null;
		
		if (item.getID() == ID_HOME) {
		    fragment = new ThreadListFragment();
		} else if (item.getID() == ID_MESSAGES) {
		    fragment = new MessageListFragment();
        } else if (item.getID() == ID_SETTINGS) {
			Intent intent = new Intent(getActivity(), SettingsActivity.class);
			startActivity(intent);
		    return;
		} else {
            // not implemented yet
            return;
        }

		
		FragmentContextActivity fca = (FragmentContextActivity)getActivity();
		fca.changeContext(fragment, 0);
	}

	private class MenuListItem {
	
		String mID;
		String mTitle;
		int mIconResource;
		
		public MenuListItem(String title, int iconResource, String id) {
			mTitle = title;
			mIconResource = iconResource;
			mID = id;
		}
		
		public String getID() {
			return mID;
		}
		
		public String getTitle() {
			return mTitle;
		}
		
		public int getIconResource() {
			return mIconResource;
		}		
	}
		
	public class MenuAdapter extends ArrayAdapter<MenuListItem> {
		
		public MenuAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textview;
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_row, null);
				textview = (TextView)convertView.findViewById(R.id.row_title);
				convertView.setTag(textview);
			}
			else {
				textview = (TextView)convertView.getTag();
			}
			
			MenuListItem item = getItem(position);
			textview.setCompoundDrawablesWithIntrinsicBounds(item.getIconResource(), 0, 0, 0);
			textview.setText(item.getTitle());
			
			return convertView;
		}
		
	}
	
}