package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.dataSource.jsonAdapter.SnackJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.BolusPattern
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.Snack
import com.nlefler.glucloser.a.models.parcelable.SnackParcelable

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import io.requery.kotlin.eq
import io.requery.kotlin.invoke
import rx.Observable
import java.util.*
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 4/25/15.
 */
public class SnackFactory @Inject constructor(val dbManager: DBManager,
                                              val bloodSugarFactory: BloodSugarFactory,
                                              val bolusPatternFactory: BolusPatternFactory,
                                              val foodFactory: FoodFactory) {
    private val LOG_TAG = "SnackFactory"

    public fun fetchSnack(id: String): Observable<Snack> {
        return snackForSnackId(id)
    }

    public fun parcelableFromSnack(snack: Snack): SnackParcelable {
        val parcelable = SnackParcelable()
        parcelable.carbs = snack.carbs
        parcelable.insulin = snack.insulin
        parcelable.primaryId = snack.primaryId
        parcelable.isCorrection = snack.isCorrection
        if (snack.beforeSugar != null) {
            parcelable.bloodSugarParcelable = bloodSugarFactory.parcelableFromBloodSugar(snack.beforeSugar!!)
        }
        parcelable.date = snack.date
        if (snack.bolusPattern != null) {
            parcelable.bolusPatternParcelable = bolusPatternFactory.parcelableFromBolusPattern(snack.bolusPattern!!)
        }
        snack.foods.forEach {food ->
            parcelable.foodParcelables.add(foodFactory.parcelableFromFood(food))
        }

        return parcelable
    }

    fun jsonAdapter(): JsonAdapter<Snack> {
        return Moshi.Builder()
                .add(SnackJsonAdapter())
                .build()
                .adapter(Snack::class.java)
    }

    public fun snackFromParcelable(parcelable: SnackParcelable): Snack {

        val sugarPar = parcelable.bloodSugarParcelable
        var beforeSugar: BloodSugar? = null
        if (sugarPar != null) {
            beforeSugar = bloodSugarFactory.bloodSugarFromParcelable(parcelable.bloodSugarParcelable!!)
        }

        val patternPar = parcelable.bolusPatternParcelable
        var pattern: BolusPattern? = null
        if (patternPar != null) {
            pattern = bolusPatternFactory.bolusPatternFromParcelable(patternPar)
        }

        var foods = ArrayList<Food>()
        parcelable.foodParcelables.forEach { par ->
            foods.add(foodFactory.foodFromParcelable(par))
        }

        return Snack(parcelable.primaryId, parcelable.date, pattern, parcelable.carbs,
                parcelable.insulin, beforeSugar, parcelable.isCorrection,
                foods)
    }

    private fun snackForSnackId(id: String): Observable<Snack> {
        return Observable.create { s ->
            dbManager.data {
                val result = select(Snack::class) where (Snack::primaryId eq id)
                val snack = result.invoke().firstOrNull()
                if (snack != null) {
                    s.onNext(snack)
                }
                else {
                    // TODO(nl) no id error
                    s.onError(Error())
                }
                s.onCompleted()
            }
        }
    }
}
