package com.nlefler.glucloser.fragments.add;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.place.PlaceUtil;
import com.nlefler.glucloser.R;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.food.FoodUtil;
import com.nlefler.glucloser.util.FoursquareUtil;
import com.nlefler.glucloser.util.LocationUtil;
import com.nlefler.glucloser.util.database.save.FoodUpdatedEvent;
import com.nlefler.glucloser.util.database.save.MealUpdatedEvent;
import com.nlefler.glucloser.util.database.save.PlaceUpdatedEvent;
import com.nlefler.glucloser.util.database.save.SaveManager;
import com.nlefler.nlfoursquare.Explore.NLFoursquareVenueExplore;
import com.nlefler.nlfoursquare.Explore.NLFoursquareVenueExploreParametersBuilder;
import com.nlefler.nlfoursquare.Explore.NLFoursquareVenueExploreSection;
import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse;
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters;
import com.nlefler.nlfoursquare.Model.Venue.Explore.NLFoursquareVenueExploreGroup;
import com.nlefler.nlfoursquare.Model.Venue.Explore.NLFoursquareVenueExploreGroupRecommendedItem;
import com.nlefler.nlfoursquare.Model.Venue.Explore.NLFoursquareVenueExploreResponse;
import com.nlefler.nlfoursquare.Model.Venue.NLFoursquareVenue;
import com.squareup.otto.Subscribe;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

