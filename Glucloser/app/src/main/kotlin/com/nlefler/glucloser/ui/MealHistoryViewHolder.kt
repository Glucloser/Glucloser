package com.nlefler.glucloser.ui

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.nlefler.glucloser.models.BolusEvent
import com.nlefler.glucloser.R
import com.nlefler.glucloser.dataSource.BolusEventFactory
import javax.inject.Inject

/**
 * Created by nathan on 10/27/15.
 */
public class MealHistoryViewHolder(itemView: View, activity: Activity) : RecyclerView.ViewHolder(itemView) {
    @Inject var bolusEventFactory: BolusEventFactory? = null

    var bolusEvent: BolusEvent? = null
    var placeName: TextView
    var carbsValue: TextView
    var insulinValue: TextView
    var clickListener: View.OnClickListener

    init{

        this.placeName = itemView.findViewById(R.id.meal_detail_card_place_name) as TextView
        this.carbsValue = itemView.findViewById(R.id.meal_detail_card_carbs_value) as TextView
        this.insulinValue = itemView.findViewById(R.id.meal_detail_card_insulin_value) as TextView
        this.clickListener = object : View.OnClickListener {
            override fun onClick(view: View) {
                if (bolusEvent == null) {
                    return
                }
                val bolusEventParcelable = bolusEventFactory?.parcelableFromBolusEvent(bolusEvent!!)
                if (bolusEventParcelable == null) {
                    return
                }

                val intent = android.content.Intent(view.getContext(), com.nlefler.glucloser.HistoricalBolusDetailActivity::class.java)
                intent.putExtra(com.nlefler.glucloser.HistoricalBolusDetailActivity.BolusKey, bolusEventParcelable)

                activity.startActivity(intent)
            }
        }
        itemView.setOnClickListener(this.clickListener)
    }
}
