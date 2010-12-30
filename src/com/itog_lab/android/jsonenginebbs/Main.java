package com.itog_lab.android.jsonenginebbs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
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

	private static final String JSONENGINE_URL = "http://jsonengine.appspot.com/_je";
	private static final String DOC_TYPE = "msg";
	
	private static final String MSG_QUERY_URL = JSONENGINE_URL + "/" + DOC_TYPE + "?sort=_createdAt.desc&limit=100";
	private static final String MSG_POST_URL = JSONENGINE_URL + "/" + DOC_TYPE;
	private static final String MSG_DELETE_URL = JSONENGINE_URL + "/" + DOC_TYPE + "/"; //?_method=delete
	private static final String MSG_UPDATE_URL = JSONENGINE_URL + "/" + DOC_TYPE + "/";
	
	private Context context;

	private Button getButton;
	private Button postButton;
	private EditText postMsgText;
	private ListView listView;
	
//	private CustomArrayAdapter<BbsItem> adapter;
	private CustomArrayAdapter adapter;
	//TODO 楽な実装。itemに持たせる方が良い
//	private String docIds[];

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
//		private String[] msgs;
		private BbsItem[] items;
		
		protected Long doInBackground(String... urls) {
			try {
				InputStream is = new URL(urls[0]).openConnection().getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer buf = new ByteArrayBuffer(50);

				int current = 0;
				while((current = bis.read()) != -1){
					buf.append((byte)current);
				}

				/* Convert the Bytes read to a String. */
				String html = new String(buf.toByteArray());
				JSONArray jsons = new JSONArray(html);

//				msgs = new String[jsons.length()];
//				docIds = new String[jsons.length()];
				items = new BbsItem[jsons.length()];
				for (int i = 0; i < jsons.length(); i++) {
				    JSONObject jsonObj = jsons.getJSONObject(i);
				    
//				    msgs[i] = jsonObj.getString("msg");
//				    docIds[i] = jsonObj.getString("_docId");
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
		}
	}

	private class JsonEngineDeleteTask extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... ids) {
			// Create a new HttpClient and Post Header  
			HttpClient httpclient = new DefaultHttpClient();  
			HttpPost httppost = new HttpPost(MSG_DELETE_URL + ids[0] + "?_method=delete");

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("msg", ids[0]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

				HttpResponse response = httpclient.execute(httppost);
			} catch (ClientProtocolException e) {  
				Log.e(TAG, "delete error");
			} catch (IOException e) {  
				Log.e(TAG, "delete error");
			}
			return Long.valueOf(0);
		}

		protected void onProgressUpdate(Integer... progress) {
			//TODO show progress
		}

		protected void onPostExecute(Long result) {
			Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
			// update the view
			new JsonEngineGetTask().execute(MSG_QUERY_URL);
		}
	}

	private class JsonEngineUpdateTask extends AsyncTask<String, Integer, Long> {
		protected Long doInBackground(String... msgs) {
			// Create a new HttpClient and Post Header  
			HttpClient httpclient = new DefaultHttpClient();  
			HttpPost httppost = new HttpPost(MSG_UPDATE_URL + msgs[0]);
			
			try {  
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("msg", "更新してやったぜ"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

				HttpResponse response = httpclient.execute(httppost);
			} catch (ClientProtocolException e) {  
				Log.e(TAG, "update error");
			} catch (IOException e) {  
				Log.e(TAG, "update error");
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
	
	@Override
	public boolean onItemLongClick(AdapterView<?> list, final View item, final int pos, long id) {
		final String OPTION_DELETE = "delete";
		final String OPTION_UPDATE = "update";

		Log.v(TAG, "(pos, id) = (" + pos + ", " + id + ")");
		final String[] str_items = {OPTION_DELETE, OPTION_UPDATE};
		new AlertDialog.Builder(context)
		.setIcon(R.drawable.icon)
		.setTitle(context.getString(R.string.app_name))
		.setItems(str_items, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
//					Toast.makeText(context, "Delete : docId = " + docIds[pos], Toast.LENGTH_SHORT).show();
//					new JsonEngineDeleteTask().execute(docIds[pos]);
					Toast.makeText(context, "Delete : docId = " + item.getTag(), Toast.LENGTH_SHORT).show();
					new JsonEngineDeleteTask().execute((String)item.getTag());
					break;
				case 1:
//					Toast.makeText(context, "Update : docId = " + docIds[pos], Toast.LENGTH_SHORT).show();
//					new JsonEngineUpdateTask().execute(docIds[pos]);
				default:
					break;
				}
			}
		}).show();
		return false;
	}
}
