package com.antso.expensesmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.antso.expensesmanager.transactions.TransactionEntryActivity;
import com.antso.expensesmanager.utils.Constants;
import com.antso.expensesmanager.utils.IntentParamNames;

public class AboutFragment extends Fragment {

    private String feedbackMail = "";
    private String feedbackMailSubject = "";
    private String feedbackMailBody = "";
    private String versionLabel = "";
    private String version = "";
    private String versionPostfix = "";

    private int iconClickedTimes = 0;

    public AboutFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment, container, false);

        if (getActivity() != null) {
            feedbackMail = getActivity().getText(R.string.app_feedback_mail).toString();
            feedbackMailSubject = getActivity().getText(R.string.app_feedback_mail_subjejct).toString();
            feedbackMailBody = getActivity().getText(R.string.app_feedback_mail_body).toString();

            versionLabel = getActivity().getText(R.string.app_version_label).toString();
            version = getActivity().getText(R.string.app_version).toString();
            versionPostfix = getActivity().getText(R.string.app_version_postfix).toString();
        }

        final TextView versionText = (TextView) view.findViewById(R.id.versionText);
        versionText.setText(versionLabel + " " + version + " " + versionPostfix);

        final Button feedbackButton = (Button) view.findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + feedbackMail +
                        "?subject=" + feedbackMailSubject +
                        "&body=" + feedbackMailBody));
                startActivity(intent);
            }
        });

        final ImageView icon = (ImageView) view.findViewById(R.id.about_app_icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconClickedTimes++;
                if(iconClickedTimes == 1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            iconClickedTimes = 0;
                        }
                    }, 2000);
                }
                if(iconClickedTimes == 5) {
                    Intent debug = new Intent(getActivity().getApplicationContext(), DebugActivity.class);
                    getActivity().startActivity(debug);
                    iconClickedTimes = 0;
                }
            }
        });
        return view;
    }

}
