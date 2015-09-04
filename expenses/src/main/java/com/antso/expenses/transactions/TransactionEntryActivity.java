package com.antso.expenses.transactions;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

import static com.antso.expenses.enums.TransactionType.Undef;
import static com.antso.expenses.enums.TransactionType.valueOf;


public class TransactionEntryActivity extends AppCompatActivity {
    private TransactionLayout layout;
    private DateEditText dateEditText;
    private DateEditText endDateEditText;
    private ValueEditText value;
    private ValueEditText fee;
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
    private Transaction loadedFeeTransaction;

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
        fee = new ValueEditText(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_entry_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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
                R.id.transactionRecurrentCheckbox, R.id.transactionRecurrentDetailsLayout,
                R.id.transactionFeeCheckbox
                , R.id.feeDetailLayout);
        value.createView(R.id.transactionValue, R.id.transactionValueCurrency, BigDecimal.ZERO);
        fee.createView(R.id.transactionFee, R.id.transactionFeeCurrency, BigDecimal.ZERO);
        accountSpinner.createView(R.id.transactionAccountSpinner, R.id.transactionAccountButton,
                accountSpinnerAdapter);
        accountSecondarySpinner.createView(R.id.transactionSecondaryAccountSpinner, R.id.transactionSecondaryAccountButton,
                accountSpinnerAdapter);
        budgetSpinner.createView(R.id.transactionBudgetSpinner, R.id.transactionBudgetButton,
                budgetSpinnerAdapter);

        recurrentFrequency.createView(R.id.transactionFrequencyUnit, R.id.transactionFrequency);
        dateEditText.createView(R.id.transactionDate, Utils.now());
        endDateEditText.createView(R.id.transactionRecurrentStartDate, Utils.now());
        description.setAdapter(StringArrayAdapter.create(this, R.layout.text_spinner_item,
                TransactionManager.TRANSACTION_MANAGER().getDescriptionsArray()));
        description.setTokenizer(new SpaceTokenizer());

