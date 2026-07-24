package com.example.devicetrackerapp.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.example.devicetrackerapp.dto.ImageFolderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.example.devicetrackerapp.dto.ImageItem;

import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    public static List<ImageItem> getImages(Context context) {

        List<ImageItem> list = new ArrayList<>(500);

        String[] projection = {

                MediaStore.Images.Media._ID,

                MediaStore.Images.Media.DISPLAY_NAME,

                MediaStore.Images.Media.SIZE,

                MediaStore.Images.Media.BUCKET_ID,

                MediaStore.Images.Media.BUCKET_DISPLAY_NAME

        };

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        );

        if (cursor != null) {

            int idIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media._ID);

            int nameIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.DISPLAY_NAME);

            int bucketIdIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.BUCKET_ID);

            int bucketNameIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int sizeIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.SIZE);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idIndex);

                String name = cursor.getString(nameIndex);

                long size = cursor.getLong(sizeIndex);

                Uri imageUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                );

                String bucketId =
                        cursor.getString(bucketIdIndex);

                String bucketName =
                        cursor.getString(bucketNameIndex);

                list.add(

                        new ImageItem(

                                name,

                                imageUri,

                                size,

                                bucketId,

                                bucketName

                        )

                );
            }

            cursor.close();
        }

        return list;
    }

    public static List<ImageItem> getImages(

            Context context,

            String folder,

            int limit,

            int offset,

            String order

    ) {

        if (limit <= 0) {

            limit = Integer.MAX_VALUE;

        }

        List<ImageItem> list = new ArrayList<>();

        String[] projection = {

                MediaStore.Images.Media._ID,

                MediaStore.Images.Media.DISPLAY_NAME,

                MediaStore.Images.Media.SIZE,

                MediaStore.Images.Media.BUCKET_ID,

                MediaStore.Images.Media.BUCKET_DISPLAY_NAME

        };

        String selection = null;
        String[] selectionArgs = null;

        if (folder != null && !folder.trim().isEmpty()) {

            selection =
                    MediaStore.Images.Media.BUCKET_ID + "=?";

            selectionArgs = new String[]{folder};

        }

        String sortOrder;

        if (order != null && order.equalsIgnoreCase("OLDEST")) {

            sortOrder =
                    MediaStore.Images.Media.DATE_MODIFIED + " ASC";

        } else {

            sortOrder =
                    MediaStore.Images.Media.DATE_MODIFIED + " DESC";

        }

        Cursor cursor = context.getContentResolver().query(

                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

                projection,

                selection,

                selectionArgs,

                sortOrder

        );

        if (cursor == null) {

            return list;

        }

        int idIndex =
                cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media._ID);

        int nameIndex =
                cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.DISPLAY_NAME);

        int sizeIndex =
                cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.SIZE);

        int bucketIdIndex =
                cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.BUCKET_ID);

        int bucketNameIndex =
                cursor.getColumnIndexOrThrow(
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

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
                    cursor.getLong(idIndex);

            Uri imageUri =
                    ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id);

            list.add(

                    new ImageItem(

                            cursor.getString(nameIndex),

                            imageUri,

                            cursor.getLong(sizeIndex),

                            cursor.getString(bucketIdIndex),

                            cursor.getString(bucketNameIndex)

                    )

            );

        }

        cursor.close();

        return list;

    }

    public static List<ImageFolderItem> getImageFolders(Context context) {

        HashMap<String, ImageFolderItem> folderMap = new HashMap<>();

        String[] projection = {

                MediaStore.Images.Media.BUCKET_ID,

                MediaStore.Images.Media.BUCKET_DISPLAY_NAME

        };

        Cursor cursor = context.getContentResolver().query(

                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

                projection,

                null,

                null,

                null

        );

        if (cursor != null) {

            int bucketIdIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.BUCKET_ID);

            int bucketNameIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            while (cursor.moveToNext()) {

                String bucketId =
                        cursor.getString(bucketIdIndex);

                String bucketName =
                        cursor.getString(bucketNameIndex);

                if (bucketName == null)
                    bucketName = "Unknown";

                ImageFolderItem folder =
                        folderMap.get(bucketId);

                if (folder == null) {

                    folder = new ImageFolderItem(

                            bucketId,

                            bucketName,

                            1

                    );

                    folderMap.put(bucketId, folder);

                } else {

                    folder.setImageCount(

                            folder.getImageCount() + 1

                    );

                }

            }

            cursor.close();

        }

        return new ArrayList<>(folderMap.values());

    }

}