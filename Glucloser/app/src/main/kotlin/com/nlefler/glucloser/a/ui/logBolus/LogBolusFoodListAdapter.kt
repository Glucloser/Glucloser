package com.nlefler.glucloser.a.ui.logBolus

import android.content.Context
import android.database.DataSetObserver
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListAdapter
import android.widget.TextView
import com.jakewharton.rxbinding.widget.RxTextView
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.models.FoodEntity
import com.nlefler.glucloser.a.models.MealEntity
import com.nlefler.glucloser.a.models.parcelable.FoodParcelable
import rx.Observable
import rx.Subscription
import rx.android.plugins.RxAndroidPlugins
import rx.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by nathan on 8/5/16.
 */

// TODO(nl): This should be a recycler adapter
class LogBolusFoodListAdapter(val ctx: Context, foodsListObservable: Observable<List<FoodParcelable>>): ListAdapter {

    val foodEdited = PublishSubject.create<FoodParcelable>()

    private var foods = ArrayList<FoodParcelable>()
    private var listSub: Subscription
    private var observers = ArrayList<DataSetObserver>()
    private val layoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        listSub = foodsListObservable.subscribe { newList ->
            foods = ArrayList(newList)
            observers.forEach { ob -> ob.onChanged() }
        }
    }

    fun addNewItem() {
        foods.add(FoodParcelable())
        observers.forEach { ob -> ob.onChanged() }
    }

    override fun isEnabled(p0: Int): Boolean {
        // No seperators
        return true
    }

    override fun areAllItemsEnabled(): Boolean {
        return true
    }

    override fun getView(idx: Int, existingView: View?, parent: ViewGroup?): View? {
        var view = existingView
        var addTextChangeListeners = false
        if (view == null) {
            view = layoutInflater.inflate(R.layout.log_bolus_activity_food_list_item, parent, false)
            addTextChangeListeners = true
        }

        val nameField = view?.findViewById(R.id.log_bolus_activity_food_list_item_food_name) as EditText
        val carbField = view?.findViewById(R.id.log_bolus_activity_food_list_item_carb) as EditText
        val insulinField = view?.findViewById(R.id.log_bolus_activity_food_list_item_insulin) as EditText
        if (addTextChangeListeners) {
            RxTextView.afterTextChangeEvents(nameField)
                    .distinctUntilChanged()
                    .debounce(800, TimeUnit.MILLISECONDS)
                    .subscribe { event ->
                        val food = this.getItem(idx) as FoodParcelable
                        val text = event.editable().toString()
                        if (food.foodName != text) {
                            food.foodName = text
                            foodEdited.onNext(food)
                        }
            }
            RxTextView.afterTextChangeEvents(carbField)
                    .distinctUntilChanged()
                    .debounce(800, TimeUnit.MILLISECONDS)
                    .subscribe { event ->
                        val food = this.getItem(idx) as FoodParcelable
                        val text = event.editable().toString()
                        try {
                            val carbs = text.toInt()
                            if (food.carbs != carbs) {
                                food.carbs = carbs
                                foodEdited.onNext(food)
                            }
                        }
                        catch (e: Exception) {

                        }
            }
            RxTextView.afterTextChangeEvents(insulinField)
                    .distinctUntilChanged()
                    .debounce(800, TimeUnit.MILLISECONDS)
                    .subscribe { event ->
                        val food = this.getItem(idx) as FoodParcelable
                        val text = event.editable().toString()
                        try {
                            val insulin = text.toFloat()
                            if (food.insulin != insulin) {
                                food.insulin = insulin
                                foodEdited.onNext(food)
                            }
                        }
                        catch (e: Exception) {

                        }
            }
        }

        val food = foods.get(idx)

        val foodName = food.foodName
        if (foodName != nameField.text.toString() && foodName != null && !foodName.isEmpty()) {
            nameField.setText(food.foodName, TextView.BufferType.EDITABLE)
        }

        try {
            val carbs = carbField.text.toString().toInt()
            if (food.carbs != carbs) {
                carbField.setText(food.carbs.toString(), TextView.BufferType.EDITABLE)
            }
        }
        catch (e: Exception) {

        }

        try {
            val insulin = insulinField.text.toString().toFloat()
            if (food.insulin != insulin) {
                insulinField.setText(food.insulin.toString(), TextView.BufferType.EDITABLE)
            }
        }
        catch (e: Exception) {

        }

        return view
    }

    override fun getItem(idx: Int): Any? {
        return foods.get(idx)
    }

    override fun getCount(): Int {
        return foods.count()
    }

    override fun isEmpty(): Boolean {
        return foods.isEmpty()
    }

    override fun registerDataSetObserver(ob: DataSetObserver?) {
        if (ob == null) {
            return
        }
        observers.add(ob)
    }

    override fun getItemViewType(idx: Int): Int {
        return R.id.main_history_list_item
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(idx: Int): Long {
        return foods[idx].foodId.hashCode().toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun unregisterDataSetObserver(ob: DataSetObserver?) {
        if (ob == null) {
            return
        }
        observers.remove(ob)
    }
}

