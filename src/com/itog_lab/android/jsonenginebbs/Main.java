package com.itog_lab.android.jsonenginebbs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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


public class Main extends Activity implements OnClickListener, OnItemLongClickListener {
	protected static final String TAG = "JsonEngine";

	private static final String APP_ID = "jsonengine"; // App Engineでアプリケーション作成時に登録するID
	private static final String JSONENGINE_URL = "http://" + APP_ID + ".appspot.com/_je";
	private static final String DOC_TYPE = "msg"; // DBのテーブル名に相当する任意の文字列
	private static final String URL_POSTFIX_GET = "?sort=_createdAt.desc&limit=100";	
	private static final String URL_POSTFIX_DELETE = "?_method=delete";
	
	private Context context;

	private Button getButton;
	private Button postButton;
	private EditText postMsgText;
	private ListView listView;
	
	private CustomArrayAdapter adapter;

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
		listView.setOnItemLongClickListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		new JsonEngineGetTask().execute(JSONENGINE_URL + "/" + DOC_TYPE + URL_POSTFIX_GET);
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.get_button:
			new JsonEngineGetTask().execute(JSONENGINE_URL + "/" + DOC_TYPE + URL_POSTFIX_GET);
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
		private int statusCode;
		private ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("通信中...");
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
			progressDialog.show();
		}
		
		protected Long doInBackground(String... msgs) {
			// Create a new HttpClient and Post Header  
			HttpClient httpclient = new DefaultHttpClient();  
			HttpPost httppost = new HttpPost(JSONENGINE_URL + "/" + DOC_TYPE);

			try {  
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("msg", msgs[0]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

				HttpResponse response = httpclient.execute(httppost);
				statusCode = response.getStatusLine().getStatusCode();
				Log.v(TAG, "status code = " + statusCode);
			} catch (ClientProtocolException e) {  
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return Long.valueOf(0);
		}

		protected void onProgressUpdate(Integer... progress) {
			//TODO show progress
		}

		protected void onPostExecute(Long result) {
			if ((statusCode & 200) != 0) {
				Toast.makeText(context, "Posted. Updating view...", Toast.LENGTH_SHORT).show();
				new JsonEngineGetTask().execute(JSONENGINE_URL + "/" + DOC_TYPE + URL_POSTFIX_GET);
			} else {
				Toast.makeText(context, "Post Error (" + statusCode + ")", Toast.LENGTH_SHORT).show();
			}
			progressDialog.dismiss();
		}
	}

	private class JsonEngineGetTask extends AsyncTask<String, Integer, Long> {
		private BbsItem[] items;
		private ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("通信中...");
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
			progressDialog.show();
		}

		protected Long doInBackground(String... urls) {
			try {
				InputStream is = new URL(urls[0]).openConnection().getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer buf = new ByteArrayBuffer(0);

				int current = 0;
				while((current = bis.read()) != -1){
					buf.append((byte)current);
				}
				Log.v(TAG, "capacity = " + buf.capacity());

				/* Convert the Bytes read to a String. */
				String html = new String(buf.toByteArray());
				JSONArray jsons = new JSONArray(html);

				items = new BbsItem[jsons.length()];
				for (int i = 0; i < jsons.length(); i++) {
				    JSONObject jsonObj = jsons.getJSONObject(i);				    
				    items[i] = new BbsItem();
				    items[i].setMessage(jsonObj.getString("msg"));
				    items[i].setDocId(jsonObj.getString("_docId"));
				}
			} catch (Exception e) {
				Log.e(TAG, e.getStackTrace().toString());
			}
			return Long.valueOf(0);
		}

		protected void onProgressUpdate(Integer... progress) {
			//TODO show progress
		}

		protected void onPostExecute(Long result) {
			if (items != null) {
//				adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, msgs);
				adapter = new CustomArrayAdapter(context, items);
				listView.setAdapter(adapter);
				Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
			}
			progressDialog.dismiss();
		}
	}

	private class JsonEngineDeleteTask extends AsyncTask<String, Integer, Long> {
		private int statusCode;
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage("通信中...");
			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(false);
				}
			});
			progressDialog.show();
		}
		
		protected Long doInBackground(String... ids) {
			// Create a new HttpClient and Post Header  
			HttpClient httpclient = new DefaultHttpClient();  
			HttpPost httppost = new HttpPost(JSONENGINE_URL + "/" + DOC_TYPE + "/" + ids[0] + URL_POSTFIX_DELETE);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("msg", ids[0]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

				HttpResponse response = httpclient.execute(httppost);
				statusCode = response.getStatusLine().getStatusCode();
			} catch (ClientProtocolException e) {  
				e.printStackTrace();
			} catch (IOException e) {  
				e.printStackTrace();
			}
			return Long.valueOf(0);
		}

		protected void onProgressUpdate(Integer... progress) {
			//TODO show progress
		}

		protected void onPostExecute(Long result) {
			if ((statusCode & 200) != 0) {
				Toast.makeText(context, "Deleted. Updating View...", Toast.LENGTH_SHORT).show();
				new JsonEngineGetTask().execute(JSONENGINE_URL + "/" + DOC_TYPE + URL_POSTFIX_GET);
			} else {
				Toast.makeText(context, "Delete Error (" + statusCode + ")", Toast.LENGTH_SHORT).show();
			}
			progressDialog.dismiss();
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> list, final View item, final int pos, long id) {
		final String OPTION_DELETE = "delete";

		Log.v(TAG, "(pos, id) = (" + pos + ", " + id + ")");
		final String[] str_items = {OPTION_DELETE};
		new AlertDialog.Builder(context)
		.setIcon(R.drawable.icon)
		.setTitle(context.getString(R.string.app_name))
		.setItems(str_items, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					Toast.makeText(context, "Delete : docId = " + item.getTag(), Toast.LENGTH_SHORT).show();
					new JsonEngineDeleteTask().execute((String)item.getTag());
					break;
				default:
					break;
				}
			}
		}).show();
		return false;
	}
}
