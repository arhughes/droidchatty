package cc.hughes.droidchatty2.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

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

        setHasOptionsMenu(true);

        List<Message> messages = new ArrayList<Message>();
        int page = 0;
        mAdapter = new MessageListAdapter(messages, page, getActivity(), R.layout.message_list_item, R.layout.row_loading, R.layout.row_finished);
        setListAdapter(mAdapter);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.message_list_options, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem inbox = menu.findItem(R.id.message_list_inbox);
        MenuItem sent = menu.findItem(R.id.message_list_sent);

        inbox.setVisible(mAdapter.getFolder() != Message.FOLDER_INBOX);
        sent.setVisible(mAdapter.getFolder() != Message.FOLDER_SENT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.message_list_refresh:
                mAdapter.clear();
                return true;
            case R.id.message_list_inbox:
                mAdapter.setFolder(Message.FOLDER_INBOX);
                return true;
            case R.id.message_list_sent:
                mAdapter.setFolder(Message.FOLDER_SENT);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        if (!message.Read) {
            message.Read = true;
            mAdapter.notifyDataSetChanged();

            ReadMessageTask markRead = new ReadMessageTask();
            markRead.execute(message.Id);
        }

	}

    class ReadMessageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... values) {
            String id = values[0];

            try {
                mAdapter.mService.markMessageAsRead(id);
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }
            return null;
        }
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

        public String getFolder() {
            return mFolder;
        }

        public void setFolder(String value) {
            mFolder = value;
            clear();
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
            holder.messageAuthor.setText(message.OtherUser);
            holder.messageTime.setText(TimeUtil.format(getContext(), message.Date));
            holder.messageContent.setText(message.Body);
            holder.messageSubject.setText(message.Subject);

            if (message.Read) {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                holder.messageSubject.setTypeface(null, Typeface.NORMAL);
                holder.messageContent.setTypeface(null, Typeface.NORMAL);
            } else {
                convertView.setBackgroundResource(R.color.message_unread);
                holder.messageSubject.setTypeface(null, Typeface.BOLD);
                holder.messageContent.setTypeface(null, Typeface.BOLD);
            }

            return convertView;
        }

        class ViewHolder {
            @ViewInjected(R.id.author_name)
            TextView messageAuthor;

            @ViewInjected(R.id.message_subject)
            TextView messageSubject;

            @ViewInjected(R.id.message_content)
            TextView messageContent;

            @ViewInjected(R.id.message_time)
            TextView messageTime;
        }
    }
}
