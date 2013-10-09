package cc.hughes.droidchatty2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;
import cc.hughes.droidchatty2.dummy.DummyContent;
import cc.hughes.droidchatty2.net.Message;

/**
 * A fragment representing a single Message detail screen. This fragment is
 * either contained in a {@link MessageListActivity} in two-pane mode (on
 * tablets) or a {@link MessageDetailActivity} on handsets.
 */
public class MessageDetailFragment extends Fragment {
	public static final String ARG_MESSAGE = "message";

	private Message mMessage;

    @ViewInjected(R.id.message_detail)
    TextView mMessageView;

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.message_detail, container, false);
        ViewInjector.inject(this, view);

        mMessageView.setText(mMessage.Body);

		return view;
	}
}
