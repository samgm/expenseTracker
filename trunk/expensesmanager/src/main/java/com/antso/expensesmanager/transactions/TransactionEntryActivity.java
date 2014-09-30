package com.antso.expensesmanager.transactions;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.AccountSpinnerAdapter;
import com.antso.expensesmanager.utils.BudgetSpinnerAdapter;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.utils.SpaceTokenizer;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.views.CircleSectorView;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collection;


public class TransactionEntryActivity extends Activity {

    private DateTime transactionDate = DateTime.now();
    private BigDecimal transactionValue = BigDecimal.ZERO;

    private Collection<Account> accounts;
    private AccountSpinnerAdapter accountSpinnerAdapter;
    private Collection<Budget> budgets;
    private BudgetSpinnerAdapter budgetSpinnerAdapter;

    private TransactionDirection direction;
    private TransactionType type;
    private boolean isOrderEdit;
    private Transaction editTransaction;

    private CircleSectorView color;
    private EditText date;
    private EditText value;
    private MultiAutoCompleteTextView description;
    private Spinner accountSpinner;
    private Spinner accountSecondarySpinner;
    private Spinner budgetSpinner;
    private ImageButton accountChange;
    private ImageButton accountSecondaryChange;
    private ImageButton budgetChange;
    private Button confirm;
    private Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_entry_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if (accounts == null) {
            accounts = AccountManager.ACCOUNT_MANAGER.getAccounts();
            accountSpinnerAdapter = new AccountSpinnerAdapter(this, R.layout.text_spinner_item,
                    accounts.toArray(new Account[0]));
        }

        if (budgets == null) {
            budgets = BudgetManager.BUDGET_MANAGER.getBudgets();
            budgetSpinnerAdapter = new BudgetSpinnerAdapter(this, R.layout.text_spinner_item,
                            budgets.toArray(new Budget[0]));
        }

        //Creating view
        color = (CircleSectorView) findViewById(R.id.transactionColor);
        date = (EditText) findViewById(R.id.transactionDate);
        value = (EditText) findViewById(R.id.transactionValue);
        description = (MultiAutoCompleteTextView) findViewById(R.id.transactionDesc);
        accountSpinner = (Spinner) findViewById(R.id.transactionAccountSpinner);
        accountSecondarySpinner = (Spinner) findViewById(R.id.transactionSecondaryAccountSpinner);
        budgetSpinner = (Spinner) findViewById(R.id.transactionBudgetSpinner);

        accountChange = (ImageButton) findViewById(R.id.transactionAccountButton);
        accountSecondaryChange = (ImageButton) findViewById(R.id.transactionSecondaryAccountButton);
        budgetChange = (ImageButton) findViewById(R.id.transactionBudgetButton);

        confirm = (Button) findViewById(R.id.transactionConfirm);
        cancel = (Button) findViewById(R.id.transactionCancel);

        description.setAdapter(new ArrayAdapter<String>(this,
                R.layout.text_spinner_item,
                TransactionManager.TRANSACTION_MANAGER.getDescriptionsArray()));
        description.setTokenizer(new SpaceTokenizer());

        accountSpinner.setAdapter(accountSpinnerAdapter);
        accountSecondarySpinner.setAdapter(accountSpinnerAdapter);
        budgetSpinner.setAdapter(budgetSpinnerAdapter);

        date.setOnFocusChangeListener(onDateFocusChanged());
        date.setOnClickListener(onDateClick());
        value.setOnFocusChangeListener(onValueFocusChanged());
        accountChange.setOnClickListener(onAccountChangeClick());
        accountSecondaryChange.setOnClickListener(onAccountSecondaryChangeClick());
        budgetChange.setOnClickListener(onBudgetChangeClick());

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueStr = value.getText().toString();
                valueStr = Utils.washDecimalNumber(valueStr);
                transactionValue = BigDecimal.valueOf(Double.parseDouble(valueStr));

                if (!isOrderEdit) {
                    createNewTransactionAndSave();
                } else {
                    updateTransactionAndSave();
                }

                setResult(RESULT_OK);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        //get params
        String id = getIntent().getStringExtra("transaction_id");

