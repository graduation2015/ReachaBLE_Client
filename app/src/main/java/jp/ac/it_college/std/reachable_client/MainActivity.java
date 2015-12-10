package jp.ac.it_college.std.reachable_client;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentials;

import java.util.List;

import jp.ac.it_college.std.reachable_client.aws.AWSClientManager;
import jp.ac.it_college.std.reachable_client.json.CouponInfo;
import jp.ac.it_college.std.reachable_client.json.JsonManager;

public class MainActivity extends AppCompatActivity
            implements CouponDetailsFragment.OnFragmentInteractionListener, MainFragment.ChangeFragmentListener, LoaderManager.LoaderCallbacks<AWSCredentials>{


    private String[] mPlanetTitles;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private Toolbar mToolbar;
    private AWSClientManager mClientManager;
    private ProgressDialogFragment mDialogFragment;
    private final int REQUEST_ENABLE_BT = 0x01;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFragment(savedInstanceState);
        findViews();
//        setUpDrawerList();
        setUpToolbar();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
        boolean result = true;
        switch (item.getItemId()) {
            case R.id.home:
                fragment = new MainFragment();
                getFragmentManager().beginTransaction().replace(R.id.container_content, fragment).commit();
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }
        return result;
    }

    private void findViews() {
        mPlanetTitles = getResources().getStringArray(R.array.categories);
//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        mDrawerList = (ListView) findViewById(R.id.list_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    private void setUpDrawerList() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mTitle = mDrawerTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar
                ,R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //ドロワーを開いた時に呼ばれる
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //ドロワーを閉じた時に呼ばれる
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }


    private void initFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_content, new MainFragment())
                    .commit();
        }
        mDialogFragment = new ProgressDialogFragment().newInstance("Credentials", "Getting credentials");
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public AWSClientManager getClientManager() {
        return mClientManager;
    }

    /* Implemented LoaderManager.LoaderCallbacks */
    @Override
    public Loader<AWSCredentials> onCreateLoader(int i, Bundle bundle) {
        mDialogFragment.show(getFragmentManager(), "Credentials");
        return new CognitoAsyncTaskLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<AWSCredentials> loader, AWSCredentials awsCredentials) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mDialogFragment.dismiss();
            }
        });

        mClientManager = new AWSClientManager(awsCredentials);
    }

    @Override
    public void onLoaderReset(Loader<AWSCredentials> loader) {

    }

    @Override
    public void goToCouponDetails(String key, int index) {
        new CouponDetailsDialog(this, key, index).show();
/*        fragment = new CouponDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putInt("index", index);
        fragment.setArguments(bundle);

        getFragmentManager().beginTransaction().replace(R.id.container_content, fragment).addToBackStack(null).commit();*/
    }

    @Override
    public void onFragmentInteraction() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int backStackCnt = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCnt != 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /*

    private class DrawerItemClickListener implements android.widget.AdapterView.OnItemClickListener {

        public static final int FRAGMENT_MAIN = 0;

        //TODO:フィルター
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            selectItem(Category.values()[position]);
        }

        private void selectItem(Category category) {
            String select;
            switch (category) {
                case Food:
                    select = "Food";
                    break;
                case Fashion:
                    select = "Fashion";
                    break;
                default:
                    select = "";
                    break;
            }



            Fragment fragment = getFragment(position);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container_content, fragment)
                    .commit();

            mDrawerList.setItemChecked(position, true);
            setTitle(mPlanetTitles[position]);
            mDrawerLayout.closeDrawers();

        }

        private Fragment getFragment(int position) {
            switch (position) {
                case FRAGMENT_MAIN:
                    return new MainFragment();
                default:
                    return new MainFragment();
            }
        }
    }
*/
}