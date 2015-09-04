package com.antso.expenses.budgets;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.antso.expenses.R;
import com.antso.expenses.entities.Budget;
import com.antso.expenses.enums.TimeUnit;
import com.antso.expenses.persistence.EntityIdGenerator;
import com.antso.expenses.utils.IntentParamNames;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.Utils;
import com.antso.expenses.views.CircleSectorView;
import com.antso.expenses.views.ColorPickerDialog;
import com.antso.expenses.views_helpers.DateEditText;
import com.antso.expenses.views_helpers.FrequencySpinner;
import com.antso.expenses.views_helpers.ValueEditText;

import java.math.BigDecimal;


public class BudgetEntryActivity extends AppCompatActivity {
    private CircleSectorView color;
    private EditText name;
    private ValueEditText budgetThreshold;
    private DateEditText startDateEditText;
    private FrequencySpinner period;

    private Budget loadedBudget;
    private boolean isEdit;

    public BudgetEntryActivity() {
        super();

        budgetThreshold = new ValueEditText(this);
        startDateEditText = new DateEditText(this);
        period = new FrequencySpinner(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_entry_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Creating view
        name = (EditText)findViewById(R.id.budgetName);
        color = (CircleSectorView)findViewById(R.id.budgetColor);
        budgetThreshold.createView(R.id.budgetThreshold, R.id.budgetThresholdCurrency, BigDecimal.ZERO);
        startDateEditText.createView(R.id.budgetStartDate, Utils.now());
        period.createView(R.id.TimeUnitSpinner, R.id.budgetPeriodLenghtSpinner);
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialog c = new ColorPickerDialog(BudgetEntryActivity.this, new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void colorChanged(int c) {
                        if(color != null) {
                            color.setColor(c);
                            color.invalidate();
                        }
                    }
                }, MaterialColours.getBudgetColors());
                c.show();
            }
        });

        //Get params and load defaults
        String id = getIntent().getStringExtra(IntentParamNames.BUDGET_ID);
        loadBudget(id);

        color.setColor(loadedBudget.getColor());
        name.setText(loadedBudget.getName());
        budgetThreshold.setValue(loadedBudget.getThreshold());
        period.setUnit(loadedBudget.getPeriodUnit());
        period.setValue(loadedBudget.getPeriodLength());
        startDateEditText.setDate(loadedBudget.getPeriodStart());

        final TextView title = (TextView) findViewById(R.id.budgetEntryTitle);
        if (isEdit) {
            title.setText(R.string.budget_edit_title);
        } else {
            title.setText(R.string.budget_entry_title);
        }
    }

    private void loadBudget(String id) {
        if (id == null || id.isEmpty()) {
            isEdit = false;
            loadedBudget = new Budget(
                    null,
                    getText(R.string.name).toString(),
                    BigDecimal.ZERO,
                    MaterialColours.getAccountColors().get(0),
                    1,
                    TimeUnit.Month,
                    Utils.now());
        } else {
            isEdit = true;
            loadedBudget = BudgetManager.BUDGET_MANAGER().getBudgetInfo(id).budget;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_budget_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id ==  android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        if (id == R.id.action_budget_confirm) {
            if (isEdit) {
                Budget budget = new Budget(
                        loadedBudget.getId(),
                        name.getText().toString(),
                        budgetThreshold.getValue(),
                        color.getColor(),
                        period.getValue(), period.getUnit(),
                        startDateEditText.getDate());
                BudgetManager.BUDGET_MANAGER().updateBudget(budget);
                Utils.showUpdatedToast(this, budget.toString());
            } else {
                Budget budget = new Budget(
                        EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Budget.class),
                        name.getText().toString(),
                        budgetThreshold.getValue(),
                        color.getColor(),
                        period.getValue(), period.getUnit(),
                        startDateEditText.getDate());
                BudgetManager.BUDGET_MANAGER().insertBudget(budget);
                Utils.showAddedToast(this, budget.toString());
            }

            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