        //Get params and load defaults
        String id = getIntent().getStringExtra(IntentParamNames.TRANSACTION_ID);
        int direction = getIntent().getIntExtra(IntentParamNames.TRANSACTION_DIRECTION, TransactionDirection.Undef.getIntValue());
        int type  = getIntent().getIntExtra(IntentParamNames.TRANSACTION_TYPE, Undef.getIntValue());

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
                    Settings.getDefaultTransferToAccountId(this)));
        }

        if (loadedFeeTransaction != null) {
            fee.setValue(loadedFeeTransaction.getValue());
        }

        final TextView title = (TextView) findViewById(R.id.transactionEntryTitle);
        if (isEdit) {
            title.setText(R.string.transaction_edit_title);
        } else {
            title.setText(R.string.transaction_entry_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transaction_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        if (id == R.id.action_transaction_confirm) {
            if (isEdit) {
                Transaction transaction = updateTransactionAndSave();
                Utils.showUpdatedToast(this, transaction.toDetailedString(this));
            } else {
                Transaction transaction = createNewTransactionAndSave();
                Utils.showAddedToast(this, transaction.toDetailedString(this));
            }

            setResult(RESULT_OK);
            finish();
            return true;
        }

        if (id == R.id.action_transaction_delete) {
            if (isEdit) {
                TransactionManager.TRANSACTION_MANAGER().removeTransaction(loadedTransaction1);
                Utils.showDeletedToast(this, loadedTransaction1.toDetailedString(this));
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getDefaultTransactionAccount(TransactionType type, TransactionDirection dir) {
        if (type.equals(TransactionType.Transfer)) {
            return Settings.getDefaultTransferFromAccountId(this);
        } else {
            switch (dir) {
                case In:
                    return Settings.getDefaultRevenueAccountId(this);
                case Out:
                default:
                    return Settings.getDefaultExpenseAccountId(this);
            }
        }
    }

    private String getDefaultTransactionBudget(TransactionType type, TransactionDirection dir) {
        if (type.equals(TransactionType.Transfer)) {
            return Settings.getDefaultTransferBudgetId(this);
        } else {
            switch (dir) {
                case In:
                    return Settings.getDefaultRevenueBudgetId(this);
                case Out:
                default:
                    return Settings.getDefaultExpenseBudgetId(this);
            }
        }
    }

    private void loadTransactions(String id, int type, int direction) {
        if (id == null || id.isEmpty()) {
            isEdit = false;
            loadedTransaction1 = new Transaction(
                    null,
                    this.getString(R.string.description),
                    TransactionDirection.valueOf(direction),
                    valueOf(type),
                    getDefaultTransactionAccount(valueOf(type), TransactionDirection.valueOf(direction)),
                    getDefaultTransactionBudget(valueOf(type), TransactionDirection.valueOf(direction)),
                    BigDecimal.ZERO,
                    Utils.now());
            loadedTransaction1.setEndDate(Utils.now());
        } else {
            isEdit = true;
            loadedTransaction1 = TransactionManager.TRANSACTION_MANAGER().getTransactionById(id);
            String linkedTransactionId = loadedTransaction1.getLinkedTransactionId();
            String feeTransactionId = loadedTransaction1.getFeeTransactionId();

            if (linkedTransactionId != null && !linkedTransactionId.isEmpty()) {
                loadedTransaction2 = TransactionManager.TRANSACTION_MANAGER().getTransactionById(linkedTransactionId);
            }

            if (feeTransactionId != null && !feeTransactionId.isEmpty()) {
                loadedFeeTransaction = TransactionManager.TRANSACTION_MANAGER().getTransactionById(feeTransactionId);
            }

        }
    }

    private Transaction createNewTransactionAndSave() {
        Transaction mainTransaction = null;
        switch (loadedTransaction1.getType()) {
            case Transfer:
                String t1Id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);
                String t2Id = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);

                CompoundedTransferTransaction compoundedTransactions = createTransferTransaction(t1Id, t2Id, null);
                TransactionManager.TRANSACTION_MANAGER().insertTransaction(
                        compoundedTransactions.getOutTransaction(),
                        compoundedTransactions.getInTransaction());
                if (layout.hasFee()) {
                    TransactionManager.TRANSACTION_MANAGER().insertTransaction(
                            compoundedTransactions.getFeeTransaction());
                }
                mainTransaction = compoundedTransactions.getOutTransaction();
                break;
            case Single:
            case Undef:
                String tId = EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class);

                Transaction t = createSingleTransaction(tId);

                TransactionManager.TRANSACTION_MANAGER().insertTransaction(t);
                mainTransaction = t;
                break;
        }

        return mainTransaction;
    }

    private Transaction updateTransactionAndSave() {
        Transaction mainTransaction = null;
        switch (loadedTransaction1.getType()) {
            case Transfer:
                CompoundedTransferTransaction compoundedTransactions = createTransferTransaction(
                        loadedTransaction1.getId(),
                        loadedTransaction2.getId(),
                        (loadedFeeTransaction != null ? loadedFeeTransaction.getId() : null));
                mainTransaction = compoundedTransactions.getOutTransaction();

                TransactionManager.TRANSACTION_MANAGER().updateTransaction(
                        compoundedTransactions.getOutTransaction(),
                        compoundedTransactions.getInTransaction());
                if (layout.hasFee()) {
                    if (loadedFeeTransaction == null) {
                        TransactionManager.TRANSACTION_MANAGER().insertTransaction(
                                compoundedTransactions.getFeeTransaction());
                    } else {
                        TransactionManager.TRANSACTION_MANAGER().updateTransaction(
                                compoundedTransactions.getFeeTransaction());
                    }
                } else {
                    if (loadedFeeTransaction != null) {
                        TransactionManager.TRANSACTION_MANAGER().removeTransaction(loadedFeeTransaction);
                    }
                }
                break;
            case Single:
                Transaction newTransaction = createSingleTransaction(loadedTransaction1.getId());
                TransactionManager.TRANSACTION_MANAGER().updateTransaction(newTransaction);
                mainTransaction = newTransaction;
                break;
            case Undef:
                break;
        }

        return mainTransaction;
    }

    private CompoundedTransferTransaction createTransferTransaction(String t1Id,
                                                                     String t2Id,
                                                                     String tFeeId) {
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

        t1.setLinkedTransactionId(t2Id);
        t2.setLinkedTransactionId(t1Id);
        CompoundedTransferTransaction result = new CompoundedTransferTransaction(t1, t2);

        Transaction tFee;
        if(layout.hasFee()) {
            tFee = new Transaction(
                    tFeeId != null ? tFeeId : EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Transaction.class),
                    description.getText().toString(),
                    TransactionDirection.Out,
                    TransactionType.Fee,
                    account != null ? account.getId() : "",
                    budget != null ? budget.getId() : "",
                    fee.getValue(),
                    dateEditText.getDate());

            if(layout.isRecurrent()) {
                tFee.setRecurrent(true);
                tFee.setFrequency(recurrentFrequency.getValue());
                tFee.setFrequencyUnit(recurrentFrequency.getUnit());
                tFee.setEndDate(endDateEditText.getDate());
            }

            t1.setFeeTransactionId(tFee.getId());
            t2.setFeeTransactionId(tFee.getId());
            result.setFeeTransaction(tFee);
        }

        return result;
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