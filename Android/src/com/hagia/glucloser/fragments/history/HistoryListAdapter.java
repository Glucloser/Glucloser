package com.hagia.glucloser.fragments.history;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hagia.glucloser.detail.MealDetailActivity;
import com.hagia.glucloser.types.Meal;
import com.hagia.glucloser.util.MealUtil;
import com.hagia.glucloser.util.RequestIdUtil;
import com.hagia.glucloser.R;

public class HistoryListAdapter extends BaseAdapter implements ListAdapter {
	private static final String LOG_TAG = "Pump_History_List_Adapter";

	private AsyncTask<Integer, List<Meal>, List<Meal>> dataSource;

	private List<Meal> meals;
	private Context context;
	private LayoutInflater inflater;

	public HistoryListAdapter(Context c, LayoutInflater l) {
		// TODO Correctly handle no max meals
		this(c, l, 1000);
	}

	public HistoryListAdapter(Context c, LayoutInflater l, int maxMeals) {
		context = c;
		inflater = l;
		meals = new ArrayList<Meal>();

		dataSource = new AsyncTask<Integer, List<Meal>, List<Meal>>() {
			@Override
			protected List<Meal> doInBackground(Integer... maxMealsToReturn) {
				return MealUtil.getRecentMeals(maxMealsToReturn[0]);
			}		

			@Override
			protected void onPostExecute(List<Meal> result) {
				meals = result;
				notifyDataSetChanged();

				super.onPostExecute(result);
			}

		};
		dataSource.execute(maxMeals);
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
		Meal meal = meals.get(position);
		if (meal != null) {
			return meal.hashCode();
		}
		return 0;
	}

	@Override
	public View getView(int position, View reuseView, ViewGroup parent) {
		final Meal meal = meals.get(position);

		LinearLayout theView = null;
		final long requestId = RequestIdUtil.getNewId();

		if (reuseView == null || reuseView.getId() != R.layout.history_list_item) {
			theView = (LinearLayout)inflater.inflate(R.layout.history_list_item, null);
		} else {
			theView = (LinearLayout) reuseView;
		}


		TextView name = (TextView)theView.findViewById(R.id.history_list_item_name);
		TextView time = (TextView)theView.findViewById(R.id.history_list_item_time);
		theView.setTag(requestId);

		if (meal.placeToMeal == null ||
				meal.placeToMeal.place == null) {
			meal.linkPlace();
			// TODO remove
			if (meal.placeToMeal == null ||
					meal.placeToMeal.place == null) {
				return theView;
			}
		}
		name.setText(meal.placeToMeal.place.name);
		time.setText(meal.getDateEatenForDisplay());

		theView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (requestId == (Long)v.getTag()) {
					Intent detailIntent = new Intent(context, MealDetailActivity.class);
					detailIntent.putExtra(MealDetailActivity.MEAL_KEY, meal);
					context.startActivity(detailIntent);
				}
			}
		});

		return theView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		if (position >= meals.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}

		Meal meal = meals.get(position);

		return Meal.verifyMeal(meal);
	}
}
