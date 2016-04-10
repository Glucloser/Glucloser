package com.nlefler.glucloser.a.models.json

import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.Food
import java.util.*

/**
 * Created by nathan on 1/31/16.
 */
data class SnackJson(
        val primaryId: String,
        val date: Date,
        val bolusPattern: BolusPatternJson?,
        val carbs: Int,
        val insulin: Float,
        val beforeSugar: BloodSugarJson?,
        val isCorrection: Boolean,
        val foods: List<FoodJson>
    ) {
}
