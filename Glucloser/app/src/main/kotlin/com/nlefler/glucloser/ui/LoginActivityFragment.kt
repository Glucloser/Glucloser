package com.nlefler.glucloser.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button
import android.widget.EditText;

import com.nlefler.glucloser.R;
import com.parse.ParseUser

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment : Fragment() {

    var usernameField: EditText? = null
    var passwordField: EditText? = null
    var loginButton: Button? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var rootView = inflater?.inflate(R.layout.fragment_login, container, false);
        usernameField = rootView?.findViewById(R.id.login_username_value) as EditText?
        passwordField = rootView?.findViewById(R.id.login_password_value) as EditText?
        loginButton = rootView?.findViewById(R.id.login_sign_in_button) as Button?

        loginButton?.setOnClickListener { v: View ->
            val username = usernameField?.getText().toString()
            val password = passwordField?.getText().toString()

            if (username.length() > 0 && password.length() > 0) {
                ParseUser.logInInBackground(username, password);
            }
        }

        return rootView;
    }

}
