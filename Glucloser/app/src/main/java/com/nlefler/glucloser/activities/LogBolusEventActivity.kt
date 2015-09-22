package com.nlefler.glucloser.activities

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.nlefler.glucloser.R
import com.nlefler.glucloser.actions.LogBolusEventAction
import com.nlefler.glucloser.dataSource.PlaceFactory
import com.nlefler.glucloser.models.*
import com.nlefler.glucloser.ui.BolusEventDetailsFragment
import com.nlefler.glucloser.ui.PlaceSelectionFragment

public class LogBolusEventActivity : AppCompatActivity(), PlaceSelectionDelegate, BolusEventDetailDelegate, FoodDetailDelegate {

    private var logBolusEventAction: LogBolusEventAction = LogBolusEventAction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_bolus_event)

        val bolusEventType = getBolusEventTypeFromBundle(savedInstanceState, getIntent().getExtras())
        if (bolusEventType == null) {
            return
        }
        setupFragmentForEventType(bolusEventType, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_meal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    private fun setupFragmentForEventType(eventType: BolusEventType, savedInstanceState: Bundle?) {
        when (eventType) {
            BolusEventType.BolusEventTypeMeal -> {
                val fragment = PlaceSelectionFragment()

                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction().add(R.id.log_bolus_event_activity_container, fragment).commit()
                }

                val intent = getIntent()
                val extras = intent.getExtras()
                if (extras != null) {
                    val placeParcelable = PlaceFactory.PlaceParcelableFromCheckInData(extras)
                    if (placeParcelable != null) {
                        this.placeSelected(placeParcelable)
                    }
                }
            }
            BolusEventType.BolusEventTypeSnack -> {
                switchToBolusEventDetailsFragment(SnackParcelable(), null)
            }
        }
    }

    /** PlaceSelectionDelegate  */
    override fun placeSelected(placeParcelable: PlaceParcelable) {
        this.logBolusEventAction.setPlaceParcelable(placeParcelable)
        switchToBolusEventDetailsFragment(MealParcelable(), placeParcelable)
    }

    /** MealDetailDelegate  */
    override fun bolusEventDetailUpdated(bolusEventParcelable: BolusEventParcelable) {
        this.logBolusEventAction.setBolusEventParcelable(bolusEventParcelable)
        finishLoggingBolusEvent()
    }

    /** FoodDetailDelegate */
    override fun foodDetailUpdated(foodParcelable: FoodParcelable) {
        this.logBolusEventAction.addFoodParcelable(foodParcelable)
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

        getSupportFragmentManager().beginTransaction().replace(R.id.log_bolus_event_activity_container, fragment, BolusEventFragmentId).addToBackStack(null).commit()
    }

    private fun finishLoggingBolusEvent() {
        this.logBolusEventAction.log()
        finish()
    }

    private fun getBolusEventTypeFromBundle(savedInstanceState: Bundle?, extras: Bundle?): BolusEventType? {
        for (bundle in arrayOf<Bundle?>(savedInstanceState, extras)) {
            if (bundle?.containsKey(BolusEventTypeKey) ?: null != null) {
                val eventName = bundle!!.getString(BolusEventTypeKey)
                return try { BolusEventType.valueOf(eventName) } catch (e: Exception ) { null }
            }
        }
        return null
    }

    companion object {
        private val LOG_TAG = "LogBolusEventActivity"
        private val BolusEventFragmentId = "BolusEventFragmentId"
        private val LogFoodActivityResultKey: Int = 2134

        public val BolusEventTypeKey: String = "BolusEventTypeKey"


    }
}