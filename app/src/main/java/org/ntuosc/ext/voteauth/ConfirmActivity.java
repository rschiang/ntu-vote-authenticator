package org.ntuosc.ext.voteauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


import static org.ntuosc.ext.voteauth.AppConfig.*;

public class ConfirmActivity extends Activity {
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_TICKET_TYPE = "ticketType";
    private static final String ARG_AUTH_TOKEN = "authToken";

    private String mUserId = "";
    private String mAuthToken = "";
    private String mAuthCode = null;
    private int mStationId = 0;

    private Handler handler = new Handler();
    private NewVoteHandler mNewVoteHandler = new NewVoteHandler(this);
    private AuthConfirmHandler mAuthConfirmHandler = new AuthConfirmHandler(this);
    private AuthReportHandler mAuthReportHandler = new AuthReportHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        Bundle bundle = getIntent().getExtras();
        mUserId = bundle.getString(ARG_USER_ID);
        mAuthToken = bundle.getString(ARG_AUTH_TOKEN);
        ((TextView) findViewById(R.id.prompt_user_id)).setText(mUserId);
        ((TextView) findViewById(R.id.prompt_ticket_type)).setText(bundle.getString(ARG_TICKET_TYPE));

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mStationId = preferences.getInt(PREF_STATION_ID, 0);

