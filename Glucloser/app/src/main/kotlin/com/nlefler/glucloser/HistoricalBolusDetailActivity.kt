package com.nlefler.glucloser

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.nlefler.glucloser.models.BolusEventParcelable

public class HistoricalBolusDetailActivity : AppCompatActivity() {

    var bolusEventPar: BolusEventParcelable? = null

    var carbsLabel: TextView? = null
    var insulinLabel: TextView? = null
    var bloodSugarLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historical_bolus_detail)

        getStateFromBundles(getIntent().getExtras(), savedInstanceState)
        linkViewOutlets()
        setupViewValues()
    }

    private fun getStateFromBundles(extras: Bundle?, savedInstanceState: Bundle?) {
        if (extras?.containsKey(BolusKey) ?: false) {
            bolusEventPar = extras?.getParcelable(BolusKey)
        }
        else if (savedInstanceState?.containsKey(BolusKey) ?: false) {
            bolusEventPar = savedInstanceState?.getParcelable(BolusKey)
        }
    }

    private fun linkViewOutlets() {
        carbsLabel = findViewById(R.id.historical_bolus_detail_total_carb_value) as TextView
        insulinLabel = findViewById(R.id.historical_bolus_detail_total_insulin_value) as TextView
        bloodSugarLabel = findViewById(R.id.historical_bolus_detail_blood_sugar_before_value) as TextView
    }

    private fun setupViewValues() {
        carbsLabel?.text = "${bolusEventPar?.carbs}"
        insulinLabel?.text = "${bolusEventPar?.insulin}"
        bloodSugarLabel?.text = "${bolusEventPar?.bloodSugarParcelable?.value}"

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

    companion object {
        public val BolusKey:String = "BolusKey"
    }

}
