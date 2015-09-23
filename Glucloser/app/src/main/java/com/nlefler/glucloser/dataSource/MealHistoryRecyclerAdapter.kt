package com.nlefler.glucloser.dataSource

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nlefler.glucloser.HistoricalBolusDetailActivity
import com.nlefler.glucloser.GlucloserApplication

import com.nlefler.glucloser.R
import com.nlefler.glucloser.models.BolusEvent
import com.nlefler.glucloser.models.BolusEventParcelable
import com.nlefler.glucloser.models.Meal

/**
 * Created by Nathan Lefler on 12/25/14.
 */
public class MealHistoryRecyclerAdapter(private var activity: Activity,
                                        private var bolusEvents: List<BolusEvent>?) :
        RecyclerView.Adapter<MealHistoryRecyclerAdapter.ViewHolder>() {

    public inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var bolusEvent: BolusEvent? = null
        internal var placeName: TextView
        internal var carbsValue: TextView
        internal var insulinValue: TextView
        internal var clickListener: View.OnClickListener

        init{

            this.placeName = itemView.findViewById(R.id.meal_detail_card_place_name) as TextView
            this.carbsValue = itemView.findViewById(R.id.meal_detail_card_carbs_value) as TextView
            this.insulinValue = itemView.findViewById(R.id.meal_detail_card_insulin_value) as TextView
            this.clickListener = object : View.OnClickListener {
                override fun onClick(view: View) {
                    if (bolusEvent == null) {
                        return
                    }
                    val bolusEventParcelable = BolusEventFactory.ParcelableFromBolusEvent(bolusEvent!!)
                    if (bolusEventParcelable == null) {
                        return
                    }

                    val intent = Intent(view.getContext(), HistoricalBolusDetailActivity::class.java)
                    intent.putExtra(HistoricalBolusDetailActivity.BolusKey, bolusEventParcelable)

                    activity.startActivity(intent)
                }
            }
            itemView.setOnClickListener(this.clickListener)
        }
    }

    public fun setEvents(events: List<BolusEvent>) {
        this.bolusEvents = events
        notifyDataSetChanged()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.meal_detail_card, viewGroup, false)

        // Setup view

        return ViewHolder(view)
    }

    // Replaces the contents of a view (invoked by the view holder)
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        if (i >= this.bolusEvents!!.size()) {
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
        return this.bolusEvents!!.size()
    }
}
