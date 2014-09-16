package com.antso.expensesmanager.transactions;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.ParcelableTransaction;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.Utils;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collection;


public class TransactionEntryActivity extends Activity {

    private DateTime transactionDate = DateTime.now();
    private BigDecimal transactionValue = BigDecimal.ZERO;
    private DatabaseHelper dbHelper;

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

        final LinearLayout color = (LinearLayout)findViewById(R.id.transactionColor);
        int directionInt = getIntent().getIntExtra("transaction_direction", TransactionDirection.Undef.getIntValue());
        int typeInt = getIntent().getIntExtra("transaction_type", TransactionType.Undef.getIntValue());
        direction = TransactionDirection.valueOf(directionInt);
        type = TransactionType.valueOf(typeInt);

        switch (direction) {
            case In:
                color.setBackgroundColor(Color.GREEN);
                break;
            case Out:
                color.setBackgroundColor(Color.RED);
                break;
            case Undef:
                break;
        }

        switch (type) {
            case Transfer:
                color.setBackgroundColor(Color.BLUE);
                final TextView accountLabel = (TextView)findViewById(R.id.transactionAccountLabel);
                final LinearLayout secondaryAccountLayout = (LinearLayout)findViewById(R.id.transactionSecondaryAccountLayout);
                final TextView secondaryAccountLabel = (TextView)findViewById(R.id.transactionSecondaryAccountLabel);
                accountLabel.setVisibility(View.VISIBLE);
                secondaryAccountLayout.setVisibility(View.VISIBLE);
                secondaryAccountLabel.setVisibility(View.VISIBLE);
                break;
            case Single:
            case Recurrent:
            case Undef:
                break;
        }

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getApplicationContext());
            accounts = dbHelper.getAccounts();
            //TODO budgets = dbHelper.getBudgets();
        }

        final EditText date = (EditText)findViewById(R.id.transactionDate);
        date.setText(DateTime.now().toString(Utils.getDatePatten()));
        final EditText value = (EditText)findViewById(R.id.transactionValue);
        final AutoCompleteTextView description = (AutoCompleteTextView)findViewById(R.id.transactionDesc);
        final Spinner accountSpinner = (Spinner)findViewById(R.id.transactionAccountSpinner);
        final Spinner accountSecondarySpinner = (Spinner)findViewById(R.id.transactionSecondaryAccountSpinner);

        Spinner budgetSpinner = (Spinner)findViewById(R.id.transactionBudgetSpinner);

        Button confirm = (Button)findViewById(R.id.transactionConfirm);
        Button cancel = (Button)findViewById(R.id.transactionCancel);

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
                                    transactionDate = new DateTime(year, monthOfYear, dayOfMonth, 0, 0);
                                    date.setText(transactionDate.toString(Utils.getDatePatten()));
                                }
                            }, now.getYear(), now.getMonthOfYear(), now.getDayOfMonth()
                    );
                    datePicker.show();
                }
            }
        });

        value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String valueStr = value.getText().toString();
                    //TODO wash not allowed chars
                    transactionValue = BigDecimal.valueOf(Double.parseDouble(valueStr));
                }
            }
        });

        accountSpinner.setAdapter(
                new ArrayAdapter<Account>(this, R.layout.text_spinner_item,
                        accounts.toArray(new Account[0])));
        accountSecondarySpinner.setAdapter(
                new ArrayAdapter<Account>(this, R.layout.text_spinner_item,
                        accounts.toArray(new Account[0]))
        );


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = (Account)(accountSpinner.getSelectedItem());
                Account accountSecondary = (Account)(accountSecondarySpinner.getSelectedItem());

                String valueStr = value.getText().toString();
                //TODO wash not allowed chars
                transactionValue = BigDecimal.valueOf(Double.parseDouble(valueStr));
                Intent returnIntent = new Intent();

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
                                "budget",
                                transactionValue,
                                transactionDate);
                        Transaction t2 = new Transaction(
                                t2Id,
                                description.getText().toString(),
                                TransactionDirection.In,
                                TransactionType.Transfer,
                                accountSecondary != null ? accountSecondary.getId() : "",
                                "budget",
                                transactionValue,
                                transactionDate);
                        t1.setLinkedTransactionId(t2Id);
                        t2.setLinkedTransactionId(t1Id);
                        dbHelper.insertTransactions(t1);
                        dbHelper.insertTransactions(t2);
                        returnIntent.putExtra("transaction_out", new ParcelableTransaction(t1));
                        returnIntent.putExtra("transaction_in", new ParcelableTransaction(t2));
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
                                "budget",
                                transactionValue,
                                transactionDate);

                        dbHelper.insertTransactions(transaction);
                        returnIntent.putExtra("transaction", new ParcelableTransaction(transaction));
                        break;
                }

                setResult(RESULT_OK, returnIntent);
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
