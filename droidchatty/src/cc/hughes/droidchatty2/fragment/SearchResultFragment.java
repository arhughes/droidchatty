package cc.hughes.droidchatty2.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cc.hughes.droidchatty2.FragmentContextActivity;
import cc.hughes.droidchatty2.LoadMoreArrayAdapter;
import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;
import cc.hughes.droidchatty2.net.ChattyService;
import cc.hughes.droidchatty2.net.RootPost;
import cc.hughes.droidchatty2.text.TagParser;
import cc.hughes.droidchatty2.util.TimeUtil;

public class SearchResultFragment extends ListFragment {

    public static final String ARG_TERM = "TERM";
    public static final String ARG_AUTHOR = "AUTHOR";
    public static final String ARG_PARENT_AUTHOR = "PARENT_AUTHOR";

    private String mSearchTerms;
    private String mSearchAuthor;
    private String mSearchParentAuthor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchTerms = getArguments().getString(ARG_TERM);
        mSearchAuthor = getArguments().getString(ARG_AUTHOR);
        mSearchParentAuthor = getArguments().getString(ARG_PARENT_AUTHOR);

        setListAdapter(new SearchResultsAdapter(getActivity()));
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        RootPost rootPost = (RootPost)getListAdapter().getItem(position);
        Bundle args = new Bundle();

        args.putInt(ThreadDetailFragment.ARG_POST_ID, rootPost.id);
        ThreadDetailFragment fragment = new ThreadDetailFragment();
        fragment.setArguments(args);

        FragmentContextActivity fca = (FragmentContextActivity)getActivity();
        fca.changeContext(fragment, 2);
    }

    private class SearchResultsAdapter extends LoadMoreArrayAdapter<RootPost> {

        private int mCurrentPage = 0;
        private int mLayoutRes = R.layout.search_result_item;

        private LayoutInflater mInflater;
        private ChattyService mService;

        private List<RootPost> mItemCache;

        public SearchResultsAdapter(Context context) {
            super(context, R.layout.row_loading, R.layout.row_finished);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mService = new ChattyService(PreferenceManager.getDefaultSharedPreferences(context));
        }

        @Override
        protected View getNormalView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(mLayoutRes, null);
                holder = new ViewHolder();
                ViewInjector.inject(holder, convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder)convertView.getTag();
            }

            RootPost rootPost = getItem(position);

            String timeAgo = TimeUtil.format(getContext(), rootPost.date);

            holder.authorName.setText(rootPost.author);
            holder.postContent.setText(TagParser.fromHtml(rootPost.body));
            holder.postTime.setText(timeAgo);

            return convertView;
        }

        @Override
        protected boolean loadItems() throws Exception {
            List<RootPost> threads = mService.search(mCurrentPage + 1, mSearchTerms, mSearchAuthor, mSearchParentAuthor);
            if (threads != null && threads.size() > 0) {
                mItemCache = threads;
                mCurrentPage += 1;
                return true;
            }

            mItemCache = null;
            return false;
        }

        @Override
        protected void appendItems() {
            if (mItemCache != null) {
                super.addAll(mItemCache);
                mItemCache = null;
            }
        }

        class ViewHolder {
            @ViewInjected(R.id.author_name)
            TextView authorName;

            @ViewInjected(R.id.post_content)
            TextView postContent;

            @ViewInjected(R.id.post_time)
            TextView postTime;
        }

    }

}
