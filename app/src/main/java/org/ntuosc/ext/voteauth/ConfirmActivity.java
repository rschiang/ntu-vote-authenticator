package org.ntuosc.ext.voteauth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


import static org.ntuosc.ext.voteauth.AppConfig.*;

public class ConfirmActivity extends Activity implements Callback<Api.NewVoteResponse> {
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_TICKET_TYPE = "ticketType";
    private static final String ARG_AUTH_CODE = "authCode";

    private String mAuthCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        Bundle bundle = getIntent().getExtras();
        ((TextView) findViewById(R.id.prompt_user_id)).setText(bundle.getString(ARG_USER_ID));
        ((TextView) findViewById(R.id.prompt_ticket_type)).setText(bundle.getString(ARG_TICKET_TYPE));
        mAuthCode = bundle.getString(ARG_AUTH_CODE);
    }

    public static void startInstance(Context context, String userId, String ticketType, String authCode) {
        Intent intent = new Intent(context, ConfirmActivity.class);
        intent.putExtra(ARG_USER_ID, userId.substring(0, 9));
        intent.putExtra(ARG_TICKET_TYPE, ticketType);
        intent.putExtra(ARG_AUTH_CODE, authCode);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // DO NOTHING
    }

    public void onReportButtonClicked(View view) {
        super.onBackPressed();
    }

    public void onConfirmButtonClicked(View view) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Api.getVoteService().newVote(API_KEY, preferences.getInt(PREF_STATION_ID, 0), mAuthCode, this);
    }

    public void onDoneButtonClicked(View view) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void success(Api.NewVoteResponse newVoteResponse, Response response) {
        ((TextView) findViewById(R.id.prompt_text))
                .setText(String.format(Locale.getDefault(),
                        getString(R.string.prompt_booth_id),
                        newVoteResponse.boothId));

        ((Button) findViewById(R.id.report_button)).setVisibility(View.GONE);
        ((Button) findViewById(R.id.confirm_button)).setVisibility(View.GONE);
        ((Button) findViewById(R.id.done_button)).setVisibility(View.VISIBLE);
    }

    @Override
    public void failure(RetrofitError error) {
        try {
            Api.VoteError errorResponse = (Api.VoteError) error.getBodyAs(Api.VoteError.class);
            if (errorResponse.message.equals("authcode step must 0")) {
                ErrorFragment.newInstance(this, CODE_ALREADY_VOTED);
            }
            else if (errorResponse.message.equals("there are no more online-booth-tablet")) {
                ((TextView) findViewById(R.id.prompt_text))
                        .setText(R.string.prompt_booth_unavailable);
                ((Button) findViewById(R.id.confirm_button))
                        .setText(R.string.action_retry);
            }
            else {
                Log.w(PACKAGE_NAME, String.format(Locale.getDefault(),
                        "Login failed with error %s", errorResponse.message));
                ErrorFragment.newInstance(this, CODE_GENERIC_ERROR);
            }
        }
        catch (RuntimeException ex) {
            ErrorFragment.newInstance(this, CODE_SERVER_ERROR);
        }
    }
}
