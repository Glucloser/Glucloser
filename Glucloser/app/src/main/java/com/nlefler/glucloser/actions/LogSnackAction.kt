package com.nlefler.glucloser.actions

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log

import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.dataSource.BloodSugarFactory
import com.nlefler.glucloser.dataSource.ParseUploader
import com.nlefler.glucloser.dataSource.SnackFactory
import com.nlefler.glucloser.models.BloodSugar
import com.nlefler.glucloser.models.Snack
import com.nlefler.glucloser.models.SnackParcelable
import com.parse.Parse

import io.realm.Realm

/**
 * Created by Nathan Lefler on 4/24/15.
 */
public class LogSnackAction {

    public var snackParcelable: SnackParcelable? = null

    public constructor() {
    }

    public fun log() {
        if (this.snackParcelable == null) {
            Log.e(LOG_TAG, "Can't log snack, snack null")
            return
        }

        val sharedContext = GlucloserApplication.SharedApplication().getApplicationContext()
        val snack = SnackFactory.SnackFromParcelable(this.snackParcelable!!, sharedContext)

        ParseUploader.SharedInstance().uploadSnack(snack)
    }

    /** Parcelable  */
    public constructor(parcel: Parcel) {
        this.snackParcelable = parcel.readParcelable<Parcelable>(javaClass<SnackParcelable>().getClassLoader()) as SnackParcelable
    }

    public fun describeContents(): Int {
        return 0
    }

    public fun writeToParcel(out: Parcel, flags: Int) {
        out.writeParcelable(this.snackParcelable, flags)
    }

    companion object {
        private val LOG_TAG = "LogSnackAction"

        public val CREATOR: Parcelable.Creator<LogSnackAction> = object : Parcelable.Creator<LogSnackAction> {
            override fun createFromParcel(`in`: Parcel): LogSnackAction {
                return LogSnackAction(`in`)
            }

            override fun newArray(size: Int): Array<LogSnackAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
