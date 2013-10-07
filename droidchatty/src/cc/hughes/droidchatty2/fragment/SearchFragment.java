package cc.hughes.droidchatty2.fragment;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import cc.hughes.droidchatty2.FragmentContextActivity;
import cc.hughes.droidchatty2.R;
import cc.hughes.droidchatty2.ViewInjected;
import cc.hughes.droidchatty2.ViewInjector;

public class SearchFragment extends Fragment {

    @ViewInjected(R.id.search_term)
    private EditText mSearchTerm;

    @ViewInjected(R.id.search_author)
    private EditText mSearchAuthor;

    @ViewInjected(R.id.search_parent_author)
    private EditText mSearchParentAuthor;

    @ViewInjected(R.id.search_term_button)
    private Button mSearchTermButton;

    @ViewInjected(R.id.search_author_button)
    private Button mSearchAuthorButton;

    @ViewInjected(R.id.search_parent_author_button)
    private Button mSearchParentAuthorButton;

    @ViewInjected(R.id.search)
    private Button mSearchButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search, container, false);
        ViewInjector.inject(this, view);

        mSearchTermButton.setOnClickListener(mVanitySearch);
        mSearchAuthorButton.setOnClickListener(mOwnSearch);
        mSearchParentAuthorButton.setOnClickListener(mReplySearch);
        mSearchButton.setOnClickListener(mSearch);

        return view;
    }

    View.OnClickListener mSearch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bundle args = new Bundle();
            args.putString(SearchResultFragment.ARG_TERM, mSearchTerm.getText().toString());
            args.putString(SearchResultFragment.ARG_AUTHOR, mSearchAuthor.getText().toString());
            args.putString(SearchResultFragment.ARG_PARENT_AUTHOR, mSearchParentAuthor.getText().toString());

            Fragment f = new SearchResultFragment();
            f.setArguments(args);

            FragmentContextActivity fca = (FragmentContextActivity)getActivity();
            fca.changeContext(f, 1);
        }
    };

    View.OnClickListener mVanitySearch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSearchTerm.setText(getUserName());
        }
    };

    View.OnClickListener mOwnSearch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSearchAuthor.setText(getUserName());
        }
    };


    View.OnClickListener mReplySearch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mSearchParentAuthor.setText(getUserName());
        }
    };

    private String getUserName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString("pref_shacknews_user", "");
    }

}
