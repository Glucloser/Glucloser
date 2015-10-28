package com.nlefler.glucloser

import com.nlefler.glucloser.dataSource.*
import dagger.Component
import io.realm.Realm
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Component(modules = arrayOf(DataFactoryModule::class))
public interface DataFactoryComponent {
    public fun realm(): Realm
    public fun bloodSugarFactory(): BloodSugarFactory
    public fun bolusEventFactory(): BolusEventFactory
    public fun bolusPatternFactory(): BolusPatternFactory
    public fun bolusRateFactory(): BolusRateFactory
    public fun foodFactory(): FoodFactory
    public fun mealFactory(): MealFactory
    public fun placeFactory(): PlaceFactory
    public fun snackFactory(): SnackFactory
}
