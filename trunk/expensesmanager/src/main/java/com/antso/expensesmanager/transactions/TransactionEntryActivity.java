package com.antso.expensesmanager.transactions;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.antso.expensesmanager.enums.TransactionFrequencyUnit;
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
    private DateTime endDate = DateTime.now();

    private Collection<Account> accounts;
    private AccountSpinnerAdapter accountSpinnerAdapter;
    private Collection<Budget> budgets;
    private BudgetSpinnerAdapter budgetSpinnerAdapter;

    private TransactionDirection direction;
    private TransactionType type;
    private boolean isOrderEdit;
    private Transaction editTransaction;

    private CircleSectorView color;
    private EditText transactionDateText;
    private EditText value;
    private MultiAutoCompleteTextView description;
    private Spinner accountSpinner;
    private Spinner accountSecondarySpinner;
    private Spinner budgetSpinner;
    private ImageButton accountChange;
    private ImageButton accountSecondaryChange;
    private ImageButton budgetChange;
    private CheckBox recurrent;
    private LinearLayout recurrentDetails;
    private Spinner frequencySpinner;
    private Spinner frequencyUnitSpinner;
    private EditText endDateText;

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
            accountSpinnerAdapter = AccountSpinnerAdapter.create(this, R.layout.text_spinner_item,
                    accounts.toArray(new Account[0]));
        }

        if (budgets == null) {
            budgets = BudgetManager.BUDGET_MANAGER.getBudgets();
            budgetSpinnerAdapter = BudgetSpinnerAdapter.create(this, R.layout.text_spinner_item,
                    budgets.toArray(new Budget[0]));
        }

        //Creating view
        color = (CircleSectorView) findViewById(R.id.transactionColor);
        transactionDateText = (EditText) findViewById(R.id.transactionDate);
        value = (EditText) findViewById(R.id.transactionValue);
        description = (MultiAutoCompleteTextView) findViewById(R.id.transactionDesc);
        accountSpinner = (Spinner) findViewById(R.id.transactionAccountSpinner);
        accountSecondarySpinner = (Spinner) findViewById(R.id.transactionSecondaryAccountSpinner);
        budgetSpinner = (Spinner) findViewById(R.id.transactionBudgetSpinner);

        accountChange = (ImageButton) findViewById(R.id.transactionAccountButton);
        accountSecondaryChange = (ImageButton) findViewById(R.id.transactionSecondaryAccountButton);
        budgetChange = (ImageButton) findViewById(R.id.transactionBudgetButton);

        recurrent = (CheckBox) findViewById(R.id.transactionRecurrentCheckbox);
        recurrentDetails = (LinearLayout) findViewById(R.id.transactionRecurrentDetailsLayout);
        frequencySpinner = (Spinner) findViewById(R.id.transactionFrequency);
        frequencyUnitSpinner = (Spinner) findViewById(R.id.transactionFrequencyUnit);
        endDateText = (EditText) findViewById(R.id.transactionRecurrentStartDate);

        confirm = (Button) findViewById(R.id.transactionConfirm);
        cancel = (Button) findViewById(R.id.transactionCancel);

        description.setAdapter(new ArrayAdapter<String>(this,
                R.layout.text_spinner_item,
                TransactionManager.TRANSACTION_MANAGER.getDescriptionsArray()));
        description.setTokenizer(new SpaceTokenizer());

        accountSpinner.setAdapter(accountSpinnerAdapter);
        accountSecondarySpinner.setAdapter(accountSpinnerAdapter);
        budgetSpinner.setAdapter(budgetSpinnerAdapter);

        transactionDateText.setOnFocusChangeListener(onDateFocusChanged());
        transactionDateText.setOnClickListener(onDateClick());
        value.setOnFocusChangeListener(onValueFocusChanged());
        accountChange.setOnClickListener(onAccountChangeClick());
        accountSecondaryChange.setOnClickListener(onAccountSecondaryChangeClick());
        budgetChange.setOnClickListener(onBudgetChangeClick());

        recurrent.setOnClickListener(onRecurrentClick());
        frequencySpinner.setAdapter(frequencySpinnerAdapter());
        frequencySpinner.setOnItemSelectedListener(onFrequencySpinnerSelected());
        endDateText.setOnClickListener(onEndDateClick());
        endDateText.setOnFocusChangeListener(onEndDateFocusChanged());

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
            endDateText.setText(Utils.formatDate(endDate));

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
                case Undef:
                    break;
            }

            transactionDateText.setText(Utils.formatDate(DateTime.now()));
        } else {
            //init for edit
            isOrderEdit = true;
            editTransaction = TransactionManager.TRANSACTION_MANAGER.getTransactionById(id);
            direction = editTransaction.getDirection();
            type = editTransaction.getType();
            color.setColor(MaterialColours.RED_500);
            description.setText(editTransaction.getDescription());
            value.setText(editTransaction.getValue().toString());
            transactionDateText.setText(Utils.formatDate(editTransaction.getDateTime()));
            accountSpinner.setSelection(accountSpinnerAdapter.getIndexById(editTransaction.getAccountId()));
            budgetSpinner.setSelection(budgetSpinnerAdapter.getIndexById(editTransaction.getBudgetId()));
//TODO
//            frequencySpinner.setSelection(frequencySpinnerAdapter().getIndexById(editTransaction.getAccountId()));
//            frequencyUnitSpinner.setSelection(budgetSpinnerAdapter.getIndexById(editTransaction.getBudgetId()));
            endDateText.setText(Utils.formatDate(editTransaction.getEndDate()));
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

    private View.OnClickListener onRecurrentClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox)v;
                recurrentDetails.setVisibility(checkBox.isChecked() ? View.VISIBLE : View.GONE);
            }
        };
    }

    private ArrayAdapter<TransactionFrequencyUnit> frequencySpinnerAdapter() {
        return new ArrayAdapter<TransactionFrequencyUnit>(this, R.layout.text_spinner_item,
                TransactionFrequencyUnit.valuesButUndef());
    }

    private AdapterView.OnItemSelectedListener onFrequencySpinnerSelected() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TransactionFrequencyUnit unit = TransactionFrequencyUnit.valueOf(position + 1);
                Integer[] lengthArray = null;
                switch (unit) {
                    case Day:
                        lengthArray = Utils.DaySingleValues;
                        break;
                    case Week:
                        lengthArray = Utils.WeekValues;
                        break;
                    case Month:
                        lengthArray = Utils.MonthValues;
                        break;
                    case Year:
                        lengthArray = Utils.YearValues;
                        break;
                    case Undef:
                    default:
                        lengthArray = new Integer[0];
                        break;
                }

                frequencyUnitSpinner.setAdapter(new ArrayAdapter<Integer>(TransactionEntryActivity.this,
                        R.layout.text_spinner_item, lengthArray));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }

    private View.OnFocusChangeListener onEndDateFocusChanged() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DateTime now = DateTime.now();
                    DatePickerDialog datePicker = new DatePickerDialog(
                            TransactionEntryActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    endDate = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                                    endDateText.setText(Utils.formatDate(endDate));
                                }
                            }, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth()
                    );
                    datePicker.show();
                }
            }
        };
    }

    private View.OnClickListener onEndDateClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime now = DateTime.now();
                DatePickerDialog datePicker = new DatePickerDialog(
                        TransactionEntryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                endDate = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                                endDateText.setText(Utils.formatDate(endDate));
                            }
                        }, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth()
                );
                datePicker.show();
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
// TODO
//                Pair<Transaction, Transaction> pair = createTransferTransaction(t1Id, t2Id);
//
//                TransactionManager.TRANSACTION_MANAGER.removeTransaction(editTransaction);
//                TransactionManager.TRANSACTION_MANAGER.removeTransaction(editTransaction);
//
//                TransactionManager.TRANSACTION_MANAGER.insertTransaction(pair.first);
//                TransactionManager.TRANSACTION_MANAGER.insertTransaction(pair.second);
                break;
            case Single:
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
        if(recurrent.isChecked()) {
            TransactionFrequencyUnit frequencyUnit = (TransactionFrequencyUnit) (frequencySpinner.getSelectedItem());
            Integer frequency = (Integer) (frequencyUnitSpinner.getSelectedItem());
            t1.setRecurrent(true);
            t1.setFrequency(frequency);
            t1.setFrequencyUnit(frequencyUnit);
            t1.setEndDate(endDate);
            t2.setRecurrent(true);
            t2.setFrequency(frequency);
            t2.setFrequencyUnit(frequencyUnit);
            t2.setEndDate(endDate);
        }
        return new Pair<Transaction, Transaction>(t1, t2);
    }

    private Transaction createSingleTransaction(String id) {
        Account account = (Account) (accountSpinner.getSelectedItem());
        Budget budget = (Budget) (budgetSpinner.getSelectedItem());

        Transaction t = new Transaction(
                id,
                description.getText().toString(),
                direction,
                TransactionType.Single,
                account != null ? account.getId() : "",
                budget != null ? budget.getId() : "",
                transactionValue,
                transactionDate);
        if(recurrent.isChecked()) {
            TransactionFrequencyUnit frequencyUnit = (TransactionFrequencyUnit) (frequencySpinner.getSelectedItem());
            Integer frequency = (Integer) (frequencyUnitSpinner.getSelectedItem());
            t.setRecurrent(true);
            t.setFrequency(frequency);
            t.setFrequencyUnit(frequencyUnit);
            t.setEndDate(endDate);
        }
        return t;
    }
}