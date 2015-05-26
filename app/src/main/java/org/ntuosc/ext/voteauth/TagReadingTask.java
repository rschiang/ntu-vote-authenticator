package org.ntuosc.ext.voteauth;

import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;

import static org.ntuosc.ext.voteauth.AppConfig.*;

public class TagReadingTask extends AsyncTask<Tag, Integer, TagReadingTask.Result> {

    private MainActivity mActivity;

    public TagReadingTask(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    protected Result doInBackground(Tag... tags) {
        // Get available tag. We assume there is only one per task.
        Tag tag = tags[0];

        // Read IDs from card
        String cid = Util.toHexString(tag.getId());
        String uid = null;

        MifareClassic card = MifareClassic.get(tag);
        if (card == null)
            return new Result(CODE_CARD_NOT_SUPPORTED, cid, null);

        try {
            card.connect();

            byte block = Private.CARD_BLOCK;
            byte[] magic = Private.CARD_MAGIC;

            if (card.authenticateSectorWithKeyA(block, magic)) {
                byte[] data = card.readBlock(card.sectorToBlock(block));
                uid = new String(data);

                if (uid.length() > 10)
                    uid = uid.substring(0, 10);
            }
            else {
                return new Result(CODE_CARD_INVALID, cid, null);
            }

            card.close();
        } catch (IOException ex) {
            return new Result(CODE_CARD_IO_ERROR, cid, null);
        }

        return new Result(CODE_SUCCESS, cid, uid);
    }

    @Override
    protected void onPostExecute(Result result) {
        // NOTE: This approach might leak memory as stated in
        // http://simonvt.net/2014/04/17/asynctask-is-bad-and-you-should-feel-bad/
        // But since it's getting late, whatever (flee)
        if (mActivity != null) {
            mActivity.onTagRead(result);

            // Release reference
            mActivity = null;
        }
    }

    public class Result {
        public Integer code;
        public String cid;
        public String uid;

        protected Result(Integer code, String cid, String uid) {
            this.code = code;
            this.cid = cid;
            this.uid = uid;
        }
    }
}
