package com.nlefler.glucloser.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public class SnackParcelable implements Parcelable, BolusEventParcelable {
    private String snackId;
    private Date date;
    private int carbs;
    private float insulin;
    private BloodSugarParcelable beforeSugarParcelable;
    private boolean correction;

    public SnackParcelable() {

    }

    public String getSnackId() {
        return snackId;
    }

    public void setSnackId(String snackId) {
        this.snackId = snackId;
    }

    @NotNull
    @Override
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int getCarbs() {
        return carbs;
    }

    @Override
    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    @Override
    public float getInsulin() {
        return insulin;
    }

    @Override
    public void setInsulin(float insulin) {
        this.insulin = insulin;
    }

    @Nullable
    @Override
    public BloodSugarParcelable getBeforeSugarParcelable() {
        return beforeSugarParcelable;
    }

    @Override
    public void setBeforeSugarParcelable(BloodSugarParcelable beforeSugarParcelable) {
        this.beforeSugarParcelable = beforeSugarParcelable;
    }

    @Override
    public boolean isCorrection() {
        return correction;
    }

    @Override
    public void setCorrection(boolean correction) {
        this.correction = correction;
    }
      /** Parcelable */
    protected SnackParcelable(Parcel in) {
        snackId = in.readString();
        carbs = in.readInt();
        insulin = in.readFloat();
        correction = in.readInt() != 0;
        beforeSugarParcelable = (BloodSugarParcelable)in.readParcelable(BloodSugar.class.getClassLoader());
        date = new Date(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(snackId);
        dest.writeInt(carbs);
        dest.writeFloat(insulin);
        dest.writeInt(correction ? 1 : 0);
        dest.writeParcelable(beforeSugarParcelable, flags);
        dest.writeLong(date.getTime());
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SnackParcelable> CREATOR = new Parcelable.Creator<SnackParcelable>() {
        @Override
        public SnackParcelable createFromParcel(Parcel in) {
            return new SnackParcelable(in);
        }

        @Override
        public SnackParcelable[] newArray(int size) {
            return new SnackParcelable[size];
        }
    };
}
