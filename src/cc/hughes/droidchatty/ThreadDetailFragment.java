package cc.hughes.droidchatty;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import cc.hughes.droidchatty.dummy.DummyContent;
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
@SuppressLint("ValidFragment")
public class ThreadDetailFragment extends ListFragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ROOT_POST = "root_post";

    private RootPost mRootPost;
    private String mThreadID;
    private int mIndentPx = 5;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    @SuppressLint("ValidFragment")
    public ThreadDetailFragment(RootPost rootPost) {
        super();
        mRootPost = rootPost;
        mThreadID = mRootPost.getId();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setListAdapter(new ThreadDetailAdapter(getActivity(), R.layout.thread_detail_item, R.layout.row_loading));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // determine the width of the indent for each comment level
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        mIndentPx = Math.min(display.getWidth(), display.getHeight()) / 50;
    }

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
            if (reply.getCategeory() == CATEGORY_NWS)
                holder.postCategory.setText(CATEGORY_NWS);
            else
                holder.postCategory.setText("");
            
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
            
        }
        
    }
 }
