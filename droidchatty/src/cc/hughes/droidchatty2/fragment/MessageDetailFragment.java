package cc.hughes.droidchatty2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;
import cc.hughes.droidchatty2.net.Message;

/**
 * A fragment representing a single Message detail screen. This fragment is
 * either contained in a {@link MessageListActivity} in two-pane mode (on
 * tablets) or a {@link MessageDetailActivity} on handsets.
 */
public class MessageDetailFragment extends Fragment {
	public static final String ARG_MESSAGE = "message";

	private Message mMessage;

    @ViewInjected(R.id.author_name)
    TextView mMessageAuthor;

    @ViewInjected(R.id.message_subject)
    TextView mMessageSubject;

    @ViewInjected(R.id.message_content)
    TextView mMessageContent;

    @ViewInjected(R.id.message_time)
    TextView mMessageTime;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MessageDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_MESSAGE)) {
            mMessage = (Message)getArguments().getSerializable(ARG_MESSAGE);
        }

        setHasOptionsMenu(true);

	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.message_detail_options, menu);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.message_detail_item, container, false);
        ViewInjector.inject(this, view);

        mMessageAuthor.setText(mMessage.OtherUser);
        mMessageSubject.setText(mMessage.Subject);
        mMessageTime.setText(mMessage.Date);
        mMessageContent.setText(mMessage.Body);

		return view;
	}
}
