package cc.hughes.droidchatty;

import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import cc.hughes.droidchatty.net.ChattyService;
import cc.hughes.droidchatty.net.Message;
import cc.hughes.droidchatty.net.Message.Thread.Reply;
import cc.hughes.droidchatty.net.Message.ThreadList.RootPost;
import cc.hughes.droidchatty.util.TimeUtil;

/**
 * A fragment representing a single Thread detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link ThreadDetailActivity}
 * on handsets.
 */
public class ThreadDetailFragment extends ListFragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ROOT_POST = "root_post";

    private static final String TAG = "ThreadDetailFragment";

    private RootPost mRootPost;
    private String mThreadID;
    private int mIndentPx = 15;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ThreadDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            mRootPost = RootPost.parseFrom(getArguments().getByteArray(ARG_ROOT_POST));
            mThreadID = mRootPost.getId();
        } catch (InvalidProtocolBufferException e) {
            Log.e(TAG, "Error parsing root post item.", e);
        }
        
        setListAdapter(new ThreadDetailAdapter(getActivity(), R.layout.thread_detail_item, R.layout.row_loading));
    }
    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        ((ThreadDetailAdapter)getListAdapter()).setSelected(position);
    }
    

    class ThreadDetailAdapter extends LoadMoreArrayAdapter<Reply> {

        final static String CATEGORY_NWS = "nws";

        private ChattyService mService;
        private int mLayoutRes;
        private LayoutInflater mInflater;
        private Context mContext;
        private List<Reply> mItemCache;
        private int mSelected = -1;
        
        public ThreadDetailAdapter(Context context, int itemResource, int loadingResource) {
            super(context, loadingResource, LAYOUT_NONE);
            mLayoutRes = itemResource;
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mService = new ChattyService();
            
            // add the root post so it is displayed even while loading
            if (mRootPost != null) {
                Reply.Builder builder = Reply.newBuilder();
                builder.setAuthor(mRootPost.getAuthor());
                builder.setBody(mRootPost.getBody());
                builder.setCategeory(mRootPost.getCategory());
                builder.setDate(mRootPost.getDate());
                builder.setId(mRootPost.getId());
                builder.setDepth(0);
                
                super.add(builder.build());
            }
        }
        
        public void setSelected(int position) {
            if (mSelected == position)
                mSelected = -1;
            else
                mSelected = position;
            super.notifyDataSetChanged();
        }

        @Override
        protected View getNormalView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            
            if (convertView == null) {
                convertView = mInflater.inflate(mLayoutRes, null);
                holder = new ViewHolder();
                holder.authorName = (TextView)convertView.findViewById(R.id.author_name);
                holder.postContent = (TextView)convertView.findViewById(R.id.post_content);
                holder.postCategory = (TextView)convertView.findViewById(R.id.post_category);
                holder.postTime = (TextView)convertView.findViewById(R.id.post_time);
                holder.spacerContainer = (LinearLayout)convertView.findViewById(R.id.spacer_container);
                holder.postButtonsBar = convertView.findViewById(R.id.post_buttons_bar);
                
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            
            Reply reply = getItem(position);
		    long time = TimeUtil.parseDateTime(reply.getDate());
		    CharSequence timeAgo = DateUtils.getRelativeDateTimeString(getContext(), time, DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
		    
            holder.authorName.setText(reply.getAuthor());
            holder.postContent.setText(Html.fromHtml(reply.getBody()));
            holder.postTime.setText(timeAgo);
            holder.postCategory.setText(reply.getCategeory());
            
            boolean is_nws = reply.getCategeory().equals(CATEGORY_NWS);
            holder.postCategory.setVisibility(is_nws ? View.VISIBLE : View.GONE);
            
            holder.postButtonsBar.setVisibility((position == mSelected) ? View.VISIBLE : View.GONE);
            
            holder.spacerContainer.removeAllViews();
            for (int i = 0; i < reply.getDepth(); i++) {
                View spacer = new View(mContext);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(mIndentPx, LayoutParams.MATCH_PARENT));
                               
                int color = Math.min(8 * (i+5), 200);
                spacer.setBackgroundColor(Color.rgb(color, color, color));
                holder.spacerContainer.addView(spacer, i);
            }
            
            return convertView;
        }

        @Override
        protected boolean loadItems() throws Exception {
            Message.Thread thread = mService.getThread(mThreadID);
            mItemCache = thread.getReplyList();
            
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
            TextView authorName;
            TextView postContent;
            TextView postCategory;
            TextView postTime;
            LinearLayout spacerContainer;
            View postButtonsBar;
        }

        
    }
 }
