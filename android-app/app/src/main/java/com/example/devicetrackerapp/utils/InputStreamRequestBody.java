package com.example.devicetrackerapp.utils;

import android.content.Context;
import android.net.Uri;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;


public class InputStreamRequestBody extends RequestBody {

    private final Context context;
    private final Uri uri;
    private final MediaType mediaType;

    public InputStreamRequestBody(Context context,
                                  Uri uri,
                                  MediaType mediaType) {
        this.context = context;
        this.uri = uri;
        this.mediaType = mediaType;
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        InputStream inputStream =
                context.getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new IOException("Cannot open input stream");
        }

        byte[] buffer = new byte[8192];
        int read;

        try {

            while ((read = inputStream.read(buffer)) != -1) {

                sink.write(buffer, 0, read);

            }

        } finally {

            inputStream.close();

        }
    }
}