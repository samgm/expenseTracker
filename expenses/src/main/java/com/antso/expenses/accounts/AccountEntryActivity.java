package com.antso.expenses.accounts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.entities.Account;
import com.antso.expenses.persistence.EntityIdGenerator;
import com.antso.expenses.utils.IntentParamNames;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.Utils;
import com.antso.expenses.views.CircleSectorView;
import com.antso.expenses.views.ColorPickerDialog;
import com.antso.expenses.views_helpers.ValueEditText;

import java.math.BigDecimal;


public class AccountEntryActivity extends AppCompatActivity {
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Creating view
        color = (CircleSectorView)findViewById(R.id.accountColor);
        name = (EditText)findViewById(R.id.accountName);
        value.createView(R.id.accountValue, R.id.accountValueCurrency, BigDecimal.ZERO);
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
        if (isEdit) {
            title.setText(R.string.account_edit_title);
        } else {
            title.setText(R.string.account_entry_title);
        }
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
        getMenuInflater().inflate(R.menu.menu_account_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id ==  android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        if (id == R.id.action_account_confirm) {
            if (isEdit) {
                Account account = new Account(
                        loadedAccount.getId(),name.getText().toString(),
                        value.getValue(), color.getColor());

                AccountManager.ACCOUNT_MANAGER().updateAccount(account);
                Utils.showUpdatedToast(this, account.toString());
            } else {
                Account account = new Account(
                        EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Account.class),
                        name.getText().toString(),
                        value.getValue(),color.getColor());

                AccountManager.ACCOUNT_MANAGER().insertAccount(account);
                Utils.showAddedToast(this, account.toString());
            }

            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
