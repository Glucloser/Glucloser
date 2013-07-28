package com.hagia.glucloser.detail;

import java.util.ArrayList;
import java.util.List;

import com.hagia.glucloser.types.MealToFood;
import com.hagia.glucloser.types.Place;
import com.hagia.glucloser.util.PlaceUtil;
import com.hagia.glucloser.types.Meal;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class PlaceDetailMealListAdapter extends BaseAdapter implements
		ListAdapter {
	private static final String LOG_TAG = "Pump_Place_Detail_Meal_List_Adapter";
	
	private List<Meal> meals;
	
	public PlaceDetailMealListAdapter(Place place) {
		meals = new ArrayList<Meal>();
		
		new AsyncTask<Place, Void, List<Meal>>() {

			@Override
			protected List<Meal> doInBackground(Place... params) {
				Place place = params[0];

				List<Meal>meals = PlaceUtil.getAllMealsForPlace(place);
				for (Meal meal : meals) {
					meal.linkFoods();
				}
				
				return meals;
			}

			@Override
			protected void onPostExecute(List<Meal> result) {
				meals.addAll(result);
				notifyDataSetChanged();
			}

		}.execute(place);
	}

	@Override
	public int getCount() {
		return meals.size();
	}

	@Override
	public Object getItem(int position) {
		return meals.get(position);
	}

	@Override
	public long getItemId(int position) {
		return meals.get(position).hashCode();
	}

	private static final int dateViewId = 4456;
	private static final int foodListId = 3245;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout mealLayout = (LinearLayout) convertView;
		if (mealLayout == null) {
			mealLayout = new LinearLayout(parent.getContext());
			mealLayout.setOrientation(LinearLayout.VERTICAL);
			TextView dateView = new TextView(parent.getContext());
			dateView.setId(dateViewId);
			dateView.setTextSize(22);
			mealLayout.addView(dateView);
			LinearLayout foodLayout = new LinearLayout(parent.getContext());
			foodLayout.setId(foodListId);
			foodLayout.setOrientation(LinearLayout.VERTICAL);
			mealLayout.addView(foodLayout);
		}

		final Meal meal = (Meal) getItem(position);
		
		String date = meal.getDateEatenForDisplay().toString();
		TextView dateView = (TextView) mealLayout.findViewById(dateViewId);
		dateView.setText(date);
		
		LinearLayout foodsList = (LinearLayout)mealLayout.findViewById(foodListId);
		foodsList.removeAllViews();
		for (MealToFood m2f : meal.mealToFoods) {
			TextView foodView = new TextView(mealLayout.getContext());
			foodView.setText(m2f.food.name);
			foodView.setTextSize(18);
			foodsList.addView(foodView);
		}
		
		final Context context = mealLayout.getContext();
		mealLayout.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent detailIntent = new Intent(context, MealDetailActivity.class);
				detailIntent.putExtra(MealDetailActivity.MEAL_KEY, meal);
				context.startActivity(detailIntent);
			}
		});
		
		return mealLayout;
	}
	
}