        enableConfirmButtonLater();
    }

    public static void startInstance(Context context, String userId, String ticketType, String authToken) {
        Intent intent = new Intent(context, ConfirmActivity.class);
        intent.putExtra(ARG_USER_ID, userId.substring(0, 9));
        intent.putExtra(ARG_TICKET_TYPE, ticketType);
        intent.putExtra(ARG_AUTH_TOKEN, authToken);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // DO NOTHING
    }

    public void onReportButtonClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mAuthCode != null) {
            builder.setTitle(R.string.title_cancel_vote);
            builder.setMessage(R.string.prompt_cancel_vote);
        }
        else {
            builder.setTitle(R.string.title_report);
            builder.setMessage(R.string.prompt_report);
        }
        builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                doReport();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void doReport() {
        if (mAuthCode != null) {
            super.onBackPressed();
        }
        else {
            Api.getAuthService().report(API_KEY, "1", mUserId, mAuthToken, mStationId, mAuthReportHandler);
        }
    }

    public void enableConfirmButtonLater() {
        ((Button) findViewById(R.id.confirm_button)).setEnabled(false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((Button) findViewById(R.id.confirm_button)).setEnabled(true);
            }
        }, CONFIRM_THROTTLE_INTERVAL);
    }

    public void onConfirmButtonClicked(View view) {
        if (mAuthCode != null) {
            Api.getVoteService().newVote(API_KEY, mStationId, mAuthCode, mNewVoteHandler);
        }
        else {
            Api.getAuthService().confirm(API_KEY, "1", mUserId, mAuthToken, mStationId, mAuthConfirmHandler);
        }
        enableConfirmButtonLater();
    }

    public void onDoneButtonClicked(View view) {
        setResult(RESULT_OK);
        finish();
    }

    private class AuthConfirmHandler implements Callback<Api.AuthConfirmResponse> {

        private ConfirmActivity parent = null;

        public AuthConfirmHandler(ConfirmActivity parent) {
            this.parent = parent;
        }

        @Override
        public void success(Api.AuthConfirmResponse authConfirmResponse, Response response) {
            parent.mAuthCode = authConfirmResponse.code;
            Log.i(PACKAGE_NAME, String.format(Locale.getDefault(), "Auth code %s", parent.mAuthCode));

            ((TextView) parent.findViewById(R.id.prompt_text))
                    .setText(R.string.prompt_auth_code_received);
            ((Button) parent.findViewById(R.id.report_button))
                    .setText(R.string.action_cancel);
            Api.getVoteService().newVote(API_KEY, parent.mStationId, parent.mAuthCode, parent.mNewVoteHandler);
        }

        @Override
        public void failure(RetrofitError error) {
            try {
                Api.AuthError errorResponse = (Api.AuthError) error.getBodyAs(Api.AuthError.class);
                switch (errorResponse.reason) {
                    case "token_invalid":
                        ErrorFragment.newInstance(parent, CODE_ALREADY_VOTED);
                        break;
                    case "service_closed":
                        ErrorFragment.newInstance(parent, CODE_SERVICE_CLOSED);
                        break;
                    case "out_of_auth_code":
                        ErrorFragment.newInstance(parent, CODE_INSUFFICIENT_CODE);
                        break;
                    case "version_not_supported":
                        ErrorFragment.newInstance(parent, CODE_VERSION_NOT_SUPPORTED);
                        break;
                    default:
                        Log.w(PACKAGE_NAME, String.format(Locale.getDefault(),
                                "Login failed with error %s", errorResponse.reason));
                        ErrorFragment.newInstance(parent, CODE_GENERIC_ERROR);
                        break;
                }
            }
            catch (RuntimeException ex) {
                Log.e(PACKAGE_NAME, String.format(Locale.getDefault(),
                        "Login failed with error %s", error.toString()));
                ErrorFragment.newInstance(parent, CODE_SERVER_ERROR);
            }
        }
    }


    private class AuthReportHandler implements Callback<Api.AuthReportResponse> {

        private ConfirmActivity parent = null;

        public AuthReportHandler(ConfirmActivity parent) {
            this.parent = parent;
        }

        @Override
        public void success(Api.AuthReportResponse authReportResponse, Response response) {
            Log.i(PACKAGE_NAME, String.format(Locale.getDefault(), "User %s reported", parent.mUserId));
            parent.setResult(RESULT_CANCELED);
            parent.finish();
        }

        @Override
        public void failure(RetrofitError error) {
            try {
                Api.AuthError errorResponse = (Api.AuthError) error.getBodyAs(Api.AuthError.class);
                switch (errorResponse.reason) {
                    case "service_closed":
                        ErrorFragment.newInstance(parent, CODE_SERVICE_CLOSED);
                        break;
                    case "version_not_supported":
                        ErrorFragment.newInstance(parent, CODE_VERSION_NOT_SUPPORTED);
                        break;
                    default:
                        Log.w(PACKAGE_NAME, String.format(Locale.getDefault(),
                                "Report user failed with error %s", errorResponse.reason));
                        ErrorFragment.newInstance(parent, CODE_GENERIC_ERROR);
                        break;
                }
            }
            catch (RuntimeException ex) {
                Log.e(PACKAGE_NAME, String.format(Locale.getDefault(),
                        "Report user failed with error %s", error.toString()));
                ErrorFragment.newInstance(parent, CODE_SERVER_ERROR);
            }
        }
    }

    private class NewVoteHandler implements Callback<Api.NewVoteResponse> {

        private ConfirmActivity parent = null;

        public NewVoteHandler(ConfirmActivity parent) {
            this.parent = parent;
        }

        @Override
        public void success(Api.NewVoteResponse newVoteResponse, Response response) {
            ((TextView) parent.findViewById(R.id.prompt_text))
                    .setText(String.format(Locale.getDefault(),
                            getString(R.string.prompt_booth_id),
                            newVoteResponse.boothId));

            ((Button) parent.findViewById(R.id.report_button)).setVisibility(View.GONE);
            ((Button) parent.findViewById(R.id.confirm_button)).setVisibility(View.GONE);
            ((Button) parent.findViewById(R.id.done_button)).setVisibility(View.VISIBLE);
        }

        @Override
        public void failure(RetrofitError error) {
            try {
                Api.VoteError errorResponse = (Api.VoteError) error.getBodyAs(Api.VoteError.class);
                if (errorResponse.message.equals("authcode step must 0")) {
                    ErrorFragment.newInstance(parent, CODE_ALREADY_VOTED);
                } else if (errorResponse.message.equals("there are no more online-booth-tablet")) {
                    ((TextView) parent.findViewById(R.id.prompt_text))
                            .setText(R.string.prompt_booth_unavailable);
                    ((Button) parent.findViewById(R.id.confirm_button))
                            .setText(R.string.action_retry);
                } else {
                    Log.w(PACKAGE_NAME, String.format(Locale.getDefault(),
                            "New vote failed with error %s", errorResponse.message));
                    ErrorFragment.newInstance(parent, CODE_GENERIC_ERROR);
                }
            } catch (RuntimeException ex) {
                Log.e(PACKAGE_NAME, String.format(Locale.getDefault(),
                        "New vote failed with error %s", error.toString()));
                ErrorFragment.newInstance(parent, CODE_SERVER_ERROR);
            }
        }
    }
}
