package org.ntuosc.ext.voteauth;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.nfc.*;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.ntuosc.ext.voteauth.AppConfig.*;


public class MainActivity extends Activity implements ErrorFragment.Listener, Callback<Api.AuthResponse> {
    private int REQUEST_LOGIN = 1;

    Handler handler = new Handler();

    PingTask periodicPing = new PingTask(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPreferences();
    }

    public void loadPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (preferences.contains(PREF_STATION_ID)) {
            String stationName = preferences.getString(PREF_STATION_NAME, getString(R.string.placeholder_station_name));
            ((TextView) findViewById(R.id.station_name)).setText(stationName);
        }
        else {
            // Ask user to login
            startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                return onAboutMenuItemClicked();
            case R.id.action_logout:
                return onLogoutMenuItemClicked();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onAboutMenuItemClicked() {
        // Launch NTUOSC site
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ntuosc.org"));
        try {
            startActivity(intent);
            return true;
        }
        catch (ActivityNotFoundException ex) {
            // No browser found. Just ignore it and all is good.
            return false;
        }
    }

    public boolean onLogoutMenuItemClicked() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
        finish();
        return true;
    }

    @Override
    protected void onPause() {
        disableNfcDispatch();
        hideLoadingIndicator();
        periodicPing.clearCallback();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        periodicPing.runCallback();
        enableNfcDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            TagReadingTask task = new TagReadingTask(this);
            task.execute(tag);
        }

        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOGIN) {
            if (resultCode != RESULT_OK) {
                finish();
            } else {
                loadPreferences();
            }
        }
    }

    public void onOpenSettingsButtonClicked(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                return;
            }
            catch (ActivityNotFoundException ex) {
                // Alright, use fallback
            }
        }

        // Open Settings app instead in lower API levels
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    public void onTagRead(TagReadingTask.Result result) {
        if (result.code == CODE_SUCCESS) {
            Log.i(PACKAGE_NAME, String.format(Locale.getDefault(), "Tag %s read", result.uid));
            showLoadingIndicator();

            SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Api.getAuthService().authenticate(AppConfig.API_KEY, "1",
                            preferences.getInt(PREF_STATION_ID, 0), result.cid, result.uid, this);
        }
        else {
            ErrorFragment.newInstance(this, result.code);
        }
    }

    @Override
    public void onErrorDialogDismiss(int errorCode) {
        // Do nothing
    }

    public void updatePrompt(Boolean status) {
        // Update prompt text
        TextView promptText = (TextView) findViewById(R.id.prompt_text);
        promptText.setText(
                status == null ? R.string.prompt_waiting_device :
                        status ? R.string.prompt_scan_card :
                                R.string.prompt_enable_nfc
        );

        // Set up buttons
        ((Button) findViewById(R.id.button_configure_nfc))
                .setVisibility((status == null || status) ? View.GONE : View.VISIBLE);
    }

    public void enableNfcDispatch() {
        updatePrompt(null);
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);

        if (nfc != null && nfc.isEnabled()) {
            PendingIntent nfcIntent = PendingIntent.getActivity(this, AppConfig.CODE_NFC_REQUEST,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            nfc.enableForegroundDispatch(this, nfcIntent, null, null);
            updatePrompt(true);

        } else {
            updatePrompt(false);
        }
    }

    public void disableNfcDispatch() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);
        if (nfc != null && nfc.isEnabled()) {
            nfc.disableForegroundDispatch(this);
        }
    }

    public void showLoadingIndicator() {
        findViewById(R.id.prompt_text).setVisibility(View.GONE);
        findViewById(R.id.indicator).setVisibility(View.VISIBLE);
    }

    public void hideLoadingIndicator() {
        findViewById(R.id.prompt_text).setVisibility(View.VISIBLE);
        findViewById(R.id.indicator).setVisibility(View.GONE);
    }

    @Override
    public void success(Api.AuthResponse authResponse, Response response) {
        hideLoadingIndicator();
        Log.i(PACKAGE_NAME, String.format(Locale.getDefault(), "Auth token %s", authResponse.token));
        ConfirmActivity.startInstance(this, authResponse.uid, authResponse.type, authResponse.token);
    }

    @Override
    public void failure(RetrofitError error) {
        hideLoadingIndicator();
        try {
            Api.AuthError errorResponse = (Api.AuthError) error.getBodyAs(Api.AuthError.class);
            if (errorResponse.reason.equals("duplicate_entry")) {
                ErrorFragment.newInstance(this, CODE_ALREADY_VOTED);
            }
            else if (errorResponse.reason.equals("card_invalid")) {
                ErrorFragment.newInstance(this, CODE_CARD_INVALID);
            }
            else if (errorResponse.reason.equals("card_suspicious")) {
                ErrorFragment.newInstance(this, CODE_CARD_SUSPICIOUS);
            }
            else if (errorResponse.reason.equals("unqualified")) {
                ErrorFragment.newInstance(this, CODE_UNQUALIFIED);
            }
            else if (errorResponse.reason.equals("service_closed")) {
                ErrorFragment.newInstance(this, CODE_SERVICE_CLOSED);
            }
            else if (errorResponse.reason.equals("out_of_auth_code")) {
                ErrorFragment.newInstance(this, CODE_INSUFFICIENT_CODE);
            }
            else if (errorResponse.reason.equals("version_not_supported")) {
                ErrorFragment.newInstance(this, CODE_VERSION_NOT_SUPPORTED);
            }
            else if (errorResponse.reason.equals("external_error")) {
                ErrorFragment.newInstance(this, CODE_EXTERNAL_ERROR);
            }
            else {
                Log.w(PACKAGE_NAME, String.format(Locale.getDefault(),
                        "Login failed with error %s", errorResponse.reason));
                ErrorFragment.newInstance(this, CODE_GENERIC_ERROR);
            }
        }
        catch (RuntimeException ex) {
            Log.e(PACKAGE_NAME, String.format(Locale.getDefault(),
                    "Login failed with error %s", error.toString()));
            ErrorFragment.newInstance(this, CODE_SERVER_ERROR);
        }
    }
}