        if (id == null || id.isEmpty()) {
            //init for entry
            isOrderEdit = false;

            int directionInt = getIntent().getIntExtra("transaction_direction", TransactionDirection.Undef.getIntValue());
            direction = TransactionDirection.valueOf(directionInt);
            int typeInt = getIntent().getIntExtra("transaction_type", TransactionType.Undef.getIntValue());
            type = TransactionType.valueOf(typeInt);

            switch (direction) {
                case In:
                    color.setColor(MaterialColours.GREEN_500);
                    break;
                case Out:
                    color.setColor(MaterialColours.RED_500);
                    break;
                case Undef:
                    break;
            }

            switch (type) {
                case Transfer:
                    color.setColor(MaterialColours.YELLOW_500);
                    final LinearLayout secondaryAccountLayout = (LinearLayout) findViewById(R.id.transactionSecondaryAccountLayout);
                    final TextView secondaryAccountLabel = (TextView) findViewById(R.id.transactionSecondaryAccountLabel);
                    secondaryAccountLayout.setVisibility(View.VISIBLE);
                    secondaryAccountLabel.setVisibility(View.VISIBLE);
                    break;
                case Single:
                case Recurrent:
                case Undef:
                    break;
            }

            date.setText(Utils.formatDate(DateTime.now()));
        } else {
            //init for edit
            isOrderEdit = true;
            editTransaction = TransactionManager.TRANSACTION_MANAGER.getTransactionById(id);
            direction = editTransaction.getDirection();
            type = editTransaction.getType();
            color.setColor(MaterialColours.RED_500);
            description.setText(editTransaction.getDescription());
            value.setText(editTransaction.getValue().toString());
            date.setText(Utils.formatDate(editTransaction.getDateTime()));
            accountSpinner.setSelection(accountSpinnerAdapter.getIndexById(editTransaction.getAccountId()));
            budgetSpinner.setSelection(budgetSpinnerAdapter.getIndexById(editTransaction.getBudgetId()));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private View.OnFocusChangeListener onDateFocusChanged() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    final EditText date = (EditText) v;
                    DateTime now = DateTime.now();
                    DatePickerDialog datePicker = new DatePickerDialog(
                            TransactionEntryActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    transactionDate = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                                    date.setText(Utils.formatDate(transactionDate));
                                }
                            }, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth()
                    );
                    datePicker.show();
                }
            }
        };
    }

    private View.OnClickListener onDateClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText date = (EditText) v;
                DateTime now = DateTime.now();
                DatePickerDialog datePicker = new DatePickerDialog(
                        TransactionEntryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                transactionDate = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                                date.setText(Utils.formatDate(transactionDate));
                            }
                        }, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth()
                );
                datePicker.show();
            }
        };
    }

    private View.OnFocusChangeListener onValueFocusChanged() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final EditText value = (EditText) v;
                if (!hasFocus) {
                    String valueStr = value.getText().toString();
                    valueStr = Utils.washDecimalNumber(valueStr);
                    value.setText(valueStr);
                    transactionValue = BigDecimal.valueOf(Double.parseDouble(valueStr)).setScale(2);
                }
            }
        };
    }

    private View.OnClickListener onAccountChangeClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = accountSpinner.getSelectedItemPosition();
                accountSpinner.setSelection((index + 1) % accountSpinner.getAdapter().getCount());
            }
        };
    }

    private View.OnClickListener onAccountSecondaryChangeClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = accountSecondarySpinner.getSelectedItemPosition();
                accountSecondarySpinner.setSelection((index + 1) % accountSecondarySpinner.getAdapter().getCount());
            }
        };
    }

    private View.OnClickListener onBudgetChangeClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = budgetSpinner.getSelectedItemPosition();
                budgetSpinner.setSelection((index + 1) % budgetSpinner.getAdapter().getCount());
            }
        };
    }

    private void createNewTransactionAndSave() {

        switch (type) {
            case Transfer:
                String t1Id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);
                String t2Id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);

                Pair<Transaction, Transaction> pair = createTransferTransaction(t1Id, t2Id);

                TransactionManager.TRANSACTION_MANAGER.insertTransaction(pair.first);
                TransactionManager.TRANSACTION_MANAGER.insertTransaction(pair.second);
                break;
            case Single:
            case Recurrent:
            case Undef:
                String tId = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);

                Transaction t = createSingleTransaction(tId);

                TransactionManager.TRANSACTION_MANAGER.insertTransaction(t);
                break;
        }
    }

    private void updateTransactionAndSave() {

        switch (type) {
            case Transfer:
//                Pair<Transaction, Transaction> pair = createTransferTransaction(t1Id, t2Id);
//
//                TransactionManager.TRANSACTION_MANAGER.removeTransaction(editTransaction);
//                TransactionManager.TRANSACTION_MANAGER.removeTransaction(editTransaction);
//
//                TransactionManager.TRANSACTION_MANAGER.insertTransaction(pair.first);
//                TransactionManager.TRANSACTION_MANAGER.insertTransaction(pair.second);
                break;
            case Single:
            case Recurrent:
            case Undef:
                Transaction newTransaction = createSingleTransaction(editTransaction.getId());

                TransactionManager.TRANSACTION_MANAGER.removeTransaction(editTransaction);
                TransactionManager.TRANSACTION_MANAGER.insertTransaction(newTransaction);
                break;
        }
    }

    private Pair<Transaction, Transaction> createTransferTransaction(String t1Id, String t2Id) {
        Account account = (Account) (accountSpinner.getSelectedItem());
        Account accountSecondary = (Account) (accountSecondarySpinner.getSelectedItem());
        Budget budget = (Budget) (budgetSpinner.getSelectedItem());

        Transaction t1 = new Transaction(
                t1Id,
                description.getText().toString(),
                TransactionDirection.Out,
                TransactionType.Transfer,
                account != null ? account.getId() : "",
                budget != null ? budget.getId() : "",
                transactionValue,
                transactionDate);
        Transaction t2 = new Transaction(
                t2Id,
                description.getText().toString(),
                TransactionDirection.In,
                TransactionType.Transfer,
                accountSecondary != null ? accountSecondary.getId() : "",
                budget != null ? budget.getId() : "",
                transactionValue,
                transactionDate);
        t1.setLinkedTransactionId(t2Id);
        t2.setLinkedTransactionId(t1Id);

        return new Pair<Transaction, Transaction>(t1, t2);
    }

    private Transaction createSingleTransaction(String id) {
        Account account = (Account) (accountSpinner.getSelectedItem());
        Budget budget = (Budget) (budgetSpinner.getSelectedItem());

        return new Transaction(
                id,
                description.getText().toString(),
                direction,
                TransactionType.Single,
                account != null ? account.getId() : "",
                budget != null ? budget.getId() : "",
                transactionValue,
                transactionDate);
    }
}