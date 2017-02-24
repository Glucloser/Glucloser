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
class LogBolusFoodListAdapter(val ctx: Context, foodObservable: Observable<Food>): ListAdapter {

    val foodEdited = PublishSubject.create<Food>()

    private var items = ArrayList<Item>()
    private var viewHolders = HashMap<View, View>()
    private var listSub: Subscription
    private var observers = ArrayList<DataSetObserver>()
    private val layoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val textChangeSubs = HashMap<View, Subscription>()

    init {
        listSub = foodObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { food ->
                    val item = itemForFood(items, food)
                    if (item == null) {
                        addNewItem(food)
                        observers.forEach { ob -> ob.onChanged() }
                    }
                    else {
                        item.food = food
                        val view = item.view
                        if (view != null) {
                            updateView(view, food)
                        }
                    }
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
        val item = itemForIndex(items, idx)

        if (view == null) {
            val itemView = item?.view
            if (itemView != null) {
                removeTextChangeListeners(itemView)
                item?.view = null
            }
            view = layoutInflater.inflate(R.layout.log_bolus_activity_food_list_item, parent, false)
            addTextChangeListeners(view)
        }
        if (view == null || item == null) {
            return view
        }
        val food = item.food ?: return view

        updateView(view, food)
        item.view = view

        return view
    }

    override fun getItem(idx: Int): Any? {
        return itemForIndex(items, idx)
    }

    override fun getCount(): Int {
        return items.count();
    }

    override fun isEmpty(): Boolean {
        return items.isEmpty()
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
        return itemForIndex(items, idx)?.food?.primaryId?.hashCode()?.toLong() ?: 0
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

    private fun updateView(view: View, food: Food) {
        val nameField = view.findViewById(R.id.log_bolus_activity_food_list_item_food_name) as EditText
        viewHolders.put(nameField, view)
        val carbField = view.findViewById(R.id.log_bolus_activity_food_list_item_carb) as EditText
        viewHolders.put(carbField, view)
        val insulinField = view.findViewById(R.id.log_bolus_activity_food_list_item_insulin) as EditText
        viewHolders.put(insulinField, view)

        nameField.setText(food.foodName, TextView.BufferType.EDITABLE)

        val carbsText = food.carbs.toString()
        carbField.setText(carbsText,TextView.BufferType.EDITABLE)

        val insulinText = food.insulin.toString()
        insulinField.setText(insulinText, TextView.BufferType.EDITABLE)

    }

    private fun removeTextChangeListeners(view: View) {
        val nameField = view.findViewById(R.id.log_bolus_activity_food_list_item_food_name) as EditText
        val carbField = view.findViewById(R.id.log_bolus_activity_food_list_item_carb) as EditText
        val insulinField = view.findViewById(R.id.log_bolus_activity_food_list_item_insulin) as EditText
        textChangeSubs.remove(nameField)?.unsubscribe()
        textChangeSubs.remove(carbField)?.unsubscribe()
        textChangeSubs.remove(insulinField)?.unsubscribe()
    }

    private fun addTextChangeListeners(view: View) {
        val nameField = view.findViewById(R.id.log_bolus_activity_food_list_item_food_name) as EditText
        val carbField = view.findViewById(R.id.log_bolus_activity_food_list_item_carb) as EditText
        val insulinField = view.findViewById(R.id.log_bolus_activity_food_list_item_insulin) as EditText

         textChangeSubs.put(nameField, RxTextView.afterTextChangeEvents(nameField)
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe { event ->
                    val parentView = viewHolders.get(event.view()) ?: return@subscribe
                    val item = itemForView(items, parentView) ?: return@subscribe
                    val currFood = item.food ?: return@subscribe
                    val text = event.editable().toString()
                    if (!text.isEmpty() && currFood.foodName != text) {
                        currFood.foodName = text
                        foodEdited.onNext(currFood)
                    }
        })

        textChangeSubs.put(carbField, RxTextView.afterTextChangeEvents(carbField)
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe { event ->
                    val parentView = viewHolders.get(event.view()) ?: return@subscribe
                    val item = itemForView(items, parentView) ?: return@subscribe
                    val currFood = item.food ?: return@subscribe
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
        })
        textChangeSubs.put(insulinField, RxTextView.afterTextChangeEvents(insulinField)
                .distinctUntilChanged()
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribe { event ->
                    val parentView = viewHolders.get(event.view()) ?: return@subscribe
                    val item = itemForView(items, parentView) ?: return@subscribe
                    val currFood = item.food ?: return@subscribe
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
        })
    }

    private fun addNewItem(food: Food) {
        val item = Item(null, food, 0)
        items.forEach { i -> i.idx++ }
        items.add(item)
    }

    private fun itemForFood(items: List<Item>, food: Food): Item? {
        return items.findLast { i -> i.food == food }
    }

    private fun itemForIndex(items: List<Item>, idx: Int): Item? {
        return items.findLast { i -> i.idx == idx }
    }

    private fun itemForView(items: List<Item>, view: View): Item? {
        return items.findLast { i -> i.view == view }
    }

    private data class Item(var view: View?, var food: Food?, var idx: Int) {}
}

