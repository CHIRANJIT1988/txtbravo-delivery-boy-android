package educing.tech.salesperson.activities;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;
import educing.tech.salesperson.R;
import educing.tech.salesperson.ServerUtilities;
import educing.tech.salesperson.model.User;
import educing.tech.salesperson.network.InternetConnectionDetector;

import static educing.tech.salesperson.CommonUtilities.SENDER_ID;


public class LoginFragment extends Fragment implements OnClickListener
{

    private Button btnLogin;
    private EditText editPhone, editPassword;
    private TextView tvStatus;
    private ProgressBar pBar;

    private LinearLayout linear_main;

    private Context context = null;
    public static User user;


    // AsyncTask
    private AsyncTask<Void, Void, Void> mRegisterTask;



    public LoginFragment()
    {

    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
    }


	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
 
        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        findViewById(rootView);
        context = this.getActivity();

        btnLogin.setOnClickListener(this);

        hideKeyboard(rootView);

        return rootView;
    }


    /** Called when the activity is about to become visible. */
    @Override
    public void onStart()
    {

        super.onStart();
        Log.d("Inside : ", "onStart() event");
    }


    /** Called when another activity is taking focus. */
    @Override
    public void onPause()
    {
        super.onPause();
        Log.d("Inside : ", "onPause() event");
    }


    /** Called when the activity is no longer visible. */
    @Override
    public void onStop()
    {
        super.onStop();
        Log.d("Inside : ", "onStop() event");
    }


    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy()
    {

        if (mRegisterTask != null)
        {
            mRegisterTask.cancel(true);
        }


        try
        {
            //context.unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(context);
        }

        catch (Exception e)
        {
            Log.e("UnRegister Error", "> " + e.getMessage());
        }

        super.onDestroy();
        Log.d("Inside : ", "onDestroy() event");
    }


    private void findViewById(View rootView)
    {

        btnLogin = (Button) rootView.findViewById(R.id.btnLogin);
        editPhone = (EditText) rootView.findViewById(R.id.editPhoneNumber);
        editPassword = (EditText) rootView.findViewById(R.id.editPassword);
        pBar = (ProgressBar) rootView.findViewById(R.id.pbLoading);
        tvStatus = (TextView) rootView.findViewById(R.id.status);
        linear_main = (LinearLayout) rootView.findViewById(R.id.linear_main);
    }


    public void onClick(View v)
    {

        switch (v.getId())
        {

            case R.id.btnLogin:

                if(validateForm())
                {

                    if (!new InternetConnectionDetector(getActivity()).isConnected())
                    {
                        makeSnackbar("Internet Connection Fail");
                        return;
                    }

                    pBar.setVisibility(View.VISIBLE);
                    tvStatus.setText(String.valueOf("Logging ... "));

                    user = initUserObject();
                    gcm_registration();
                }

                break;
        }
    }


    private boolean validateForm()
    {

        if(editPhone.getText().toString().trim().length() != 10)
        {

            makeSnackbar("Invalid Phone Number");
            return false;
        }

        if(editPassword.getText().toString().trim().length() == 0)
        {

            makeSnackbar("Enter Password");
            return false;
        }

        return  true;
    }


    private void makeSnackbar(String msg)
    {

        Snackbar snackbar = Snackbar.make(linear_main, msg, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.myPrimaryColor));
        snackbar.show();
    }


    private User initUserObject()
    {

        WifiManager m_wm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        User user = new User();

        user.setPhoneNo(editPhone.getText().toString());
        user.setPassword(editPassword.getText().toString());
        user.setDeviceId(String.valueOf(m_wm.getConnectionInfo().getMacAddress()));

        return user;
    }


    private void hideKeyboard(final View rootView)
    {

        editPhone.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editPhone.getText().toString().trim().length() == 10) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                }
            }
        });
    }



    @Override
    public void onResume()
    {

        super.onResume();
    }


    private void gcm_registration()
    {

        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(context);

        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(context);

        //context.registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));

        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(context);

        // Check if regid already presents
        if (regId.equals(""))
        {
            // Registration is not present, register now with GCM
            GCMRegistrar.register(context, SENDER_ID);
        }

        else
        {

            // Try to register again, but not in the UI thread.
            // It's also necessary to cancel the thread onDestroy(),
            // hence the use of AsyncTask instead of a raw thread.
            // final Context context = this;

            mRegisterTask = new AsyncTask<Void, Void, Void>()
            {

                @Override
                protected Void doInBackground(Void... params)
                {
                    // Register on our server
                    // On server creates a new user
                    ServerUtilities.register(context, user, regId);
                    return null;
                }


                @Override
                protected void onPostExecute(Void result)
                {
                    mRegisterTask = null;
                }

            };

            mRegisterTask.execute(null, null, null);
        }
    }
}