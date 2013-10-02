package cc.hughes.droidchatty2.fragment;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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

import com.squareup.wire.Wire;

import cc.hughes.droidchatty2.LoadMoreArrayAdapter;
import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;
import cc.hughes.droidchatty2.activity.PostActivity;
import cc.hughes.droidchatty2.net.*;
import cc.hughes.droidchatty2.net.Thread;
import cc.hughes.droidchatty2.net.Thread.Reply;
import cc.hughes.droidchatty2.net.ThreadList.RootPost;
import cc.hughes.droidchatty2.text.TagParser;
import cc.hughes.droidchatty2.util.TimeUtil;

/**
 * A fragment representing a single Thread detail screen.
 * This fragment is either contained in a {@link cc.hughes.droidchatty2.activity.MainActivity}
 */
public class ThreadDetailFragment extends ListFragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ROOT_POST = "root_post";

    private static final int REQUEST_POST_REPLY = 0;

    private static final String TAG = "ThreadDetailFragment";

    private RootPost mRootPost;
    private String mThreadID;
    private int mIndentPx = 15;
    
    private ActionMode mActionMode;
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
        
        Wire wire = new Wire();

        try {
            mRootPost = wire.parseFrom(getArguments().getByteArray(ARG_ROOT_POST), RootPost.class);
            mThreadID = mRootPost.id;
        } catch (IOException e) {
            e.printStackTrace();
        }

        setHasOptionsMenu(true);
        setListAdapter(new ThreadDetailAdapter(getActivity(), R.layout.thread_detail_item, R.layout.row_loading));
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
        
        if (position == mSelectedPosition) {
            // un-select the post, kill the contextual action bar
            mActionMode.finish();
            getListView().setItemChecked(position, false);
            mSelectedPosition = ListView.INVALID_POSITION;
        } else {
            // make sure we know what is selected
            mSelectedPosition = position;

            // if we aren't already showing the contextual action bar, show it now
            if (mActionMode == null) {
                mActionMode = getActivity().startActionMode(mActionModeCallback);
            }
        }
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
                        int id = data.getExtras().getInt(PostActivity.RESULT_POST_ID);
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
            mActionMode = null;
            
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

    class ThreadDetailAdapter extends LoadMoreArrayAdapter<Reply> {

        final static String CATEGORY_NWS = "nws";

        private ChattyService mService;
        private int mLayoutRes;
        private LayoutInflater mInflater;
        private Context mContext;
        private List<Reply> mItemCache;
        
        public ThreadDetailAdapter(Context context, int itemResource, int loadingResource) {
            super(context, loadingResource, LAYOUT_NONE);
            mLayoutRes = itemResource;
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mService = new ChattyService();
            
            // add the root post so it is displayed even while loading
            addRootPost();
        }

        private void addRootPost() {
            if (mRootPost != null) {
                Reply.Builder builder = new Reply.Builder()
                    .author(mRootPost.author)
                    .body(mRootPost.body)
                    .categeory(mRootPost.category)
                    .date(mRootPost.date)
                    .id(mRootPost.id)
                    .depth(0);

                super.add(builder.build());
            }
        }

        @Override
        protected View getNormalView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            
            if (convertView == null) {
                convertView = mInflater.inflate(mLayoutRes, null);
                holder = new ViewHolder();
                ViewInjector.inject(holder, convertView);
                //holder.postContent.setMovementMethod(LinkMovementMethod.getInstance());
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            
            Reply reply = getItem(position);
            String timeAgo = TimeUtil.format(getContext(), reply.date);
		    
            holder.authorName.setText(reply.author);
            holder.postContent.setText(reply.bodyParsed);
            holder.postTime.setText(timeAgo);
            holder.postCategory.setText(reply.categeory);
            
            boolean is_nws = reply.categeory.equals(CATEGORY_NWS);
            holder.postCategory.setVisibility(is_nws ? View.VISIBLE : View.GONE);
            
            holder.spacerContainer.removeAllViews();
            for (int i = 0; i < reply.depth; i++) {
                View spacer = new View(mContext);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(mIndentPx, LayoutParams.MATCH_PARENT));
                               
                int color = Math.min(8 * (i+5), 200);
                spacer.setBackgroundColor(Color.rgb(color, color, color));
                holder.spacerContainer.addView(spacer, i);
            }
            
            return convertView;
        }

        @Override
        public void clear() {
            // clear the existing posts, add the root post back, and tell the adapter to load
            super.clear();
            addRootPost();
            setKeepLoading(true);
        }

        @Override
        protected boolean loadItems() throws Exception {
            Thread thread = mService.getThread(mThreadID);
            mItemCache = thread.reply;
            
            // if root post is already added, don't add it again
            if (mRootPost != null)
                mItemCache = mItemCache.subList(1, mItemCache.size());
            
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
            @ViewInjected(R.id.spacer_container)
            LinearLayout spacerContainer;
        }
        
    }
 }
