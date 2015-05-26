package org.ntuosc.ext.voteauth;

import android.content.*;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.ntuosc.ext.voteauth.AppConfig.*;

public class PingTask implements Runnable, Callback<Api.StationPingResponse> {
    private MainActivity activity;

    public PingTask(MainActivity activity)
    {
        this.activity = activity;
        clearCallback();
    }

    @Override
    public void run() {
        SharedPreferences preferences = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (preferences.contains(PREF_STATION_TOKEN)) {
            Api.getAuthService().ping(API_KEY, API_VERSION,
                    preferences.getString(PREF_STATION_TOKEN, NONE), this);
            // Schedule next event
            scheduleCallback();
        }
    }

    @Override
    public void success(Api.StationPingResponse stationPingResponse, Response response) {
        // Do nothing
    }

    @Override
    public void failure(RetrofitError error) {
        // TODO: Show network disconnected warning
    }

    public void runCallback() {
        activity.handler.removeCallbacks(this);
        activity.handler.post(this);
    }

    public void scheduleCallback() {
        activity.handler.removeCallbacks(this);
        activity.handler.postDelayed(this, PING_INTERVAL);
    }

    public void clearCallback() {
        activity.handler.removeCallbacks(this);
    }
}
