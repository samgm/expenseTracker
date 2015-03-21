package com.antso.expenses;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.antso.expenses.StartActivity;
import com.antso.expenses.entities.Transaction;
import com.antso.expenses.transactions.TransactionEntryActivity;
import com.robotium.solo.Solo;

public class StartActivityTest extends ActivityInstrumentationTestCase2<StartActivity> {
    private Solo solo;
    private int short_timeout = 1000;
    private int timeout = 2000;

    public StartActivityTest() {
        super(StartActivity.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation());
        getActivity();

        assertTrue("StartActivity not found", solo.waitForActivity(StartActivity.class, timeout));

        View progress = solo.getView(R.id.progressBar);
        while(progress.getVisibility() == View.VISIBLE) {
            Thread.sleep(short_timeout);
        }
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testExpenseAdd() throws InterruptedException {

        solo.clickOnActionBarItem(R.id.action_transaction_add);
        assertTrue("TransactionEntry not found", solo.waitForActivity(TransactionEntryActivity.class, timeout));

        EditText value = (EditText)solo.getView(R.id.transactionValue);
        value.setText("22.22");
        EditText desc = (EditText)solo.getView(R.id.transactionDesc);
        desc.setText("test_add");

        solo.waitForView(R.id.transactionConfirm, 1, timeout);
        solo.clickOnButton("Add");

        assertTrue("StartActivity not found", solo.waitForActivity(StartActivity.class, timeout));

        ListView list = solo.getView(ListView.class, 0);
        solo.scrollListToTop(0);

        Transaction t1 = (Transaction)list.getAdapter().getItem(0);
        assertTrue("Transaction value unexpected " + t1.getValue(),
                t1.getValue().setScale(2).toPlainString().equals("22.22"));
        assertTrue("Transaction value unexpected " + t1.getDescription(),
                t1.getDescription().equals("test_add"));

        solo.clickLongOnText("test_add");
        solo.clickOnText("Delete");
        solo.waitForDialogToClose(timeout);

        Transaction t2 = (Transaction)list.getAdapter().getItem(0);
        assertFalse("Transaction value unexpected " + t2.getDescription(),
                t2.getDescription().equals("test_add"));
    }

}
