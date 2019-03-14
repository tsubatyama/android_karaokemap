package local.hal.st21.android.karaokemap;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.support.v4.app.FragmentManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ReservationActivity extends AppCompatActivity  implements TimePickerFragment.TimePickerListener {


    public ProgressDialog _pDialog;
    public String day2 = "";
    public String id = "";
    public String name = "";
    public String arrivaltime;
    public String usetime;

    public static Context context;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        //ツールバー(レイアウトを変更可)。
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("予約登録");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //戻るボタンを有効化

        context = this;

        Intent intent = getIntent();
        id = (intent.getStringExtra("id"));
        name = (intent.getStringExtra("name"));
        TextView tvName = findViewById(R.id.tv_StoreName);
        tvName.setText(name);

        Calendar cal = Calendar.getInstance();
        int nowYear = cal.get(Calendar.YEAR);
        int nowMonth = cal.get(Calendar.MONTH) +1;
        int nowDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        String sYear = String.valueOf(nowYear);
        String sMonth = String.valueOf(nowMonth);
        if(sMonth.length() == 1){
            sMonth = "0" + sMonth;
        }
        String sDayOfMonth = String.valueOf(nowDayOfMonth);
        if(sDayOfMonth.length() == 1){
            sDayOfMonth = "0" + sDayOfMonth;
        }
        day2 = sYear + sMonth + sDayOfMonth;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.send_menu, menu);

        return true;
    }

    public void showTimeNumberClick(View view) {

        TimePickerFragment dialog = new TimePickerFragment();
        FragmentManager manager = getSupportFragmentManager();
        dialog.show(manager, "onTimeSet");
    }

    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        // 選択された時、分がhourOfDay、minuteに入ってくる

        String hour = String.valueOf(hourOfDay);
        if(hour.length() == 1){
            usetime = "0" + hour;
        }else{
            usetime = hour;
        }

        if(hour.equals("0")){
            hour = "";
        }else{
            hour = hour + "時間";
        }
        String s_minute = String.valueOf(minute);
        if(s_minute.length() == 1){
            s_minute = "0" + s_minute;
        }
        usetime += s_minute;

        if(s_minute.equals("00")){
            s_minute = "";
        }else{
            s_minute = s_minute + "分";
        }

        TextView tvUsetime = findViewById(R.id.tv_Use);
        String time = hour + s_minute;
        tvUsetime.setText(time);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menuSend:


                ConfirmationDialogFragment dialog = new ConfirmationDialogFragment();
                FragmentManager manager = getSupportFragmentManager();
                dialog.show(manager, "ConfirmationDialogFragment");

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void reservationDialog(){

        EditText etPeople = findViewById(R.id.et_people);
        String stPeople = etPeople.getText().toString();

        EditText etName = findViewById(R.id.et_name);
        String stName = etName.getText().toString();

        EditText etTel = findViewById(R.id.et_tel);
        String stTel = etTel.getText().toString();

        EditText etMail = findViewById(R.id.et_mail);
        String stMail = etMail.getText().toString();

        EditText etRemarks = findViewById(R.id.et_remarks);
        String stRemarks = etRemarks.getText().toString();

        //非同期処理を開始する。
        ReservationActivity.ReservationTaskReceiver receiver = new ReservationActivity.ReservationTaskReceiver();
        //ここで渡した引数はLoginTaskReceiverクラスのdoInBackground(String... params)で受け取れる。
        receiver.execute(GetUrl.ReInsertUrl, id, day2, arrivaltime, usetime, stPeople, stName, stTel, stMail, stRemarks);

        finish();
    }
    /**
     * 非同期通信を行うAsyncTaskクラスを継承したメンバクラス.
     */
    private class ReservationTaskReceiver extends AsyncTask<String, Void, String> {

        private static final String DEBUG_TAG = "RestAccess";

        /**
         * 非同期に処理したい内容を記述するメソッド.
         * このメソッドは必ず実装する必要がある。
         *
         * @param params String型の配列。（可変長）
         * @return String型の結果JSONデータ。
         */
        @Override
        public String doInBackground(String... params) {
            String urlStr = params[0];
            String s_id = params[1];
            String s_day = params[2];
            String s_arri = params[3];
            String s_use = params[4];
            String s_peo = params[5];
            String s_na = params[6];
            String s_tel = params[7];
            String s_mai = params[8];
            String s_rem = params[9];

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";
            String postData = "s_id=" + s_id + "&s_day=" + s_day + "&s_arri=" + s_arri + "&s_use=" + s_use + "&s_peo=" + s_peo + "&s_na=" + s_na + "&s_tel=" + s_tel + "&s_mai=" + s_mai + "&s_rem=" + s_rem;

            try {
                URL url = new URL(urlStr);
                con = (HttpURLConnection) url.openConnection();

                //GET通信かPOST通信かを指定する。
                con.setRequestMethod("POST");

                //自動リダイレクトを許可するかどうか。
                con.setInstanceFollowRedirects(false);

                //時間制限。（ミリ秒単位）
                con.setReadTimeout(10000);
                con.setConnectTimeout(20000);

                con.setDoOutput(true);

                //POSTデータ送信処理。InputStream処理よりも先に記述する。
                OutputStream os = null;
                try {
                    os = con.getOutputStream();

                    //送信する値をByteデータに変換する（UTF-8）
                    os.write(postData.getBytes("UTF-8"));
                    os.flush();
                }
                catch (IOException ex) {
                    Log.e(DEBUG_TAG, "POST送信エラー", ex);
                }
                finally {
                    if(os != null) {
                        try {
                            os.close();
                        }
                        catch (IOException ex) {
                            Log.e(DEBUG_TAG, "OutputStream解放失敗", ex);
                        }
                    }
                }
                is = con.getInputStream();
                result = Tools.is2String(is);
            }
            catch (MalformedURLException ex) {
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            }
            catch (IOException ex) {
                Log.e(DEBUG_TAG, "通信失敗", ex);
            }
            finally {
                if(con != null) {
                    con.disconnect();
                }
                if(is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException ex) {
                        Log.e(DEBUG_TAG, "InputStream解放失敗", ex);
                    }
                }
            }
            return result;
        }

        @Override
        public void onPostExecute(String result) {
        }
    }
    public void showTimePicker(View view) {
        EditText etPeople = findViewById(R.id.et_people);
        String stPeople = etPeople.getText().toString();

        Intent intent = new Intent(ReservationActivity.this, TimeScheduleActivity.class);
        intent.putExtra("day2",day2);
        intent.putExtra("id",id);
        intent.putExtra("name",name);
        intent.putExtra("peoplenum",stPeople);
        startActivityForResult(intent,REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            TextView edit = findViewById(R.id.tv_edittime);
            edit.setText(data.getStringExtra("display"));
            day2 = data.getStringExtra("datadate");
            arrivaltime = data.getStringExtra("datatime");
        }else{
            //その他
        }
    }
}
