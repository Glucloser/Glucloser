package com.nlefler.glucloser.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import bolts.Task
import bolts.TaskCompletionSource
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.nlefler.glucloser.R
import com.nlefler.glucloser.components.datafactory.DaggerDataFactoryComponent
import com.nlefler.glucloser.dataSource.MealHistoryRecyclerAdapter
import com.nlefler.glucloser.dataSource.RealmManager
import com.nlefler.glucloser.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.models.BolusEvent
import com.nlefler.glucloser.models.BolusEventType
import com.nlefler.glucloser.models.Meal
import com.nlefler.glucloser.models.Snack
import com.nlefler.glucloser.ui.DividerItemDecoration
import io.realm.*
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

public class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private var navBarItems: Array<String>? = null
    private var navDrawerLayout: DrawerLayout? = null
    private var navDrawerListView: ListView? = null
    private var navDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, HistoryListFragment(), HistoryFragmentId).commit()
        }

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        this.navBarItems = arrayOf(getString(R.string.nav_drawer_item_home),
                getString(R.string.nav_drawer_item_glucloser_login),
                getString(R.string.nav_drawer_item_foursquare_login),
                "v$versionName")
        this.navDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        this.navDrawerListView = findViewById(R.id.left_drawer) as ListView
        this.navDrawerListView?.adapter = ArrayAdapter(this, R.layout.drawer_list_item, this.navBarItems)
        this.navDrawerListView?.onItemClickListener = this

        this.navDrawerToggle = ActionBarDrawerToggle(this, this.navDrawerLayout, R.string.nav_drawer_open, R.string.nav_drawer_closed)
        this.navDrawerLayout!!.setDrawerListener(this.navDrawerToggle)

        supportActionBar.setDisplayHomeAsUpEnabled(true)
        supportActionBar.setHomeButtonEnabled(true)

        requestLocationPermission()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        this.navDrawerToggle?.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        this.navDrawerToggle?.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (this.navDrawerToggle!!.onOptionsItemSelected(item)) {
            return true
        }
        // Handle your other action bar items...

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    /** OnClickListener  */
    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (position) {
            1 -> {
                val intent = Intent(view.getContext(), LoginActivity::class.java)
                startActivity(intent)
            }
            2 -> {
                FoursquareAuthManager.SharedManager().startAuthRequest(this)
            }
        }
    }

    /** Foursquare Connect Intent  */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FoursquareAuthManager.FOURSQUARE_CONNECT_INTENT_CODE -> {
                FoursquareAuthManager.SharedManager().gotAuthResponse(this, resultCode, data ?: Intent())
            }
            FoursquareAuthManager.FOURSQUARE_TOKEN_EXCHG_INTENT_CODE -> {
                FoursquareAuthManager.SharedManager().gotTokenExchangeResponse(this, resultCode, data ?: Intent())
            }
            LogBolusEventActivityIntentCode -> {
                (getSupportFragmentManager().findFragmentByTag(HistoryFragmentId) as HistoryListFragment).updateMealHistory()
            }
        }
    }

    private fun requestLocationPermission() {
                if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

    }

    @Override
    public fun onRequestPermissionsResult(requestCode: Int,
            permissions: Array<String>, grantResults: Array<Int>) {
//        when (requestCode) {
//            is MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
    }

    public class HistoryListFragment : Fragment() {

        var realmManager: RealmManager? = null

        private var mealHistoryListView: RecyclerView? = null
        private var mealHistoryLayoutManager: RecyclerView.LayoutManager? = null
        private var mealHistoryAdapter: MealHistoryRecyclerAdapter? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val dataFactoryComponent = DaggerDataFactoryComponent.create()
            realmManager = dataFactoryComponent.realmFactory()
        }

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
            val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)

            this.mealHistoryListView = rootView.findViewById(R.id.meal_history_list) as RecyclerView

            this.mealHistoryLayoutManager = LinearLayoutManager(getActivity())
            this.mealHistoryListView!!.setLayoutManager(this.mealHistoryLayoutManager)

            this.mealHistoryAdapter = MealHistoryRecyclerAdapter(getActivity(), ArrayList<Meal>())
            this.mealHistoryListView!!.setAdapter(this.mealHistoryAdapter)
            this.mealHistoryListView!!.addItemDecoration(DividerItemDecoration(getActivity()))

            val activity = getActivity();

            val floatingActionsMenu = rootView.findViewById(R.id.main_floating_action_menu) as FloatingActionsMenu
            val logMealButton = rootView.findViewById(R.id.fab_log_meal_item) as FloatingActionButton
            logMealButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val intent = Intent(view.getContext(), LogBolusEventActivity::class.java)
                    intent.putExtra(LogBolusEventActivity.BolusEventTypeKey, BolusEventType.BolusEventTypeMeal.name)

                    activity.startActivityForResult(intent, LogBolusEventActivityIntentCode)
                    floatingActionsMenu.collapse()
                }
            })
            val logSnackButton = rootView.findViewById(R.id.fab_log_snack_item) as FloatingActionButton
            logSnackButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val intent = Intent(view.getContext(), LogBolusEventActivity::class.java)
                    intent.putExtra(LogBolusEventActivity.BolusEventTypeKey, BolusEventType.BolusEventTypeSnack.name)

                    activity.startActivityForResult(intent, LogBolusEventActivityIntentCode)
                    floatingActionsMenu.collapse()
                }
            })

            updateMealHistory()

            return rootView
        }

        internal fun updateMealHistory() {
            val mealResultsTask = realmManager?.executeTransaction(object: RealmManager.TxList<Meal> {
                override fun dependsOn(): List<RealmObject?> {
                    return emptyList()
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<Meal> {
                    val mealResults = realm.allObjectsSorted(Meal::class.java, Meal.DateFieldName, Sort.DESCENDING)
                    return mealResults
                }
            })
            val snackResultsTask = realmManager?.executeTransaction(object: RealmManager.TxList<Snack> {
                override fun dependsOn(): List<RealmObject?> {
                    return emptyList()
                }

                override fun execute(dependsOn: List<RealmObject?>, realm: Realm): List<Snack> {
                    val snackResults = realm.allObjectsSorted(Snack::class.java, Snack.DateFieldName, Sort.DESCENDING)
                    return snackResults
                }
            })

            val comparator = object: Comparator<BolusEvent> {
                override fun compare(a: BolusEvent, b: BolusEvent): Int {
                    return -1 * a.date.compareTo(b.date)
                }

                override fun equals(other: Any?): Boolean {
                    return other == this
                }
            }

            Task.whenAll(arrayListOf(mealResultsTask, snackResultsTask)).continueWith {
                val sortedCollections = ArrayList<BolusEvent>()
                sortedCollections.addAll(mealResultsTask?.result as List<Meal>)
                sortedCollections.addAll(snackResultsTask?.result as List<Snack>)
                Collections.sort(sortedCollections, comparator)

                this.mealHistoryAdapter!!.setEvents(sortedCollections)
            }
        }

        companion object {
            private val LOG_TAG = "PlaceholderFragment"
        }
    }

    companion object {
        private val LOG_TAG = "MainActivity"
        private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 22
        internal val LogBolusEventActivityIntentCode: Int = 4136
        internal val HistoryFragmentId: String = "HistoryFragmentId"
    }
}