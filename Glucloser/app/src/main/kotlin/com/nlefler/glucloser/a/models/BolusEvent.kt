package com.nlefler.glucloser.a.models

import java.util.Date

import io.realm.RealmList

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public interface BolusEvent {
    var primaryId: String
    var date: Date
    var bolusPattern: BolusPattern?
    var carbs: Int
    var insulin: Float
    var beforeSugar: BloodSugar?
    var isCorrection: Boolean
    var foods: RealmList<Food>
}
