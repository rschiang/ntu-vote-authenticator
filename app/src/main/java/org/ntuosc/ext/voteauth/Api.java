package org.ntuosc.ext.voteauth;

import com.google.gson.annotations.SerializedName;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.*;

import static org.ntuosc.ext.voteauth.AppConfig.*;

public class Api {

    private static AuthService mAuthService = null;

    public static AuthService getAuthService() {
        if (mAuthService == null) {
            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint(ENDPOINT_AUTH_URL).build();

            mAuthService = adapter.create(AuthService.class);
        }

        return mAuthService;
    }

    public interface AuthService {

        @FormUrlEncoded
        @POST("/authenticate")
        void authenticate(
                @Field("api_key") String apiKey,
                @Field("version") String protocolVersion,
                @Field("station") Integer stationId,
                @Field("cid") String cid,
                @Field("uid") String uid,
                Callback<AuthResponse> callback);

        @FormUrlEncoded
        @POST("/confirm")
        void confirm(
                @Field("api_key") String apiKey,
                @Field("version") String protocolVersion,
                @Field("uid") String uid,
                @Field("token") String token,
                @Field("station") Integer stationId,
                Callback<AuthConfirmResponse> callback);

        @FormUrlEncoded
        @POST("/report")
        void report(
                @Field("api_key") String apiKey,
                @Field("version") String protocolVersion,
                @Field("uid") String uid,
                @Field("token") String token,
                @Field("station") Integer stationId,
                Callback<AuthReportResponse> callback);
    }

    public class AuthResponse {
        public String status;
        public String token;
        public String uid;
        public String type;
    }

    public class AuthConfirmResponse {
        public String status;
        public String code;
    }

    public class AuthReportResponse {
        public String status;
    }

    public class AuthError {
        public String status;
        public String reason;
    }

    private static VoteService mVoteService = null;

    public static VoteService getVoteService() {
        if (mVoteService == null) {
            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint(ENDPOINT_VOTE_URL).build();

            mVoteService = adapter.create(VoteService.class);
        }

        return mVoteService;
    }

    public interface VoteService {

        @FormUrlEncoded
        @POST("/account/login")
        void login(@Field("apikey") String apiKey,
                   @Field("username") String username,
                   @Field("password") String password,
                   Callback<LoginResponse> callback);

        @FormUrlEncoded
        @POST("/vote/new")
        void newVote(@Field("apikey") String apiKey,
                     @Field("a_id") int stationId,
                     @Field("authcode") String authCode,
                     Callback<NewVoteResponse> callback);

    }

    public class LoginResponse {
        @SerializedName("a_id")
        public int stationId;
        public String name;
    }

    public class NewVoteResponse {
        @SerializedName("num")
        public int boothId;
    }

    public class VoteError {
        public String status;
        public String message;
    }
}
