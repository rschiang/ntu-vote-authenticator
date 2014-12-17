package org.ntuosc.ext.voteauth;

public final class AppConfig {

    private AppConfig() {
        // Disallow instance creation
    }

    public static final String PACKAGE_NAME = "org.ntuosc.ext.voteauth";

    public static final String PREF_NAME = "NTUVoteAuth";

    public static final String PREF_STATION_USER = "username";

    public static final String PREF_STATION_PASS = "password";

    public static final String PREF_STATION_ID = "stationID";

    public static final String PREF_STATION_NAME = "stationName";

    public static final String API_KEY = "<Your API Key>";

    public static final int CODE_NFC_REQUEST = 200;

    public static final String ENDPOINT_VOTE_URL = "https://ntuvote.org/api/";

    public static final String ENDPOINT_AUTH_URL = "http://104.236.48.149/api/";

    public static final int CODE_SUCCESS = 0;

    public static final int CODE_STATION_BUSY = 303;

    public static final int CODE_CARD_IO_ERROR = 400;

    public static final int CODE_LOGIN_FAILED = 401;

    public static final int CODE_CARD_SUSPICIOUS = 403;

    public static final int CODE_CARD_NOT_SUPPORTED = 405;

    public static final int CODE_CARD_INVALID = 406;

    public static final int CODE_ALREADY_VOTED = 410;

    public static final int CODE_UNQUALIFIED = 412;

    public static final int CODE_GENERIC_ERROR = 500;

    public static final int CODE_NETWORK_ERROR = 502;

    public static final int CODE_SERVER_ERROR = 503;

    public static final int CODE_VERSION_NOT_SUPPORTED = 505;

    public static final int CODE_EXTERNAL_ERROR = 597;

    public static final int CODE_INSUFFICIENT_CODE = 598;

    public static final int CODE_SERVICE_CLOSED = 599;

    public static final String DEFAULT_ENCODING = "UTF-8";

}
