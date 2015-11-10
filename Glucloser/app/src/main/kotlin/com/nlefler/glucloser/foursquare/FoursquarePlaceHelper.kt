package com.nlefler.glucloser.foursquare

import android.content.Context
import android.location.Location
import android.util.Log

import com.google.android.gms.location.LocationRequest
import com.nlefler.glucloser.R
import com.nlefler.nlfoursquare.Common.NLFoursquareEndpoint
import com.nlefler.nlfoursquare.Common.NLFoursquareEndpointParametersBuilder
import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue
import com.nlefler.nlfoursquare.Model.Venue.Search.NLFoursquareVenueSearchResponse
import com.nlefler.nlfoursquare.Search.NLFoursquareVenueSearch
import com.nlefler.nlfoursquare.Search.NLFoursquareVenueSearchIntent
import com.nlefler.nlfoursquare.Search.NLFoursquareVenueSearchParametersBuilder

import java.util.ArrayList

import pl.charmas.android.reactivelocation.ReactiveLocationProvider
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.functions.Action1

/**
 * Created by Nathan Lefler on 12/12/14.
 */
public class FoursquarePlaceHelper(private val context: Context) {
    private val restAdapter: RestAdapter
    private val foursquareSearchCategories: MutableList<String>
    private val locationProvider: ReactiveLocationProvider
    private val locationSubscription: Subscription
    private var lastLocation: Location? = null

    init {
        this.locationProvider = ReactiveLocationProvider(context)
        this.locationSubscription = locationProvider.getUpdatedLocation(createLocationRequest()).subscribe(object : Action1<Location> {
            override fun call(location: Location) {
                lastLocation = location
            }
        })

        this.restAdapter = RestAdapter.Builder().setEndpoint(NLFoursquareEndpoint.NLFOURSQUARE_V2_ENDPOINT).build()
        this.foursquareSearchCategories = ArrayList<String>()
        this.foursquareSearchCategories.add("4d4b7105d754a06374d81259") // Food
        this.foursquareSearchCategories.add("4d4b7105d754a06376d81259") // Nightlife
        this.foursquareSearchCategories.add("4bf58dd8d48988d103941735") // Private Homes
        // TODO: Bodegas, etc.
    }

    public fun finalize() {
        this.locationSubscription.unsubscribe()
    }

    public fun closestVenues(): Observable<List<NLFoursquareVenue>> {
        return this.closestVenues(null)
    }

    public fun closestVenues(searchTerm: String?): Observable<List<NLFoursquareVenue>> {
        return Observable.create<List<NLFoursquareVenue>>(object : Observable.OnSubscribe<List<NLFoursquareVenue>> {
            override fun call(subscriber: Subscriber<in List<NLFoursquareVenue>>) {
                if (lastLocation != null) {
                    closestVenuesHelper(lastLocation!!, searchTerm, subscriber)
                } else {
                    val subscription = LocationSubscription()
                    val action = object : Action1<Location> {
                        override fun call(location: Location) {
                            locationSubscription.unsubscribe()
                            closestVenuesHelper(location, searchTerm, subscriber)
                        }
                    }
                    subscription.subscribe(action)
                }
            }
        })
    }

    private fun closestVenuesHelper(location: Location, searchTerm: String?, subscriber: Subscriber<in List<NLFoursquareVenue>>) {
        val parametersBuilder = NLFoursquareVenueSearchParametersBuilder()
        parametersBuilder.latLon(location.getLatitude(), location.getLongitude())
                .intent(NLFoursquareVenueSearchIntent.NLFoursquareVenueSearchIntentCheckIn)
                .radius(500.0).limitToCategories(this.foursquareSearchCategories)
                .limit(100)
        if (searchTerm?.length() ?: 0 > 0) {
            parametersBuilder.query(searchTerm)
        }

        val venueSearch = restAdapter.create<NLFoursquareVenueSearch>(NLFoursquareVenueSearch::class.java)
        venueSearch.search(parametersBuilder.buildWithClientParameters(FoursquareAuthManager.SharedManager().getClientAuthParameters(this.context)), object : Callback<NLFoursquareResponse<NLFoursquareVenueSearchResponse>> {
            override fun success(foursquareResponse: NLFoursquareResponse<NLFoursquareVenueSearchResponse>, response: Response) {
                subscriber.onNext(foursquareResponse.response.venues)
                subscriber.onCompleted()
            }

            override fun failure(error: RetrofitError) {
                Log.e("4SQ", error.getMessage())
                Log.e("4SQ", error.getBody().toString())
                subscriber.onError(error)
            }
        })
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        locationRequest.setFastestInterval(250).setInterval(1000)
        locationRequest.setNumUpdates(1).setSmallestDisplacement(10.0.toFloat())

        return locationRequest
    }

    private inner class LocationSubscription {
        protected var subscription: Subscription? = null

        internal fun subscribe(action: Action1<Location>) {
            locationProvider.getUpdatedLocation(createLocationRequest()).subscribe(action)
        }

        internal fun unsubscribe() {
            if (this.subscription != null) {
                this.subscription!!.unsubscribe()
            }
        }
    }

    companion object {
        private val LOG_TAG = "FoursquarePlaceHelper"
    }
}
