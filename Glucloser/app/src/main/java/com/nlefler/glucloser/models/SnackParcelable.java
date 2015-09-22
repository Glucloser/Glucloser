package com.nlefler.glucloser.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Nathan Lefler on 5/8/15.
 */
public class SnackParcelable implements Parcelable, BolusEventParcelable {
    private String id;
    private Date date;
    private BolusPatternParcelable bolusPatternParcelable;
    private int carbs;
    private float insulin;
    private BloodSugarParcelable beforeSugarParcelable;
    private boolean correction;
    private List<FoodParcelable> foodParcelables;

    public SnackParcelable() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NotNull
    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public BolusPatternParcelable getBolusPatternParcelable() { return this.bolusPatternParcelable; }

    @Override
    public void setBolusPatternParcelable(BolusPatternParcelable patternParcelable) {
        this.bolusPatternParcelable = patternParcelable;
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

    @Override
    public List<FoodParcelable> getFoodParcelables() {
        return foodParcelables;
    }

    @Override
    public void setFoodParcelables(List<FoodParcelable> foodParcelables) {
        this.foodParcelables = foodParcelables;
    }

    /** Parcelable */
    protected SnackParcelable(Parcel in) {
        id = in.readString();
        carbs = in.readInt();
        insulin = in.readFloat();
        correction = in.readInt() != 0;
        beforeSugarParcelable = in.readParcelable(BloodSugar.class.getClassLoader());
        long time = in.readLong();
        if (time > 0) {
            date = new Date();
        }
        bolusPatternParcelable = in.readParcelable(BolusPatternParcelable.class.getClassLoader());
        this.foodParcelables = new ArrayList<FoodParcelable>();
        in.readList(this.foodParcelables, FoodParcelable.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(carbs);
        dest.writeFloat(insulin);
        dest.writeInt(correction ? 1 : 0);
        dest.writeParcelable(beforeSugarParcelable, flags);
        if (date != null) {
            dest.writeLong(date.getTime());
        }
        dest.writeParcelable(bolusPatternParcelable, 0);
        dest.writeTypedList(this.foodParcelables);
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
