package com.example.himanshu.myapplication;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.MimeTypeMap;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Async task to upload a file to a directory
 */
class UploadFileTask extends AsyncTask<Uri, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);
        void onError(Exception e);
    }

    UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    @Override
    protected FileMetadata doInBackground(Uri... params) {


//        String remoteFolderPath = params[1];

        // Note - this is not ensuring the name is a valid dropbox file name
//            String remoteFileName = localFile.getName();
        File file = new File(params[0].getPath());
        try (InputStream inputStream = new FileInputStream(file)) {
            String uniqueID = UUID.randomUUID().toString();
            return mDbxClient.files().uploadBuilder("/"+uniqueID+ "."+MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()))
                    .withMode(WriteMode.ADD)
                    .uploadAndFinish(inputStream);
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}