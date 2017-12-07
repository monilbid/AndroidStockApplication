package edu.temple.stockapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class Company implements Parcelable{
    // Global variables
    String symbol;
    String name;

    public Company() {
    }

    public Company(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    protected Company(Parcel in) {
        symbol = in.readString();
        name = in.readString();
    }

    public static final Creator<Company> CREATOR = new Creator<Company>() {
        @Override
        public Company createFromParcel(Parcel in) {
            return new Company(in);
        }

        @Override
        public Company[] newArray(int size) {
            return new Company[size];
        }
    };

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(symbol);
        dest.writeString(name);
    }
}
