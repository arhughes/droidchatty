package cc.hughes.droidchatty2.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.hughes.droidchatty2.FragmentContextActivity;
import cc.hughes.droidchatty2.LoadMoreArrayAdapter;
import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;
import cc.hughes.droidchatty2.data.PostCountDatabase;
import cc.hughes.droidchatty2.dummy.DummyContent;
import cc.hughes.droidchatty2.net.ChattyService;
import cc.hughes.droidchatty2.net.Message;
import cc.hughes.droidchatty2.net.ThreadList;
import cc.hughes.droidchatty2.text.TagParser;
import cc.hughes.droidchatty2.util.TimeUtil;

/**
 * A list fragment representing a list of Messages. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link MessageDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MessageListFragment extends ListFragment {

    private MessageListAdapter mAdapter;

	public MessageListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        List<Message> messages = new ArrayList<Message>();
        int page = 0;
        mAdapter = new MessageListAdapter(messages, page, getActivity(), R.layout.thread_list_item, R.layout.row_loading, R.layout.row_finished);
        setListAdapter(mAdapter);

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

        Message message = mAdapter.getItem(position);

        Bundle args = new Bundle();
        args.putSerializable(MessageDetailFragment.ARG_MESSAGE, message);
        MessageDetailFragment fragment = new MessageDetailFragment();
        fragment.setArguments(args);

		FragmentContextActivity fca = (FragmentContextActivity)getActivity();
		fca.changeContext(fragment, 1);
	}

    class MessageListAdapter extends LoadMoreArrayAdapter<Message> {

        private ChattyService mService;
        private int mCurrentPage = 0;
        private int mLayoutRes;
        private LayoutInflater mInflater;
        private List<Message> mItemCache;
        private String mFolder = Message.FOLDER_INBOX;

        public MessageListAdapter(List<Message> threads, int page, Context context, int itemResource, int loadingResource, int finishedResource) {
            super(context, loadingResource, finishedResource, threads);
            mLayoutRes = itemResource;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mService = new ChattyService(PreferenceManager.getDefaultSharedPreferences(getActivity()));
            mCurrentPage = page;
        }

        @Override
        public void clear() {
            mCurrentPage = 0;
            super.clear();
        }

        @Override
        protected boolean loadItems() throws Exception {
            List<Message> messages = mService.getMessages(mFolder, mCurrentPage + 1);
            if (messages != null && messages.size() > 0) {
                mItemCache = messages;
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

        @Override
        public View getNormalView(int position, View convertView, ViewGroup parent) {
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

            Message message = getItem(position);

            //String timeAgo = TimeUtil.format(getContext(), rootPost.date);
            holder.authorName.setText(message.OtherUser);
            holder.postTime.setText(message.Date);
            holder.postContent.setText(message.Body);
            holder.threadCategory.setText(message.Read ? "Read" : "Unread");

            return convertView;
        }

        class ViewHolder {
            @ViewInjected(R.id.thread_category)
            TextView threadCategory;

            @ViewInjected(R.id.author_name)
            TextView authorName;

            @ViewInjected(R.id.post_content)
            TextView postContent;

            @ViewInjected(R.id.thread_replies)
            TextView threadReplies;

            @ViewInjected(R.id.post_time)
            TextView postTime;
        }
    }
}
