package com.nlefler.glucloser.fragments.home.listItems;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nlefler.glucloser.R;
import com.nlefler.glucloser.model.food.Food;
import com.nlefler.glucloser.model.food.FoodUtil;
import com.nlefler.glucloser.model.meal.Meal;
import com.nlefler.glucloser.model.MealToFood;
import com.nlefler.glucloser.model.place.Place;
import com.nlefler.glucloser.model.meal.MealUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by lefler on 11/3/13.
 */
public class HistoricMealItem implements HomeListItem {
    private Meal meal;
    private Place place;

    private Comparator<Place> placeDistanceComparator = null;

    public HistoricMealItem(Meal meal) {
        this.meal = meal;
        this.place = this.meal.linkPlace().placeToMeal.place;
    }

    public Meal getMeal() { return meal; }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public long getItemId() {
        return meal.hashCode() * 31 + place.hashCode() * 31;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
		if (meal == null) {
			return null;
		}

        LinearLayout theView = null;

		if (convertView == null || convertView.getId() != R.layout.home_line_item) {
			theView = (LinearLayout)inflater.inflate(R.layout.home_line_item, null);
		} else {
			theView = (LinearLayout) convertView;
			if (((Double)theView.getTag()) == meal.glucloserId.hashCode()) {
				return theView;
			}
		}
		LinearLayout mealItem = (LinearLayout)inflater.inflate(
				R.layout.popular_meal_line_item, null);
		theView.setTag(meal.glucloserId.hashCode());
        theView.addView(mealItem);

		TextView placeName = (TextView) mealItem.findViewById(R.id.popular_meal_place_name);
		// TODO: Stop using the for the date
		TextView placeAddress = (TextView) mealItem.findViewById(R.id.popular_meal_place_address);
		LinearLayout foodsLayout = (LinearLayout) mealItem.findViewById(R.id.popular_meal_foods_container);

		placeName.setText(place.name);
		placeAddress.setText(meal.getDateEatenForDisplay());

		List<Food> foods = new ArrayList<Food>();
		// TODO: Is this running in a thread?
		for (Food food : MealUtil.getFoodsForMeal(meal)) {
			foods.add(food);

			RelativeLayout foodLayout = (RelativeLayout) inflater.inflate(
					R.layout.food_line_item, null);
			TextView foodName = (TextView) foodLayout.findViewById(R.id.food_line_item_food_name);
			TextView carbsValue = (TextView) foodLayout.findViewById(R.id.food_line_item_carbs_value);

			foodName.setText(food.name);
			carbsValue.setText(String.valueOf(food.carbs));
			foodsLayout.addView(foodLayout);
		}

		return theView;
    }
}
