package org.ntuosc.ext.voteauth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.ntuosc.ext.voteauth.AppConfig.*;

public class LoginActivity extends Activity implements Callback<Api.StationRegisterResponse> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText userField = (EditText) findViewById(R.id.field_station_user);
        EditText passField = (EditText) findViewById(R.id.field_station_pass);

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        userField.setText(preferences.getString(PREF_STATION_USER, ""));
        passField.setText(preferences.getString(PREF_STATION_PASS, ""));

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                ((Button) findViewById(R.id.login_button)).setEnabled(
                        ((EditText) findViewById(R.id.field_station_user)).length() > 0 &&
                        ((EditText) findViewById(R.id.field_station_pass)).length() > 0);
            }
        };

        userField.addTextChangedListener(textWatcher);
        passField.addTextChangedListener(textWatcher);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onLoginButtonClicked(View view) {
        String username = ((EditText) findViewById(R.id.field_station_user))
                .getText().toString();

        String password = ((EditText) findViewById(R.id.field_station_pass))
                .getText().toString();

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        preferences.edit()
                .putString(PREF_STATION_USER, username)
                .putString(PREF_STATION_PASS, password)
                .commit();

        Api.getAuthService().register(API_KEY, username, password, this);
    }

    @Override
    public void success(Api.StationRegisterResponse registerResponse, Response response) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        preferences.edit()
                .putString(PREF_STATION_NAME, registerResponse.name)
                .putInt(PREF_STATION_ID, registerResponse.station)
                .putString(PREF_STATION_TOKEN, registerResponse.token)
                .commit();

        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void failure(RetrofitError error) {
        try {
            Api.AuthError errorResponse = (Api.AuthError) error.getBodyAs(Api.AuthError.class);
            if (errorResponse.reason.equals("unauthorized")) {
                ErrorFragment.newInstance(this, CODE_LOGIN_FAILED);
            }
            else {
                Log.w(PACKAGE_NAME, String.format(Locale.getDefault(),
                        "Login failed with error %s", errorResponse.reason));
                ErrorFragment.newInstance(this, CODE_GENERIC_ERROR);
            }
        }
        catch (RuntimeException ex) {
            ErrorFragment.newInstance(this, CODE_SERVER_ERROR);
        }
    }
}
