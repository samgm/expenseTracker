//package com.antso.expensesmanager.entities;
//
//import android.os.Parcel;
//import android.os.Parcelable;
//
//import com.antso.expensesmanager.enums.TransactionDirection;
//import com.antso.expensesmanager.enums.TransactionType;
//import com.antso.expensesmanager.utils.Utils;
//
//import org.joda.time.DateTime;
//
//import java.math.BigDecimal;
//
//public class ParcelableTransaction implements Parcelable {
//    private Transaction transaction;
//
//    public ParcelableTransaction(Transaction transaction) {
//        super();
//        this.transaction = transaction;
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(transaction.getId());
//        dest.writeString(transaction.getDescription());
//        dest.writeInt(transaction.getDirection().getIntValue());
//        dest.writeInt(transaction.getType().getIntValue());
//        dest.writeString(transaction.getAccountId());
//        dest.writeString(transaction.getBudgetId());
//        dest.writeDouble(transaction.getValue().doubleValue());
//        dest.writeString(transaction.getDateTime().toString(Utils.getDatePatten()));
//        dest.writeString(transaction.getLinkedTransactionId());
//    }
//
//    public Transaction getTransaction() {
//        return transaction;
//    }
//
//    public static final Parcelable.Creator CREATOR
//            = new Parcelable.Creator<ParcelableTransaction>() {
//        @Override
//        public ParcelableTransaction createFromParcel(Parcel source) {
//            Transaction transaction = new Transaction(
//                    source.readString(),
//                    source.readString(),
//                    TransactionDirection.valueOf(source.readInt()),
//                    TransactionType.valueOf(source.readInt()),
//                    source.readString(),
//                    source.readString(),
//                    BigDecimal.valueOf(source.readDouble()),
//                    DateTime.parse(source.readString(), Utils.getDateFormatter()));
//            transaction.setLinkedTransactionId(source.readString());
//            return new ParcelableTransaction(transaction);
//        }
//
//        @Override
//        public ParcelableTransaction[] newArray(int size) {
//            return new ParcelableTransaction[size];
//        }
//    };
//}
