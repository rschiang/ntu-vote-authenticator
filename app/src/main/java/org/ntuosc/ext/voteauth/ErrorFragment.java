package org.ntuosc.ext.voteauth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ErrorFragment extends DialogFragment {
    private static final String ARG_DIALOG_TITLE = "title";
    private static final String ARG_DIALOG_MESSAGE = "message";
    private static final String ARG_DIALOG_BUTTON_TEXT = "buttonText";
    private static final String ARG_DIALOG_ERROR_CODE = "errorCode";

    private String mTitle;
    private String mMessage;
    private String mButtonText;
    private int mErrorCode;

    public static ErrorFragment newInstance(String title, String message, String buttonText, int errorCode) {
        ErrorFragment fragment = new ErrorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIALOG_TITLE, title);
        args.putString(ARG_DIALOG_MESSAGE, message);
        args.putString(ARG_DIALOG_BUTTON_TEXT, buttonText);
        args.putInt(ARG_DIALOG_ERROR_CODE, errorCode);
        fragment.setArguments(args);
        return fragment;
    }

    public static ErrorFragment newInstance(Activity activity, int errorCode) {
        ErrorFragment fragment = null;
        switch (errorCode) {
            case AppConfig.CODE_LOGIN_FAILED:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_login_failed),
                        activity.getString(R.string.prompt_login_failed),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_GENERIC_ERROR:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_generic_error),
                        activity.getString(R.string.prompt_generic_error),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_NETWORK_ERROR:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_network_error),
                        activity.getString(R.string.prompt_network_error),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_SERVER_ERROR:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_server_error),
                        activity.getString(R.string.prompt_server_error),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_EXTERNAL_ERROR:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_external_error),
                        activity.getString(R.string.prompt_external_error),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_VERSION_NOT_SUPPORTED:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_version_not_supported),
                        activity.getString(R.string.prompt_version_not_supported),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_SERVICE_CLOSED:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_service_closed),
                        activity.getString(R.string.prompt_service_closed),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_INSUFFICIENT_CODE:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_insufficient_code),
                        activity.getString(R.string.prompt_insufficient_code),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_CARD_IO_ERROR:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_card_error),
                        activity.getString(R.string.prompt_card_error),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_CARD_NOT_SUPPORTED:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_card_not_supported),
                        activity.getString(R.string.prompt_card_not_supported),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_CARD_INVALID:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_card_invalid),
                        activity.getString(R.string.prompt_card_invalid),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_CARD_SUSPICIOUS:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_card_suspicious),
                        activity.getString(R.string.prompt_card_suspicious),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_ALREADY_VOTED:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_already_voted),
                        activity.getString(R.string.prompt_already_voted),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;

            case AppConfig.CODE_UNQUALIFIED:
                fragment = ErrorFragment.newInstance(
                        activity.getString(R.string.title_unqualified),
                        activity.getString(R.string.prompt_unqualified),
                        activity.getString(R.string.action_ok),
                        errorCode);
                break;
        }

        if (fragment != null)
            fragment.show(activity.getFragmentManager(), "error");

        return fragment;
    }

    public ErrorFragment() {
        // Required empty public constructor
    }

    protected void loadArguments(Bundle params) {
        if (params != null) {
            mTitle = params.getString(ARG_DIALOG_TITLE);
            mMessage = params.getString(ARG_DIALOG_MESSAGE);
            mButtonText = params.getString(ARG_DIALOG_BUTTON_TEXT);
            mErrorCode = params.getInt(ARG_DIALOG_ERROR_CODE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadArguments(getArguments());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loadArguments(getArguments());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(mTitle)
                .setMessage(mMessage)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(mButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mErrorCode != 0) {
            try {
                Listener listener = (Listener) getActivity();
                listener.onErrorDialogDismiss(mErrorCode);
            }
            catch (ClassCastException ex) {
                // Do nothing if activity not implementing listener
            }
        }
    }

    public interface Listener {

        public void onErrorDialogDismiss(int errorCode);

    }
}
