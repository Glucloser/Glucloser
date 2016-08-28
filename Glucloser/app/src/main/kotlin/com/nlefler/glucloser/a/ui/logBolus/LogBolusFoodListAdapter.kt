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
import com.nlefler.glucloser.a.models.Food
import com.nlefler.glucloser.a.models.FoodEntity
import com.nlefler.glucloser.a.models.MealEntity
import com.nlefler.glucloser.a.models.parcelable.FoodParcelable
import rx.Observable
import rx.Subscription
import rx.android.plugins.RxAndroidPlugins
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by nathan on 8/5/16.
 */

// TODO(nl): This should be a recycler adapter
class LogBolusFoodListAdapter(val ctx: Context, foodsListObservable: Observable<List<Food>>): ListAdapter {

    val foodEdited = PublishSubject.create<Food>()

    private var foods = ArrayList<Food>()
    private var listSub: Subscription
    private val viewHolders = HashMap<View, ViewHolder>()
    private var observers = ArrayList<DataSetObserver>()
    private val layoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        listSub = foodsListObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { newList ->
                    foods = ArrayList(newList)
                    print(foods)
                    observers.forEach { ob -> ob.onChanged() }
        }
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
        val food = getItem(idx) as FoodParcelable
        var addTextChangeListeners = false

        if (view == null) {
            view = layoutInflater.inflate(R.layout.log_bolus_activity_food_list_item, parent, false)
            addTextChangeListeners = true
        }

        val nameField = view?.findViewById(R.id.log_bolus_activity_food_list_item_food_name) as EditText
        viewHolders.put(nameField, ViewHolder(nameField, food))

        val carbField = view?.findViewById(R.id.log_bolus_activity_food_list_item_carb) as EditText
        viewHolders.put(carbField, ViewHolder(carbField, food))

        val insulinField = view?.findViewById(R.id.log_bolus_activity_food_list_item_insulin) as EditText
        viewHolders.put(insulinField, ViewHolder(insulinField, food))

        if (addTextChangeListeners) {
            RxTextView.afterTextChangeEvents(nameField)
                    .distinctUntilChanged()
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .subscribe { event ->
                        val currFood = viewHolders.get(event.view())?.food ?: return@subscribe
                        val text = event.editable().toString()
                        if (!text.isEmpty() && currFood.foodName != text) {
                            currFood.foodName = text
                            foodEdited.onNext(currFood)
                        }
            }
            RxTextView.afterTextChangeEvents(carbField)
                    .distinctUntilChanged()
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .subscribe { event ->
                        val currFood = viewHolders.get(event.view())?.food ?: return@subscribe
                        val text = event.editable().toString()
                        if (text.isEmpty()) {
                            return@subscribe
                        }
                        try {
                            val carbs = text.toInt()
                            if (currFood.carbs != carbs) {
                                currFood.carbs = carbs
                                foodEdited.onNext(currFood)
                            }
                        }
                        catch (e: Exception) {

                        }
            }
            RxTextView.afterTextChangeEvents(insulinField)
                    .distinctUntilChanged()
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .subscribe { event ->
                        val currFood = viewHolders.get(event.view())?.food ?: return@subscribe
                        val text = event.editable().toString()
                        if (text.isEmpty()) {
                            return@subscribe
                        }
                        try {
                            val insulin = text.toFloat()
                            if (currFood.insulin != insulin) {
                                currFood.insulin = insulin
                                foodEdited.onNext(currFood)
                            }
                        }
                        catch (e: Exception) {

                        }
            }
        }

        nameField.setText(food.foodName ?: "", TextView.BufferType.EDITABLE)

        val carbsText = food.carbs?.toString() ?: ""
        carbField.setText(carbsText,TextView.BufferType.EDITABLE)

        val insulinText = food.insulin?.toString() ?: ""
        insulinField.setText(insulinText, TextView.BufferType.EDITABLE)

        return view
    }

    override fun getItem(idx: Int): Any? {
        return foods[idx]
    }

    override fun getCount(): Int {
        var count = foods.count()
        return count;
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
        val food = getItem(idx) as FoodParcelable
        return food.primaryId.hashCode().toLong()
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

    data class ViewHolder(val view: View, var food: FoodParcelable) {
    }

}

