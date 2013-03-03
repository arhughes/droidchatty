package cc.hughes.droidchatty;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuListFragment extends ListFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState)	{
		super.onActivityCreated(savedInstanceState);
		
		MenuAdapter adapter = new MenuAdapter(getActivity());
		adapter.add(new MenuListItem("Home", android.R.drawable.ic_menu_info_details));
		adapter.add(new MenuListItem("Search", android.R.drawable.ic_menu_search));
		adapter.add(new MenuListItem("Settings", android.R.drawable.ic_menu_preferences));
		setListAdapter(adapter);
	}
	
	private class MenuListItem {
	
		String mTitle;
		int mIconResource;
		
		public MenuListItem(String title, int iconResource) {
			mTitle = title;
			mIconResource = iconResource;
		}
		
		public String getTitle() {
			return mTitle;
		}
		
		public int getIconResource() {
			return mIconResource;
		}
		
	}
	
	private class ViewHolder {
		ImageView icon;
		TextView title;
	}
	
	public class MenuAdapter extends ArrayAdapter<MenuListItem> {
		
		public MenuAdapter(Context context) {
			super(context, 0);
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.row, null);
				holder = new ViewHolder();
				holder.icon = (ImageView)convertView.findViewById(R.id.row_icon);
				holder.title = (TextView)convertView.findViewById(R.id.row_title);	
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			MenuListItem item = getItem(position);
			holder.icon.setImageResource(item.getIconResource());
			holder.title.setText(item.getTitle());
			
			return convertView;
		}
		
	}
	
}
