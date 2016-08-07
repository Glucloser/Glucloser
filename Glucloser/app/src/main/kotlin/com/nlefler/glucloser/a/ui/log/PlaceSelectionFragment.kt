package com.nlefler.glucloser.a.ui.log

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import com.nlefler.glucloser.a.GlucloserApplication

import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.dataSource.PlaceSelectionRecyclerAdapter
import com.nlefler.glucloser.a.foursquare.FoursquareAuthManager
import com.nlefler.glucloser.a.foursquare.FoursquarePlaceHelper
import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.parcelable.PlaceParcelable
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import com.nlefler.glucloser.a.ui.DividerItemDecoration
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
class PlaceSelectionFragment @Inject constructor() : Fragment(), PlaceSelectionDelegate {

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

    override fun onCreate(bundle: Bundle?) {
        super<Fragment>.onCreate(bundle)

        this.setHasOptionsMenu(true)

        val toolbar = (activity as AppCompatActivity).supportActionBar
        toolbar?.title = getString(R.string.place_selection_toolbar_title)
        toolbar?.setDisplayHomeAsUpEnabled(true)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        dataFactory?.inject(this)

        foursquareHelper = FoursquarePlaceHelper(getActivity(), foursquareAuthManager)
        placesNetworkScheduler = Schedulers.io()

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.place_selection_fragment, container, false)
        this.placeSelectionList = rootView.findViewById(R.id.place_selection_list) as RecyclerView

        this.placeSelectionLayoutManager = LinearLayoutManager(getActivity())
        this.placeSelectionAdapter = PlaceSelectionRecyclerAdapter(this, emptyList(), emptyList())

        this.placeSelectionList!!.layoutManager = this.placeSelectionLayoutManager
        this.placeSelectionList!!.adapter = this.placeSelectionAdapter
        this.placeSelectionList!!.addItemDecoration(DividerItemDecoration(getActivity()))

        getClosestPlaces(null)
        placeFactory.mostUsedPlaces(4).toList().subscribeOn(placesNetworkScheduler).subscribe { list ->
            placeSelectionAdapter?.mostUsedPlaces = list
            placeSelectionAdapter?.notifyDataSetChanged()
        }

        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_place_selection, menu)


        val toolbar = (activity as AppCompatActivity).supportActionBar
        val searchItem = menu!!.findItem(R.id.action_place_search)
        MenuItemCompat.setOnActionExpandListener(searchItem, object: MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                item == searchItem ?: return false
                toolbar?.title = ""
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                item == searchItem ?: return false
                toolbar?.title = activity.getString(R.string.place_selection_toolbar_title)
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
    }

    override fun onDestroy() {
        super<Fragment>.onDestroy()

        closestPlacesSubscription!!.unsubscribe()
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
        closestPlacesSubscription = foursquareHelper!!.closestVenues(searchTerm)
                .subscribeOn(placesNetworkScheduler).subscribe(object: Observer<List<NLFoursquareVenue>> {
            override fun onCompleted() {
                closestPlacesSubscription!!.unsubscribe()
            }

            override fun onError(e: Throwable?) {
                Log.e(LOG_TAG, "Unable to get places from 4sq " + e.toString())
                activity.finish()
            }

            override fun onNext(venues: List<NLFoursquareVenue>?) {
                placeSelectionAdapter?.nearestVenues = venues ?: emptyList()
                placeSelectionAdapter?.notifyDataSetChanged()
            }
        })

    }

    companion object {
        private val LOG_TAG = "PlaceSelectionFragment"
    }
}
