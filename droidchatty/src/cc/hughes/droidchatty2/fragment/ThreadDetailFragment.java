package cc.hughes.droidchatty2.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Spanned;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import cc.hughes.droidchatty2.LoadMoreArrayAdapter;
import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;
import cc.hughes.droidchatty2.activity.BrowserActivity;
import cc.hughes.droidchatty2.activity.MainActivity;
import cc.hughes.droidchatty2.activity.PostActivity;
import cc.hughes.droidchatty2.net.*;
import cc.hughes.droidchatty2.net.Reply;
import cc.hughes.droidchatty2.net.RootPost;
import cc.hughes.droidchatty2.text.InternalURLSpan;
import cc.hughes.droidchatty2.util.AppUtil;
import cc.hughes.droidchatty2.util.TimeUtil;

/**
 * A fragment representing a single Thread detail screen.
 * This fragment is either contained in a {@link cc.hughes.droidchatty2.activity.MainActivity}
 */
public class ThreadDetailFragment extends ListFragment implements InternalURLSpan.LinkListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ROOT_POST = "root_post";
    public static final String ARG_POST_ID = "POST_ID";

    private static final int REQUEST_POST_REPLY = 0;

    private static final String TAG = "ThreadDetailFragment";

    private RootPost mRootPost;
    private int mThreadID;

    private int mSelectedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ThreadDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments().containsKey(ARG_ROOT_POST)) {
            mRootPost = (RootPost)getArguments().getSerializable(ARG_ROOT_POST);
            mThreadID = mRootPost.id;
        } else {
            mThreadID = getArguments().getInt(ARG_POST_ID);
        }

        Crashlytics.log(Log.DEBUG, TAG, "Viewing thread with id: " + mThreadID);

        setHasOptionsMenu(true);
        ThreadDetailAdapter adapter = new ThreadDetailAdapter(getActivity(), R.layout.thread_detail_item, R.layout.thread_detail_item_expanded, R.layout.row_loading);
        setListAdapter(adapter);
        adapter.startLoadingItems();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // set single choice mode to highlight the selected item
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.thread_detail_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.thread_detail_refresh:
                ((ThreadDetailAdapter)getListAdapter()).clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        ((ThreadDetailAdapter)getListAdapter()).setExpanded(position);
    }
    
    
    ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            // probably need to know which post we are on
            Reply reply = (Reply)getListAdapter().getItem(mSelectedPosition);

            switch (item.getItemId()) {
                case R.id.menu_post_reply:
                    replyPost(reply);
                    mode.finish();
                    return true;
                case R.id.menu_post_tag:
                    Toast.makeText(getActivity(), "This is where you would tag the post.", Toast.LENGTH_SHORT).show();
                    mode.finish();
                    return true;
                case R.id.menu_post_share:
                    sharePost(reply);
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        void replyPost(Reply reply) {

            boolean isNewsThread = ((Reply)getListAdapter().getItem(0)).author.equalsIgnoreCase("shacknews");
            Intent intent = new Intent(getActivity(), PostActivity.class);
            intent.putExtra(PostActivity.PARENT_ID, reply.id);
            intent.putExtra(PostActivity.IS_NEWS_THREAD, isNewsThread);
            startActivityForResult(intent, REQUEST_POST_REPLY);
        }

        void sharePost(Reply reply) {
            String url = "http://www.shacknews.com/chatty?id=" + reply.id;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, url);

            startActivity(Intent.createChooser(intent, "Share link"));
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case REQUEST_POST_REPLY:
                    if (resultCode == Activity.RESULT_OK) {
                        //int id = data.getExtras().getInt(PostActivity.RESULT_POST_ID);
                        // just reload for now
                        //TODO: force scroll to new post
                        //mThreadID = Integer.toString(id);
                        ((ThreadDetailAdapter)getListAdapter()).clear();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.post_context, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

            // all done, make sure the item is unselected
            if (mSelectedPosition != ListView.INVALID_POSITION) {
                getListView().setItemChecked(mSelectedPosition, false);
                mSelectedPosition = ListView.INVALID_POSITION;
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        
    };

    @Override
    public void onLinkClicked(String href) {

        Uri uri = Uri.parse(href);
        if (uri.getHost().endsWith("shacknews.com") && uri.getQueryParameterNames().contains("id")) {
            // chatty link
            Bundle args = new Bundle();
            args.putInt(ARG_POST_ID, Integer.parseInt(uri.getQueryParameter("id").trim()));

            ThreadDetailFragment fragment = new ThreadDetailFragment();
            fragment.setArguments(args);
            ((MainActivity)getActivity()).changeContext(fragment, this);
            return;
        }

        Intent intent;
        if (!useInAppBrowser() || linkIsSpecial(uri)) {
            // show outside app
            intent = new Intent(Intent.ACTION_VIEW, uri);
        } else {
            // show inside app
            intent = new Intent(getActivity(), BrowserActivity.class);
            intent.putExtra(BrowserActivity.ARG_URL, href);
        }
        startActivity(intent);
    }

    private boolean useInAppBrowser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getBoolean("pref_internal_browser", true);
    }

    private boolean linkIsSpecial(Uri uri) {

        if (AppUtil.isInstalled("com.google.android.youtube", getActivity()) && (uri.getHost().equals("youtu.be") || uri.getHost().equals("www.youtube.com"))) {
            return true;
        }
        else if (uri.getHost().startsWith("play.google")) {
            return true;
        }
        else if (AppUtil.isInstalled("com.vimeo.android.videoapp", getActivity()) && uri.getHost().equals("vimeo.com")) {
            return true;
        }

        return false;
    }

    class ThreadDetailAdapter extends LoadMoreArrayAdapter<Reply> {

        final static String CATEGORY_NWS = "nws";

        private ChattyService mService;
        private int mLayoutRes;
        private int mLayoutExpandedRes;
        private LayoutInflater mInflater;
        private Context mContext;
        private List<Reply> mItemCache;
        private int mExpandedPosition = 0;

        private String mOriginalPoster = "";
        
        public ThreadDetailAdapter(Context context, int itemResource, int itemResourceExpanded, int loadingResource) {
            super(context, loadingResource, LAYOUT_NONE);
            mLayoutRes = itemResource;
            mLayoutExpandedRes = itemResourceExpanded;
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mService = new ChattyService(PreferenceManager.getDefaultSharedPreferences(context));
            
            // add the root post so it is displayed even while loading
            addRootPost();
        }

        private void addRootPost() {
            if (mRootPost != null) {
                Reply root = Reply.fromRootPost(mRootPost);
                super.add(root);
                mOriginalPoster = mRootPost.author;
            }
        }

        public void setExpanded(int position) {
            mExpandedPosition = position;
            notifyDataSetChanged();
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            int type = super.getItemViewType(position);
            if (type != IGNORE_ITEM_VIEW_TYPE && (position == 0 || position == mExpandedPosition))
                return 1;

            return type;
        }

        @Override
        protected View getNormalView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            boolean is_expanded = position == 0 || position == mExpandedPosition;
            
            if (convertView == null) {
                convertView = mInflater.inflate(is_expanded ? mLayoutExpandedRes : mLayoutRes, null);
                holder = new ViewHolder();
                ViewInjector.inject(holder, convertView);
                //holder.postContent.setMovementMethod(LinkMovementMethod.getInstance());
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            Reply reply = getItem(position);
            holder.authorName.setText(reply.author);
            holder.postContent.setText(is_expanded ? reply.bodyParsed() : reply.bodyParsedPreview());

            LayerDrawable branches = getTreeBranches(reply.bullets);

            if (is_expanded)
            {
                String timeAgo = TimeUtil.format(getContext(), reply.date);
                handleLinks(reply.bodyParsed());

                holder.postTime.setText(timeAgo);
                holder.postCategory.setText(reply.category);
                boolean is_nws = reply.category.equals(CATEGORY_NWS);
                holder.postCategory.setVisibility(is_nws ? View.VISIBLE : View.GONE);
                holder.authorName.setCompoundDrawablesWithIntrinsicBounds(branches, null, null, null);
            }
            else
            {
                holder.postContent.setCompoundDrawablesWithIntrinsicBounds(branches, null, null, null);

                // highlight new posts
                int newness_color = (17 * reply.newness) + 85;
                holder.postContent.setTextColor(Color.argb(255, newness_color, newness_color, newness_color));
            }

            // highlight op's name
            if (position != 0 && reply.author.equals(mOriginalPoster))
                holder.authorName.setTextColor(getResources().getColor(R.color.author_name_op));
            else
                holder.authorName.setTextColor(getResources().getColor(R.color.author_name));

            return convertView;
        }

        private LayerDrawable getTreeBranches(TreeBullet[] bullets) {
            int n = bullets.length;
            Drawable[] d = new Drawable[n];
            for (int i = 0; i < n; i++)
                d[i] = getResources().getDrawable(bullets[i].getResource());

            int width = 0;
            if (n > 0)
                width = d[0].getIntrinsicWidth();

            LayerDrawable l = new LayerDrawable(d);
            for (int i = 0; i < n; i++) {
                l.setLayerInset(i, i * width, 0, ((n - 1)-i) * width, 0);
            }
            return l;
        }

        private void handleLinks(Spanned body) {
            InternalURLSpan spans[] = body.getSpans(0, body.length(), InternalURLSpan.class);
            for (InternalURLSpan span : spans) {
                span.setLinkListener(ThreadDetailFragment.this);
            }
        }

        @Override
        public void clear() {
            // don't try to refresh the view while we are mucking with it
            setNotifyOnChange(false);

            // clear the existing posts, add the root post back, and tell the adapter to load
            super.clear();
            addRootPost();
            setKeepLoading(true);

            // tell list view we are updated
            notifyDataSetChanged();
        }

        @Override
        protected boolean loadItems() throws Exception {
            List<Reply> replies = mService.getThread(mThreadID);
            mItemCache = replies;
            
            // if root post is already added, don't add it again
            if (mRootPost != null)
                mItemCache = mItemCache.subList(1, mItemCache.size());
            else if (mItemCache.size() > 0)
                mOriginalPoster = mItemCache.get(0).author;

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
            @ViewInjected(R.id.post_category)
            TextView postCategory;
            @ViewInjected(R.id.post_time)
            TextView postTime;
        }
        
    }
 }
