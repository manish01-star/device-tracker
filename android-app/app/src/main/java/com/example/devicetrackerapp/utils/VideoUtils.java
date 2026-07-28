package com.example.devicetrackerapp.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import com.example.devicetrackerapp.dto.VideoFolderItem;
import com.example.devicetrackerapp.dto.VideoItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class VideoUtils {

    public static List<VideoItem> getVideos(
            Context context,
            String folder,
            int limit,
            int offset,
            String order
    ) {

        List<VideoItem> list = new ArrayList<>();

        if (limit <= 0) {
            limit = Integer.MAX_VALUE;
        }

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        };

        String selection = null;
        String[] selectionArgs = null;

        if (folder != null && !folder.trim().isEmpty()) {
            selection = MediaStore.Video.Media.BUCKET_ID + "=?";
            selectionArgs = new String[]{folder};
        }

        String sortOrder;

        if ("OLDEST".equalsIgnoreCase(order)) {
            sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " ASC";
        } else {
            sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        }

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

        if (cursor == null) {
            return list;
        }

        int idIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);

        int nameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);

        int sizeIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);

        int durationIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);

        int bucketIdIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);

        int bucketNameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

        int skipped = 0;

        while (cursor.moveToNext()) {

            if (skipped < offset) {
                skipped++;
                continue;
            }

            if (list.size() >= limit) {
                break;
            }

            long id = cursor.getLong(idIndex);

            Uri uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
            );

            list.add(
                    new VideoItem(
                            cursor.getString(nameIndex),
                            uri,
                            cursor.getLong(sizeIndex),
                            cursor.getString(bucketIdIndex),
                            cursor.getString(bucketNameIndex),
                            cursor.getLong(durationIndex)
                    )
            );
        }

        cursor.close();

        return list;
    }

    public static List<VideoFolderItem> getVideoFolders(Context context) {

        HashMap<String, VideoFolderItem> folderMap = new HashMap<>();

        String[] projection = {
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        if (cursor == null) {
            return new ArrayList<>();
        }

        Log.d("VIDEO_FOLDER", "Total Videos = " + cursor.getCount());

        int bucketIdIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);

        int bucketNameIndex =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {

            String bucketId = cursor.getString(bucketIdIndex);
            String bucketName = cursor.getString(bucketNameIndex);

            if (bucketId == null || bucketId.isEmpty()) {
                continue;
            }

            if (bucketName == null || bucketName.trim().isEmpty()) {
                bucketName = "Unknown";
            }

            Log.d(
                    "VIDEO_FOLDER",
                    bucketId + " -> " + bucketName
            );

            VideoFolderItem folder = folderMap.get(bucketId);

            if (folder == null) {

                folder = new VideoFolderItem(
                        bucketId,
                        bucketName,
                        1
                );

                folderMap.put(bucketId, folder);

            } else {

                folder.setVideoCount(
                        folder.getVideoCount() + 1
                );

            }
        }

        cursor.close();

        return new ArrayList<>(folderMap.values());
    }
}