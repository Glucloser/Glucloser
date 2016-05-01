package com.nlefler.glucloser.a.dataSource

import bolts.Continuation
import bolts.Task
import com.nlefler.glucloser.a.dataSource.jsonAdapter.*
import com.nlefler.glucloser.a.dataSource.jsonAdapter.SnackJsonAdapter
import com.nlefler.glucloser.a.db.DBManager
import com.nlefler.glucloser.a.models.BloodSugar
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.Snack
import com.nlefler.glucloser.a.models.parcelable.SnackParcelable

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import io.realm.Realm
import io.realm.RealmObject
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

    public fun snack(): Task<Snack?> {
        return snackForSnackId(UUID.randomUUID().toString(), true)
    }

    public fun fetchSnack(id: String): Task<Snack?> {
        return snackForSnackId(id, false)
    }

    public fun parcelableFromSnack(snack: Snack): SnackParcelable {
        val parcelable = SnackParcelable()
        parcelable.carbs = snack.carbs
        parcelable.insulin = snack.insulin
        parcelable.id = snack.primaryId
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
        val realm = dbManager.defaultRealm()
        return Moshi.Builder()
                .add(SnackJsonAdapter(realm))
                .build()
                .adapter(Snack::class.java)
    }

    public fun snackFromParcelable(parcelable: SnackParcelable): Task<Snack?> {

        var beforeSugarTask: Task<BloodSugar?>? = Task.forResult(null)
        if (parcelable.bloodSugarParcelable != null) {
            beforeSugarTask = bloodSugarFactory.bloodSugarFromParcelable(parcelable.bloodSugarParcelable!!)
        }

        val snackTask = snackForSnackId(parcelable.id, true)
        return Task.whenAll(arrayListOf(beforeSugarTask, snackTask))
                .continueWithTask(Continuation<Void, Task<Snack?>> whenAll@ { task ->
                    if (task.isFaulted) {
                        return@whenAll Task.forError(task.error)
                    }

                    val snack = snackTask.result
                    val sugar = beforeSugarTask?.result

                    return@whenAll dbManager.executeTransaction(object : DBManager.Tx<Snack?> {
                        override fun dependsOn(): List<RealmObject?> {
                            return listOf(sugar, snack)
                        }

                        override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Snack? {
                            val liveSugar = dependsOn.first() as BloodSugar?
                            val liveSnack = dependsOn.last() as Snack?
                            liveSnack?.insulin = parcelable.insulin
                            liveSnack?.carbs = parcelable.carbs
                            liveSnack?.isCorrection = parcelable.isCorrection
                            liveSnack?.beforeSugar = liveSugar
                            liveSnack?.date = parcelable.date
                            return liveSnack
                        }
                    })
                })
    }

    private fun snackForSnackId(id: String, create: Boolean): Task<Snack?> {
        return dbManager.executeTransaction(object: DBManager.Tx<Snack?> {
            override fun dependsOn(): List<RealmObject?> {
                return emptyList()
            }

            override fun execute(dependsOn: List<RealmObject?>, realm: Realm): Snack? {
                if (create && id.isEmpty()) {
                    val snack = realm.createObject<Snack>(Snack::class.java)
                    snack?.primaryId = UUID.randomUUID().toString()
                    snack?.date = Date()
                    return snack
                }

                val query = realm.where<Snack>(Snack::class.java)

                query?.equalTo(Snack.PrimaryKeyName, id)
                var snack: Snack? = query?.findFirst()

                if (snack == null && create) {
                    snack = realm.createObject<Snack>(Snack::class.java)
                    snack?.primaryId = id
                    snack?.date = Date()
                }
                return snack
            }
        })
    }
}
