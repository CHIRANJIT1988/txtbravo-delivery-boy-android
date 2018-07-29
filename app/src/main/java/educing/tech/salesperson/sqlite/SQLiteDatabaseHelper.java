package educing.tech.salesperson.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import educing.tech.salesperson.model.ChatMessage;
import educing.tech.salesperson.model.User;
import educing.tech.salesperson.session.SessionManager;


public class SQLiteDatabaseHelper extends SQLiteOpenHelper
{

    private SessionManager sessionManager;

    // Database version
    private static final int DATABASE_VERSION = 2;

    // Database name
    private static final String DATABASE_NAME = "EducingTechDB";


    public static final String TABLE_CHAT_USERS = "chat_users";
    public static final String TABLE_CHAT_STORES = "chat_stores";
    public static final String TABLE_USER_CHAT_MESSAGES = "user_chat_messages";
    public static final String TABLE_STORE_CHAT_MESSAGES = "store_chat_messages";
    public static final String TABLE_CHAT_IMAGES = "chat_images";


    private static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_STORE_ID = "store_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_STORE_NAME = "store_name";
    private static final String KEY_ID = "id";
    private static final String KEY_SENDER_ID = "sender_id";
    private static final String KEY_SENDER_NAME = "sender_name";
    private static final String KEY_RECIPIENT_ID = "recipient_id";
    public static final String KEY_MESSAGE_ID = "message_id";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_SYNC_STATUS = "sync_status";
    private static final String KEY_READ_STATUS = "read_status";
    private static final String KEY_MESSAGE_TYPE = "message_type";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_PATH = "path";


    private static final String CREATE_TABLE_CHAT_USERS = "CREATE TABLE "
            + TABLE_CHAT_USERS + "(" + KEY_USER_ID + " INTEGER PRIMARY KEY," + KEY_USER_NAME + " TEXT," + KEY_TIMESTAMP + " TEXT)";


    private static final String CREATE_TABLE_CHAT_STORES = "CREATE TABLE "
            + TABLE_CHAT_STORES + "(" + KEY_STORE_ID + " INTEGER PRIMARY KEY," + KEY_STORE_NAME + " TEXT," + KEY_TIMESTAMP + " TEXT)";


    private static final String CREATE_TABLE_USER_CHAT_MESSAGES = "CREATE TABLE "
            + TABLE_USER_CHAT_MESSAGES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_MESSAGE_ID + " TEXT," + KEY_USER_ID + " INTEGER,"
            + KEY_MESSAGE + " TEXT, " + KEY_IMAGE + " TEXT, " + KEY_TIMESTAMP + " TEXT," + KEY_READ_STATUS + " INTEGER DEFAULT 0," + KEY_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + KEY_MESSAGE_TYPE + " INTEGER," + " FOREIGN KEY (" + KEY_USER_ID + ") REFERENCES " + TABLE_CHAT_USERS + "(" + KEY_USER_ID + ") ON DELETE CASCADE, UNIQUE "
            + "(" + KEY_MESSAGE_ID + "))";


    private static final String CREATE_TABLE_STORE_CHAT_MESSAGES = "CREATE TABLE "
            + TABLE_STORE_CHAT_MESSAGES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_MESSAGE_ID + " TEXT," + KEY_STORE_ID + " INTEGER,"
            + KEY_MESSAGE + " TEXT, " + KEY_IMAGE + " TEXT, " + KEY_TIMESTAMP + " TEXT," + KEY_READ_STATUS + " INTEGER DEFAULT 0," + KEY_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + KEY_MESSAGE_TYPE + " INTEGER," + " FOREIGN KEY (" + KEY_STORE_ID + ") REFERENCES " + TABLE_CHAT_STORES + "(" + KEY_STORE_ID + ") ON DELETE CASCADE, UNIQUE "
            + "(" + KEY_MESSAGE_ID + "))";


    private static final String CREATE_TABLE_CHAT_IMAGES = "CREATE TABLE "
            + TABLE_CHAT_IMAGES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_MESSAGE_ID + " TEXT," + KEY_PATH + " TEXT," + KEY_SYNC_STATUS + " INTEGER DEFAULT 0)";



