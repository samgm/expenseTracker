//package com.antso.expenses.analytics;
//
//import com.antso.expenses.entities.Transaction;
//import com.antso.expenses.enums.TransactionDirection;
//import com.antso.expenses.enums.TransactionType;
//import com.antso.expenses.transactions.TransactionManager;
//import com.antso.expenses.transactions.TransactionUpdateEvent;
//
//import java.math.BigDecimal;
//import java.util.Collection;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Observable;
//import java.util.Observer;
//import java.util.TreeMap;
//
//public class Analytics implements Observer {
//
//    class TransactionValue {
//        BigDecimal value;
//        Integer order;
//    }
//
//    private final TransactionDirection direction;
//    private final TransactionType type;
//    //Order
//    // direction and type
//    // budget
//    // account
//    // value
//    // ?description
//    // ?date
//
//    //all are by direction and type
//
//    private final Map<BigDecimal, Integer> valueRanking = new HashMap<>();
//    private final LinkedList<String> accountRanking = new LinkedList<>();
//    private final LinkedList<String> budgetRanking = new LinkedList<>();
//
//    //Select Value
//    private final Map<String, LinkedList<String>> accountRankingByValue = new HashMap<>();
//    private final Map<String, LinkedList<String>> budgetRankingByValue = new HashMap<>();
//    // then account
//    private final Map<String, LinkedList<String>> budgetRankingByValueAccount = new HashMap<>();
//    // then budget
//    private final Map<String, LinkedList<String>> accountRankingByValueBudget = new HashMap<>();
//
//    //Select Account
//    private final Map<String, LinkedList<BigDecimal>> valueRankingByAccount = new HashMap<>();
//    private final Map<String, LinkedList<String>> budgetRankingByAccount = new HashMap<>();
//    // then value -> budgetRankingByValueAccount
//    // then budget
//    private final Map<String, LinkedList<BigDecimal>> valueRankingByAccountBudget = new HashMap<>();
//
//    //Select Budget
//    private final Map<String, LinkedList<BigDecimal>> valueRankingByBudget = new HashMap<>();
//    private final Map<String, LinkedList<String>> budgetRankingByBudget = new HashMap<>();
//    // then value -> accountRankingByValueBudget
//
//    // then account
//    private final Map<String, LinkedList<BigDecimal>> valueRankingByBudgetAccount = new HashMap<>();
//
//
//
//    public Analytics(final TransactionDirection direction, final TransactionType type) {
//        this.direction = direction;
//        this.type = type;
//    }
//
//    public void initialize() {
//        TransactionManager.TRANSACTION_MANAGER().addObserver(this);
//    }
//
//    @Override
//    public void update(Observable observable, Object data) {
//        if (observable instanceof TransactionManager) {
//            TransactionUpdateEvent event = (TransactionUpdateEvent)data;
//            switch (event.reason) {
//                case START:
//                    break;
//                case ADD:
//                case UPD:
//                    break;
//
//                case DEL:
//                    break;
//                case UNDEF:
//                    break;
//            }
//
//        }
//
//    }
//
//    private void update(Transaction transaction) {
//        if (transaction.getType().equals(type) && transaction.getDirection().equals(direction)) {
//            if(valueRanking.containsKey(transaction.getValue())) {
//                Integer num = valueRanking.get(transaction.getValue());
//                valueRanking.put(transaction.getValue(), num++);
//            } else {
//                valueRanking.put(transaction.getValue(), new Integer(1));
//            }
//
//            private final LinkedList<String> accountRanking = new LinkedList<>();
//            private final LinkedList<String> budgetRanking = new LinkedList<>();
//
////            accountRankingByValue.
////            budgetRankingByValue = new HashMap<>();
////            budgetRankingByValueAccount = new HashMap<>();
////            accountRankingByValueBudget = new HashMap<>();
////            valueRankingByAccount = new HashMap<>();
////            budgetRankingByAccount = new HashMap<>();
////            valueRankingByAccountBudget = new HashMap<>();
////            valueRankingByBudget = new HashMap<>();
////            budgetRankingByBudget = new HashMap<>();
////            valueRankingByBudgetAccount
//        }
//    }
//
//    public Collection<Transaction> getFrequentTransaction(TransactionType type, TransactionDirection direction) {
//        return null;
//    }
//
//    public Collection<Transaction> getFrequentTransaction(TransactionDirection direction, BigDecimal value) {
//        return null;
//    }
//
//}
