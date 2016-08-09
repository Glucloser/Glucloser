package com.nlefler.glucloser.a.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.models.parcelable.*
import com.nlefler.glucloser.a.models.BolusEventDetailDelegate
import com.nlefler.glucloser.a.models.FoodDetailDelegate
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import javax.inject.Inject

class LogBolusEventActivity: AppCompatActivity(), BolusEventDetailDelegate, FoodDetailDelegate {

    lateinit var placeFactory: PlaceFactory
        @Inject set

    private var bolusParcelable = MealParcelable()
//    private var logBolusEventAction: LogBolusEventAction = LogBolusEventAction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_bolus_activity)

        bolusParcelable = savedInstanceState?.getParcelable(SavedStateBolusParcelableKey) ?: bolusParcelable

        val toolbar = findViewById(R.id.log_bolus_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        dataFactory?.inject(this)
//        dataFactory?.inject(logBolusEventAction)

        setupView()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        outState?.putParcelable(SavedStateBolusParcelableKey, bolusParcelable)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_meal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    private fun setupView() {
        val root = findViewById(R.id.log_bolus_activity) as ConstraintLayout

        val placePar: PlaceParcelable?
        val extras = intent.extras
        if (extras != null) {
            placePar = placeFactory.placeParcelableFromCheckInData(extras)
        }
        else {
            placePar = bolusParcelable.placeParcelable
        }

        if (placePar != null) {
            placeSelected(placePar)
        }
        else {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val container = root.findViewById(R.id.log_bolus_activity_place_info_container) as ViewGroup
            val prompt = inflater.inflate(R.layout.log_bolus_select_place_prompt, container, true)
            val button = prompt.findViewById(R.id.log_bolus_select_place_prompt_button) as Button
            button.setOnClickListener { view -> showPlaceSelection() }
        }

        val addFoodButton = root.findViewById(R.id.log_bolus_activity_add_food_button) as Button
        addFoodButton.setOnClickListener { view -> addNewFood() }

        bolusParcelable.foodParcelables.forEach { fp -> addViewForFood(fp) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PlaceSelectionRequestCode) {
            if (resultCode == RESULT_OK) {
                val placeParcelable = data?.getParcelableExtra<PlaceParcelable>(PlaceSelectionActivity.SelectedPlaceResultKey)
                if (placeParcelable != null) {
                    placeSelected(placeParcelable)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showPlaceSelection() {
        val intent = Intent(this, PlaceSelectionActivity::class.java)
        startActivityForResult(intent, PlaceSelectionRequestCode)
    }

    fun placeSelected(placeParcelable: PlaceParcelable) {
        val root = findViewById(R.id.log_bolus_activity) as ConstraintLayout
        val placeNameView = root.findViewById(R.id.log_bolus_activity_place_name) as TextView
        val placeDetailView = root.findViewById(R.id.log_bolus_activity_place_info_container)

        placeNameView.text = placeParcelable.name
        // TODO(nl): place detail view: place details if we have a place
    }

    /** BolusEventDetailDelegate  */
    override fun bolusEventDetailUpdated(mealParcelable: MealParcelable) {
//        this.logBolusEventAction.setBolusEventParcelable(mealParcelable)
        finishLoggingBolusEvent()
    }

    private fun addNewFood() {

    }

    private fun addViewForFood(fp: FoodParcelable) {

    }

    /** FoodDetailDelegate */
    override fun foodDetailUpdated(foodParcelable: FoodParcelable) {
//        this.logBolusEventAction.addFoodParcelable(foodParcelable)
    }

    private fun finishLoggingBolusEvent() {
        // TODO(nl) log
//        this.logBolusEventAction.log()
        finish()
    }

    companion object {
        private val LOG_TAG = "LogBolusEventActivity"

        private val PlaceSelectionRequestCode = 9838

        private val SavedStateBolusParcelableKey = "savedStateBolusParcelableKey"
    }
}