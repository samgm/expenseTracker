package com.antso.expenses.transactions;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.accounts.AccountManager;
import com.antso.expenses.adapters.AccountSpinnerAdapter;
import com.antso.expenses.adapters.BudgetSpinnerAdapter;
import com.antso.expenses.adapters.StringArrayAdapter;
import com.antso.expenses.budgets.BudgetManager;
import com.antso.expenses.entities.Account;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.enums.TransactionDirection;
import com.antso.expenses.enums.TransactionType;
import com.antso.expenses.persistence.EntityIdGenerator;
import com.antso.expenses.utils.IntentParamNames;
import com.antso.expenses.utils.Settings;
import com.antso.expenses.utils.SpaceTokenizer;
import com.antso.expenses.utils.Utils;
import com.antso.expenses.views_helpers.ButtonChangeSpinner;
import com.antso.expenses.views_helpers.DateEditText;
import com.antso.expenses.views_helpers.FrequencySpinner;
import com.antso.expenses.views_helpers.TransactionLayout;
import com.antso.expenses.views_helpers.ValueEditText;

import java.math.BigDecimal;
import java.util.Collection;


public class TransactionEntryActivity extends Activity {
    private TransactionLayout layout;
    private DateEditText dateEditText;
    private DateEditText endDateEditText;
    private ValueEditText value;
    private MultiAutoCompleteTextView description;
    private ButtonChangeSpinner accountSpinner;
    private ButtonChangeSpinner accountSecondarySpinner;
    private ButtonChangeSpinner budgetSpinner;
    private FrequencySpinner recurrentFrequency;

    private Collection<Account> accounts;
    private AccountSpinnerAdapter accountSpinnerAdapter;
    private Collection<Budget> budgets;
    private BudgetSpinnerAdapter budgetSpinnerAdapter;

    private boolean isEdit;
    private Transaction loadedTransaction1;
    private Transaction loadedTransaction2;

    public TransactionEntryActivity(){
        super();

        layout = new TransactionLayout(this);
        endDateEditText = new DateEditText(this);
        dateEditText = new DateEditText(this);
        recurrentFrequency = new FrequencySpinner(this);
        accountSpinner = new ButtonChangeSpinner(this);
        accountSecondarySpinner = new ButtonChangeSpinner(this);
        budgetSpinner = new ButtonChangeSpinner(this);
        value = new ValueEditText(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_entry_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if (accounts == null) {
            accounts = AccountManager.ACCOUNT_MANAGER().getAccounts();
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            accountSpinnerAdapter = AccountSpinnerAdapter.create(this, R.layout.text_spinner_item,
                    accounts.toArray(new Account[0]));
        }

        if (budgets == null) {
            budgets = BudgetManager.BUDGET_MANAGER().getBudgets();
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            budgetSpinnerAdapter = BudgetSpinnerAdapter.create(this, R.layout.text_spinner_item,
                    budgets.toArray(new Budget[0]));
        }

        //Creating view
        description = (MultiAutoCompleteTextView) findViewById(R.id.transactionDesc);

        layout.createView(R.id.transactionColor, R.id.transactionSecondaryAccountLayout,
                R.id.transactionAccountLabel, R.id.transactionSecondaryAccountLabel,
                R.id.transactionRecurrentCheckbox, R.id.transactionRecurrentDetailsLayout);
        value.createView(R.id.transactionValue, R.id.transactionValueCurrency, BigDecimal.ZERO);
        accountSpinner.createView(R.id.transactionAccountSpinner, R.id.transactionAccountButton,
                accountSpinnerAdapter);
        accountSecondarySpinner.createView(R.id.transactionSecondaryAccountSpinner, R.id.transactionSecondaryAccountButton,
                accountSpinnerAdapter);
        budgetSpinner.createView(R.id.transactionBudgetSpinner, R.id.transactionBudgetButton,
                budgetSpinnerAdapter);

        recurrentFrequency.createView(R.id.transactionFrequencyUnit, R.id.transactionFrequency, true);
        dateEditText.createView(R.id.transactionDate, Utils.now());
        endDateEditText.createView(R.id.transactionRecurrentStartDate, Utils.now());
        description.setAdapter(StringArrayAdapter.create(this, R.layout.text_spinner_item,
                TransactionManager.TRANSACTION_MANAGER().getDescriptionsArray()));
        description.setTokenizer(new SpaceTokenizer());

        //Get params and load defaults
        String id = getIntent().getStringExtra(IntentParamNames.TRANSACTION_ID);
        int direction = getIntent().getIntExtra(IntentParamNames.TRANSACTION_DIRECTION, TransactionDirection.Undef.getIntValue());
        int type  = getIntent().getIntExtra(IntentParamNames.TRANSACTION_TYPE, TransactionType.Undef.getIntValue());

        loadTransactions(id, type, direction);

        layout.setTransaction(loadedTransaction1);
        description.setText(loadedTransaction1.getDescription());
        value.setValue(loadedTransaction1.getValue());
        accountSpinner.setSelection(accountSpinnerAdapter.getIndexById(loadedTransaction1.getAccountId()));
        budgetSpinner.setSelection(budgetSpinnerAdapter.getIndexById(loadedTransaction1.getBudgetId()));
        recurrentFrequency.setUnit(loadedTransaction1.getFrequencyUnit());
        recurrentFrequency.setValue(loadedTransaction1.getFrequency());
        dateEditText.setDate(loadedTransaction1.getDate());
        endDateEditText.setDate(loadedTransaction1.getEndDate());
        if (loadedTransaction2 != null) {
            accountSecondarySpinner.setSelection(accountSpinnerAdapter.getIndexById(loadedTransaction2.getAccountId()));
        } else {
            accountSecondarySpinner.setSelection(accountSpinnerAdapter.getIndexById(
                    Settings.getDefaultAccountId(this)));
        }

        final TextView title = (TextView) findViewById(R.id.transactionEntryTitle);
        final Button confirm = (Button) findViewById(R.id.transactionConfirm);
        final Button cancel = (Button) findViewById(R.id.transactionCancel);
        if (isEdit) {
            title.setText(R.string.transaction_edit_title);
            confirm.setText(R.string.button_confirm_edit_label);
        } else {
            title.setText(R.string.transaction_entry_title);
            confirm.setText(R.string.button_confirm_add_label);
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEdit) {
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

    private void loadTransactions(String id, int type, int direction) {
        if (id == null || id.isEmpty()) {
            isEdit = false;
            loadedTransaction1 = new Transaction(
                    null,
                    this.getString(R.string.description),
                    TransactionDirection.valueOf(direction),
                    TransactionType.valueOf(type),
                    Settings.getDefaultAccountId(this),
                    Settings.getDefaultBudgetId(this),
                    BigDecimal.ZERO,
                    Utils.now());
            loadedTransaction1.setEndDate(Utils.now());
        } else {
            isEdit = true;
            loadedTransaction1 = TransactionManager.TRANSACTION_MANAGER().getTransactionById(id);
            String linkedTransactionId = loadedTransaction1.getLinkedTransactionId();
            if (linkedTransactionId != null && !linkedTransactionId.isEmpty()) {
                loadedTransaction2 = TransactionManager.TRANSACTION_MANAGER().getTransactionById(linkedTransactionId);
            }
        }
    }

    private void createNewTransactionAndSave() {
        switch (loadedTransaction1.getType()) {
            case Transfer:
                String t1Id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);
                String t2Id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);

                Pair<Transaction, Transaction> pair = createTransferTransaction(t1Id, t2Id);

                TransactionManager.TRANSACTION_MANAGER().insertTransaction(pair.first, pair.second);
                break;
            case Single:
            case Undef:
                String tId = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);

                Transaction t = createSingleTransaction(tId);

                TransactionManager.TRANSACTION_MANAGER().insertTransaction(t);
                break;
        }
    }

