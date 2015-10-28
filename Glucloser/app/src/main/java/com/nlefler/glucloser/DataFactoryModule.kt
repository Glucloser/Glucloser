package com.nlefler.glucloser

import com.nlefler.glucloser.dataSource.*
import dagger.Module
import dagger.Provides
import io.realm.Realm
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Module
public class DataFactoryModule : DataFactoryComponent {
    @Provides override public fun realm(): Realm {
        return Realm.getDefaultInstance()
    }

    @Provides override fun bloodSugarFactory(): BloodSugarFactory {
        return BloodSugarFactory()
    }

    @Provides override fun bolusEventFactory(): BolusEventFactory {
        return BolusEventFactory()
    }

    @Provides override fun bolusPatternFactory(): BolusPatternFactory {
        return BolusPatternFactory()
    }

    @Provides override fun bolusRateFactory(): BolusRateFactory {
        return BolusRateFactory()
    }

    @Provides override fun foodFactory(): FoodFactory {
        return FoodFactory()
    }

    @Provides override fun mealFactory(): MealFactory {
        return MealFactory()
    }

    @Provides override fun placeFactory(): PlaceFactory {
        return PlaceFactory()
    }

    @Provides override fun snackFactory(): SnackFactory {
        return SnackFactory()
    }
}