    public SQLiteDatabaseHelper(Context context)
    {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sessionManager = new SessionManager(context);
    }


    @Override
    public void onCreate(SQLiteDatabase database)
    {

        database.execSQL(CREATE_TABLE_CHAT_USERS);
        database.execSQL(CREATE_TABLE_CHAT_STORES);
        database.execSQL(CREATE_TABLE_USER_CHAT_MESSAGES);
        database.execSQL(CREATE_TABLE_STORE_CHAT_MESSAGES);
        database.execSQL(CREATE_TABLE_CHAT_IMAGES);

        Log.v("CREATE TABLE: ", CREATE_TABLE_CHAT_USERS);
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version)
    {

        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_USERS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_STORES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CHAT_MESSAGES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_STORE_CHAT_MESSAGES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_IMAGES);

        onCreate(database);
    }


    public ArrayList<ChatMessage> getAllChatMessage(boolean is_store, String user_id, int x, int y)
    {

        ArrayList<ChatMessage> messagesList = new ArrayList<>();

        String selectQuery;

        if(is_store)
        {
            selectQuery = "SELECT * FROM " + TABLE_STORE_CHAT_MESSAGES + " WHERE " + KEY_STORE_ID + "='" + user_id
                    + "' ORDER BY " + KEY_ID + " DESC LIMIT " + x + "," + y;
        }

        else
        {
            selectQuery = "SELECT * FROM " + TABLE_USER_CHAT_MESSAGES + " WHERE " + KEY_USER_ID + "='" + user_id
                    + "' ORDER BY " + KEY_ID + " DESC LIMIT " + x + "," + y;
        }


        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                ChatMessage message = new ChatMessage();

                message.setMessageId(cursor.getString(1));
                message.setUserId(cursor.getString(2));
                message.setMessage(cursor.getString(3));
                message.setImage(cursor.getString(4));
                message.setTimestamp(cursor.getString(5));
                message.setReadStatus(cursor.getInt(6));
                message.setSyncStatus(cursor.getInt(7));
                message.setMessageType(cursor.getInt(8));

                messagesList.add(message);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return messagesList;
    }


    public ChatMessage getLastMessage(String user_id, boolean is_store)
    {

        String query;

        ChatMessage message = new ChatMessage();

        if(!is_store)
        {

            query = "SELECT " + KEY_MESSAGE + "," + KEY_TIMESTAMP + ", "
                    + "(SELECT COUNT("+ KEY_READ_STATUS + ") FROM " + TABLE_USER_CHAT_MESSAGES + " WHERE " + KEY_USER_ID + "='" + user_id + "' AND " + KEY_READ_STATUS + "='0')"
                    + " FROM " + TABLE_USER_CHAT_MESSAGES + " WHERE " + KEY_ID + "=(" + "SELECT MAX(" + KEY_ID + ") FROM " + TABLE_USER_CHAT_MESSAGES + " WHERE " + KEY_USER_ID + "='" + user_id + "')";
        }

        else
        {
            query = "SELECT " + KEY_MESSAGE + "," + KEY_TIMESTAMP + ", "
                    + "(SELECT COUNT("+ KEY_READ_STATUS + ") FROM " + TABLE_STORE_CHAT_MESSAGES + " WHERE " + KEY_STORE_ID + "='" + user_id + "' AND " + KEY_READ_STATUS + "='0')"
                    + " FROM " + TABLE_STORE_CHAT_MESSAGES + " WHERE " + KEY_ID + "=(" + "SELECT MAX(" + KEY_ID + ") FROM " + TABLE_STORE_CHAT_MESSAGES + " WHERE " + KEY_STORE_ID + "='" + user_id + "')";
        }

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst())
        {
            message.setMessage(cursor.getString(0));
            message.setTimestamp(cursor.getString(1));
            message.setUnreadMessageCount(cursor.getInt(2));
        }

        database.close();
        cursor.close();

        return message;
    }


    public ArrayList<ChatMessage> getAllChatUsers()
    {

        ArrayList<ChatMessage> messagesList = new ArrayList<>();

        /*String selectQuery = "SELECT " + TABLE_CHAT_USERS + "." + KEY_USER_ID + ", " + KEY_USER_NAME + ", " + KEY_MESSAGE + ", "
                + KEY_TIMESTAMP + ", (SELECT COUNT("+ KEY_READ_STATUS + ") FROM " + TABLE_USER_CHAT_MESSAGES + " AS unread_message  WHERE "
                + KEY_READ_STATUS + "='0')" + " FROM " + TABLE_USER_CHAT_MESSAGES + " JOIN " + TABLE_CHAT_USERS + " ON " + TABLE_CHAT_USERS + "."
                + KEY_USER_ID + "=" + TABLE_USER_CHAT_MESSAGES + "." + KEY_USER_ID + " WHERE " + KEY_ID + "=(" + "SELECT MAX(" + KEY_ID + ") FROM "
                + TABLE_USER_CHAT_MESSAGES + ")";*/

        String selectQuery = "SELECT DISTINCT " + KEY_USER_ID + ", " + KEY_USER_NAME + ", " + KEY_TIMESTAMP
                + " FROM " + TABLE_CHAT_USERS + " ORDER BY " + KEY_TIMESTAMP + " DESC";

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                ChatMessage message = new ChatMessage();

                message.setUserId(cursor.getString(0));
                message.setUserName(cursor.getString(1));
                message.setTimestamp(cursor.getString(2));

                ChatMessage temp_msg = getLastMessage(message.user_id, false);

                message.setMessage(temp_msg.message);
                message.setUnreadMessageCount(temp_msg.unread_message);

                messagesList.add(message);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return messagesList;
    }


    public ArrayList<ChatMessage> getAllChatStores()
    {

        ArrayList<ChatMessage> messagesList = new ArrayList<>();

        /*String selectQuery = "SELECT " + TABLE_CHAT_STORES + "." + KEY_STORE_ID + ", " + KEY_STORE_NAME + ", " + KEY_MESSAGE + ", "
                + KEY_TIMESTAMP + ", (SELECT COUNT("+ KEY_READ_STATUS + ") FROM " + TABLE_STORE_CHAT_MESSAGES + " AS unread_message  WHERE "
                + KEY_READ_STATUS + "='0')" + " FROM " + TABLE_STORE_CHAT_MESSAGES + " JOIN " + TABLE_CHAT_STORES + " ON " + TABLE_CHAT_STORES + "."
                + KEY_STORE_ID + "=" + TABLE_STORE_CHAT_MESSAGES + "." + KEY_STORE_ID + " WHERE " + KEY_ID + "=(" + "SELECT MAX(" + KEY_ID + ") FROM "
                + TABLE_STORE_CHAT_MESSAGES + ")";*/

        String selectQuery = "SELECT DISTINCT " + KEY_STORE_ID + ", " + KEY_STORE_NAME + ", " + KEY_TIMESTAMP
                + " FROM " + TABLE_CHAT_STORES + " ORDER BY " + KEY_TIMESTAMP + " DESC";

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                ChatMessage message = new ChatMessage();

                message.setUserId(cursor.getString(0));
                message.setUserName(cursor.getString(1));
                message.setTimestamp(cursor.getString(2));

                ChatMessage temp_msg = getLastMessage(message.user_id, true);

                message.setMessage(temp_msg.message);
                message.setUnreadMessageCount(temp_msg.unread_message);

                messagesList.add(message);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return messagesList;
    }



    public boolean insertChatUser(ChatMessage message)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_USER_ID, message.getUserId());
        values.put(KEY_USER_NAME, message.getUserName());
        values.put(KEY_TIMESTAMP, message.getTimestamp());

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_CHAT_USERS, null, values) > 0;

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public boolean insertChatStore(ChatMessage message)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_STORE_ID, message.getUserId());
        values.put(KEY_STORE_NAME, message.getUserName());
        values.put(KEY_TIMESTAMP, message.getTimestamp());

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_CHAT_STORES, null, values) > 0;

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public boolean insertUserChatMessage(ChatMessage message, int sync_status)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_MESSAGE_ID, message.getMessageId());
        values.put(KEY_USER_ID, message.getUserId());
        values.put(KEY_MESSAGE, message.getMessage());
        values.put(KEY_IMAGE, message.getImage());
        values.put(KEY_TIMESTAMP, message.getTimestamp());
        values.put(KEY_READ_STATUS, message.getReadStatus());
        values.put(KEY_MESSAGE_TYPE, message.getMessageType());
        values.put(KEY_SYNC_STATUS, sync_status);

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_USER_CHAT_MESSAGES, null, values) > 0;

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public boolean insertStoreChatMessage(ChatMessage message, int sync_status)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_MESSAGE_ID, message.getMessageId());
        values.put(KEY_STORE_ID, message.getUserId());
        values.put(KEY_MESSAGE, message.getMessage());
        values.put(KEY_IMAGE, message.getImage());
        values.put(KEY_TIMESTAMP, message.getTimestamp());
        values.put(KEY_READ_STATUS, message.getReadStatus());
        values.put(KEY_MESSAGE_TYPE, message.getMessageType());
        values.put(KEY_SYNC_STATUS, sync_status);

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_STORE_CHAT_MESSAGES, null, values) > 0;

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public boolean insertChatImages(String message_id, String path)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_MESSAGE_ID, message_id);
        values.put(KEY_PATH, path);

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_CHAT_IMAGES, null, values) > 0;

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public ArrayList<ChatMessage> getAllChatImages()
    {

        ArrayList<ChatMessage> numberList = new ArrayList<>();

        /*String selectQuery = "SELECT " + TABLE_CHAT_IMAGES + "." + KEY_MESSAGE_ID + ", " + KEY_PATH + "," + KEY_IMAGE
                + " FROM " + TABLE_USER_CHAT_MESSAGES + " LEFT JOIN " + TABLE_CHAT_IMAGES + " ON " + TABLE_USER_CHAT_MESSAGES + "." + KEY_MESSAGE_ID
                + "=" + TABLE_CHAT_IMAGES + "." + KEY_MESSAGE_ID + " WHERE " + TABLE_CHAT_IMAGES + "." + KEY_SYNC_STATUS + "='0'";*/

        String selectQuery = "SELECT " + KEY_MESSAGE_ID + ", " + KEY_PATH + " FROM " + TABLE_CHAT_IMAGES + " WHERE " + KEY_SYNC_STATUS + "='0'";

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                ChatMessage numberObj = new ChatMessage();

                numberObj.setMessageId(cursor.getString(0));
                numberObj.setFilePath(cursor.getString(1));

                numberList.add(numberObj);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return numberList;
    }


    public String chatUserMessageJSONData()
    {

        ArrayList<HashMap<String, String>> wordList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_USER_CHAT_MESSAGES + " WHERE " + KEY_SYNC_STATUS + " = '0'";

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                HashMap<String, String> map = new HashMap<>();

                map.put(KEY_ID, cursor.getString(0));
                map.put(KEY_MESSAGE_ID, cursor.getString(1));
                map.put(KEY_RECIPIENT_ID, cursor.getString(2));
                map.put(KEY_MESSAGE, cursor.getString(3));
                map.put(KEY_IMAGE, cursor.getString(4));
                map.put(KEY_TIMESTAMP, cursor.getString(5));
                map.put(KEY_SENDER_NAME, getUserDetails().getUserName());
                map.put(KEY_SENDER_ID, String.valueOf(getUserDetails().getUserID()));

                wordList.add(map);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        Gson gson = new GsonBuilder().create();

        // Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }


    // Compose JSON from Incident table
    public String chatStoreMessageJSONData()
    {

        ArrayList<HashMap<String, String>> wordList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_STORE_CHAT_MESSAGES + " WHERE " + KEY_SYNC_STATUS + " = '0'";

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                HashMap<String, String> map = new HashMap<>();

                map.put(KEY_ID, cursor.getString(0));
                map.put(KEY_MESSAGE_ID, cursor.getString(1));
                map.put(KEY_RECIPIENT_ID, cursor.getString(2));
                map.put(KEY_MESSAGE, cursor.getString(3));
                map.put(KEY_IMAGE, cursor.getString(4));
                map.put(KEY_TIMESTAMP, cursor.getString(5));
                map.put(KEY_SENDER_NAME, getUserDetails().getUserName());
                map.put(KEY_SENDER_ID, String.valueOf(getUserDetails().getUserID()));

                wordList.add(map);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        Gson gson = new GsonBuilder().create();

        // Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }


    public void updateSyncStatus(String TABLE_NAME, int id, int status)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_NAME + " SET " + KEY_SYNC_STATUS + " = '" + status + "' WHERE " + KEY_ID + " = '" + id + "'";
        Log.d("query", updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    public void updateSyncStatus(String TABLE_NAME, String COLUMN_NAME, String id, int status)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_NAME + " SET " + KEY_SYNC_STATUS + " = '" + status + "' WHERE " + COLUMN_NAME + " = '" + id + "'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    public int dbRowCount(String TABLE_NAME)
    {

        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    public void updateChatUser(ChatMessage user)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_CHAT_USERS + " SET " + KEY_USER_NAME + " = '" + user.user_name + "'," + KEY_TIMESTAMP + " = '" + user.timestamp + "' WHERE " + KEY_USER_ID + " = '" + user.user_id + "'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    public void updateChatStore(ChatMessage user)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_CHAT_STORES + " SET " + KEY_STORE_NAME + " = '" + user.user_name + "'," + KEY_TIMESTAMP + " = '" + user.timestamp + "' WHERE " + KEY_STORE_ID + " = '" + user.user_id + "'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    public int setAsRead(boolean is_store, String user_id)
    {

        String selectQuery;

        if(is_store)
        {
            selectQuery = "UPDATE " + TABLE_STORE_CHAT_MESSAGES + " SET " + KEY_READ_STATUS + "='1' WHERE " + KEY_STORE_ID + "='" + user_id + "'";
        }

        else
        {
            selectQuery = "UPDATE " + TABLE_USER_CHAT_MESSAGES + " SET " + KEY_READ_STATUS + "='1' WHERE " + KEY_USER_ID + "='" + user_id + "'";
        }

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    public void clearChatMessages(boolean is_store, String user_id)
    {

        String updateQuery;

        if(is_store)
        {
            updateQuery = "DELETE FROM " + TABLE_STORE_CHAT_MESSAGES + " WHERE " + KEY_STORE_ID + "='" + user_id + "'";
        }

        else
        {
            updateQuery = "DELETE FROM " + TABLE_USER_CHAT_MESSAGES + " WHERE " + KEY_USER_ID + "='" + user_id + "'";
        }

        SQLiteDatabase database = this.getWritableDatabase();
        Log.d("query",updateQuery);
        database.execSQL("PRAGMA foreign_keys=ON");
        database.execSQL(updateQuery);
        database.close();
    }


    public void clearChatUsers(String TABLE_NAME)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "DELETE FROM " + TABLE_NAME;
        Log.d("query",updateQuery);
        database.execSQL("PRAGMA foreign_keys=ON");
        database.execSQL(updateQuery);
        database.close();
    }


    public void clearChatUsers(String TABLE_NAME, String COLUMN_NAME, String user_id)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + "='" + user_id + "'";
        Log.d("query",updateQuery);
        database.execSQL("PRAGMA foreign_keys=ON");
        database.execSQL(updateQuery);
        database.close();
    }


    public int unreadMessageCount(String TABLE_NAME)
    {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_READ_STATUS + "='0'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    public int dbSyncCount(String TABLE_NAME)
    {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_SYNC_STATUS + " = '0'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    private User getUserDetails()
    {

        User userObj = new User();

        if (sessionManager.checkLogin())
        {
            HashMap<String, String> user = sessionManager.getUserDetails();

            userObj.setUserID(Integer.valueOf(user.get(SessionManager.KEY_USER_ID)));
            userObj.setPhoneNo(user.get(SessionManager.KEY_PHONE));
            userObj.setUserName(user.get(SessionManager.KEY_USER_NAME));
        }

        return userObj;
    }
}