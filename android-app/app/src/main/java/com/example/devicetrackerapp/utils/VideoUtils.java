package com.example.devicetrackerapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.devicetrackerapp.dto.VideoItem;
import java.util.ArrayList;
import java.util.List;

public class VideoUtils {

    public static List<VideoItem> getVideos(Context context) {

        List<VideoItem> list = new ArrayList<>();

        String[] projection = {

                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION

        };

        Cursor cursor = context.getContentResolver().query(

                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,

                projection,

                null,

                null,

                MediaStore.Video.Media.DATE_ADDED + " DESC"

        );

        if (cursor != null) {

            int nameCol =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Video.Media.DISPLAY_NAME);

            int pathCol =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Video.Media.DATA);

            int sizeCol =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Video.Media.SIZE);

            int durationCol =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Video.Media.DURATION);

            while (cursor.moveToNext()) {

                list.add(

                        new VideoItem(

                                cursor.getString(nameCol),

                                cursor.getString(pathCol),

                                cursor.getLong(sizeCol),

                                cursor.getLong(durationCol)

                        )

                );

            }

            cursor.close();

        }

        return list;

    }

}