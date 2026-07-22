package com.example.devicetrackerapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.example.devicetrackerapp.dto.AudioItem;

import java.util.ArrayList;
import java.util.List;

public class AudioUtils {

    public static List<AudioItem> getAudios(Context context) {

        List<AudioItem> list = new ArrayList<>();

        String[] projection = {

                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION

        };

        Cursor cursor = context.getContentResolver().query(

                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,

                projection,

                null,

                null,

                MediaStore.Audio.Media.DATE_ADDED + " DESC"

        );

        if (cursor != null) {

            int nameCol =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Media.DISPLAY_NAME);

            int pathCol =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Media.DATA);

            int sizeCol =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Media.SIZE);

            int durationCol =
                    cursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Media.DURATION);

            while (cursor.moveToNext()) {

                AudioItem item = new AudioItem();

                item.setAudioName(
                        cursor.getString(nameCol));

                item.setAudioPath(
                        cursor.getString(pathCol));

                item.setAudioSize(
                        cursor.getLong(sizeCol));

                item.setDuration(
                        cursor.getLong(durationCol));

                list.add(item);

            }

            cursor.close();
        }

        return list;
    }

}