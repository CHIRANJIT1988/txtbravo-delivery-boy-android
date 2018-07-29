package educing.tech.salesperson.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import educing.tech.salesperson.R;
import educing.tech.salesperson.model.User;
import educing.tech.salesperson.services.AlarmService;
import educing.tech.salesperson.session.SessionManager;
import educing.tech.salesperson.sqlite.SQLiteDatabaseHelper;


import static educing.tech.salesperson.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static educing.tech.salesperson.CommonUtilities.EXTRA_MESSAGE;

import static educing.tech.salesperson.configuration.Configuration.PACKAGE_NAME_SELLER;
import static educing.tech.salesperson.configuration.Configuration.PACKAGE_NAME_BUYER;

import static educing.tech.salesperson.sqlite.SQLiteDatabaseHelper.TABLE_USER_CHAT_MESSAGES;
import static educing.tech.salesperson.sqlite.SQLiteDatabaseHelper.TABLE_STORE_CHAT_MESSAGES;


public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private ViewPager mViewPager;
    private SessionManager session;
    private TextView nav_user_name, nav_mobile_number;
    private Menu menu;
    private SQLiteDatabaseHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("txtBravo");


        this.session = new SessionManager(DashboardActivity.this); // Session Manager
        this.helper = new SQLiteDatabaseHelper(this);

        if(!session.isLoggedIn())
        {
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        View header = navigationView.getHeaderView(0);
        nav_user_name = (TextView) header.findViewById(R.id.nav_user_name);
        nav_mobile_number = (TextView) header.findViewById(R.id.nav_mobile_number);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        // Fixes bug for disappearing fragment content
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        this.registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));


        Intent service = new Intent(getApplicationContext(), AlarmService.class);
        startService(service);
    }


    @Override
    public void onBackPressed()
    {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }

        else
        {
            super.onBackPressed();
        }
    }


    /** Called just before the activity is destroyed. */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mHandleMessageReceiver);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.nav_share_buyer:

                displayView(0);
                break;

            case R.id.nav_share_seller:

                displayView(1);
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    private void setupViewPager(ViewPager viewPager)
    {

        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        adapter.addFrag(new UserChatFragment(), "USER CHAT");
        adapter.addFrag(new StoreChatFragment(), "STORE CHAT");

        viewPager.setAdapter(adapter);
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();


        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }


        @Override
        public Fragment getItem(int position)
        {
            return mFragmentList.get(position);
        }


        @Override
        public int getCount()
        {
            return mFragmentList.size();
        }


        public void addFrag(Fragment fragment, String title)
        {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position)
        {
            return mFragmentTitleList.get(position);
        }
    }


    private void displayView(int position)
    {

        switch (position)
        {

            case 0:

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "I have been using txtBravo for some time and found it to be a handy app to find and connect with local businesses on the go via free chat. Download now https://play.google.com/store/apps/details?id=" + PACKAGE_NAME_BUYER;
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "txtBravo Android App");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                startActivity(Intent.createChooser(sharingIntent, "Share Via"));
                break;

            case 1:

                Intent sharingIntent1 = new Intent(Intent.ACTION_SEND);
                sharingIntent1.setType("text/plain");
                String shareBody1 = "I have been using txtBravo for some time and found it to be a handy app to find and connect with local businesses on the go via free chat. Download now https://play.google.com/store/apps/details?id=" + PACKAGE_NAME_SELLER;
                sharingIntent1.putExtra(Intent.EXTRA_SUBJECT, "txtBravo Seller Android App");
                sharingIntent1.putExtra(Intent.EXTRA_TEXT, shareBody1);

                startActivity(Intent.createChooser(sharingIntent1, "Share Via"));
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        try
        {
            menu.clear();
        }

        catch (Exception e)
        {

        }

        finally
        {

            getMenuInflater().inflate(R.menu.menu_home, menu);

            this.menu = menu;

            MenuItem menuItemBidders = menu.findItem(R.id.action_customer_message);
            menuItemBidders.setIcon(buildCounterDrawable(helper.unreadMessageCount(TABLE_USER_CHAT_MESSAGES), R.drawable.ic_account_white_24dp));

            MenuItem menuItemBidders1 = menu.findItem(R.id.action_store_message);
            menuItemBidders1.setIcon(buildCounterDrawable(helper.unreadMessageCount(TABLE_STORE_CHAT_MESSAGES), R.drawable.ic_store_white_24dp));
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.action_customer_message:

                mViewPager.setCurrentItem(0);
                return true;

            case R.id.action_store_message:

                mViewPager.setCurrentItem(1);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume()
    {

        super.onResume();

        try
        {

            User user = getUserDetails();

            nav_user_name.setText(user.getUserName().toUpperCase());
            nav_mobile_number.setText(user.getPhoneNo());

            onCreateOptionsMenu(menu);
        }

        catch (Exception e)
        {

        }
    }


    private User getUserDetails()
    {

        User userObj = new User();

        if (session.checkLogin()) {

            HashMap<String, String> user = session.getUserDetails();

            userObj.setUserID(Integer.parseInt(user.get(SessionManager.KEY_USER_ID)));
            userObj.setUserName(user.get(SessionManager.KEY_USER_NAME));
            userObj.setPhoneNo(user.get(SessionManager.KEY_PHONE));
        }

        return userObj;
    }


    private Drawable buildCounterDrawable(int count, int backgroundImageId)
    {

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.counter_menuitem_layout, null);
        view.setBackgroundResource(backgroundImageId);


        if (count == 0)
        {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        }

        else if(count > 9)
        {
            count = 9;
        }


        TextView textView = (TextView) view.findViewById(R.id.count);
        textView.setText(String.valueOf(count));


        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());


        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);
    }


    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {

            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);

            Log.v("gcm_message: ", "" + newMessage);

            if(newMessage == null)
            {
                return;
            }

            try
            {

                JSONObject jsonObj = new JSONObject(newMessage);

                if(jsonObj.getString("message_type").equalsIgnoreCase("chat_message"))
                {
                    onCreateOptionsMenu(menu);
                }


                // Waking up mobile if it is sleeping
                // WakeLocker.acquire(context);

                // Releasing wake lock
                // WakeLocker.release();
            }

            catch (Exception e)
            {

            }
        }
    };
}