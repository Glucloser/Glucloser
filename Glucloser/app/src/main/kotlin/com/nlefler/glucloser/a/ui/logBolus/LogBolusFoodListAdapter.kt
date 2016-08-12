package com.nlefler.glucloser.a.ui.logBolus

import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListAdapter
import android.widget.TextView
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.models.FoodEntity
import com.nlefler.glucloser.a.models.MealEntity
import com.nlefler.glucloser.a.models.parcelable.FoodParcelable
import rx.Observable
import rx.Subscription
import java.util.*

/**
 * Created by nathan on 8/5/16.
 */

class LogBolusFoodListAdapter(val ctx: Context, foodsListObservable: Observable<List<FoodParcelable>>): ListAdapter {

    // TODO(nl): This should be a recycler adapter
    var foods: List<FoodParcelable> = emptyList()
    var listSub: Subscription
    var observers = ArrayList<DataSetObserver>()
    val layoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    init {
        listSub = foodsListObservable.subscribe { newList ->
            foods = newList
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
        if (view == null) {
            view = layoutInflater.inflate(R.id.log_bolus_activity_food_list_item, parent, false)
        }

        val food = foods.get(idx)

        val nameField = view?.findViewById(R.id.log_bolus_activity_food_list_item_food_name) as EditText
        nameField.setText(food.foodName, TextView.BufferType.EDITABLE)

        val carbField = view?.findViewById(R.id.log_bolus_activity_food_list_item_carb) as EditText
        carbField.setText(food.carbs, TextView.BufferType.EDITABLE)

        val insulinField = view?.findViewById(R.id.log_bolus_activity_food_list_item_insulin) as EditText
        // TODO(nl): Insulin per food
        insulinField.setText(0, TextView.BufferType.EDITABLE)

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
        return foods.get(idx).foodId.hashCode().toLong()
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

