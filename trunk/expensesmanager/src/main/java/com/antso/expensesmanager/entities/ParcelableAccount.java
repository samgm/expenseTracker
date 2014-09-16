package com.antso.expensesmanager.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

public class ParcelableAccount implements Parcelable {
    private Account account;

    public ParcelableAccount(Account account) {
        super();
        this.account = account;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(account.getId());
        dest.writeString(account.getName());
        dest.writeDouble(account.getInitialBalance().doubleValue());
        dest.writeInt(account.getColor());
    }

    public Account getAccount() {
        return account;
    }

    public static final Creator CREATOR
            = new Creator<ParcelableAccount>() {
        @Override
        public ParcelableAccount createFromParcel(Parcel source) {
            return new ParcelableAccount(new Account(
                    source.readString(),
                    source.readString(),
                    BigDecimal.valueOf(source.readDouble()),
                    source.readInt()));
        }

        @Override
        public ParcelableAccount[] newArray(int size) {
            return new ParcelableAccount[size];
        }
    };
}
