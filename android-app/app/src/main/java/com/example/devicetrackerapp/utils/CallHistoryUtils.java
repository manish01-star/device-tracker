package com.example.devicetrackerapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.example.devicetrackerapp.dto.CallHistorySyncItemDTO;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallHistoryUtils {

    private static final String TAG = "CallHistoryUtils";

    private final Context context;

    public CallHistoryUtils(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<CallHistorySyncItemDTO> getCallHistory(
            LocalDate fromDate,
            LocalDate toDate) {

        List<CallHistorySyncItemDTO> calls = new ArrayList<>();

        if (fromDate == null || toDate == null) {
            Log.e(TAG, "From date or To date is null");
            return calls;
        }

        long fromMillis = fromDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        long toMillis = toDate
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli() - 1;

        String selection =
                CallLog.Calls.DATE + " >= ? AND "
                        + CallLog.Calls.DATE + " <= ?";

        String[] selectionArgs = {
                String.valueOf(fromMillis),
                String.valueOf(toMillis)
        };

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.TYPE,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION,
                        CallLog.Calls.CACHED_NAME
                },
                selection,
                selectionArgs,
                CallLog.Calls.DATE + " DESC"
        );

        if (cursor == null) {
            Log.e(TAG, "CallLog cursor is null");
            return calls;
        }

        try {

            int numberIndex =
                    cursor.getColumnIndex(CallLog.Calls.NUMBER);

            int typeIndex =
                    cursor.getColumnIndex(CallLog.Calls.TYPE);

            int dateIndex =
                    cursor.getColumnIndex(CallLog.Calls.DATE);

            int durationIndex =
                    cursor.getColumnIndex(CallLog.Calls.DURATION);

            int nameIndex =
                    cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);

            while (cursor.moveToNext()) {

                CallHistorySyncItemDTO item =
                        new CallHistorySyncItemDTO();

                if (numberIndex >= 0) {
                    item.setPhoneNumber(
                            cursor.getString(numberIndex)
                    );
                }

                if (typeIndex >= 0) {
                    item.setCallType(
                            getCallType(
                                    cursor.getInt(typeIndex)
                            )
                    );
                }

                if (dateIndex >= 0) {

                    long callDateMillis =
                            cursor.getLong(dateIndex);

                    item.setCallDate(
                            formatDate(callDateMillis)
                    );
                }

                if (durationIndex >= 0) {
                    item.setDuration(
                            cursor.getLong(durationIndex)
                    );
                }

                if (nameIndex >= 0) {
                    item.setContactName(
                            cursor.getString(nameIndex)
                    );
                }

                calls.add(item);
            }

        } finally {
            cursor.close();
        }

        Log.d(
                TAG,
                "Total call history records = " + calls.size()
        );

        return calls;
    }

    private String formatDate(long millis) {

        SimpleDateFormat sdf =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault()
                );

        return sdf.format(new Date(millis));
    }

    private String getCallType(int type) {

        switch (type) {

            case CallLog.Calls.INCOMING_TYPE:
                return "INCOMING";

            case CallLog.Calls.OUTGOING_TYPE:
                return "OUTGOING";

            case CallLog.Calls.MISSED_TYPE:
                return "MISSED";

            case CallLog.Calls.REJECTED_TYPE:
                return "REJECTED";

            case CallLog.Calls.BLOCKED_TYPE:
                return "BLOCKED";

            default:
                return "UNKNOWN";
        }
    }
}