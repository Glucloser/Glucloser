package com.nlefler.glucloser.a.ui

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
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.data.ChartData
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
//import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.FoodFactory
import com.nlefler.glucloser.a.dataSource.FoodListRecyclerAdapter
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.SensorReading
import com.nlefler.glucloser.a.models.parcelable.MealParcelable
import rx.Observer
import rx.Scheduler
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.ArrayList

class HistoricalBolusDetailActivityFragment : Fragment() {
    var foodFactory: FoodFactory? = null
    var pumpDataFactory: PumpDataFactory? = null

    private var placeName: String? = null
    private var mealParcelable: MealParcelable? = null

    private var placeNameField: TextView? = null
    private var carbValueField: TextView? = null
    private var insulinValueField: TextView? = null
    private var beforeSugarValueField: TextView? = null
    private var correctionValueBox: CheckBox? = null

//    private var sensorChart: LineChart? = null

    private var foodListView: RecyclerView? = null
    private var foodListLayoutManager: RecyclerView.LayoutManager? = null
    private var foodListAdapter: FoodListRecyclerAdapter? = null
    private var foods: MutableList<Food> = ArrayList()

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        foodFactory = dataFactory?.foodFactory()
        pumpDataFactory = dataFactory?.pumpDataFactory()

        this.mealParcelable = getBolusEventParcelableFromBundle(bundle, arguments, activity.intent.extras)
        this.placeName = getPlaceNameFromBundle(bundle, arguments, activity.intent.extras)

        for (foodPar in this.mealParcelable?.foodParcelables ?: ArrayList()) {
            // TODO(nl) make food
            val food = foodFactory?.foodFromParcelable(foodPar)
            if (food != null) {
                this.foods.add(food)
                this.foodListAdapter?.setFoods(this.foods)
                this.foodListAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.fragment_historical_bolus_detail, container, false)

        linkViews(rootView)
        setValuesInViews()
        loadSensorValues()

        return rootView
    }

    private fun linkViews(rootView: View) {
        this.placeNameField = rootView.findViewById(R.id.historical_bolus_detail_place_name) as TextView
        this.carbValueField = rootView.findViewById(R.id.historical_bolus_detail_total_carb_value) as TextView
        this.insulinValueField = rootView.findViewById(R.id.historical_bolus_detail_total_insulin_value) as TextView
        this.beforeSugarValueField = rootView.findViewById(R.id.historical_bolus_detail_blood_sugar_before_value) as TextView
        this.correctionValueBox = rootView.findViewById(R.id.historical_bolus_detail_correction_value) as CheckBox

//        sensorChart = rootView.findViewById(R.id.historical_bolus_detail_sensor_chart) as LineChart

        this.foodListView = rootView.findViewById(R.id.historical_bolus_detail_food_list) as RecyclerView

        this.foodListLayoutManager = LinearLayoutManager(activity)
        this.foodListView?.layoutManager = this.foodListLayoutManager
        this.foodListView?.addItemDecoration(DividerItemDecoration(activity))
    }

    private fun setValuesInViews() {
        this.placeNameField?.text = this.placeName ?: ""
        this.carbValueField?.text = "${this.mealParcelable?.carbs}"
        this.insulinValueField?.text = "${this.mealParcelable?.insulin}"
        this.beforeSugarValueField?.text = "${this.mealParcelable?.bloodSugarParcelable?.value}"
        this.correctionValueBox?.isChecked = this.mealParcelable?.isCorrection ?: false

        this.foodListAdapter = FoodListRecyclerAdapter(this.foods)
        this.foodListView?.adapter = this.foodListAdapter
        this.foodListAdapter?.setFoods(this.foods)
        this.foodListAdapter?.notifyDataSetChanged()
    }

    private fun loadSensorValues() {
//        val dataList = ArrayList<Entry>()
//        var minReading = Float.MAX_VALUE
//        var maxReading = Float.MIN_VALUE
//
//        val date = mealParcelable?.date ?: return
//        pumpDataFactory?.sensorReadingsAfter(date)
//                ?.subscribeOn(Schedulers.newThread())
//                ?.observeOn(AndroidSchedulers.mainThread())
//                ?.subscribe(object: Observer<SensorReading> {
//                    override fun onNext(reading: SensorReading) {
//                        val fReading = reading.reading.toFloat()
//                        dataList.add(Entry(fReading, dataList.count() + 1))
//                        minReading = if (fReading < minReading) fReading else minReading
//                        maxReading = if (fReading > maxReading) fReading else maxReading
//                    }
//
//                    override fun onCompleted() {
//                        val dataSet = LineDataSet(dataList, getString(R.string.sensor_readings_chart_label))
//                        dataSet.lineWidth = 2f
//                        dataSet.setDrawCircles(false)
//                        dataSet.color = R.color.bright_foreground_material_dark
//
//                        sensorChart?.data = LineData(ChartData.generateXVals(0, dataSet.entryCount), listOf(dataSet))
//                        sensorChart?.setDescription(getString(R.string.sensor_readings_chart_label))
//
//                        sensorChart?.axisLeft?.setAxisMinValue(minReading)
//                        sensorChart?.axisLeft?.setAxisMaxValue(maxReading)
//                        sensorChart?.axisRight?.isEnabled = false
//                        sensorChart?.invalidate()
//                    }
//
//                    override fun onError(t: Throwable) {
//
//                    }
//                })


    }

    private fun getPlaceNameFromBundle(vararg bundles: Bundle?): String {
        for (bundle in bundles) {
            if (bundle?.containsKey(HistoricalBolusDetailActivityFragment.Companion.HistoricalBolusDetailPlaceNameBundleKey) ?: false) {
                return bundle?.getString(HistoricalBolusDetailActivityFragment.Companion.HistoricalBolusDetailPlaceNameBundleKey) ?: ""
            }
        }

        return ""
    }

    private fun getBolusEventParcelableFromBundle(vararg bundles: Bundle?): MealParcelable? {
        for (bundle in bundles) {
            if (bundle?.containsKey(HistoricalBolusDetailActivityFragment.Companion.HistoricalBolusEventBolusDetailParcelableBundleKey) ?: false) {
                return bundle?.getParcelable<Parcelable>(HistoricalBolusDetailActivityFragment.Companion.HistoricalBolusEventBolusDetailParcelableBundleKey) as MealParcelable?
            }
        }
        return null
    }

    companion object {
        public val HistoricalBolusDetailPlaceNameBundleKey: String = "HistoricalBolusDetailPlaceNameBundleKey"
        public val HistoricalBolusEventBolusDetailParcelableBundleKey: String = "HistoricalBolusDetailBolusEventParcelableBundleKey"
    }
}
