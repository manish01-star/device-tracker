package com.example.devicetrackerapp.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.devicetrackerapp.api.ApiService;
import com.example.devicetrackerapp.dto.ApiResponse;
import com.example.devicetrackerapp.dto.SmsDTO;
import com.example.devicetrackerapp.dto.SmsSyncPayload;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility/Manager class responsible for:
 *
 * 1. Checking SMS permission
 * 2. Checking Contacts permission
 * 3. Reading SMS from device
 * 4. Resolving contact names
 * 5. Formatting SMS data
 * 6. Syncing SMS data with backend
 * 7. Parsing date strings into milliseconds
 */
public class SmsSyncUtils {

    private static final String TAG = "SmsSyncManager";

    private static final String DATE_FORMAT =
            "yyyy-MM-dd";

    private static final String SMS_DATE_FORMAT =
            "yyyy-MM-dd'T'HH:mm:ss";

    private static final long ONE_DAY_MILLIS =
            24L * 60L * 60L * 1000L;

    private final Context context;
    private final ApiService apiService;

    /**
     * Constructor
     *
     * @param context   Android context
     * @param apiService Retrofit API service
     */
    public SmsSyncUtils(
            Context context,
            ApiService apiService) {

        if (context == null) {
            throw new IllegalArgumentException(
                    "Context cannot be null"
            );
        }

        if (apiService == null) {
            throw new IllegalArgumentException(
                    "ApiService cannot be null"
            );
        }

        this.context =
                context.getApplicationContext();

        this.apiService =
                apiService;
    }

    // =========================================================
    // SMS PERMISSION
    // =========================================================

