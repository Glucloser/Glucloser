package com.nlefler.glucloser.a.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.constraint.ConstraintLayout
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.dataSource.PlaceSelectionRecyclerAdapter
import com.nlefler.glucloser.a.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.a.foursquare.FoursquarePlaceHelper
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import com.nlefler.glucloser.a.models.parcelable.PlaceParcelable
import com.nlefler.glucloser.a.ui.DividerItemDecoration
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue
import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.schedulers.Schedulers
import javax.inject.Inject

class PlaceSelectionActivity: AppCompatActivity(), PlaceSelectionDelegate {

    lateinit var foursquareAuthManager: FoursquareAuthManager
    @Inject set

    lateinit var placeFactory: PlaceFactory
    @Inject set

    private var foursquareHelper: FoursquarePlaceHelper? = null
    private var closestPlacesSubscription: Subscription? = null
    private var placesNetworkScheduler: Scheduler? = null

    private var placeSelectionList: RecyclerView? = null
    private var placeSelectionAdapter: PlaceSelectionRecyclerAdapter? = null
    private var placeSelectionLayoutManager: RecyclerView.LayoutManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.place_selection_activity)

        val toolbar = findViewById(R.id.place_selection_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.place_selection_activity_title)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        dataFactory?.inject(this)

        foursquareHelper = FoursquarePlaceHelper(this, foursquareAuthManager)
        placesNetworkScheduler = Schedulers.io()

        val rootView = findViewById(R.id.place_selection_activity_container)
        this.placeSelectionList = rootView.findViewById(R.id.place_selection_list) as RecyclerView

        this.placeSelectionLayoutManager = LinearLayoutManager(this)
        this.placeSelectionAdapter = PlaceSelectionRecyclerAdapter(this, emptyList(), emptyList())

        this.placeSelectionList!!.layoutManager = this.placeSelectionLayoutManager
        this.placeSelectionList!!.adapter = this.placeSelectionAdapter
        this.placeSelectionList!!.addItemDecoration(DividerItemDecoration(this))

        getClosestPlaces(null)
        placeFactory.mostUsedPlaces(4).toList().subscribeOn(placesNetworkScheduler).subscribe { list ->
            placeSelectionAdapter?.mostUsedPlaces = list
            placeSelectionAdapter?.notifyDataSetChanged()
        }

    }

    override fun onDestroy() {
        closestPlacesSubscription!!.unsubscribe()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_selection, menu)

        val toolbar = supportActionBar
        val searchItem = menu!!.findItem(R.id.action_place_search)
        MenuItemCompat.setOnActionExpandListener(searchItem, object: MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                item == searchItem ?: return false
                toolbar?.title = ""
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                item == searchItem ?: return false
                toolbar?.title = getString(R.string.place_selection_activity_title)
                return true
            }
        })
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setOnSearchClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                getClosestPlaces(searchView.getQuery().toString())
            }
        })
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                getClosestPlaces(query)
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    /** PlaceSelectionDelegate  */
    override fun placeSelected(placeParcelable: PlaceParcelable) {
        val result = Intent()
        result.putExtra(SelectedPlaceResultKey, placeParcelable)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    /** Helpers  */
    private fun getClosestPlaces(searchTerm: String?) {
        if (closestPlacesSubscription != null) {
            closestPlacesSubscription!!.unsubscribe()
        }
        closestPlacesSubscription = foursquareHelper!!.closestVenues(searchTerm)
                .subscribeOn(placesNetworkScheduler).subscribe(object: Observer<List<NLFoursquareVenue>> {
            override fun onCompleted() {
                closestPlacesSubscription!!.unsubscribe()
            }

            override fun onError(e: Throwable?) {
                finish()
            }

            override fun onNext(venues: List<NLFoursquareVenue>?) {
                placeSelectionAdapter?.nearestVenues = venues ?: emptyList()
                placeSelectionAdapter?.notifyDataSetChanged()
            }
        })
    }

    companion object {
        public val SelectedPlaceResultKey = "selectedPlaceResultKey"
    }
}