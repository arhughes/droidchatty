package cc.hughes.droidchatty2;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserPreference extends DialogPreference {

	private EditText mUsernameField;
	private EditText mPasswordField;
	
	public UserPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View onCreateDialogView() {
		View dialogView = super.onCreateDialogView();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		mUsernameField = (EditText)dialogView.findViewById(R.id.user_settings_dialog_username);
		mPasswordField = (EditText)dialogView.findViewById(R.id.user_settings_dialog_password);
	
		mUsernameField.setText(prefs.getString(getKey(), ""));
		mPasswordField.setText(prefs.getString(getKey().replace("user", "password"), ""));
				
		return dialogView;
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);
		
		final AlertDialog dialog = (AlertDialog)getDialog();
		final Button pos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		final Resources resources = getContext().getResources();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		pos.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (pos.getText() == resources.getString(R.string.validate)) {
					dialog.setCanceledOnTouchOutside(false);
					pos.setText(R.string.checking);
										
					ValidateCredentialsTask validate = new ValidateCredentialsTask(
							mUsernameField.getText().toString(),
							mPasswordField.getText().toString(),
							pos);
					validate.execute();
				}
				else if (pos.getText() == resources.getString(R.string.save)) {
				
					Editor editor = prefs.edit();
					editor.putString(getKey(), mUsernameField.getText().toString());
					editor.putString(getKey().replace("user", "password"), mPasswordField.getText().toString());
					editor.commit();
					
					dialog.dismiss();
				}
				
			}
		});
	}
	
	class ValidateCredentialsTask extends AsyncTask<Void, Void, Boolean> {

		String mUsername;
		String mPassword;
		Button mButton;
		
		public ValidateCredentialsTask(String username, String password, Button button) {
			mUsername = username;
			mPassword = password;
			mButton = button;
		}
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			URL url;
			try {
				url = new URL("http://www.shacknews.com/api/users/" + mUsername + ".json");
			
				Log.i("DroidChatty", "URL: " + url.toString());
				String encode = Base64.encodeToString((mUsername + ":" + mPassword).getBytes(), Base64.NO_WRAP);
			
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestProperty("Authorization", "Basic " + encode);
				connection.connect();
				
				int code = connection.getResponseCode();
				Log.i("DroidChatty", "Result: " + code);
				connection.disconnect();
				
				if (code == HttpURLConnection.HTTP_OK)
					return true;
				
			} catch (MalformedURLException e) {
				Log.e("DroidChatty", "Bad URL", e);
			} catch (IOException e) {
				Log.e("DroidChatty", "Bad Connection", e);
			}
			// 
			return false;
		}
		
		protected void onPostExecute(Boolean valid) {
			if (valid) {
				mButton.setText(R.string.save);
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
				builder.setMessage(R.string.error_validating)
					   .setTitle(R.string.error_title)
					   .setPositiveButton(R.string.ok, null);
								
				AlertDialog alert = builder.create();
				alert.show();
				
				mButton.setText(R.string.validate);
			}
		}
		
	}
	
}
