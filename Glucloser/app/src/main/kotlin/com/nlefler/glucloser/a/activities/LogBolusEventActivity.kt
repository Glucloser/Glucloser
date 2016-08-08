package com.nlefler.glucloser.a.activities

import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.models.*
import com.nlefler.glucloser.a.models.parcelable.*
import com.nlefler.glucloser.a.ui.BolusEventDetailsFragment
import com.nlefler.glucloser.a.models.BolusEventDetailDelegate
import com.nlefler.glucloser.a.models.BolusEventType
import com.nlefler.glucloser.a.models.FoodDetailDelegate
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import com.nlefler.glucloser.a.models.parcelable.*
import com.nlefler.glucloser.a.ui.log.PlaceSelectionFragment
import javax.inject.Inject

class LogBolusEventActivity: AppCompatActivity(), PlaceSelectionDelegate, BolusEventDetailDelegate, FoodDetailDelegate {

    lateinit var placeFactory: PlaceFactory
        @Inject set

//    private var logBolusEventAction: LogBolusEventAction = LogBolusEventAction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_bolus_activity)

        val toolbar = findViewById(R.id.log_bolus_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        dataFactory?.inject(this)
//        dataFactory?.inject(logBolusEventAction)

        setupFragment(savedInstanceState)
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

    private fun setupFragment(savedInstanceState: Bundle?) {
        val fragment = PlaceSelectionFragment()

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.log_bolus_container, fragment).commit()
        }

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            val placeParcelable = placeFactory.placeParcelableFromCheckInData(extras)
            if (placeParcelable != null) {
                this.placeSelected(placeParcelable)
            }
        }
    }

    /** PlaceSelectionDelegate  */
    override fun placeSelected(placeParcelable: PlaceParcelable) {
//        this.logBolusEventAction.setPlaceParcelable(placeParcelable)
        switchToBolusEventDetailsFragment(MealParcelable(), placeParcelable)
    }

    /** MealDetailDelegate  */
    override fun bolusEventDetailUpdated(bolusEventParcelable: BolusEventParcelable) {
//        this.logBolusEventAction.setBolusEventParcelable(bolusEventParcelable)
        finishLoggingBolusEvent()
    }

    /** FoodDetailDelegate */
    override fun foodDetailUpdated(foodParcelable: FoodParcelable) {
//        this.logBolusEventAction.addFoodParcelable(foodParcelable)
    }

    /** Helpers  */
    private fun switchToBolusEventDetailsFragment(bolusEventParcelable: BolusEventParcelable, placeParcelable: PlaceParcelable?) {
        val fragment = BolusEventDetailsFragment()

        val args = Bundle()
        if (placeParcelable != null) {
            args.putString(BolusEventDetailsFragment.BolusEventDetailPlaceNameBundleKey, placeParcelable.name)
        }
        args.putParcelable(BolusEventDetailsFragment.BolusEventDetailBolusEventParcelableBundleKey, bolusEventParcelable as Parcelable)
        fragment.setArguments(args)

        getSupportFragmentManager().beginTransaction().replace(R.id.log_bolus_container, fragment, LogBolusEventActivity.Companion.BolusEventFragmentId).addToBackStack(null).commit()
    }

    private fun finishLoggingBolusEvent() {
        // TODO(nl) log
//        this.logBolusEventAction.log()
        finish()
    }

    companion object {
        private val LOG_TAG = "LogBolusEventActivity"
        private val BolusEventFragmentId = "BolusEventFragmentId"
        private val LogFoodActivityResultKey: Int = 2134

    }
}