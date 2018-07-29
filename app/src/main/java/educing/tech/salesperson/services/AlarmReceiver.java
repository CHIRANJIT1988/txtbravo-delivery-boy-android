package educing.tech.salesperson.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import educing.tech.salesperson.helper.OnTaskCompleted;
import educing.tech.salesperson.mysql.db.send.SyncStoreChatMessage;
import educing.tech.salesperson.mysql.db.send.SyncUserChatMessage;
import educing.tech.salesperson.network.InternetConnectionDetector;
import educing.tech.salesperson.session.SessionManager;
import educing.tech.salesperson.sqlite.SQLiteDatabaseHelper;

import static educing.tech.salesperson.sqlite.SQLiteDatabaseHelper.TABLE_USER_CHAT_MESSAGES;
import static educing.tech.salesperson.sqlite.SQLiteDatabaseHelper.TABLE_STORE_CHAT_MESSAGES;
import static educing.tech.salesperson.sqlite.SQLiteDatabaseHelper.TABLE_CHAT_IMAGES;


public class AlarmReceiver extends BroadcastReceiver implements OnTaskCompleted
{
	
	Context context;


	@Override
	public void onReceive(Context context, Intent intent) 
	{
		
		this.context = context;
		SessionManager session = new SessionManager(context); // Session class instance

		if(!session.isLoggedIn())
		{
			return;
		}


		int alarm = intent.getExtras().getInt("alarm");

		if(alarm == 1)
		{

			if(new InternetConnectionDetector(context).isConnected())
			{

				//makeToast("Sync Alarm Received");
				syncData();
			}
		}
	}


	private void makeToast(String msg)
	{
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}


	private void syncData()
	{

		SQLiteDatabaseHelper helper = new SQLiteDatabaseHelper(context);

		if(helper.dbSyncCount(TABLE_STORE_CHAT_MESSAGES) != 0)
		{
			new SyncStoreChatMessage(context, this).execute();
		}

		if(helper.dbSyncCount(TABLE_USER_CHAT_MESSAGES) != 0)
		{
			new SyncStoreChatMessage(context, this).execute();
		}

		if(helper.dbSyncCount(TABLE_CHAT_IMAGES) != 0)
		{
			new SyncUserChatMessage(context, this).getAllChatImage();
		}
	}


	@Override
	public void onTaskCompleted(boolean flag, int code, String message)
	{

	}
}