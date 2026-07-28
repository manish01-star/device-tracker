package com.example.devicetrackerapp.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.devicetrackerapp.dto.AudioFolderItem;
import com.example.devicetrackerapp.dto.AudioItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AudioUtils {

    public static List<AudioItem> getAudios(
            Context context,
            String bucketId,
            int limit,
            int offset,
            String order
    ) {

        List<AudioItem> list = new ArrayList<>();

        if (limit <= 0) {
            limit = Integer.MAX_VALUE;
        }

        String[] projection = {

                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.BUCKET_ID,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME

        };

        // Only music files
        StringBuilder selection = new StringBuilder(
                MediaStore.Audio.Media.IS_MUSIC + "!=0"
        );

        List<String> args = new ArrayList<>();

        if (bucketId != null && !bucketId.trim().isEmpty()) {

            selection.append(" AND ")
                    .append(MediaStore.Audio.Media.BUCKET_ID)
                    .append("=?");

            args.add(bucketId);
        }

        String[] selectionArgs =
                args.isEmpty()
                        ? null
                        : args.toArray(new String[0]);

        String sortOrder;

        if ("OLDEST".equalsIgnoreCase(order)) {

            sortOrder =
                    MediaStore.Audio.Media.DATE_MODIFIED + " ASC";

        } else {

            sortOrder =
                    MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        }

        Cursor cursor =
                context.getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection.toString(),
                        selectionArgs,
                        sortOrder
                );

        if (cursor == null) {
            return list;
        }

        int skipped = 0;

        while (cursor.moveToNext()) {

            if (skipped < offset) {
                skipped++;
                continue;
            }

            if (list.size() >= limit) {
                break;
            }

            long id =
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media._ID
                            )
                    );

            Uri uri =
                    ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            id
                    );

            String name =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media.DISPLAY_NAME
                            )
                    );

            long size =
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media.SIZE
                            )
                    );

            long duration =
                    cursor.getLong(
                            cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media.DURATION
                            )
                    );

            String folderId =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media.BUCKET_ID
                            )
                    );

            String folderName =
                    cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
                            )
                    );

            if (folderName == null) {
                folderName = "Unknown";
            }

            list.add(
                    new AudioItem(
                            name,
                            uri,
                            size,
                            folderId,
                            folderName,
                            duration
                    )
            );
        }

        cursor.close();

        return list;
    }

    public static List<AudioFolderItem> getAudioFolders(Context context) {

        HashMap<String, AudioFolderItem> folderMap =
                new HashMap<>();

        String[] projection = {

                MediaStore.Audio.Media.BUCKET_ID,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME

        };

        String selection =
                MediaStore.Audio.Media.IS_MUSIC + "!=0";

        Cursor cursor =
                context.getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        null,
                        null
                );

        if (cursor == null) {
            return new ArrayList<>();
        }

        while (cursor.moveToNext()) {

            String bucketId = cursor.getString(0);

            String folderName = cursor.getString(1);

            if (bucketId == null) {
                continue;
            }

            if (folderName == null || folderName.trim().isEmpty()) {
                folderName = "Unknown";
            }

            AudioFolderItem folder =
                    folderMap.get(bucketId);

            if (folder == null) {

                folder =
                        new AudioFolderItem(
                                bucketId,
                                folderName,
                                1
                        );

                folderMap.put(bucketId, folder);

            } else {

                folder.setAudioCount(
                        folder.getAudioCount() + 1
                );
            }
        }

        cursor.close();

        return new ArrayList<>(folderMap.values());
    }

}