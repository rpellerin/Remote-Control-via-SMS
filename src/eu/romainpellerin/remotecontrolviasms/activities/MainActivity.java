package eu.romainpellerin.remotecontrolviasms.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;

import eu.romainpellerin.remotecontrolviasms.CustomArrayAdapter;
import eu.romainpellerin.remotecontrolviasms.MyPreferenceFragment;
import eu.romainpellerin.remotecontrolviasms.PowerButtonService;
import eu.romainpellerin.remotecontrolviasms.R;

public class MainActivity extends Activity {

	private String[] menuDrawer; // va chercher les strings dans values/strings à afficher dans le menu
	private ListView menuDrawerView; // la listview du drawer
	private DrawerLayout mDrawerLayout; // la view root, le drawer
    private ActionBarDrawerToggle mDrawerToggle;
	private static String titleFrag;
	private static boolean drawOpen = false;
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    
    /* Fragment pour l'accueil seulement */
    public static class HomeFragment extends Fragment {
    	public HomeFragment() {
            // Empty constructor required for fragment subclasses
        }
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_home, container, false);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Le menu
        menuDrawer = getResources().getStringArray(R.array.menu_drawer);
        menuDrawerView = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        //menuDrawerView.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, menuDrawer));
        menuDrawerView.setAdapter(new CustomArrayAdapter(this, menuDrawer)); // applique le layout aux items
        menuDrawerView.setOnItemClickListener(new DrawerItemClickListener()); // listener
        
        
        // Le layout root, drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START); // shadow
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                setTitle(titleFrag);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                drawOpen = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                setTitle(R.string.app_name);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                drawOpen = true;
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle); // Set the drawer toggle as the DrawerListener
        
        // Rend l'icone cliquable
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        if (savedInstanceState == null) {
            selectItem(0);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
              @Override
              public void run() {
            	  mDrawerLayout.openDrawer(menuDrawerView);
              }
            }, 500);
        }
        if(!drawOpen) { // si fermé on met le titre du fragment actuel
        	setTitle(titleFrag);
        }
        startService(new Intent(this, PowerButtonService.class));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
        	return true;
        }
        // Ici, actions de l'ActionBar sauf cliquer sur le logo, qui est géré plus haut. On sait si c'est le logo grace a la ligne du dessus
        return super.onOptionsItemSelected(item);
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
    	Fragment fragment;
    	if (position == 0) fragment = new HomeFragment();
    	else {
	        fragment = new MyPreferenceFragment();
	        Bundle args = new Bundle();
	        args.putInt(MyPreferenceFragment.ARG_PAGE, position);
	        fragment.setArguments(args);
    	}

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        menuDrawerView.setItemChecked(position, true);
        setTitle(titleFrag = menuDrawer[position]);
        mDrawerLayout.closeDrawer(menuDrawerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);
        return false; // true pour activer
    }
    
    @Override
    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }
    
    
    /* Google Analytics */
    @Override
	public void onStart(){super.onStart();EasyTracker.getInstance(this).activityStart(this);}
    @Override
	public void onStop(){super.onStart();EasyTracker.getInstance(this).activityStart(this);}
}
