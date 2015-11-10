package com.nlefler.glucloser.components.datafactory

import com.nlefler.glucloser.actions.StartupAction
import com.nlefler.glucloser.activities.LogBolusEventActivity
import com.nlefler.glucloser.dataSource.*
import com.nlefler.glucloser.ui.MealHistoryViewHolder
import com.nlefler.glucloser.ui.PlaceSelectionViewHolder
import dagger.Component
import javax.inject.Singleton

/**
 * Created by nathan on 10/20/15.
 */
@Singleton
@Component(modules = arrayOf(DataFactoryModule::class))
public interface DataFactoryComponent {
    public fun inject(client: BloodSugarFactory)
    public fun inject(client: BolusEventFactory)
    public fun inject(client: BolusPatternFactory)
    public fun inject(client: BolusRateFactory)
    public fun inject(client: FoodFactory)
    public fun inject(client: MealFactory)
    public fun inject(client: PlaceFactory)
    public fun inject(client: SnackFactory)
    public fun inject(client: LogBolusEventActivity)
    public fun inject(client: StartupAction)
    public fun inject(client: ParseUploader)
    public fun inject(client: MealHistoryViewHolder)
    public fun inject(client: PlaceSelectionViewHolder)

    fun bloodSugarFactory(): BloodSugarFactory
    fun bolusEventFactory(): BolusEventFactory
    fun bolusPatternFactory(): BolusPatternFactory
    fun bolusRateFactory(): BolusRateFactory
    fun foodFactory(): FoodFactory
    fun mealFactory(): MealFactory
    fun placeFactory(): PlaceFactory
    fun snackFactory(): SnackFactory

    @Singleton
    fun parseUploader(): ParseUploader
}
