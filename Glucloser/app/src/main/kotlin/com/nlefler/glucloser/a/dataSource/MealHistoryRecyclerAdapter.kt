package com.nlefler.glucloser.a.dataSource

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nlefler.glucloser.a.GlucloserApplication

import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.models.BolusEvent
import com.nlefler.glucloser.a.models.Meal
import com.nlefler.glucloser.a.ui.MealHistoryViewHolder

/**
 * Created by Nathan Lefler on 12/25/14.
 */
class MealHistoryRecyclerAdapter(private val activity: Activity,
                                 private var bolusEvents: List<BolusEvent>?,
                                 val bolusEventFactory: BolusEventFactory) :
        RecyclerView.Adapter<MealHistoryViewHolder>() {

    fun setEvents(events: List<BolusEvent>) {
        this.bolusEvents = events
        notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MealHistoryViewHolder {
        val view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.meal_detail_card, viewGroup, false)

        // Setup view

        val viewHolder = MealHistoryViewHolder(view, activity, bolusEventFactory)

        return viewHolder
    }

    // Replaces the contents of a view (invoked by the view holder)
    override fun onBindViewHolder(viewHolder: MealHistoryViewHolder, i: Int) {
        if (i >= this.bolusEvents!!.size) {
            return
        }

        val bolusEvent = this.bolusEvents!!.get(i)
        viewHolder.bolusEvent = bolusEvent
        if (bolusEvent is Meal) {
            viewHolder.placeName.setText(bolusEvent.place?.name ?: "")
        }
        else {
            var name = viewHolder.itemView.context.getString(R.string.snack)
            if (bolusEvent.foods.size == 1) {
                val firstFoodName = bolusEvent.foods.firstOrNull()?.foodName ?: ""
                if (firstFoodName.length > 0) {
                    name = firstFoodName
                }
            }

            viewHolder.placeName.text = name
        }
        viewHolder.carbsValue.text = "${bolusEvent.carbs}"
        viewHolder.insulinValue.text = "${bolusEvent.insulin}"
    }

    override fun getItemCount(): Int {
        return this.bolusEvents!!.size
    }
}
