package local.hal.st21.android.karaokemap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import java.util.Calendar;
import android.widget.NumberPicker;

public class TimePickerFragment extends DialogFragment {

    private TimePickerListener _listener;

    public interface TimePickerListener {
        void onTimeSet(int hourOfDay, int minute);
    }

    public static TimePickerFragment newInstance() {
        TimePickerFragment dialog = new TimePickerFragment();
        return dialog;
    }

    private TimePickerListener getListener() {
        try {
            return (TimePickerListener) ReservationActivity.context;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private View hour;
    private View minute;


    private View createPickerView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_time_picker, null);

        int hourValue;
        int minuteValue;
        Bundle args = getArguments();
        if (args != null && args.containsKey("hour")) {
            hourValue = args.getInt("hour");
            minuteValue = args.getInt("minute");
        } else {
            hourValue = 0;
            minuteValue = 0;
        }
        // 30分刻みに繰り上げる
        if (minuteValue == 0) {
            minuteValue = 0;
        } else if (minuteValue <= 30) {
            minuteValue = 30;
        } else {
            minuteValue = 0;
            hourValue = (hourValue + 1 > 23 ? 0 : hourValue + 1);
        }

            NumberPicker hour = view.findViewById(R.id.picker_hour);
            hour.setMinValue(0);
            hour.setMaxValue(23);
            hour.setValue(hourValue);
            this.hour = hour;

            NumberPicker minute = view.findViewById(R.id.picker_minute);
            minute.setMinValue(0);
            minute.setMaxValue(5);
            minute.setDisplayedValues(new String[]{"00", "30", "00", "30", "00", "30"});
            minute.setValue(minuteValue > 29 ? 1 : 0);
            this.minute = minute;
        return view;
    }

    private int getHour() {
            NumberPicker picker = (NumberPicker) hour;
            return picker.getValue();
    }

    private int getMinute() {
            NumberPicker picker = (NumberPicker) minute;
            return (picker.getValue() % 2) == 1 ? 30 : 0;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(createPickerView());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TimePickerListener listener = getListener();
                if (listener != null) {
                    listener.onTimeSet(getHour(), getMinute());
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setCancelable(true);
        return builder.create();
    }
}