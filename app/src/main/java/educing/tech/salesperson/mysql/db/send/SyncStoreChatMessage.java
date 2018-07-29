package educing.tech.salesperson.mysql.db.send;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import educing.tech.salesperson.app.MyApplication;
import educing.tech.salesperson.configuration.Configuration;
import educing.tech.salesperson.helper.OnTaskCompleted;
import educing.tech.salesperson.helper.Security;
import educing.tech.salesperson.session.SessionManager;
import educing.tech.salesperson.sqlite.SQLiteDatabaseHelper;

import static educing.tech.salesperson.configuration.Configuration.API_URL;
import static educing.tech.salesperson.sqlite.SQLiteDatabaseHelper.TABLE_STORE_CHAT_MESSAGES;
import static educing.tech.salesperson.configuration.Configuration.SECRET_KEY;


public class SyncStoreChatMessage
{

	private String URL = "";

	private Context context;
	private OnTaskCompleted listener;
	private SharedPreferences preferences;
	private SessionManager session;
	private SQLiteDatabaseHelper helper;

	private static final int MAX_ATTEMPTS = 5;
	private int ATTEMPTS_COUNT;


	public SyncStoreChatMessage(Context context, OnTaskCompleted listener)
	{

		this.context = context;
		this.listener = listener;

		this.preferences = context.getSharedPreferences(Configuration.SHARED_PREF, Context.MODE_PRIVATE);
		this.session = new SessionManager(context);
		this.helper = new SQLiteDatabaseHelper(context);

		this.URL = API_URL + "sync-store-support-chat-message.php";
	}
	

	public void execute()
	{

		StringRequest postRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {

			@Override
			public void onResponse(String response)
			{

				try
				{

					JSONArray jsonArray = new JSONArray(response);

					for(int i=0; i<jsonArray.length(); i++)
					{

						JSONObject jsonObj = (JSONObject) jsonArray.get(i);

						int id = jsonObj.getInt("id");
						int sync_status = jsonObj.getInt("sync_status");
						String message_number = jsonObj.getString("message_id");

						listener.onTaskCompleted(false, sync_status, message_number);
						new SQLiteDatabaseHelper(context).updateSyncStatus(TABLE_STORE_CHAT_MESSAGES, id, sync_status);
					}
				}

				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error)
			{

				if(ATTEMPTS_COUNT != MAX_ATTEMPTS)
				{

					execute();

					ATTEMPTS_COUNT ++;

					Log.v("#Attempt No: ", "" + ATTEMPTS_COUNT);
				}
			}
		})

		{

			@Override
			protected Map<String, String> getParams()
			{

				Map<String ,String> params=new HashMap<>();

				try
				{

					String data = String.valueOf(helper.chatStoreMessageJSONData());
					params.put("responseJSON", Security.encrypt(data, preferences.getString("key", null)));
					params.put("user", Security.encrypt(String.valueOf(session.getUserId()), SECRET_KEY));
				}

				catch (Exception e)
				{
					Log.v("error ", "" + e.getMessage());
				}

				finally
				{
					Log.v("params ", "" + params);
				}

				return params;
			}
		};

		// Adding request to request queue
		MyApplication.getInstance().addToRequestQueue(postRequest);
	}
}