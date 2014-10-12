package com.antso.expensesmanager.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.IntentParamNames;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.views.CircleSectorView;
import com.antso.expensesmanager.views.ColorPickerDialog;
import com.antso.expensesmanager.views_helpers.ValueEditText;

import java.math.BigDecimal;


public class AccountEntryActivity extends Activity {
    private ValueEditText value;
    private CircleSectorView color;
    private EditText name;

    private Account loadedAccount;
    private boolean isEdit;

    public AccountEntryActivity() {
        super();

        value = new ValueEditText(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_entry_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        //Creating view
        color = (CircleSectorView)findViewById(R.id.accountColor);
        name = (EditText)findViewById(R.id.accountName);
        value.createView(R.id.accountValue, BigDecimal.ZERO);
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog c = new ColorPickerDialog(AccountEntryActivity.this, new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int c) {
                        if (color != null) {
                            color.setColor(c);
                            color.invalidate();
                        }
                    }
                }, MaterialColours.getAccountColors());
                c.show();
            }
        });


        //Get params and load defaults
        String id = getIntent().getStringExtra(IntentParamNames.ACCOUNT_ID);
        loadAccount(id);

        color.setColor(loadedAccount.getColor());
        name.setText(loadedAccount.getName());
        value.setValue(loadedAccount.getInitialBalance());

        final TextView title = (TextView) findViewById(R.id.accountEntryTitle);
        final Button confirm = (Button)findViewById(R.id.accountConfirm);
        final Button cancel = (Button)findViewById(R.id.accountCancel);
        if (isEdit) {
            title.setText(R.string.account_edit_title);
            confirm.setText(R.string.button_confirm_edit_label);
        } else {
            title.setText(R.string.account_entry_title);
            confirm.setText(R.string.button_confirm_add_label);
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    Account account = new Account(
                            loadedAccount.getId(),
                            name.getText().toString(),
                            value.getValue(),
                            color.getColor());

                    AccountManager.ACCOUNT_MANAGER().updateAccount(account);

                } else {
                    Account account = new Account(
                            EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Account.class),
                            name.getText().toString(),
                            value.getValue(),
                            color.getColor());

                    AccountManager.ACCOUNT_MANAGER().insertAccount(account);
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

    private void loadAccount(String id) {
        if (id == null || id.isEmpty()) {
            isEdit = false;
            loadedAccount = new Account(
                    null,
                    getText(R.string.name).toString(),
                    BigDecimal.ZERO,
                    MaterialColours.getAccountColors().get(0));
        } else {
            isEdit = true;
            loadedAccount = AccountManager.ACCOUNT_MANAGER().getAccountInfo(id).account;
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
