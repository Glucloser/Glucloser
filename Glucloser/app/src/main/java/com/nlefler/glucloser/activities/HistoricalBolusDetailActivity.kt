package com.nlefler.glucloser.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import com.nlefler.glucloser.R
import com.nlefler.glucloser.dataSource.PlaceFactory
import com.nlefler.glucloser.models.*
import com.nlefler.glucloser.ui.HistoricalBolusDetailActivityFragment

public class HistoricalBolusDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historical_bolus_detail)

        val bolusEventParcelable = getBolusEventParcelableFromBundle(savedInstanceState, getIntent().getExtras())
        bolusEventParcelable ?: return

        setupHistoricalBolusDetailsFragment(bolusEventParcelable)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bolus_detail, menu)
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

        return super.onOptionsItemSelected(item)
    }

    private fun setupHistoricalBolusDetailsFragment(bolusEventParcelable: BolusEventParcelable) {
        val fragment = getSupportFragmentManager().findFragmentById(R.id.historical_bolus_detail_activity_container)
                as HistoricalBolusDetailActivityFragment?

        val args = Bundle()
        args.putParcelable(HistoricalBolusDetailActivityFragment.HistoricalBolusEventBolusDetailParcelableBundleKey,
                bolusEventParcelable as Parcelable)
        fragment?.setupWithBundle(args)
    }

    private fun getBolusEventParcelableFromBundle(savedInstanceState: Bundle?, extras: Bundle?): BolusEventParcelable? {
        for (bundle in arrayOf<Bundle?>(savedInstanceState, extras)) {
            if (bundle?.containsKey(BolusKey) ?: false) {
                val eventParcelable: Parcelable? = bundle?.getParcelable(BolusKey)
                when ((eventParcelable as BolusEventParcelable?)?.getEventType()) {
                    is BolusEventType.BolusEventTypeSnack -> {
                        return eventParcelable as SnackParcelable?
                    }
                    is BolusEventType.BolusEventTypeMeal -> {
                        return eventParcelable as MealParcelable?
                    }
                }
            }
        }
        return null
    }

    companion object {
        public val BolusKey:String = "BolusKey"
        private val HistoricalBolusDetailActivityFragmentId: String = "HistoricalBolusDetailActivityFragmentId"
    }

}