@SuppressLint("ValidFragment")
public class AddMealFragment extends Fragment 
implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
	private static final String LOG_TAG = "Glucloser_Add_Meal_Activity";

	// Bundle keys for loading a meal to edit
	public static final String MEAL_KEY = "meal";
	public static final String PLACE_KEY = "place"; // -> Place
	public static final String FOODS_KEY = "foods"; // -> List<Food>

	private Meal meal;

	private List<Place> placesList;
	private TextView placeName;
	private TextView placeAddress;
	private Place selectedPlace;
	private boolean addingPlace = false;

	LinearLayout foodTableLayout;
	TextView addFoodItem;

	private Map<String, Food> foodMap;
	private List<String> foodViewList;

	private Button saveButton;

	private LocationListener savedPlacesLocationListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle state = savedInstanceState != null ?
				savedInstanceState : 
					(getArguments() != null ? getArguments() : new Bundle());

		foodMap = new HashMap<String, Food>();
		foodViewList = new ArrayList<String>();

		placesList = new ArrayList<Place>();

		// Add location listener for places
		savedPlacesLocationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location loc) {
				Log.i(LOG_TAG, "Location change. Updating nearby places");

				updateNearbyPlacesAndShowSelectDialog(loc, false);
			}

			@Override
			public void onProviderDisabled(String provider) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				if (status == LocationProvider.AVAILABLE) {
					Log.i(LOG_TAG, "LocationProvider status change. Updating nearby places");

					Location loc = LocationUtil.getLastKnownLocation();
					updateNearbyPlacesAndShowSelectDialog(loc, false);
				}
			}

		};
		LocationUtil.addLocationListener(savedPlacesLocationListener);
		updateNearbyPlacesAndShowSelectDialog(LocationUtil.getLastKnownLocation(), false);

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.add_meal, container, false);

		Bundle state = savedInstanceState != null ?
				savedInstanceState : 
					(getArguments() != null ? getArguments() : new Bundle());

		placeName = (TextView)view.findViewById(R.id.add_meal_place_name);
		placeAddress = (TextView)view.findViewById(R.id.add_meal_place_address);


		foodTableLayout = (LinearLayout) view.findViewById(R.id.add_meal_food_table_list_layout);
		addFoodItem = (TextView) view.findViewById(R.id.add_meal_add_food_item);
		addFoodItem.setClickable(true);
		addFoodItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Bundle args = new Bundle();

				Food food = new Food();
				foodMap.put(food.glucloserId, food);
				args.putSerializable(AddFoodFragment.FOOD_KEY, food);

				AddFoodFragment fragment = new AddFoodFragment();
				fragment.setArguments(args);

                pushFragment(fragment, "AddFood");
			}

		});

		TextView editDateButton = (TextView) view.findViewById(R.id.add_meal_edit_date_eaten_button);
		editDateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar today = Calendar.getInstance();
				DatePickerDialog picker = 
						new DatePickerDialog(getActivity(), AddMealFragment.this,
								today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
				picker.show();
			}
		});

		TextView editTimeButton = (TextView) view.findViewById(R.id.add_meal_edit_time_eaten_button);
		editTimeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Calendar today = Calendar.getInstance();
				TimePickerDialog picker = 
						new TimePickerDialog(getActivity(), AddMealFragment.this,
								today.get(Calendar.HOUR_OF_DAY), today.get(Calendar.MINUTE), true);
				picker.show();
			}
		});

		saveButton = (Button) view.findViewById(R.id.add_meal_save_button);
		saveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveMealAndExit();
			}
		});

		OnClickListener placeClick = new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showSelectPlaceDialog();
			}

		};
		placeName.setClickable(true);
		placeName.setOnClickListener(placeClick);
		placeAddress.setClickable(true);
		placeAddress.setOnClickListener(placeClick);

		populateStateFromBundle(state);
		populateViewsFromState();

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		bundle.putSerializable(MEAL_KEY, meal);

		if (selectedPlace != null || !foodMap.values().isEmpty()) {
			if (selectedPlace != null) {
				bundle.putSerializable(PLACE_KEY, selectedPlace);
			}

			ArrayList<Food> foods = new ArrayList<Food>(foodMap.values());
			bundle.putSerializable(FOODS_KEY, foods);
		}
	}

    @Override
    public void onResume() {
        super.onResume();

        SaveManager.getPlaceUpdatedBus().register(this);
        SaveManager.getFoodUpdatedBus().register(this);
        SaveManager.getMealUpdatedBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        SaveManager.getPlaceUpdatedBus().unregister(this);
        SaveManager.getFoodUpdatedBus().unregister(this);
        SaveManager.getMealUpdatedBus().unregister(this);
    }

	@Override
	public void onDestroy() {
		LocationUtil.removeLocationListener(savedPlacesLocationListener);

		super.onDestroy();
	}

	private void populateStateFromBundle(Bundle bundle) {
		if (bundle.containsKey(MEAL_KEY)) {
			Log.v(LOG_TAG, "Populating add meal from bundle");

			meal = (Meal)bundle.getSerializable(MEAL_KEY);

            // TODO: Thread
			Place place = meal.getPlace();
			if (place == null) {
				place = (Place)bundle.getSerializable(PLACE_KEY);
			}
			if (place != null) {
				selectedPlace = place;
			}

			List<Food> foods = (ArrayList<Food>)
					bundle.getSerializable(FOODS_KEY);
			if (foods != null) {
				for (Food food : foods) {
					addFood(food, true, false);
				}
			} else {
                new AsyncTask<Meal, Void, List<Food>>() {

                    @Override
                    protected List<Food> doInBackground(Meal... params) {
                        return params[0].getFoods();
                    }

                    @Override
                    protected void onPostExecute(List<Food> foods) {
                        for (Food food : foods) {
                            addFood(food, true, false);
                        }
                    }
                }.execute(meal);
			}
		} else {
			meal = new Meal();
		}
	}

	private void populateViewsFromState() {
		if (selectedPlace != null) {
			setSelectedPlace(selectedPlace);
		}

		for (Food food : foodMap.values()) {
			addFood(food, true, true);
		}
	}

	@Subscribe public void foodUpdated(FoodUpdatedEvent event) {
		if (foodMap.containsKey(event.getFood().glucloserId)) {
			boolean replaceFoodView = foodViewList.contains(event.getFood().glucloserId);
			addFood(event.getFood(), true, replaceFoodView);
		} else {
			Log.v(LOG_TAG, "Ignoring updated food with id " + event.getFood().glucloserId + ". Not in our map");
		}
	}

	@Subscribe public void placeUpdated(PlaceUpdatedEvent event) {
		if (addingPlace
                || selectedPlace.equals(event.getPlace())
                || selectedPlace.glucloserId.equals(event.getPlace().glucloserId)) {
			addingPlace = false;
			setSelectedPlace(event.getPlace());
		} else {
			Log.v(LOG_TAG, "Ignoring updated place with id " + event.getPlace().glucloserId + ". Not selected");
		}
	}

	private void clearViews() {
		foodMap.clear();
		foodTableLayout.removeAllViews();

		selectedPlace = null;
		placeName.setText(R.string.place_noun);
		placeAddress.setText(R.string.location_noun);
	}

	private void addFood( Food food, boolean addView, boolean replace) {
		// TODO This breaks the date when editing
		food.setNowAsDateEaten();

		if (addView) {
			addViewForFood(food, replace);
		}
	}

	private void addViewForFood(Food food, boolean replace) {
        if (food.name == null || food.name.isEmpty() || food.carbs < 0)
        {
            Log.w(LOG_TAG, "Ignoring food with invalid name or carb count");
            return;
        }
		RelativeLayout foodItem = null;
		if (replace) {
			foodItem = (RelativeLayout) foodTableLayout.findViewWithTag(food.glucloserId);
		} 
		if (foodItem == null) {
			foodItem = (RelativeLayout)getActivity().getLayoutInflater().inflate(
					R.layout.food_line_item, null);
			foodTableLayout.addView(foodItem, Math.max(0, foodTableLayout.getChildCount() - 1));
		}
		TextView name = (TextView)foodItem.findViewById(R.id.food_line_item_food_name);
		TextView carbs = (TextView)foodItem.findViewById(R.id.food_line_item_carbs_value);

		name.setText(food.name);

		String carbValue = String.valueOf(food.carbs);
		if (food.isCorrection) {
			carbValue += " (c)";
		}
		carbs.setText(carbValue);

		foodItem.setTag(food.glucloserId);

		foodItem.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle args = new Bundle();

				String fId = (String)v.getTag();
				Food food = foodMap.get(fId);
				args.putSerializable(AddFoodFragment.FOOD_KEY, food);


				AddFoodFragment fragment = new AddFoodFragment();
				fragment.setArguments(args);

                pushFragment(fragment, "AddFood");
			}
		});
		
		foodViewList.add(food.glucloserId);
	}

	private void updateNearbyPlacesAndShowSelectDialog(final Location loc, final boolean showDialog) {
		if (loc == null) {
			Log.i(LOG_TAG, "Asked to updated nearby places with null location, returning all places");
			new AsyncTask<Void, Void, List<Place>>() {

				@Override
				protected List<Place> doInBackground(Void... params) {
					return PlaceUtil.getAllPlacesSortedByName();
				}

				@Override
				protected void onPostExecute(List<Place> places) {
					Log.i(LOG_TAG, "Got new list of all places. Got " + places.size() + " places");

					placesList.clear();
					placesList.addAll(places);

					if (showDialog) {
						showSelectPlaceDialog();
					} else {
						if (placesList.size() == 1) {
							setSelectedPlace(placesList.get(0));
						}
					}
				}
			}.execute();
		} else {
            com.nlefler.glucloser.util.Callback<List<Place>> callback =
                    new com.nlefler.glucloser.util.Callback<List<Place>>() {
                        @Override
                        public void call(List<Place> data) {
                            placesList.addAll(data);
                        }

                        @Override
                        public void error(String message) {
                        }
                    };
            NLFoursquareClientParameters clientParameters = new NLFoursquareClientParameters(
                    getString(R.string.foursquare_client_id),
                    getString(R.string.foursquare_client_secret)
            );
            FoursquareUtil.placesNearCurrentLocation(clientParameters, callback);

			new AsyncTask<Void, Void, List<Place>>() {
				@Override
                protected List<Place> doInBackground(Void... params) {

                    return PlaceUtil.getPlacesNear(loc);
				}

				@Override
				protected void onPostExecute(List<Place> places) {
					if (places == null) {
						Log.i(LOG_TAG, "Got no new nearby places");
					}

					Log.i(LOG_TAG, "Got new list of nearby places. Got " + places.size() + " places");

					placesList.clear();
					placesList.addAll(places);

					if (placesList.isEmpty()) {
						updateNearbyPlacesAndShowSelectDialog(null, showDialog);
						return;
					} else if (showDialog) {
						showSelectPlaceDialog();
					} else {
						if (placesList.size() == 1) {
							setSelectedPlace(placesList.get(0));
						}
					}
				}
			}.execute();
		}
	}
	private void showSelectPlaceDialog() {
		// Create and show the dialog.
		// +2 = Show All, Add a Place, Edit Place

        final CharSequence[] nearbyPlaceNames = new CharSequence[placesList.size() + 3];
        int idx = 0;
        for (Place place : placesList) {
            nearbyPlaceNames[idx++] = place.name;
        }

        nearbyPlaceNames[nearbyPlaceNames.length - 3] = "Show All";
        nearbyPlaceNames[nearbyPlaceNames.length - 2] = "Add a place";
        nearbyPlaceNames[nearbyPlaceNames.length - 1] = "Edit this place";

        DialogFragment newFragment = new SelectPlaceDialogFragment(nearbyPlaceNames);
        newFragment.show(getFragmentManager(), "dialog");
     }

	protected void setSelectedPlaceFromList(int i) {
		try {
            Place p = placesList.get(i);
			setSelectedPlace(p);
		} catch (IndexOutOfBoundsException ex) {
			Log.i(LOG_TAG, "Tried to select place out of bounds of list, assuming command");
			int commandNum = i - placesList.size();
			switch (commandNum) {
			case 2: // Edit
			{
				if (selectedPlace == null) {
					// TODO: Alert no place
					return;
				}

				Bundle args = new Bundle();
				args.putSerializable(AddPlaceFragment.PLACE_KEY, selectedPlace);

				AddPlaceFragment fragment = new AddPlaceFragment();
				fragment.setArguments(args);
                pushFragment(fragment, "AddPlace");
			}
			break;
			case 1: // Add
			{
				addingPlace = true;
				AddPlaceFragment fragment = new AddPlaceFragment();
                pushFragment(fragment, "AddPlace");
			}
			break;
			case 0: // Show All
			{
				updateNearbyPlacesAndShowSelectDialog(null, true);
			}
			break;
			default:
				Log.e(LOG_TAG, "Invalid place popup command " + commandNum);
			}
		}
	}

	private void setSelectedPlace(Place place) {
		if (place != null && place.name != null) {
			selectedPlace = place;
			placeName.setText(place.name);
			if (place.readableAddress != null) {
				placeAddress.setText(place.readableAddress);
			} else {
				placeAddress.setText("Location Unknown");
				PlaceUtil.updateReadableLocation(selectedPlace);
				if (place.readableAddress != null) {
					placeAddress.setText(place.readableAddress);
				}
			}
		}
	}

	public class SelectPlaceDialogFragment extends DialogFragment {		
		private CharSequence[] placeNames;

		public SelectPlaceDialogFragment(CharSequence[] names) {
			placeNames = names;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setTitle("Select Place");
			builder.setItems(placeNames, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					setSelectedPlaceFromList(item);
				}
			});	

			return builder.create();
		}
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		if (meal == null) {
			return;
		}

		// Meal dates are in UTC
		Calendar currDate = Calendar.getInstance();
		TimeZone local = TimeZone.getDefault();
		if (meal.dateEaten != null) {
			currDate.setTime(meal.dateEaten);
			currDate.add(Calendar.MILLISECOND, local.getOffset(meal.dateEaten.getTime()));
		}

		currDate.set(Calendar.YEAR, year);
		currDate.set(Calendar.MONTH, monthOfYear);
		currDate.set(Calendar.DATE, dayOfMonth);

		meal.dateEaten = currDate.getTime();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		if (meal == null) {
			return;
		}

		// Meal dates are in UTC
		Calendar currDate = Calendar.getInstance();
		TimeZone local = TimeZone.getDefault();
		if (meal.dateEaten != null) {
			currDate.setTime(meal.dateEaten);
			currDate.add(Calendar.MILLISECOND, local.getOffset(meal.dateEaten.getTime()));
		}

		currDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
		currDate.set(Calendar.MINUTE, minute);

		meal.dateEaten = currDate.getTime();
	}

	private void saveMealAndExit() {
		// Make sure we have a place for this meal
		if (selectedPlace == null) {
			Toast needPlace = Toast.makeText(getActivity().getApplicationContext(),
					"Tap 'Place' to choose a place", Toast.LENGTH_SHORT);
			needPlace.show();
			return;
		}

		meal.setPlace(selectedPlace);

		Log.i(LOG_TAG, "Setting up " + foodMap.values().size() + " meal to foods");
		for (Food food : foodMap.values()) {
			meal.addFood(food);
		}

        SaveManager.saveMeal(meal);
	}
	
	@Subscribe public void mealUpdated(MealUpdatedEvent event) {
		if (meal.equals(event.getMeal())
                || meal.glucloserId.equals(event.getMeal().glucloserId)) {
			clearViews();
		}
	}

    private void pushFragment(Fragment fragment, String name) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment_container,
                fragment, name);
        transaction.addToBackStack(name);
        transaction.commit();
    }
}
