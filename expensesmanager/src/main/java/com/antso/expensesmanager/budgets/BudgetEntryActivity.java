package com.antso.expensesmanager.budgets;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.entities.Budget;
import com.antso.expensesmanager.enums.TimeUnit;
import com.antso.expensesmanager.persistence.EntityIdGenerator;
import com.antso.expensesmanager.utils.MaterialColours;
import com.antso.expensesmanager.utils.Utils;
import com.antso.expensesmanager.views.CircleSectorView;
import com.antso.expensesmanager.views.ColorPickerDialog;

import org.joda.time.DateTime;

import java.math.BigDecimal;


public class BudgetEntryActivity extends Activity {

    private BigDecimal budgetThreshold = BigDecimal.ZERO;
    private DateTime startDate = DateTime.now();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_entry_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        final EditText name = (EditText)findViewById(R.id.budgetName);
        final EditText value = (EditText)findViewById(R.id.budgetThreshold);
        final EditText date = (EditText)findViewById(R.id.budgetStartDate);
        date.setText(Utils.formatDate(DateTime.now()));
        final CircleSectorView color = (CircleSectorView)findViewById(R.id.budgetColor);
        color.setColor(MaterialColours.getBudgetColors().get(0));

        final Spinner periodLengthSpinner = (Spinner)findViewById(R.id.budgetPeriodLenghtSpinner);
        final Spinner periodUnitSpinner = (Spinner)findViewById(R.id.TimeUnitSpinner);

        final Button confirm = (Button)findViewById(R.id.budgetConfirm);
        final Button cancel = (Button)findViewById(R.id.budgetCancel);

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

        value.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String valueStr = value.getText().toString();
                    valueStr = Utils.washDecimalNumber(valueStr);
                    value.setText(valueStr);
                    budgetThreshold = BigDecimal.valueOf(Double.parseDouble(valueStr)).setScale(2);
                }
            }
        });

        date.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DateTime now = DateTime.now();
                    DatePickerDialog datePicker = new DatePickerDialog(
                            BudgetEntryActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    startDate = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                                    date.setText(Utils.formatDate(startDate));
                                }
                            }, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth()
                    );
                    datePicker.show();
                }
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTime now = DateTime.now();
                DatePickerDialog datePicker = new DatePickerDialog(
                        BudgetEntryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                startDate = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                                date.setText(Utils.formatDate(startDate));
                            }
                        }, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth()
                );
                datePicker.show();
            }
        });

        periodUnitSpinner.setAdapter(
                new ArrayAdapter<TimeUnit>(this, R.layout.text_spinner_item,
                        TimeUnit.valuesButUndef()));
        periodUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeUnit unit = TimeUnit.valueOf(position + 1);
                Integer[] lengthArray = null;
                switch (unit) {
                    case Day:
                        lengthArray = Utils.DayValues;
                        break;
                    case Week:
                        lengthArray = Utils.WeekValues;
                        break;
                    case Month:
                        lengthArray = Utils.MonthValues;
                        break;
                    case Year:
                        lengthArray = Utils.YearValues;
                        break;
                    case Undef:
                    default:
                        lengthArray = new Integer[0];
                        break;
                }

                periodLengthSpinner.setAdapter(new ArrayAdapter<Integer>(BudgetEntryActivity.this,
                                R.layout.text_spinner_item, lengthArray));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valueStr = value.getText().toString();
                valueStr = Utils.washDecimalNumber(valueStr);
                budgetThreshold = BigDecimal.valueOf(Double.parseDouble(valueStr));
                TimeUnit periodUnit = (TimeUnit)periodUnitSpinner.getSelectedItem();
                if(periodUnit == null) {
                    periodUnit = TimeUnit.Undef;
                }
                Integer length = (Integer)periodLengthSpinner.getSelectedItem();
                if (length == null) {
                    length = 0;
                }

                Budget budget = new Budget(
                        EntityIdGenerator.ENTITY_ID_GENERATOR.createId(Budget.class),
                        name.getText().toString(),
                        budgetThreshold,
                        color.getColor(),
                        length, periodUnit,
                        startDate);

                BudgetManager.BUDGET_MANAGER.insertBudget(budget);

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
