package com.itog_lab.android.jsonenginebbs;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;


public class Main extends Activity implements OnClickListener {
	protected static final String TAG = "JsonEngine";

	private static final String JSONENGINE_URL = "http://jsonengine.appspot.com/_je";
	private static final String DOC_TYPE = "msg";
	
	private static final String MSG_QUERY_URL = JSONENGINE_URL + "/" + DOC_TYPE + "?sort=_createdAt.desc&limit=100";
	private static final String MSG_POST_URL = JSONENGINE_URL + "/" + DOC_TYPE;
	
	private Context context;

	private Button getButton;
	private Button postButton;
	private EditText postMsgText;
	private ListView listView;
	
	private ArrayAdapter<String> adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		context = this;

		getButton = (Button)findViewById(R.id.get_button);
		getButton.setOnClickListener(this);
		postButton = (Button)findViewById(R.id.post_button);
		postButton.setOnClickListener(this);
		postMsgText = (EditText)findViewById(R.id.post_msg_text);
		listView = (ListView) findViewById(R.id.msg_list);
	}
	
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		new JsonEngineGetTask().execute(MSG_QUERY_URL);
		super.onResume();
	}



	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.get_button:
			new JsonEngineGetTask().execute(MSG_QUERY_URL);
			break;
		case R.id.post_button:
			String str = null;
			str = postMsgText.getText().toString();
			if (!str.equals(new String(""))) {
				new JsonEnginePostTask().execute(str);
				postMsgText.setText("");
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(postMsgText.getWindowToken(), 0);
			} else {
				Toast.makeText(context, "Enter your message", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	private class JsonEnginePostTask extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... msgs) {
			// Create a new HttpClient and Post Header  
			HttpClient httpclient = new DefaultHttpClient();  
			HttpPost httppost = new HttpPost(MSG_POST_URL);

			try {  
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("msg", msgs[0]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

				HttpResponse response = httpclient.execute(httppost);
			} catch (ClientProtocolException e) {  
				Log.e(TAG, "post error");
			} catch (IOException e) {  
				Log.e(TAG, "post error");
			}
			return Long.valueOf(0);
		}

		protected void onProgressUpdate(Integer... progress) {
			//TODO show progress
		}

		protected void onPostExecute(Long result) {
			Toast.makeText(context, "Posted", Toast.LENGTH_SHORT).show();
			// update the view
			new JsonEngineGetTask().execute(MSG_QUERY_URL);
		}
	}


	private class JsonEngineGetTask extends AsyncTask<String, Integer, Long> {
		private String[] msgs;
		
		protected Long doInBackground(String... urls) {
			try {
				URL updateURL = new URL(urls[0]);
				URLConnection conn = updateURL.openConnection();
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer baf = new ByteArrayBuffer(50);

				int current = 0;
				while((current = bis.read()) != -1){
					baf.append((byte)current);
				}

				/* Convert the Bytes read to a String. */
				String html = new String(baf.toByteArray());
				JSONArray jsons = new JSONArray(html);

				msgs = new String[jsons.length()];
				for (int i = 0; i < jsons.length(); i++) {
				    JSONObject jsonObj = jsons.getJSONObject(i);
				    msgs[i] = jsonObj.getString("msg");
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			return Long.valueOf(0);
		}

		protected void onProgressUpdate(Integer... progress) {
			//TODO show progress
		}

		protected void onPostExecute(Long result) {
			if (msgs != null) {
				adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, msgs);
				listView.setAdapter(adapter);
				Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
