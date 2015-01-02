package com.nlefler.glucloser.foursquare;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;
import com.nlefler.glucloser.R;
import com.nlefler.nlfoursquare.Common.NLFoursquareEndpoint;
import com.nlefler.nlfoursquare.Common.NLFoursquareEndpointParametersBuilder;
import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse;
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;
import com.nlefler.nlfoursquare.Model.Venue.Search.NLFoursquareVenueSearchResponse;
import com.nlefler.nlfoursquare.Search.NLFoursquareVenueSearch;
import com.nlefler.nlfoursquare.Search.NLFoursquareVenueSearchIntent;
import com.nlefler.nlfoursquare.Search.NLFoursquareVenueSearchParametersBuilder;

import java.util.ArrayList;
import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by Nathan Lefler on 12/12/14.
 */
public class FoursquarePlaceHelper {
    private static String LOG_TAG = "FoursquarePlaceHelper";

    private Context context;
    private RestAdapter restAdapter;
    private List<String> foursquareSearchCategories;
    private ReactiveLocationProvider locationProvider;
    private Subscription locationSubscription;
    private Location lastLocation;

    public FoursquarePlaceHelper(Context ctx) {
        this.context = ctx;
        this.locationProvider = new ReactiveLocationProvider(ctx);
        this.locationSubscription = locationProvider.getUpdatedLocation(createLocationRequest())
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        lastLocation = location;
                    }
                });

        this.restAdapter = new RestAdapter.Builder()
                .setEndpoint(NLFoursquareEndpoint.NLFOURSQUARE_V2_ENDPOINT)
                .build();
        this.foursquareSearchCategories = new ArrayList<String>();
        this.foursquareSearchCategories.add("4d4b7105d754a06374d81259"); // Food
        this.foursquareSearchCategories.add("4d4b7105d754a06376d81259"); // Nightlife
        this.foursquareSearchCategories.add("4bf58dd8d48988d103941735"); // Private Homes
        // TODO: Bodegas, etc.
    }

    @Override
    public void finalize() {
        this.locationSubscription.unsubscribe();
    }

    public Observable<List<NLFoursquareVenue>> closestVenues() {
        return this.closestVenues(null);
    }

    public Observable<List<NLFoursquareVenue>> closestVenues(final String searchTerm) {
        return Observable.create(
                new Observable.OnSubscribe<List<NLFoursquareVenue>>() {
                    @Override
                    public void call(final Subscriber<? super List<NLFoursquareVenue>> subscriber) {
                        if (lastLocation != null) {
                            closestVenuesHelper(lastLocation, searchTerm, subscriber);
                        }
                        else {
                            LocationSubscription subscription = new LocationSubscription();
                            Action1<Location> action = new Action1<Location>() {
                                        @Override
                                        public void call(Location location) {
                                            locationSubscription.unsubscribe();
                                            closestVenuesHelper(location, searchTerm, subscriber);
                                        }
                                    };
                            subscription.subscribe(action);
                        }
                    }
                }
        );
    }

    private void closestVenuesHelper(Location location,
                                     String searchTerm,
                                     final Subscriber<? super List<NLFoursquareVenue>> subscriber) {
         NLFoursquareVenueSearchParametersBuilder parametersBuilder =
                new NLFoursquareVenueSearchParametersBuilder();
        parametersBuilder.latLon(location.getLatitude(),
                location.getLongitude())
        .intent(NLFoursquareVenueSearchIntent.NLFoursquareVenueSearchIntentCheckIn)
        .radius(150.0)
        .limitToCategories(this.foursquareSearchCategories)
        .limit(50);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            parametersBuilder.query(searchTerm);
        }

        NLFoursquareVenueSearch venueSearch = restAdapter.create(NLFoursquareVenueSearch.class);
        venueSearch.search(parametersBuilder.buildWithClientParameters(
                        FoursquareAuthManager.SharedManager().getClientAuthParameters(this.context)),
                new Callback<NLFoursquareResponse<NLFoursquareVenueSearchResponse>>() {
                    @Override
                    public void success(NLFoursquareResponse<NLFoursquareVenueSearchResponse> foursquareResponse,
                                        Response response) {
                        subscriber.onNext(foursquareResponse.response.venues);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e("4SQ", error.getMessage());
                        Log.e("4SQ", error.getBody().toString());
                        subscriber.onError(error);
                    }
                });
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setFastestInterval(250).setInterval(1000);
        locationRequest.setNumUpdates(1).setSmallestDisplacement(10.0f);

        return locationRequest;
    }

    private class LocationSubscription {
        protected Subscription subscription;

        protected void subscribe(Action1<Location> action) {
            locationProvider.getUpdatedLocation(createLocationRequest())
                    .subscribe(action);
        }

        protected void unsubscribe() {
           if (this.subscription != null) {
               this.subscription.unsubscribe();
           }
        }
    }
}
