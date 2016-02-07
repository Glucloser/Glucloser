package com.nlefler.glucloser.ui

import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.nlefler.glucloser.R
import com.nlefler.glucloser.components.datafactory.DaggerDataFactoryComponent
import com.nlefler.glucloser.components.datafactory.DataFactoryModule
import com.nlefler.glucloser.dataSource.FoodFactory
import com.nlefler.glucloser.dataSource.FoodListRecyclerAdapter
import com.nlefler.glucloser.models.parcelable.BolusEventParcelable
import com.nlefler.glucloser.models.Food
import java.util.*

public class HistoricalBolusDetailActivityFragment : Fragment() {
    var foodFactory: FoodFactory? = null

    private var placeName: String? = null
    private var bolusEventParcelable: BolusEventParcelable? = null

    private var placeNameField: TextView? = null
    private var carbValueField: TextView? = null
    private var insulinValueField: TextView? = null
    private var beforeSugarValueField: TextView? = null
    private var correctionValueBox: CheckBox? = null

    private var foodListView: RecyclerView? = null
    private var foodListLayoutManager: RecyclerView.LayoutManager? = null
    private var foodListAdapter: FoodListRecyclerAdapter? = null
    private var foods: MutableList<Food> = ArrayList()

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        val dataFactory = DaggerDataFactoryComponent.builder()
                .dataFactoryModule(DataFactoryModule())
                .build()
        foodFactory = dataFactory.foodFactory()

        this.bolusEventParcelable = getBolusEventParcelableFromBundle(bundle, arguments, activity.intent.extras)
        this.placeName = getPlaceNameFromBundle(bundle, arguments, activity.intent.extras)

        for (foodPar in this.bolusEventParcelable?.foodParcelables ?: ArrayList()) {
            foodFactory?.foodFromParcelable(foodPar)?.continueWith { task ->
                if (!task.isFaulted && task.result != null) {
                    this.foods.add(task.result!!)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_historical_bolus_detail, container, false)

        linkViews(rootView)
        setValuesInViews()

        return rootView
    }

    private fun linkViews(rootView: View) {
        this.placeNameField = rootView.findViewById(R.id.historical_bolus_detail_place_name) as TextView
        this.carbValueField = rootView.findViewById(R.id.historical_bolus_detail_total_carb_value) as TextView
        this.insulinValueField = rootView.findViewById(R.id.historical_bolus_detail_total_insulin_value) as TextView
        this.beforeSugarValueField = rootView.findViewById(R.id.historical_bolus_detail_blood_sugar_before_value) as TextView
        this.correctionValueBox = rootView.findViewById(R.id.historical_bolus_detail_correction_value) as CheckBox

        this.foodListView = rootView.findViewById(R.id.historical_bolus_detail_food_list) as RecyclerView

        this.foodListLayoutManager = LinearLayoutManager(activity)
        this.foodListView?.layoutManager = this.foodListLayoutManager
        this.foodListView?.addItemDecoration(DividerItemDecoration(activity))
    }

    private fun setValuesInViews() {
        this.placeNameField?.text = this.placeName ?: ""
        this.carbValueField?.text = "${this.bolusEventParcelable?.carbs}"
        this.insulinValueField?.text = "${this.bolusEventParcelable?.insulin}"
        this.beforeSugarValueField?.text = "${this.bolusEventParcelable?.bloodSugarParcelable?.value}"
        this.correctionValueBox?.isChecked = this.bolusEventParcelable?.isCorrection ?: false

        this.foodListAdapter = FoodListRecyclerAdapter(this.foods)
        this.foodListView?.adapter = this.foodListAdapter
    }

    private fun getPlaceNameFromBundle(vararg bundles: Bundle?): String {
        for (bundle in bundles) {
            if (bundle?.containsKey(HistoricalBolusDetailPlaceNameBundleKey) ?: false) {
                return bundle?.getString(HistoricalBolusDetailPlaceNameBundleKey) ?: ""
            }
        }

        return ""
    }

    private fun getBolusEventParcelableFromBundle(vararg bundles: Bundle?): BolusEventParcelable? {
        for (bundle in bundles) {
            if (bundle?.containsKey(HistoricalBolusEventBolusDetailParcelableBundleKey) ?: false) {
                return bundle?.getParcelable<Parcelable>(HistoricalBolusEventBolusDetailParcelableBundleKey) as BolusEventParcelable?
            }
        }
        return null
    }

    companion object {
        public val HistoricalBolusDetailPlaceNameBundleKey: String = "HistoricalBolusDetailPlaceNameBundleKey"
        public val HistoricalBolusEventBolusDetailParcelableBundleKey: String = "HistoricalBolusDetailBolusEventParcelableBundleKey"
    }
}
