package com.nlefler.glucloser.a.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Layout
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.BloodSugarFactory
import com.nlefler.glucloser.a.dataSource.BolusPatternFactory
import com.nlefler.glucloser.a.dataSource.MealFactory
import com.nlefler.glucloser.a.dataSource.PlaceFactory
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.models.BolusPatternEntity
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.Place
import com.nlefler.glucloser.a.models.parcelable.*
import com.nlefler.glucloser.a.models.PlaceSelectionDelegate
import com.nlefler.glucloser.a.ui.logBolus.LogBolusFoodListAdapter
import rx.Observable
import rx.Scheduler
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import java.util.*
import javax.inject.Inject

class LogBolusEventActivity: AppCompatActivity() {

    lateinit var placeFactory: PlaceFactory
        @Inject set

    lateinit var bolusPatternFactory: BolusPatternFactory
        @Inject set

    lateinit var bloodSugarFactory: BloodSugarFactory
        @Inject set

    lateinit var mealFactory: MealFactory
        @Inject set

    lateinit var services: CairoServices
        @Inject set

    lateinit var mealParcelable: MealParcelable
        @Inject set

    private var foodsSubject = BehaviorSubject.create(emptyList<Food>())
    private var inflater: LayoutInflater? = null
    private var foodsAdapter: LogBolusFoodListAdapter? = null

    private var bolusPatternSub: Subscription? = null
    private var bloodSugarSub: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_bolus_activity)

        val dataFactory = GlucloserApplication.sharedApplication?.rootComponent
        dataFactory?.inject(this)

        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        mealParcelable = savedInstanceState?.getParcelable(SavedStateBolusParcelableKey) ?: mealParcelable
        fetchAsyncBolusData()

        foodsSubject.onNext(mealParcelable.foods)

        val toolbar = findViewById(R.id.log_bolus_toolbar) as Toolbar
        setSupportActionBar(toolbar)


        foodsAdapter = LogBolusFoodListAdapter(this, foodsSubject.asObservable())
        foodsAdapter?.foodEdited?.asObservable()?.subscribe { fp -> foodEdited(fp) }

        setupView()
        if (mealParcelable.foods.count() == 0) {
            addNewFood()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        outState?.putParcelable(SavedStateBolusParcelableKey, mealParcelable)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_log_meal, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_log_meal_save) {
            finishLoggingBolusEvent()
            return true
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    private fun setupView() {
        val root = findViewById(R.id.log_bolus_activity) as ConstraintLayout

        val placePar: Place?
        val extras = intent.extras
        if (extras != null) {
            placePar = placeFactory.placeParcelableFromCheckInData(extras)
        }
        else {
            placePar = mealParcelable.place
        }

        if (placePar != null) {
            placeSelected(placePar)
        }

        val container = root.findViewById(R.id.log_bolus_activity_place_info_container) as ViewGroup
        inflater?.inflate(R.layout.log_bolus_select_place_prompt, container, true)

        val setPlaceButton = root.findViewById(R.id.log_bolus_activity_set_place_button) as ImageButton
        setPlaceButton.setOnClickListener { view -> showPlaceSelection() }

        val addFoodButton = root.findViewById(R.id.log_bolus_activity_add_food_button) as Button
        addFoodButton.setOnClickListener { view -> addNewFood() }

        // TODO(nl): recycler view
        val foodListView = root.findViewById(R.id.log_bolus_activity_food_list) as ListView
        foodListView.adapter = foodsAdapter
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

    fun placeSelected(place: Place) {
        mealParcelable.place = place

        val root = findViewById(R.id.log_bolus_activity) as ConstraintLayout
        val placeNameView = root.findViewById(R.id.log_bolus_activity_place_name) as TextView
        val placeDetailView = root.findViewById(R.id.log_bolus_activity_place_info_container)

        placeNameView.text = place.name
        // TODO(nl): place detail view: place details if we have a place
    }

    private fun addNewFood() {
        val fp = FoodParcelable()
        mealParcelable.foods.add(0, fp)
        foodsSubject.onNext(mealParcelable.foods)
    }

    private fun foodEdited(food: Food) {
        val idx = mealParcelable.foods.indexOfFirst { n -> n.primaryId == food.primaryId}
        mealParcelable.foods.removeAt(idx)
        mealParcelable.foods.add(idx, food)
    }

    private fun finishLoggingBolusEvent() {
        // TODO(nl): is correction
        mealParcelable.carbs = mealParcelable.foods.sumBy { fp -> fp.carbs }
        mealParcelable.insulin = mealParcelable.foods.sumByDouble { fp -> fp.insulin.toDouble() }.toFloat()

        val meal = mealFactory.mealFromParcelable(mealParcelable)
        mealFactory.save(meal)
        // TODO(nl): mark meal for upload and move upload into service
        services.collectionService().addMeal(meal)

        finish()
    }

    private fun fetchAsyncBolusData() {
        bolusPatternSub = bolusPatternFactory.currentBolusPattern()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: Subscriber<BolusPatternEntity>() {
                    override fun onError(e: Throwable?) {
                        print(e)
                    }

                    override fun onNext(pattern: BolusPatternEntity?) {
                        if (pattern == null) {
                            return
                        }
                        if (mealParcelable.bolusPattern == null ||
                                (mealParcelable.bolusPattern?.updatedOn?.before(pattern.updatedOn) ?: true)) {
                            mealParcelable.bolusPattern = bolusPatternFactory.parcelableFromBolusPattern(pattern)
                        }
                    }

                    override fun onCompleted() {
                    }
                })
        bloodSugarSub = bloodSugarFactory.lastBloodSugarFromCGM()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { sugar ->
                    if (sugar == null) {
                        return@subscribe
                    }
                    if (mealParcelable.beforeSugar == null ||
                            (mealParcelable.beforeSugar?.recordedDate?.before(sugar.recordedDate) ?: true)) {
                        val sugarPar = BloodSugarParcelable()
                        sugarPar.recordedDate = sugar.recordedDate
                        sugarPar.readingValue = sugar.readingValue
                        mealParcelable.beforeSugar = sugarPar
                    }
        }
    }

    companion object {
        private val LOG_TAG = "LogBolusEventActivity"

        private val PlaceSelectionRequestCode = 9838

        private val SavedStateBolusParcelableKey = "savedStateBolusParcelableKey"
    }
}