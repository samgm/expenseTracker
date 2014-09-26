package com.antso.expensesmanager.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.views.CircleSectorView;
import com.antso.expensesmanager.views.ColorPickerDialog;

import java.math.BigDecimal;


public class AccountEntryActivity extends Activity {

    private BigDecimal accountValue = BigDecimal.ZERO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_entry_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        final EditText name = (EditText)findViewById(R.id.accountName);
        final EditText value = (EditText)findViewById(R.id.accountValue);

        final CircleSectorView color = (CircleSectorView)findViewById(R.id.accountColor);

        final Button confirm = (Button)findViewById(R.id.accountConfirm);
        final Button cancel = (Button)findViewById(R.id.accountCancel);

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog c = new ColorPickerDialog(AccountEntryActivity.this, new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int c) {
                        if(color != null) {
                            color.setColor(c);
                            color.invalidate();
                        }
                    }
                });
                c.show();
            }
        });

        value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String valueStr = value.getText().toString();
                    valueStr = Utils.washDecimalNumber(valueStr);
                    value.setText(valueStr);
                    accountValue = BigDecimal.valueOf(Double.parseDouble(valueStr)).setScale(2);
                }
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueStr = value.getText().toString();
                valueStr = Utils.washDecimalNumber(valueStr);
                accountValue = BigDecimal.valueOf(Double.parseDouble(valueStr));

                Account account = new Account(
                        EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Account.class),
                        name.getText().toString(),
                        accountValue,
                        color.getColor());

                AccountManager.ACCOUNT_MANAGER.insertAccount(account);

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
        getMenuInflater().inflate(R.menu.menu_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
