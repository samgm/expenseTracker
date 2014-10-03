package com.antso.expensesmanager.transactions;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.accounts.AccountManager;
import com.antso.expensesmanager.adapters.AccountSpinnerAdapter;
import com.antso.expensesmanager.adapters.BudgetSpinnerAdapter;
import com.antso.expensesmanager.budgets.BudgetManager;
import com.antso.expensesmanager.entities.Account;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.entities.Transaction;
import com.antso.expensesmanager.enums.TransactionDirection;
import com.antso.expensesmanager.enums.TransactionType;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.Settings;
import com.antso.expensesmanager.views_helpers.ButtonChangeSpinner;
import com.antso.expensesmanager.views_helpers.DateEditText;
import com.antso.expensesmanager.utils.SpaceTokenizer;
import com.antso.expensesmanager.views_helpers.TransactionFrequencySpinner;
import com.antso.expensesmanager.views_helpers.TransactionLayout;
import com.antso.expensesmanager.views_helpers.ValueEditText;

import org.joda.time.DateTime;

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
    private TransactionFrequencySpinner recurrentFrequency;

    private Collection<Account> accounts;
    private AccountSpinnerAdapter accountSpinnerAdapter;
    private Collection<Budget> budgets;
    private BudgetSpinnerAdapter budgetSpinnerAdapter;

    private boolean isOrderEdit;
    private Transaction loadedTransaction1;
    private Transaction loadedTransaction2;

    public TransactionEntryActivity(){
        super();

        layout = new TransactionLayout(this);
        endDateEditText = new DateEditText(this);
        dateEditText = new DateEditText(this);
        recurrentFrequency = new TransactionFrequencySpinner(this);
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

        //TEST
        String str = Settings.getDefaultAccountId(this);
        //End of TEST

        if (accounts == null) {
            accounts = AccountManager.ACCOUNT_MANAGER.getAccounts();
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            accountSpinnerAdapter = AccountSpinnerAdapter.create(this, R.layout.text_spinner_item,
                    accounts.toArray(new Account[0]));
        }

        if (budgets == null) {
            budgets = BudgetManager.BUDGET_MANAGER.getBudgets();
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            budgetSpinnerAdapter = BudgetSpinnerAdapter.create(this, R.layout.text_spinner_item,
                    budgets.toArray(new Budget[0]));
        }

        //Creating view
        description = (MultiAutoCompleteTextView) findViewById(R.id.transactionDesc);

        layout.createView(R.id.transactionColor,
                R.id.transactionSecondaryAccountLayout, R.id.transactionSecondaryAccountLabel,
                R.id.transactionRecurrentCheckbox, R.id.transactionRecurrentDetailsLayout);
        value.createView(R.id.transactionValue, BigDecimal.ZERO);
        accountSpinner.createView(R.id.transactionAccountSpinner, R.id.transactionAccountButton,
                accountSpinnerAdapter);
        accountSecondarySpinner.createView(R.id.transactionSecondaryAccountSpinner, R.id.transactionSecondaryAccountButton,
                accountSpinnerAdapter);
        budgetSpinner.createView(R.id.transactionBudgetSpinner, R.id.transactionBudgetButton,
                budgetSpinnerAdapter);

        recurrentFrequency.createView(R.id.transactionFrequencyUnit, R.id.transactionFrequency);
        dateEditText.createView(R.id.transactionDate, DateTime.now());
        endDateEditText.createView(R.id.transactionRecurrentStartDate, DateTime.now());


        description.setAdapter(new ArrayAdapter<String>(this, R.layout.text_spinner_item,
                TransactionManager.TRANSACTION_MANAGER.getDescriptionsArray()));
        description.setTokenizer(new SpaceTokenizer());

        //Get params and load defaults
        String id = getIntent().getStringExtra("transaction_id");
        int direction = getIntent().getIntExtra("transaction_direction", TransactionDirection.Undef.getIntValue());
        int type  = getIntent().getIntExtra("transaction_type", TransactionType.Undef.getIntValue());

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
        }

        Button confirm = (Button) findViewById(R.id.transactionConfirm);
        Button cancel = (Button) findViewById(R.id.transactionCancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            isOrderEdit = false;
            loadedTransaction1 = new Transaction(
                    null,
                    "description",
                    TransactionDirection.valueOf(direction),
                    TransactionType.valueOf(type),
                    null,
                    null,
                    BigDecimal.ZERO,
                    DateTime.now());
            loadedTransaction1.setEndDate(DateTime.now());
        } else {
            isOrderEdit = true;
            loadedTransaction1 = TransactionManager.TRANSACTION_MANAGER.getTransactionById(id);
            String linkedTransactionId = loadedTransaction1.getLinkedTransactionId();
            if (linkedTransactionId != null && !linkedTransactionId.isEmpty()) {
                loadedTransaction2 = TransactionManager.TRANSACTION_MANAGER.getTransactionById(linkedTransactionId);
            }
        }
    }

    private void createNewTransactionAndSave() {
        switch (loadedTransaction1.getType()) {
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
        switch (loadedTransaction1.getType()) {
            case Transfer:
                Pair<Transaction, Transaction> pair = createTransferTransaction(
                        loadedTransaction1.getId(), loadedTransaction2.getId());
                TransactionManager.TRANSACTION_MANAGER.updateTransaction(pair.first);
                TransactionManager.TRANSACTION_MANAGER.updateTransaction(pair.second);
                break;
            case Single:
                Transaction newTransaction = createSingleTransaction(loadedTransaction1.getId());
                TransactionManager.TRANSACTION_MANAGER.updateTransaction(newTransaction);
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
        if(loadedTransaction1.getRecurrent()) {
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
        if(loadedTransaction1.getRecurrent()) {
            t.setRecurrent(true);
            t.setFrequency(recurrentFrequency.getValue());
            t.setFrequencyUnit(recurrentFrequency.getUnit());
            t.setEndDate(endDateEditText.getDate());
        }
        return t;
    }
}