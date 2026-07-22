package com.example.devicetrackerapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.devicetrackerapp.dto.ImageItem;

import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    public static List<ImageItem> getImages(Context context) {

        List<ImageItem> list = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        );

        if (cursor != null) {

            int nameIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.DISPLAY_NAME);

            int pathIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.DATA);

            int sizeIndex =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.SIZE);

            while (cursor.moveToNext()) {

                String name = cursor.getString(nameIndex);

                String path = cursor.getString(pathIndex);

                long size = cursor.getLong(sizeIndex);

                list.add(
                        new ImageItem(
                                name,
                                path,
                                size
                        )
                );
            }

            cursor.close();
        }

        return list;
    }
}