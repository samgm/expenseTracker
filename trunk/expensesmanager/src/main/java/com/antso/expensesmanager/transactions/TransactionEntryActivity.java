package com.antso.expensesmanager.transactions;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
    private Collection<Budget> budgets;

    private TransactionDirection direction;
    private TransactionType type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_entry_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        final CircleSectorView color = (CircleSectorView)findViewById(R.id.transactionColor);
        int directionInt = getIntent().getIntExtra("transaction_direction", TransactionDirection.Undef.getIntValue());
        int typeInt = getIntent().getIntExtra("transaction_type", TransactionType.Undef.getIntValue());
        direction = TransactionDirection.valueOf(directionInt);
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
                final LinearLayout secondaryAccountLayout = (LinearLayout)findViewById(R.id.transactionSecondaryAccountLayout);
                final TextView secondaryAccountLabel = (TextView)findViewById(R.id.transactionSecondaryAccountLabel);
                secondaryAccountLayout.setVisibility(View.VISIBLE);
                secondaryAccountLabel.setVisibility(View.VISIBLE);
                break;
            case Single:
            case Recurrent:
            case Undef:
                break;
        }

        if (accounts == null) {
            accounts = AccountManager.ACCOUNT_MANAGER.getAccounts();
        }
        if (budgets == null) {
            budgets = BudgetManager.BUDGET_MANAGER.getBudgets();
        }

        final EditText date = (EditText)findViewById(R.id.transactionDate);
        date.setText(Utils.formatDate(DateTime.now()));
        final EditText value = (EditText)findViewById(R.id.transactionValue);
        final MultiAutoCompleteTextView description = (MultiAutoCompleteTextView)findViewById(R.id.transactionDesc);
        final Spinner accountSpinner = (Spinner)findViewById(R.id.transactionAccountSpinner);
        final Spinner accountSecondarySpinner = (Spinner)findViewById(R.id.transactionSecondaryAccountSpinner);
        final Spinner budgetSpinner = (Spinner)findViewById(R.id.transactionBudgetSpinner);

        ImageButton accountChange = (ImageButton)findViewById(R.id.transactionAccountButton);
        ImageButton accountSecondaryChange = (ImageButton)findViewById(R.id.transactionSecondaryAccountButton);
        ImageButton budgetChange = (ImageButton)findViewById(R.id.transactionBudgetButton);

        Button confirm = (Button)findViewById(R.id.transactionConfirm);
        Button cancel = (Button)findViewById(R.id.transactionCancel);

        description.setAdapter(new ArrayAdapter<String>(this,
                R.layout.text_spinner_item,
                TransactionManager.TRANSACTION_MANAGER.getDescriptionsArray()));
        description.setTokenizer(new SpaceTokenizer());

        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
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
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String valueStr = value.getText().toString();
                    valueStr = Utils.washDecimalNumber(valueStr);
                    value.setText(valueStr);
                    transactionValue = BigDecimal.valueOf(Double.parseDouble(valueStr)).setScale(2);
                }
            }
        });

        accountSpinner.setAdapter(
                new ArrayAdapter<Account>(this, R.layout.text_spinner_item,
                        accounts.toArray(new Account[0])));
        accountSecondarySpinner.setAdapter(
                new ArrayAdapter<Account>(this, R.layout.text_spinner_item,
                        accounts.toArray(new Account[0])));
        budgetSpinner.setAdapter(
                new ArrayAdapter<Budget>(this, R.layout.text_spinner_item,
                budgets.toArray(new Budget[0])));

        accountChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = accountSpinner.getSelectedItemPosition();
                accountSpinner.setSelection((index + 1) % accountSpinner.getAdapter().getCount());
            }
        });

        accountSecondaryChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = accountSecondarySpinner.getSelectedItemPosition();
                accountSecondarySpinner.setSelection((index + 1) % accountSecondarySpinner.getAdapter().getCount());
            }
        });

        budgetChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = budgetSpinner.getSelectedItemPosition();
                budgetSpinner.setSelection((index + 1) % budgetSpinner.getAdapter().getCount());
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = (Account)(accountSpinner.getSelectedItem());
                Account accountSecondary = (Account)(accountSecondarySpinner.getSelectedItem());

                Budget budget = (Budget)(budgetSpinner.getSelectedItem());

                String valueStr = value.getText().toString();
                valueStr = Utils.washDecimalNumber(valueStr);
                transactionValue = BigDecimal.valueOf(Double.parseDouble(valueStr));

                switch (type) {
                    case Transfer:
                        String t1Id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);
                        String t2Id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);
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
                        TransactionManager.TRANSACTION_MANAGER.insertTransaction(t1);
                        TransactionManager.TRANSACTION_MANAGER.insertTransaction(t2);
                        break;
                    case Single:
                    case Recurrent:
                    case Undef:
                        Transaction transaction = new Transaction(
                                EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class),
                                description.getText().toString(),
                                direction,
                                TransactionType.Single,
                                account != null ? account.getId() : "",
                                budget != null ? budget.getId() : "",
                                transactionValue,
                                transactionDate);

                        TransactionManager.TRANSACTION_MANAGER.insertTransaction(transaction);
                        break;
                }

                setResult(RESULT_OK);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if(id ==  android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
