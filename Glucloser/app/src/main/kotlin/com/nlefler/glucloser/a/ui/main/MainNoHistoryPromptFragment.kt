package com.nlefler.glucloser.a.ui.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.nlefler.glucloser.a.R

/**
 * Created by nathan on 8/6/16.
 */
class MainHistoryPromptFragment constructor(): Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater!!.inflate(R.layout.main_history_fragment, container, false)

        val promptView = rootView.findViewById(R.id.main_no_history_prompt_text) as TextView
        val logButton = rootView.findViewById(R.id.main_no_history_log_button) as Button
        logButton.setOnClickListener { view -> onLogButtonClick() }

////            val activity = getActivity();
//
//                    val intent = Intent(rootView.getContext(), LogBolusEventActivity::class.java)
//                    intent.putExtra(LogBolusEventActivity.BolusEventTypeKey, BolusEventType.BolusEventTypeMeal.name)
//
//                    activity.startActivityForResult(intent, LogBolusEventActivityIntentCode)

        return rootView
    }

    private fun onLogButtonClick() {

    }

    companion object {
        private val LOG_TAG = "PlaceholderFragment"
    }
}