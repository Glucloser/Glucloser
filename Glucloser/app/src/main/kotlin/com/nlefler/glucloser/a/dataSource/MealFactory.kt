package com.nlefler.glucloser.a.dataSource

import com.nlefler.glucloser.a.dataSource.jsonAdapter.MealJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.parcelable.MealParcelable
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.requery.kotlin.eq
import io.requery.query.Result
import rx.Observable
import java.util.*

import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/24/14.
 */
class MealFactory @Inject constructor(val dbManager: DBManager,
                                      val bolusPatternFactory: BolusPatternFactory,
                                      val bloodSugarFactory: BloodSugarFactory,
                                      val placeFactory: PlaceFactory,
                                      val foodFactory: FoodFactory) {
    private val LOG_TAG = "MealFactory"

    fun save(meal: Meal) {
        meal.needsUpload = true
        dbManager.data.upsert(meal)
    }

    fun parcelableFrom(meal: Meal): MealParcelable {
        if (meal is MealParcelable) {
            return meal
        }
        val parcelable = MealParcelable(placeFactory, bloodSugarFactory, bolusPatternFactory, foodFactory)
        val place = meal.place
        if (place != null) {
            parcelable.place = placeFactory.parcelableFrom(place)
        }
        parcelable.carbs = meal.carbs
        parcelable.insulin = meal.insulin
        parcelable.primaryId = meal.primaryId
        parcelable.isCorrection = meal.isCorrection

        val beforeSugar = meal.beforeSugar
        if (beforeSugar != null) {
            parcelable.beforeSugar = bloodSugarFactory.parcelableFromBloodSugar(beforeSugar)
        }
        parcelable.eatenDate = meal.eatenDate

        val bolusPattern = meal.bolusPattern
        if (bolusPattern != null) {
            parcelable.bolusPattern = bolusPatternFactory.parcelableFrom(bolusPattern)
        }
        parcelable.foods = meal.foods.map { f -> foodFactory.parcelableFrom(f) }.toMutableSet()

        return parcelable
    }

    fun jsonAdapter(): JsonAdapter<MealEntity> {
        return Moshi.Builder()
                .add(MealJsonAdapter())
                .build()
                .adapter(MealEntity::class.java)
    }

    fun entityFrom(meal: Meal): MealEntity {
        if (meal is MealEntity) {
            return meal
        }
        val entity = MealEntity()
        entity.primaryId = meal.primaryId
        entity.carbs = meal.carbs
        entity.insulin = meal.insulin
        entity.isCorrection = meal.isCorrection
        entity.eatenDate = meal.eatenDate

        val beforeSugar = meal.beforeSugar
        if (beforeSugar != null) {
            entity.beforeSugar = bloodSugarFactory.entityFrom(beforeSugar)
        }

        val bolusPattern = meal.bolusPattern
        if (bolusPattern != null) {
            entity.bolusPattern = bolusPatternFactory.entityFrom(bolusPattern)
        }

        val place = meal.place
        if (place != null) {
            entity.place = placeFactory.entityFrom(place)
        }

        entity.foods.addAll(meal.foods.map {f -> foodFactory.entityFrom(f) })

        return entity
    }

    private fun mealForMealId(id: String): Observable<Result<MealEntity>> {
        if (id.isEmpty()) {
            return Observable.error(Exception("Invalid Id"))
        }
        return dbManager.data.select(MealEntity::class).where(Meal::primaryId.eq(id)).get().toSelfObservable()
    }
}
