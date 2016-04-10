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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.FoodFactory
import com.nlefler.glucloser.a.dataSource.FoodListRecyclerAdapter
import com.nlefler.glucloser.a.dataSource.PumpDataFactory
import com.nlefler.glucloser.a.models.parcelable.BolusEventParcelable
import com.nlefler.glucloser.a.models.Food
import java.util.*

class HistoricalBolusDetailActivityFragment : Fragment() {
    var foodFactory: FoodFactory? = null
    var pumpDataFactory: PumpDataFactory? = null

    private var placeName: String? = null
    private var bolusEventParcelable: BolusEventParcelable? = null

    private var placeNameField: TextView? = null
    private var carbValueField: TextView? = null
    private var insulinValueField: TextView? = null
    private var beforeSugarValueField: TextView? = null
    private var correctionValueBox: CheckBox? = null

    private var sensorChart: LineChart? = null

    private var foodListView: RecyclerView? = null
    private var foodListLayoutManager: RecyclerView.LayoutManager? = null
    private var foodListAdapter: FoodListRecyclerAdapter? = null
    private var foods: MutableList<Food> = ArrayList()

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        foodFactory = dataFactory?.foodFactory()
        pumpDataFactory = dataFactory?.pumpDataFactory()

        this.bolusEventParcelable = getBolusEventParcelableFromBundle(bundle, arguments, activity.intent.extras)
        this.placeName = getPlaceNameFromBundle(bundle, arguments, activity.intent.extras)

        for (foodPar in this.bolusEventParcelable?.foodParcelables ?: ArrayList()) {
            foodFactory?.foodFromParcelable(foodPar)?.continueWith { task ->
                if (!task.isFaulted && task.result != null) {
                    this.foods.add(task.result!!)
                    this.foodListAdapter?.setFoods(this.foods)
                    this.foodListAdapter?.notifyDataSetChanged()
                }
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

        sensorChart = rootView.findViewById(R.id.historical_bolus_detail_sensor_chart) as LineChart

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
        this.foodListAdapter?.setFoods(this.foods)
        this.foodListAdapter?.notifyDataSetChanged()
    }

    private fun loadSensorValues() {
        val date = bolusEventParcelable?.date ?: return
        pumpDataFactory?.sensorReadingsAfter(date)?.continueWith { task ->
            if (task.isFaulted) {
                return@continueWith
            }

           val dataList = ArrayList<Entry>()
            task.result.withIndex().forEach { indexed ->
                dataList.add(Entry(indexed.value.reading.toFloat(), indexed.index))
            }
            val dataSet = LineDataSet(dataList, getString(R.string.sensor_readings_chart_label))
            dataSet.lineWidth = 2f
            dataSet.setDrawCircles(false)
            dataSet.color = R.color.colorPrimaryDark

            sensorChart?.data = LineData(ChartData.generateXVals(0, dataSet.entryCount), listOf(dataSet))
            sensorChart?.setDescription(getString(R.string.sensor_readings_chart_label))

            sensorChart?.axisLeft?.axisMinValue = task.result.minBy { reading -> reading.reading }?.reading?.toFloat() ?: 0f
            sensorChart?.axisLeft?.axisMinValue = task.result.maxBy { reading -> reading.reading }?.reading?.toFloat() ?: 0f
            sensorChart?.axisRight?.isEnabled = false
            sensorChart?.invalidate()
        }
    }

    private fun getPlaceNameFromBundle(vararg bundles: Bundle?): String {
        for (bundle in bundles) {
            if (bundle?.containsKey(HistoricalBolusDetailActivityFragment.Companion.HistoricalBolusDetailPlaceNameBundleKey) ?: false) {
                return bundle?.getString(HistoricalBolusDetailActivityFragment.Companion.HistoricalBolusDetailPlaceNameBundleKey) ?: ""
            }
        }

        return ""
    }

    private fun getBolusEventParcelableFromBundle(vararg bundles: Bundle?): BolusEventParcelable? {
        for (bundle in bundles) {
            if (bundle?.containsKey(HistoricalBolusDetailActivityFragment.Companion.HistoricalBolusEventBolusDetailParcelableBundleKey) ?: false) {
                return bundle?.getParcelable<Parcelable>(HistoricalBolusDetailActivityFragment.Companion.HistoricalBolusEventBolusDetailParcelableBundleKey) as BolusEventParcelable?
            }
        }
        return null
    }

    companion object {
        public val HistoricalBolusDetailPlaceNameBundleKey: String = "HistoricalBolusDetailPlaceNameBundleKey"
        public val HistoricalBolusEventBolusDetailParcelableBundleKey: String = "HistoricalBolusDetailBolusEventParcelableBundleKey"
    }
}
