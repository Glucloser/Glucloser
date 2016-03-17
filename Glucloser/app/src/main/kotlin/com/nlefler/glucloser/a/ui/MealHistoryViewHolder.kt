package com.nlefler.glucloser.a.ui

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.nlefler.glucloser.a.models.BolusEvent
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.activities.HistoricalBolusDetailActivity
import com.nlefler.glucloser.a.dataSource.BolusEventFactory
import com.nlefler.glucloser.a.models.HasPlace
import com.nlefler.glucloser.a.models.Snack
import javax.inject.Inject

/**
 * Created by nathan on 10/27/15.
 */
class MealHistoryViewHolder @Inject constructor(itemView: View, activity: Activity, bolusEventFactory: BolusEventFactory)
    : RecyclerView.ViewHolder(itemView) {

    var bolusEvent: BolusEvent? = null
    var placeName: TextView
    var carbsValue: TextView
    var insulinValue: TextView
    var clickListener: View.OnClickListener

    init {

        this.placeName = itemView.findViewById(R.id.meal_detail_card_place_name) as TextView
        this.carbsValue = itemView.findViewById(R.id.meal_detail_card_carbs_value) as TextView
        this.insulinValue = itemView.findViewById(R.id.meal_detail_card_insulin_value) as TextView
        this.clickListener = View.OnClickListener { view ->
            val bolusEventCopy = bolusEvent
            if (bolusEventCopy == null) {
                return@OnClickListener
            }
            val bolusEventParcelable = bolusEventFactory.parcelableFromBolusEvent(bolusEventCopy) ?: return@OnClickListener

            val intent = android.content.Intent(view.context, HistoricalBolusDetailActivity::class.java)
            intent.putExtra(HistoricalBolusDetailActivityFragment.HistoricalBolusEventBolusDetailParcelableBundleKey,
                    bolusEventParcelable)
            if (bolusEventCopy is HasPlace) {
                intent.putExtra(HistoricalBolusDetailActivityFragment.HistoricalBolusDetailPlaceNameBundleKey,
                        bolusEventCopy.place?.name ?: "");
            }
            else if (bolusEventCopy is Snack) {
                intent.putExtra(HistoricalBolusDetailActivityFragment.HistoricalBolusDetailPlaceNameBundleKey, activity.getString(R.string.snack))
            }

            activity.startActivity(intent)
        }
        itemView.setOnClickListener(this.clickListener)
    }
}
