package com.antso.expensesmanager.budgets;

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
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.enums.TimeUnit;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.IntentParamNames;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.views.CircleSectorView;
import com.antso.expensesmanager.views.ColorPickerDialog;
import com.antso.expensesmanager.views_helpers.DateEditText;
import com.antso.expensesmanager.views_helpers.FrequencySpinner;
import com.antso.expensesmanager.views_helpers.ValueEditText;

import org.joda.time.DateTime;

import java.math.BigDecimal;


public class BudgetEntryActivity extends Activity {
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

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        //Creating view
        name = (EditText)findViewById(R.id.budgetName);
        color = (CircleSectorView)findViewById(R.id.budgetColor);
        budgetThreshold.createView(R.id.budgetThreshold, BigDecimal.ZERO);
        startDateEditText.createView(R.id.budgetStartDate, DateTime.now());
        period.createView(R.id.TimeUnitSpinner, R.id.budgetPeriodLenghtSpinner, false);
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
        final Button confirm = (Button)findViewById(R.id.budgetConfirm);
        final Button cancel = (Button)findViewById(R.id.budgetCancel);
        if (isEdit) {
            title.setText(R.string.budget_edit_title);
            confirm.setText(R.string.button_confirm_edit_label);
        } else {
            title.setText(R.string.budget_entry_title);
            confirm.setText(R.string.button_confirm_add_label);
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    Budget budget = new Budget(
                            loadedBudget.getId(),
                            name.getText().toString(),
                            budgetThreshold.getValue(),
                            color.getColor(),
                            period.getValue(), period.getUnit(),
                            startDateEditText.getDate());
                    BudgetManager.BUDGET_MANAGER().updateBudget(budget);

                } else {
                    Budget budget = new Budget(
                            EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Budget.class),
                            name.getText().toString(),
                            budgetThreshold.getValue(),
                            color.getColor(),
                            period.getValue(), period.getUnit(),
                            startDateEditText.getDate());
                    BudgetManager.BUDGET_MANAGER().insertBudget(budget);
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
                    DateTime.now());
        } else {
            isEdit = true;
            loadedBudget = BudgetManager.BUDGET_MANAGER().getBudgetInfo(id).budget;
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
