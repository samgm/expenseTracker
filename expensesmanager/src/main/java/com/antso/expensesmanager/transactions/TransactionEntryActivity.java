package com.antso.expensesmanager.transactions;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.entities.TransactionDirection;
import com.antso.expensesmanager.entities.TransactionType;
import com.antso.expensesmanager.persistence.DatabaseHelper;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_entry_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getApplicationContext());
            accounts = dbHelper.getAccounts();
            //TODO budgets = dbHelper.getBudgets();
        }

        final EditText date = (EditText)findViewById(R.id.transactionDate);
        date.setText(DateTime.now().toString(Utils.getDatePatten()));
        final EditText value = (EditText)findViewById(R.id.transactionValue);
        final AutoCompleteTextView description = (AutoCompleteTextView)findViewById(R.id.transactionDesc);
        Spinner accountSpinner = (Spinner)findViewById(R.id.transactionAccountSpinner);
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

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transaction transaction = new Transaction("id",
                        description.getText().toString(),
                        TransactionDirection.Out,
                        TransactionType.Single,
                        "account",
                        "budget",
                        transactionValue,
                        transactionDate);

                dbHelper.insertTransactions(transaction);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO navigate back in the UI
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
