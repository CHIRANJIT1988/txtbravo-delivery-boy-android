package educing.tech.salesperson;

import static educing.tech.salesperson.CommonUtilities.SENDER_ID;
import static educing.tech.salesperson.CommonUtilities.displayMessage;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

import org.json.JSONObject;

import educing.tech.salesperson.activities.DashboardActivity;
import educing.tech.salesperson.activities.LoginFragment;
import educing.tech.salesperson.model.ChatMessage;
import educing.tech.salesperson.session.SessionManager;
import educing.tech.salesperson.sqlite.SQLiteDatabaseHelper;


public class GCMIntentService extends GCMBaseIntentService
{

	private static final String TAG = "GCMIntentService";

	
    public GCMIntentService() 
    {
        super(SENDER_ID);
    }

    
        
    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) 
    {
    
    	Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, "Your device registered with GCM");

        ServerUtilities.register(context, LoginFragment.user, registrationId);
    }

    
    
    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) 
    {
    
    	Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
        ServerUtilities.unregister(context, registrationId);
    }

    
    
    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) 
    {
    
    	Log.i(TAG, "Received message");
        String message = intent.getExtras().getString("price");
        
        displayMessage(context, message);

        // notifies user
        generateNotification(context, message);
    }

   
    
    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total)
    {
    
    	Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        
        displayMessage(context, message);

        // notifies user
        generateNotification(context, message);
    }

    
    
    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) 
    {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    
    
    @Override
    protected boolean onRecoverableError(Context context, String errorId) 
    {
    
    	// log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
        
        return super.onRecoverableError(context, errorId);
    }

    
    
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message)
    {

        if(message == null)
        {
            return;
        }


        try
        {

            SQLiteDatabaseHelper helper = new SQLiteDatabaseHelper(context);

            Log.v("message: ", String.valueOf(message));

            JSONObject jsonObj = new JSONObject(message);

            String message_type = jsonObj.getString("message_type");

            if(message_type.equals("chat_message"))
            {

                String message_id = jsonObj.getString("message_id");
                String sender_id = jsonObj.getString("sender_id");
                String sender_name = jsonObj.getString("sender_name");
                String chat_message = jsonObj.getString("message");
                String chat_image = jsonObj.getString("image");
                String timestamp = jsonObj.getString("timestamp");


                if(jsonObj.getString("sender_type").equals("store"))
                {

                    if(!helper.insertChatStore(new ChatMessage(sender_id, sender_name, timestamp)))
                    {
                        helper.updateChatStore(new ChatMessage(sender_id, sender_name, timestamp));
                    }

                    boolean inserted = helper.insertStoreChatMessage(new ChatMessage(message_id, sender_id, chat_message, chat_image, timestamp, 0, 0), 1);

                    if(inserted)
                    {

                        SessionManager session = new SessionManager(context); // Session Manager

                        if(session.checkLogin())
                        {
                            notify_user(context, "New Message !!", "New Message from Store !!");
                        }
                    }
                }

                else if(jsonObj.getString("sender_type").equals("user"))
                {

                    if(!helper.insertChatUser(new ChatMessage(sender_id, sender_name, timestamp)))
                    {
                        helper.updateChatUser(new ChatMessage(sender_id, sender_name, timestamp));
                    }

                    boolean inserted = helper.insertUserChatMessage(new ChatMessage(message_id, sender_id, chat_message, chat_image, timestamp, 0, 0), 1);

                    if(inserted)
                    {

                        SessionManager session = new SessionManager(context); // Session Manager

                        if(session.checkLogin())
                        {
                            notify_user(context, "New Message !!", "New Message from Customer !!");
                        }
                    }
                }
            }
        }

        catch (Exception e)
        {

        }
    }


    private static void notify_user(Context context, String title, String message)
    {

        int icon = R.drawable.logo;
        long when = System.currentTimeMillis();


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, DashboardActivity.class);

        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        long[] pattern = { 500, 500, 500 };


        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(intent)
                .setContentTitle(title)
                .setWhen(when)
                .setContentText(message);


        // Play default notification sound
        notification.setDefaults(Notification.DEFAULT_SOUND);
        notification.setVibrate(pattern);
        notification.setLights(Color.BLUE, 500, 500);
        notification.setStyle(new NotificationCompat.InboxStyle());
        notificationManager.notify(0, notification.build());
    }
}