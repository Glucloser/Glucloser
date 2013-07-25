package com.hagia.pump.fragments.home;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.hagia.pump.PumpActivity;
import com.hagia.pump.R;
import com.hagia.pump.detail.MealDetailActivity;
import com.hagia.pump.detail.PlaceDetailActivity;
import com.hagia.pump.fragments.add.AddMealFragment;
import com.hagia.pump.fragments.home.HomeListAdapter.ResultContainer;
import com.hagia.pump.types.Food;
import com.hagia.pump.types.Meal;
import com.hagia.pump.types.MealToFood;
import com.hagia.pump.types.Place;
import com.hagia.pump.types.PlaceToMeal;
import com.hagia.pump.util.LocationUtil;
import com.hagia.pump.util.MealUtil;

public class HomeFragment extends ListFragment {
	private static final String LOG_TAG = "Pump_Home_Fragment";

	private EditText omniBar;

	private String searchTerm;

	private HomeListAdapter listAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		listAdapter = new HomeListAdapter(getActivity().getLayoutInflater());
		setListAdapter(listAdapter);
		
		listAdapter.fetchData(LocationUtil.getCurrentLocation());
		LocationUtil.addLocationListener(new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {				
			}

			@Override
			public void onLocationChanged(Location location) {
				listAdapter.fetchData(location);
			}
		});
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		View view = inflater.inflate(R.layout.home_view, container, false);

		omniBar = (EditText) view.findViewById(R.id.home_omni_bar);
		omniBar.setOnEditorActionListener(new OnEditorActionListener() {
			private InputMethodManager manager = (InputMethodManager)
					getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH ||
						actionId == EditorInfo.IME_ACTION_DONE) {
					refineContent();

					manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

		return view;
	}

	@Override
	public void onPause() {
		InputMethodManager manager = (InputMethodManager)
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(omniBar.getWindowToken(), 0);

		super.onPause();
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		ResultContainer container = (ResultContainer) getListView().getItemAtPosition(position);

		if (container.placeAndFoods != null) {
			// Popular Meal
			Bundle args = new Bundle();

			Place place = (Place)container.placeAndFoods[0];
			ArrayList<Food> foods = (ArrayList<Food>)container.placeAndFoods[1];
			
			args.putSerializable(AddMealFragment.MEAL_KEY, setupMealWithPlaceAndFoods(place, foods));

			PumpActivity.getPumpActivity().selectNavigationItemWithBundle(
					PumpActivity.NavigationItem.AddMealItem, args);
		} else if (container.placeAndMeal != null) {
			final Place place = (Place)container.placeAndMeal[0];
			final Meal meal = (Meal)container.placeAndMeal[1];

			// Historic Meal

			// Show the action popup
			// 1) View Meal Details
			// 2) Use For New Meal

			// Keep this in syc with the OnClickListener
			CharSequence[] actions = new CharSequence[] {
					"View Meal Details",
					"Use For New Meal"
			};

			final DialogFragment actionFragment = 
					new ActionDialogFrament(actions,
							new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							switch (which) {
							case 0: // Details
								showDetails();
								break;
							case 1: // New Meal
								addNewMeal();
								break;
							default: // Details
								showDetails();
							}
						}

						private void showDetails() {
							Intent detailIntent = new Intent(getActivity(), MealDetailActivity.class);
							detailIntent.putExtra(MealDetailActivity.MEAL_KEY, meal);
							getActivity().startActivity(detailIntent);
						}

						private void addNewMeal() {
							Log.v(LOG_TAG, "Add new meal selected for meal with id " + meal.id);
							Log.v(LOG_TAG, "Place named " + place.name);

							Bundle args = new Bundle();
														
							ArrayList<Food> foods = new ArrayList<Food>();
							// TODO: Is this running in a thread?
							for (MealToFood m2f : MealUtil.getFoodsForMeal(meal)) {
								Food food = m2f.food;
								foods.add(food);
							}
							Meal meal = setupMealWithPlaceAndFoods(place, foods);

							args.putSerializable(AddMealFragment.MEAL_KEY, meal);

							PumpActivity.getPumpActivity().selectNavigationItemWithBundle(
									PumpActivity.NavigationItem.AddMealItem, args);
						}

					});
			actionFragment.show(getActivity().getFragmentManager(), 
					"meal_action_dialog");
		} else if (container.placeAndMealCount != null) {
			final Place place = (Place)container.placeAndMealCount[0];
			final Integer mealCount = (Integer)container.placeAndMealCount[1];
			
			// Place

			// Show the action popup
			// 1) View Place Details
			// 2) Use For New Meal

			// Keep this in sycn with the OnClickListener
			CharSequence[] actions = new CharSequence[] {
					"View Place Details",
					"Use For New Meal"
			};

			final DialogFragment actionFragment = 
					new ActionDialogFrament(actions,
							new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							switch (which) {
							case 0: // Details
								showDetails();
								break;
							case 1: // New Meal
								addNewMeal();
								break;
							default: // Details
								showDetails();
							}
						}

						private void showDetails() {
							Intent detailIntent = new Intent(getActivity(), PlaceDetailActivity.class);
							detailIntent.putExtra(PlaceDetailActivity.PLACE_EXTRA_KEY, place);
							getActivity().startActivity(detailIntent);
						}

						private void addNewMeal() {
							Bundle args = new Bundle();
							
							args.putSerializable(AddMealFragment.MEAL_KEY, setupMealWithPlaceAndFoods(place, new ArrayList<Food>()));

							PumpActivity.getPumpActivity().selectNavigationItemWithBundle(
									PumpActivity.NavigationItem.AddMealItem, args);
						}

					});
			actionFragment.show(getActivity().getFragmentManager(), 
					"meal_action_dialog");
		}
	}

	private void refineContent() {
		searchTerm = omniBar.getText().toString().trim();

		// getCurrentLocation may be null, but it's better than doing nothing
		// while waiting for location and appearing to lag after the user
		// presses search
		listAdapter.fetchDataWithTerm(LocationUtil.getCurrentLocation(), searchTerm);	
	}

	private Meal setupMealWithPlaceAndFoods(Place place, List<Food> foods) {
		Meal meal = new Meal();
		
		meal.placeToMeal = new PlaceToMeal();
		meal.placeToMeal.meal = meal;
		meal.placeToMeal.place = place;
		
		for (Food food : foods) {
			MealToFood m2f = new MealToFood();
			m2f.meal = meal;
			m2f.food = food;
			
			meal.addFood(m2f);
		}
		return meal;
	}
	//	private void addListItemForTagAtPosition(Tag tag, int position) {
	//		LinearLayout parent = getNewItemContainer();
	//		LinearLayout tagItem = (LinearLayout)getActivity().getLayoutInflater()
	//				.inflate(R.layout.search_view_tag_result_item, null);
	//
	//		parent.addView(tagItem);
	//
	//		final TextView name = (TextView)tagItem.findViewById(R.id.search_view_tag_result_item_name);
	//
	//		name.setText(tag.name);
	//
	//		final String tagId = tag.id;
	//		tagItem.setOnClickListener(new OnClickListener() {
	//
	//			@Override
	//			public void onClick(View v) {
	//				Intent detailIntent = new Intent(getActivity(), TagDetailActivity.class);
	//				detailIntent.putExtra(TagDetailActivity.TAG_EXTRA_KEY, tagId);
	//				//detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	//				startActivity(detailIntent);
	//			}
	//		});
	//	}
}
