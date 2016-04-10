package com.nlefler.glucloser.a.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button
import android.widget.EditText;
import com.nlefler.glucloser.a.GlucloserApplication

import com.nlefler.glucloser.a.R;
import com.nlefler.glucloser.a.user.UserManager
import javax.inject.Inject

public class LoginActivityFragment : Fragment() {

    @Inject lateinit var userManager: UserManager

    var emailField: EditText? = null
    var loginButton: Button? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        GlucloserApplication.sharedApplication?.rootComponent?.inject(this)

        var rootView = inflater?.inflate(R.layout.fragment_login, container, false);
        emailField = rootView?.findViewById(R.id.login_email_value) as EditText?
        loginButton = rootView?.findViewById(R.id.login_sign_in_button) as Button?

        loginButton?.setOnClickListener { v: View ->
            val username = emailField?.getText().toString()

            if (username.length > 0) {
                userManager.loginOrCreateUser(username).continueWith { task ->
                    if (task.isFaulted) {
                        // TODO(nl) Message user
                        return@continueWith
                    }
                    activity.finish()
                }
            }
        }

        return rootView;
    }

}
