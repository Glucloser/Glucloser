package com.nlefler.glucloser.dataSource

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nlefler.glucloser.R
import com.nlefler.glucloser.models.Food
import java.util.ArrayList

/**
 * Created by Nathan Lefler on 5/16/15.
 */
public class FoodListRecyclerAdapter(private var foods: List<Food>) : RecyclerView.Adapter<FoodListRecyclerAdapter.ViewHolder>() {


    init {
        this.foods = ArrayList<Food>()
    }

    public class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var food: Food? = null
        internal var foodName: TextView
        internal var carbsValue: TextView

        init {
            this.foodName = itemView.findViewById(R.id.food_list_item_name) as TextView
            this.carbsValue = itemView.findViewById(R.id.food_list_item_carbs) as TextView
        }
    }

    public fun setFoods(foods: List<Food>) {
        this.foods = foods
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_list_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        if (i >= this.foods.size()) {
            return
        }

        val food = this.foods.get(i)
        viewHolder.food = food
        viewHolder.foodName.setText("${food.name}")
        viewHolder.carbsValue.setText("${food.carbs}")
    }

    override fun getItemCount(): Int {
        return this.foods.size()
    }
}
