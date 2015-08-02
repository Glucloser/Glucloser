package com.nlefler.glucloser.models

import com.nlefler.glucloser.dataSource.MealHistoryRecyclerAdapter

/**
 * Created by Nathan on 8/2/2015.
 */
public interface MealHistoryRecyclerAdapterDelegate {
    public fun mealHistoryAdapterDidSelectBolusEvent(mealAdapter: MealHistoryRecyclerAdapter, bolusEvent: BolusEvent)
}
