package com.antso.expensesmanager.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.ParcelableAccount;
import com.antso.expensesmanager.persistence.DatabaseHelper;
import com.antso.expensesmanager.persistence.EntityIdGenerator;

import java.math.BigDecimal;


public class AccountEntryActivity extends Activity {

    private BigDecimal accountValue = BigDecimal.ZERO;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_entry_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(getApplicationContext());
        }

//        final EditText date = (EditText)findViewById(R.id.transactionDate);
//        date.setText(DateTime.now().toString(Utils.getDatePatten()));
//        final EditText value = (EditText)findViewById(R.id.transactionValue);
//        final AutoCompleteTextView description = (AutoCompleteTextView)findViewById(R.id.transactionDesc);
//        final Spinner accountSpinner = (Spinner)findViewById(R.id.transactionAccountSpinner);
//        Spinner budgetSpinner = (Spinner)findViewById(R.id.transactionBudgetSpinner);

        Button confirm = (Button)findViewById(R.id.transactionConfirm);
        Button cancel = (Button)findViewById(R.id.transactionCancel);

//        value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    String valueStr = value.getText().toString();
//                    //TODO wash not allowed chars
//                    transactionValue = BigDecimal.valueOf(Double.parseDouble(valueStr));
//                }
//            }
//        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Account account = (Account)(accountSpinner.getSelectedItem());
//
//                String valueStr = value.getText().toString();
//                //TODO wash not allowed chars
//                transactionValue = BigDecimal.valueOf(Double.parseDouble(valueStr));

                Account account = new Account(
                        EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Account.class),
                        "name",
                        BigDecimal.ONE,
                        1 /*color*/);

                dbHelper.insertAccount(account);

                Intent returnIntent = new Intent();
                returnIntent.putExtra("account", new ParcelableAccount(account));
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
