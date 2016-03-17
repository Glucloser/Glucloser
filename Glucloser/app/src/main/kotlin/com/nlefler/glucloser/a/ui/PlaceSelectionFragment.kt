package com.nlefler.glucloser.a.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import com.nlefler.glucloser.a.GlucloserApplication

import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.actions.LogBolusEventAction
import com.nlefler.glucloser.a.dataSource.PlaceSelectionRecyclerAdapter
import com.nlefler.glucloser.a.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.a.foursquare.FoursquarePlaceHelper
import com.nlefler.glucloser.a.models.parcelable.PlaceParcelable
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue

import java.util.ArrayList

import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.schedulers.Schedulers
import rx.android.*
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
class PlaceSelectionFragment @Inject constructor() : Fragment(), Observer<List<NLFoursquareVenue>>, PlaceSelectionDelegate {

    lateinit var foursquareAuthManager: FoursquareAuthManager
    @Inject set

    private var foursquareHelper: FoursquarePlaceHelper? = null
    private var closestPlacesSubscription: Subscription? = null
    private var subscriptionScheduler: Scheduler? = null
    private var logMealAction: LogBolusEventAction = LogBolusEventAction()

    private var placeSelectionList: RecyclerView? = null
    private var placeSelectionAdapter: PlaceSelectionRecyclerAdapter? = null
    private var placeSelectionLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreate(bundle: Bundle?) {
        super<Fragment>.onCreate(bundle)

        this.setHasOptionsMenu(true)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        dataFactory?.inject(this)
        dataFactory?.inject(logMealAction)

        foursquareHelper = FoursquarePlaceHelper(getActivity(), foursquareAuthManager)
        subscriptionScheduler = Schedulers.newThread()
        getClosestPlaces(null)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_select_place, container, false)
        this.placeSelectionList = rootView.findViewById(R.id.place_selection_list) as RecyclerView

        this.placeSelectionLayoutManager = LinearLayoutManager(getActivity())
        this.placeSelectionAdapter = PlaceSelectionRecyclerAdapter(this, ArrayList<NLFoursquareVenue>())

        this.placeSelectionList!!.setLayoutManager(this.placeSelectionLayoutManager)
        this.placeSelectionList!!.setAdapter(this.placeSelectionAdapter)
        this.placeSelectionList!!.addItemDecoration(DividerItemDecoration(getActivity()))

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_place_selection, menu)
        val searchItem = menu!!.findItem(R.id.action_place_search)
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
    }

    override fun onDestroy() {
        super<Fragment>.onDestroy()

        closestPlacesSubscription!!.unsubscribe()
    }

    /** Observer  */
    override fun onCompleted() {
        this.closestPlacesSubscription!!.unsubscribe()
    }

    override fun onError(e: Throwable) {
        // TODO: Show UI
        Log.e(PlaceSelectionFragment.Companion.LOG_TAG, "Unable to get places from 4sq " + e.toString())
        getActivity().finish()
    }

    override fun onNext(nlFoursquareVenues: List<NLFoursquareVenue>) {
        this.placeSelectionAdapter!!.setVenues(nlFoursquareVenues)
    }

    /** PlaceSelectionDelegate  */
    override fun placeSelected(placeParcelable: PlaceParcelable) {
        if (getActivity() !is PlaceSelectionDelegate) {
            return
        }
        (getActivity() as PlaceSelectionDelegate).placeSelected(placeParcelable)
    }

    /** Helpers  */
    private fun getClosestPlaces(searchTerm: String?) {
        if (closestPlacesSubscription != null) {
            closestPlacesSubscription!!.unsubscribe()
        }
        closestPlacesSubscription = foursquareHelper!!.closestVenues(searchTerm).subscribeOn(subscriptionScheduler).subscribe(this)

    }

    companion object {
        private val LOG_TAG = "PlaceSelectionFragment"
    }
}