    /**
     * Checks whether READ_SMS permission is granted.
     *
     * @return true if SMS permission is available
     */
    public boolean hasSmsPermission() {

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // =========================================================
    // CONTACT PERMISSION
    // =========================================================

    /**
     * Checks whether READ_CONTACTS permission is granted.
     *
     * Contact permission is optional for SMS sync.
     *
     * @return true if contacts permission is available
     */
    private boolean hasContactPermission() {

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // =========================================================
    // READ SMS
    // =========================================================

    /**
     * Reads SMS messages from the device for the given date range.
     *
     * @param deviceId       device identifier
     * @param fromDateMillis starting timestamp
     * @param toDateMillis   ending timestamp
     *
     * @return list of SMS records
     */
    public List<SmsDTO> readSms(
            String deviceId,
            long fromDateMillis,
            long toDateMillis) {

        List<SmsDTO> smsList =
                new ArrayList<>();

        // -----------------------------------------------------
        // CHECK PERMISSION
        // -----------------------------------------------------

        if (!hasSmsPermission()) {

            Log.w(
                    TAG,
                    "READ_SMS permission not granted"
            );

            return smsList;
        }

        // -----------------------------------------------------
        // VALIDATE DATE RANGE
        // -----------------------------------------------------

        if (fromDateMillis > toDateMillis) {

            Log.w(
                    TAG,
                    "Invalid SMS date range"
            );

            return smsList;
        }

        ContentResolver resolver =
                context.getContentResolver();

        Uri smsUri =
                Telephony.Sms.CONTENT_URI;

        String selection =
                Telephony.Sms.DATE + " >= ? AND "
                        + Telephony.Sms.DATE + " <= ?";

        String[] selectionArgs = {
                String.valueOf(fromDateMillis),
                String.valueOf(toDateMillis)
        };

        String sortOrder =
                Telephony.Sms.DATE + " DESC";

        Cursor cursor = null;

        try {

            cursor = resolver.query(
                    smsUri,
                    null,
                    selection,
                    selectionArgs,
                    sortOrder
            );

            if (cursor == null) {

                Log.w(
                        TAG,
                        "SMS cursor is null"
                );

                return smsList;
            }

            // -------------------------------------------------
            // COLUMN INDEXES
            // -------------------------------------------------

            int idIndex =
                    cursor.getColumnIndex(
                            Telephony.Sms._ID
                    );

            int addressIndex =
                    cursor.getColumnIndex(
                            Telephony.Sms.ADDRESS
                    );

            int bodyIndex =
                    cursor.getColumnIndex(
                            Telephony.Sms.BODY
                    );

            int dateIndex =
                    cursor.getColumnIndex(
                            Telephony.Sms.DATE
                    );

            int typeIndex =
                    cursor.getColumnIndex(
                            Telephony.Sms.TYPE
                    );

            int readIndex =
                    cursor.getColumnIndex(
                            Telephony.Sms.READ
                    );

            int threadIdIndex =
                    cursor.getColumnIndex(
                            Telephony.Sms.THREAD_ID
                    );

            // -------------------------------------------------
            // READ SMS RECORDS
            // -------------------------------------------------

            while (cursor.moveToNext()) {

                SmsDTO sms =
                        new SmsDTO();

                // -------------------------------------------------
                // SMS ID
                // -------------------------------------------------

                if (idIndex >= 0) {

                    long smsId =
                            cursor.getLong(idIndex);

                    sms.setSmsId(
                            smsId
                    );
                }

                // -------------------------------------------------
                // DEVICE ID
                // -------------------------------------------------

                sms.setDeviceId(
                        deviceId
                );

                // -------------------------------------------------
                // PHONE NUMBER
                // -------------------------------------------------

                String phoneNumber = null;

                if (addressIndex >= 0) {

                    phoneNumber =
                            cursor.getString(
                                    addressIndex
                            );
                }

                sms.setPhoneNumber(
                        phoneNumber
                );

                // -------------------------------------------------
                // CONTACT NAME
                // -------------------------------------------------

                sms.setContactName(
                        getContactName(
                                phoneNumber
                        )
                );

                // -------------------------------------------------
                // MESSAGE BODY
                // -------------------------------------------------

                if (bodyIndex >= 0) {

                    sms.setMessageBody(
                            cursor.getString(
                                    bodyIndex
                            )
                    );
                }

                // -------------------------------------------------
                // SMS DATE
                // -------------------------------------------------

                if (dateIndex >= 0) {

                    long smsTime =
                            cursor.getLong(
                                    dateIndex
                            );

                    sms.setSmsDate(
                            formatSmsDate(
                                    smsTime
                            )
                    );
                }

                // -------------------------------------------------
                // SMS TYPE
                // -------------------------------------------------

                if (typeIndex >= 0) {

                    int type =
                            cursor.getInt(
                                    typeIndex
                            );

                    sms.setSmsType(
                            getSmsType(
                                    type
                            )
                    );
                }

                // -------------------------------------------------
                // READ STATUS
                // -------------------------------------------------

                if (readIndex >= 0) {

                    int read =
                            cursor.getInt(
                                    readIndex
                            );

                    sms.setReadStatus(
                            read == 1
                    );
                }

                // -------------------------------------------------
                // THREAD ID
                // -------------------------------------------------

                if (threadIdIndex >= 0) {

                    sms.setThreadId(
                            cursor.getString(
                                    threadIdIndex
                            )
                    );
                }

                smsList.add(
                        sms
                );
            }

        } catch (SecurityException e) {

            Log.e(
                    TAG,
                    "SMS permission denied while reading SMS",
                    e
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Error while reading SMS",
                    e
            );

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(
                TAG,
                "SMS records read: "
                        + smsList.size()
        );

        return smsList;
    }

    // =========================================================
    // FORMAT SMS DATE
    // =========================================================

    /**
     * Converts SMS timestamp into API date format.
     *
     * @param timestamp SMS timestamp in milliseconds
     *
     * @return formatted date string
     */
    private String formatSmsDate(
            long timestamp) {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        SMS_DATE_FORMAT,
                        Locale.getDefault()
                );

        return format.format(
                new Date(timestamp)
        );
    }

    // =========================================================
    // SMS TYPE
    // =========================================================

    /**
     * Converts Android SMS type into application-friendly value.
     *
     * @param type Android SMS type
     *
     * @return SMS type string
     */
    private String getSmsType(
            int type) {

        switch (type) {

            case Telephony.Sms.MESSAGE_TYPE_INBOX:

                return "RECEIVED";

            case Telephony.Sms.MESSAGE_TYPE_SENT:

                return "SENT";

            case Telephony.Sms.MESSAGE_TYPE_DRAFT:

                return "DRAFT";

            case Telephony.Sms.MESSAGE_TYPE_FAILED:

                return "FAILED";

            case Telephony.Sms.MESSAGE_TYPE_OUTBOX:

                return "OUTBOX";

            case Telephony.Sms.MESSAGE_TYPE_QUEUED:

                return "QUEUED";

            default:

                return "UNKNOWN";
        }
    }

    // =========================================================
    // CONTACT NAME
    // =========================================================

    /**
     * Finds contact name for a phone number.
     *
     * Contact permission is optional.
     *
     * If contact permission is not granted,
     * null will be returned.
     *
     * @param phoneNumber phone number
     *
     * @return contact name or null
     */
    private String getContactName(
            String phoneNumber) {

        if (phoneNumber == null
                || phoneNumber.trim().isEmpty()) {

            return null;
        }

        // -----------------------------------------------------
        // CONTACT PERMISSION
        // -----------------------------------------------------

        if (!hasContactPermission()) {

            return null;
        }

        Cursor cursor = null;

        try {

            Uri lookupUri =
                    Uri.withAppendedPath(
                            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                            Uri.encode(phoneNumber)
                    );

            cursor =
                    context
                            .getContentResolver()
                            .query(
                                    lookupUri,
                                    new String[]{
                                            ContactsContract.PhoneLookup.DISPLAY_NAME
                                    },
                                    null,
                                    null,
                                    null
                            );

            if (cursor != null
                    && cursor.moveToFirst()) {

                int nameIndex =
                        cursor.getColumnIndex(
                                ContactsContract.PhoneLookup.DISPLAY_NAME
                        );

                if (nameIndex >= 0) {

                    return cursor.getString(
                            nameIndex
                    );
                }
            }

        } catch (SecurityException e) {

            Log.e(
                    TAG,
                    "Contact permission denied",
                    e
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Error finding contact name",
                    e
            );

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    // =========================================================
    // SYNC SMS TO BACKEND
    // =========================================================

    /**
     * Reads SMS from device and sends them to backend.
     *
     * Even if no SMS is found, an empty payload is sent.
     * This allows backend to mark the SMS sync request as
     * completed.
     *
     * @param deviceId       device identifier
     * @param fromDateMillis starting timestamp
     * @param toDateMillis   ending timestamp
     */
    public void syncSms(
            String deviceId,
            long fromDateMillis,
            long toDateMillis) {

        // -----------------------------------------------------
        // VALIDATE DEVICE ID
        // -----------------------------------------------------

        if (deviceId == null
                || deviceId.trim().isEmpty()) {

            Log.e(
                    TAG,
                    "Device ID is required"
            );

            return;
        }

        // -----------------------------------------------------
        // CHECK SMS PERMISSION
        // -----------------------------------------------------

        if (!hasSmsPermission()) {

            Log.w(
                    TAG,
                    "READ_SMS permission not granted"
            );

            return;
        }

        // -----------------------------------------------------
        // VALIDATE DATE RANGE
        // -----------------------------------------------------

        if (fromDateMillis > toDateMillis) {

            Log.e(
                    TAG,
                    "Invalid SMS date range"
            );

            return;
        }

        Log.d(
                TAG,
                "Starting SMS sync"
                        + " | deviceId="
                        + deviceId
                        + " | from="
                        + fromDateMillis
                        + " | to="
                        + toDateMillis
        );

        // -----------------------------------------------------
        // READ SMS
        // -----------------------------------------------------

        List<SmsDTO> smsList =
                readSms(
                        deviceId,
                        fromDateMillis,
                        toDateMillis
                );

        Log.d(
                TAG,
                "SMS records ready for sync: "
                        + smsList.size()
        );

        // -----------------------------------------------------
        // CREATE PAYLOAD
        // -----------------------------------------------------

        SmsSyncPayload payload =
                new SmsSyncPayload(
                        deviceId,
                        smsList
                );

        // -----------------------------------------------------
        // API CALL
        // -----------------------------------------------------

        apiService
                .syncSms(payload)
                .enqueue(
                        new Callback<ApiResponse<String>>() {

                            @Override
                            public void onResponse(
                                    Call<ApiResponse<String>> call,
                                    Response<ApiResponse<String>> response) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    Log.d(
                                            TAG,
                                            "SMS sync completed"
                                    );

                                    Log.d(
                                            TAG,
                                            "HTTP code: "
                                                    + response.code()
                                    );

                                } else {

                                    Log.e(
                                            TAG,
                                            "SMS sync failed"
                                                    + " | HTTP="
                                                    + response.code()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<ApiResponse<String>> call,
                                    Throwable t) {

                                Log.e(
                                        TAG,
                                        "SMS sync API error",
                                        t
                                );
                            }
                        }
                );
    }

    // =========================================================
    // DATE STRING -> MILLIS
    // =========================================================

    /**
     * Converts yyyy-MM-dd into start-of-day timestamp.
     *
     * Example:
     *
     * 2026-08-10
     *
     * -> 2026-08-10 00:00:00
     *
     * @param date date string
     *
     * @return timestamp in milliseconds
     */
    public long parseFromDate(
            String date) {

        if (date == null
                || date.trim().isEmpty()) {

            return 0L;
        }

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            DATE_FORMAT,
                            Locale.getDefault()
                    );

            format.setLenient(
                    false
            );

            Date parsedDate =
                    format.parse(date);

            if (parsedDate == null) {

                return 0L;
            }

            return parsedDate.getTime();

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Invalid from date: "
                            + date,
                    e
            );

            return 0L;
        }
    }

    // =========================================================
    // DATE STRING -> MILLIS
    // =========================================================

    /**
     * Converts yyyy-MM-dd into end-of-day timestamp.
     *
     * Example:
     *
     * 2026-08-10
     *
     * -> 2026-08-10 23:59:59.999
     *
     * @param date date string
     *
     * @return timestamp in milliseconds
     */
    public long parseToDate(
            String date) {

        if (date == null
                || date.trim().isEmpty()) {

            return 0L;
        }

        try {

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            DATE_FORMAT,
                            Locale.getDefault()
                    );

            format.setLenient(
                    false
            );

            Date parsedDate =
                    format.parse(date);

            if (parsedDate == null) {

                return 0L;
            }

            return parsedDate.getTime()
                    + ONE_DAY_MILLIS
                    - 1L;

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Invalid to date: "
                            + date,
                    e
            );

            return 0L;
        }
    }
}