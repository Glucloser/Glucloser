package com.nlefler.glucloser.dataSource

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.nlefler.glucloser.GlucloserApplication

import com.nlefler.glucloser.R
import com.nlefler.glucloser.models.BolusEvent
import com.nlefler.glucloser.models.Meal
import javax.inject.Inject
import com.nlefler.glucloser.ui.MealHistoryViewHolder

/**
 * Created by Nathan Lefler on 12/25/14.
 */
public class MealHistoryRecyclerAdapter(private var activity: Activity,
                                        private var bolusEvents: List<BolusEvent>?) :
        RecyclerView.Adapter<MealHistoryViewHolder>() {
    public fun setEvents(events: List<BolusEvent>) {
        this.bolusEvents = events
        notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MealHistoryViewHolder {
        val view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.meal_detail_card, viewGroup, false)

        // Setup view

        return MealHistoryViewHolder(view, activity)
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
            viewHolder.placeName.setText(GlucloserApplication.SharedApplication().getString(R.string.snack))
        }
        viewHolder.carbsValue.setText("${bolusEvent.carbs}")
        viewHolder.insulinValue.setText("${bolusEvent.insulin}")
    }

    override fun getItemCount(): Int {
        return this.bolusEvents!!.size
    }
}
