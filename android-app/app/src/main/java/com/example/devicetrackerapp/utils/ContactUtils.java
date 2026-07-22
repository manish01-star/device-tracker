package com.example.devicetrackerapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.example.devicetrackerapp.dto.ContactItem;

import java.util.ArrayList;
import java.util.List;

public class ContactUtils {

    public static List<ContactItem> getContacts(Context context) {

        List<ContactItem> contacts = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {

            int nameIndex = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            int phoneIndex = cursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {

                String name = nameIndex != -1
                        ? cursor.getString(nameIndex)
                        : "";

                String phone = phoneIndex != -1
                        ? cursor.getString(phoneIndex)
                        : "";

                contacts.add(new ContactItem(name, phone));
            }

            cursor.close();
        }

        return contacts;
    }
}