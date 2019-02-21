package local.hal.st21.android.karaokemap;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmationDialogFragment  extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("予約確認");
        builder.setMessage("この内容で予約してもよろしいですか？");
        builder.setNegativeButton("いいえ", new DialogButtonClickListener());
        builder.setPositiveButton("はい", new DialogButtonClickListener());
        AlertDialog dialog = builder.create();
        return dialog;
    }

    /**
     * ダイアログのボタンが押されたときの処理が記述されたメンバクラス。
     */
    private class DialogButtonClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Activity parent = getActivity();
            System.out.println(parent);
            String msg = "";
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    ReservationActivity reservationActivity = (ReservationActivity) getActivity();
                    reservationActivity.reservationDialog();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }

    }
}