    private void updateTransactionAndSave() {
        switch (loadedTransaction1.getType()) {
            case Transfer:
                Pair<Transaction, Transaction> pair = createTransferTransaction(
                        loadedTransaction1.getId(), loadedTransaction2.getId());
                TransactionManager.TRANSACTION_MANAGER().updateTransaction(pair.first, pair.second);
                break;
            case Single:
                Transaction newTransaction = createSingleTransaction(loadedTransaction1.getId());
                TransactionManager.TRANSACTION_MANAGER().updateTransaction(newTransaction);
                break;
            case Undef:
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
                value.getValue(),
                dateEditText.getDate());
        Transaction t2 = new Transaction(
                t2Id,
                description.getText().toString(),
                TransactionDirection.In,
                TransactionType.Transfer,
                accountSecondary != null ? accountSecondary.getId() : "",
                budget != null ? budget.getId() : "",
                value.getValue(),
                dateEditText.getDate());
        t1.setLinkedTransactionId(t2Id);
        t2.setLinkedTransactionId(t1Id);
        if(layout.isRecurrent()) {
            t1.setRecurrent(true);
            t1.setFrequency(recurrentFrequency.getValue());
            t1.setFrequencyUnit(recurrentFrequency.getUnit());
            t1.setEndDate(endDateEditText.getDate());
            t2.setRecurrent(true);
            t2.setFrequency(recurrentFrequency.getValue());
            t2.setFrequencyUnit(recurrentFrequency.getUnit());
            t2.setEndDate(endDateEditText.getDate());
        }
        return new Pair<Transaction, Transaction>(t1, t2);
    }

    private Transaction createSingleTransaction(String id) {
        Account account = (Account) (accountSpinner.getSelectedItem());
        Budget budget = (Budget) (budgetSpinner.getSelectedItem());

        Transaction t = new Transaction(
                id,
                description.getText().toString(),
                loadedTransaction1.getDirection(),
                TransactionType.Single,
                account != null ? account.getId() : "",
                budget != null ? budget.getId() : "",
                value.getValue(),
                dateEditText.getDate());
        if(layout.isRecurrent()) {
            t.setRecurrent(true);
            t.setFrequency(recurrentFrequency.getValue());
            t.setFrequencyUnit(recurrentFrequency.getUnit());
            t.setEndDate(endDateEditText.getDate());
        }
        return t;
    }
}