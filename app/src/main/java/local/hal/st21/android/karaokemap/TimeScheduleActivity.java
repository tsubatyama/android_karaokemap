package local.hal.st21.android.karaokemap;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TimeScheduleActivity extends AppCompatActivity {


    public String day2 = "";
    String id = "";
    String stPeople = "";

    public String displaydate1 = "";
    public String displaydate2 = "";
    public String displaydate3 = "";
    public String displaydate4 = "";
    public String displaydate5 = "";
    public String displaydate6 = "";
    public String displaydate7 = "";

    public String datadate1 = "";
    public String datadate2 = "";
    public String datadate3 = "";
    public String datadate4 = "";
    public String datadate5 = "";
    public String datadate6 = "";
    public String datadate7 = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_schedule);

        //ツールバー(レイアウトを変更可)。
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("来店日時選択");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        //戻るボタンを有効化

        Intent intent = getIntent();
        TextView tvName = findViewById(R.id.tv_StoreName);
        tvName.setText(intent.getStringExtra("name"));

        id = (intent.getStringExtra("id"));
        day2 = (intent.getStringExtra("day2"));
        stPeople = (intent.getStringExtra("peoplenum"));

        //非同期処理を開始する。
        TimeScheduleActivity.TimeTableTaskReceiver receiver = new TimeScheduleActivity.TimeTableTaskReceiver();
        //ここで渡した引数はLoginTaskReceiverクラスのdoInBackground(String... params)で受け取れる。
        receiver.execute(GetUrl.TimeTableUrl, id, day2, stPeople);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showDatePicker(View view) {
        String strYear = day2.substring(0, 4);
        String strMonth = day2.substring(4, 6);
        String strDayOfMonth = day2.substring(6, 8);
        int nowYear = Integer.parseInt(strYear);
        int nowMonth = Integer.parseInt(strMonth) - 1;
        int nowDayOfMonth = Integer.parseInt(strDayOfMonth);

        DatePickerDialog dialog = new DatePickerDialog(TimeScheduleActivity.this, new TimeScheduleActivity.DatePickerDialogDateSetListener(), nowYear, nowMonth, nowDayOfMonth);
        dialog.show();
    }
    private class DatePickerDialogDateSetListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
            monthOfYear = monthOfYear + 1;
            String strmonth = String.valueOf(monthOfYear);
            String strdate = String.valueOf(dayOfMonth);
            if(strmonth.length() == 1){
                strmonth = "0" + strmonth;
            }
            if(strdate.length() == 1){
                strdate = "0" + strdate;
            }

            TextView YearMonth = findViewById(R.id.tv_YearMonth);
            YearMonth.setText(year + "年" + strmonth + "月");

            day2 = year + strmonth + strdate;

            //非同期処理を開始する。
            TimeScheduleActivity.TimeTableTaskReceiver receiver = new TimeScheduleActivity.TimeTableTaskReceiver();
            //ここで渡した引数はLoginTaskReceiverクラスのdoInBackground(String... params)で受け取れる。
            receiver.execute(GetUrl.TimeTableUrl, id, day2, stPeople);
        }
    }


    /**
     * 非同期通信を行うAsyncTaskクラスを継承したメンバクラス.
     */
    private class TimeTableTaskReceiver extends AsyncTask<String, Void, String> {

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
            String s_peo = params[3];

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";
            String postData = "stid=" + s_id + "&date=" + s_day + "&peonum=" + s_peo;

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
            try {
                JSONObject rootJSON = new JSONObject(result);

                JSONArray datas = rootJSON.getJSONArray("postsList");
                JSONObject data = datas.getJSONObject(0);

                //選択した日付のセット
                TextView date1 = findViewById(R.id.date1);
                date1.setText(data.getString("postDate"));

                datadate1 = data.getString("postDataTime");
                displaydate1 = data.getString("postDisplayTime");

                //選択した日付のスケジュールセット
                TextView Nine1 = findViewById(R.id.Nine1);
                Nine1.setText(data.getString("postNine"));
                TextView NineHalf1 = findViewById(R.id.NineHalf1);
                NineHalf1.setText(data.getString("postNineHalf"));
                TextView Ten1 = findViewById(R.id.Ten1);
                Ten1.setText(data.getString("postTen"));
                TextView TenHalf1 = findViewById(R.id.TenHalf1);
                TenHalf1.setText(data.getString("postTenHalf"));
                TextView Eleven1 = findViewById(R.id.Eleven1);
                Eleven1.setText(data.getString("postEleven"));
                TextView ElevenHalf1 = findViewById(R.id.ElevenHalf1);
                ElevenHalf1.setText(data.getString("postElevenHalf"));
                TextView Twelve1 = findViewById(R.id.Twelve1);
                Twelve1.setText(data.getString("postTwelve"));
                TextView TwelveHalf1 = findViewById(R.id.TwelveHalf1);
                TwelveHalf1.setText(data.getString("postTwelveHalf"));
                TextView Thirteen1 = findViewById(R.id.Thirteen1);
                Thirteen1.setText(data.getString("postThirteen"));
                TextView ThirteenHalf1 = findViewById(R.id.ThirteenHalf1);
                ThirteenHalf1.setText(data.getString("postThirteenHalf"));
                TextView Fourteen1 = findViewById(R.id.Fourteen1);
                Fourteen1.setText(data.getString("postFourteen"));
                TextView FourteenHalf1 = findViewById(R.id.FourteenHalf1);
                FourteenHalf1.setText(data.getString("postFourteenHalf"));
                TextView Fifteen1 = findViewById(R.id.Fifteen1);
                Fifteen1.setText(data.getString("postFifteen"));
                TextView FifteenHalf1 = findViewById(R.id.FifteenHalf1);
                FifteenHalf1.setText(data.getString("postFifteenHalf"));
                TextView Sixteen1 = findViewById(R.id.Sixteen1);
                Sixteen1.setText(data.getString("postSixteen"));
                TextView SixteenHalf1 = findViewById(R.id.SixteenHalf1);
                SixteenHalf1.setText(data.getString("postSixteenHalf"));
                TextView Seventeen1 = findViewById(R.id.Seventeen1);
                Seventeen1.setText(data.getString("postSeventeen"));
                TextView SeventeenHalf1 = findViewById(R.id.SeventeenHalf1);
                SeventeenHalf1.setText(data.getString("postSeventeenHalf"));
                TextView Eightteen1 = findViewById(R.id.Eightteen1);
                Eightteen1.setText(data.getString("postEightteen"));
                TextView EightteenHalf1 = findViewById(R.id.EightteenHalf1);
                EightteenHalf1.setText(data.getString("postEightteenHalf"));
                TextView Nineteen1 = findViewById(R.id.Nineteen1);
                Nineteen1.setText(data.getString("postNineteen"));
                TextView NineteenHalf1 = findViewById(R.id.NineteenHalf1);
                NineteenHalf1.setText(data.getString("postNineteenHalf"));
                TextView Twenty1 = findViewById(R.id.Twenty1);
                Twenty1.setText(data.getString("postTwenty"));
                TextView TwentyHalf1 = findViewById(R.id.TwentyHalf1);
                TwentyHalf1.setText(data.getString("postTwentyHalf"));
                TextView TwentyOne1 = findViewById(R.id.Twentyone1);
                TwentyOne1.setText(data.getString("postTwentyOne"));
                TextView TwentyOneHalf1 = findViewById(R.id.TwentyoneHalf1);
                TwentyOneHalf1.setText(data.getString("postTwentyOneHalf"));
                if(!data.getString("postNine").equals("×")){
                    Nine1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNine1(v);
                        }
                    });
                }
                if(!data.getString("postNineHalf").equals("×")){
                    NineHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineHalf1(v);
                        }
                    });
                }
                if(!data.getString("postTen").equals("×")){
                    Ten1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTen1(v);
                        }
                    });
                }
                if(!data.getString("postTenHalf").equals("×")){
                    TenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postEleven").equals("×")){
                    Eleven1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEleven1(v);
                        }
                    });
                }
                if(!data.getString("postElevenHalf").equals("×")){
                    ElevenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickElevenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postTwelve").equals("×")){
                    Twelve1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelve1(v);
                        }
                    });
                }
                if(!data.getString("postTwelveHalf").equals("×")){
                    TwelveHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelveHalf1(v);
                        }
                    });
                }
                if(!data.getString("postThirteen").equals("×")){
                    Thirteen1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteen1(v);
                        }
                    });
                }
                if(!data.getString("postThirteenHalf").equals("×")){
                    ThirteenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postFourteen").equals("×")){
                    Fourteen1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteen1(v);
                        }
                    });
                }
                if(!data.getString("postFourteenHalf").equals("×")){
                    FourteenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postFifteen").equals("×")){
                    Fifteen1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteen1(v);
                        }
                    });
                }
                if(!data.getString("postFifteenHalf").equals("×")){
                    FifteenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postSixteen").equals("×")){
                    Sixteen1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteen1(v);
                        }
                    });
                }
                if(!data.getString("postSixteenHalf").equals("×")){
                    SixteenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postSeventeen").equals("×")){
                    Seventeen1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeen1(v);
                        }
                    });
                }
                if(!data.getString("postSeventeenHalf").equals("×")){
                    SeventeenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postEightteen").equals("×")){
                    Eightteen1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteen1(v);
                        }
                    });
                }
                if(!data.getString("postEightteenHalf").equals("×")){
                    EightteenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postNineteen").equals("×")){
                    Nineteen1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteen1(v);
                        }
                    });
                }
                if(!data.getString("postNineteenHalf").equals("×")){
                    NineteenHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteenHalf1(v);
                        }
                    });
                }
                if(!data.getString("postTwenty").equals("×")){
                    Twenty1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwenty1(v);
                        }
                    });
                }
                if(!data.getString("postTwentyHalf").equals("×")){
                    TwentyHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyHalf1(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOne").equals("×")){
                    TwentyOne1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyone1(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOneHalf").equals("×")){
                    TwentyOneHalf1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyoneHalf1(v);
                        }
                    });
                }

                //選択した日付＋１日のスケジュールセット
                data = datas.getJSONObject(1);

                //選択した日付＋１日のセット
                TextView date2 = findViewById(R.id.date2);
                date2.setText(data.getString("postDate"));

                datadate2 = data.getString("postDataTime");
                displaydate2 = data.getString("postDisplayTime");

                TextView Nine2 = findViewById(R.id.Nine2);
                Nine2.setText(data.getString("postNine"));
                TextView NineHalf2 = findViewById(R.id.NineHalf2);
                NineHalf2.setText(data.getString("postNineHalf"));
                TextView Ten2 = findViewById(R.id.Ten2);
                Ten2.setText(data.getString("postTen"));
                TextView TenHalf2 = findViewById(R.id.TenHalf2);
                TenHalf2.setText(data.getString("postTenHalf"));
                TextView Eleven2 = findViewById(R.id.Eleven2);
                Eleven2.setText(data.getString("postEleven"));
                TextView ElevenHalf2 = findViewById(R.id.ElevenHalf2);
                ElevenHalf2.setText(data.getString("postElevenHalf"));
                TextView Twelve2 = findViewById(R.id.Twelve2);
                Twelve2.setText(data.getString("postTwelve"));
                TextView TwelveHalf2 = findViewById(R.id.TwelveHalf2);
                TwelveHalf2.setText(data.getString("postTwelveHalf"));
                TextView Thirteen2 = findViewById(R.id.Thirteen2);
                Thirteen2.setText(data.getString("postThirteen"));
                TextView ThirteenHalf2 = findViewById(R.id.ThirteenHalf2);
                ThirteenHalf2.setText(data.getString("postThirteenHalf"));
                TextView Fourteen2 = findViewById(R.id.Fourteen2);
                Fourteen2.setText(data.getString("postFourteen"));
                TextView FourteenHalf2 = findViewById(R.id.FourteenHalf2);
                FourteenHalf2.setText(data.getString("postFourteenHalf"));
                TextView Fifteen2 = findViewById(R.id.Fifteen2);
                Fifteen2.setText(data.getString("postFifteen"));
                TextView FifteenHalf2 = findViewById(R.id.FifteenHalf2);
                FifteenHalf2.setText(data.getString("postFifteenHalf"));
                TextView Sixteen2 = findViewById(R.id.Sixteen2);
                Sixteen2.setText(data.getString("postSixteen"));
                TextView SixteenHalf2 = findViewById(R.id.SixteenHalf2);
                SixteenHalf2.setText(data.getString("postSixteenHalf"));
                TextView Seventeen2 = findViewById(R.id.Seventeen2);
                Seventeen2.setText(data.getString("postSeventeen"));
                TextView SeventeenHalf2 = findViewById(R.id.SeventeenHalf2);
                SeventeenHalf2.setText(data.getString("postSeventeenHalf"));
                TextView Eightteen2 = findViewById(R.id.Eightteen2);
                Eightteen2.setText(data.getString("postEightteen"));
                TextView EightteenHalf2 = findViewById(R.id.EightteenHalf2);
                EightteenHalf2.setText(data.getString("postEightteenHalf"));
                TextView Nineteen2 = findViewById(R.id.Nineteen2);
                Nineteen2.setText(data.getString("postNineteen"));
                TextView NineteenHalf2 = findViewById(R.id.NineteenHalf2);
                NineteenHalf2.setText(data.getString("postNineteenHalf"));
                TextView Twenty2 = findViewById(R.id.Twenty2);
                Twenty2.setText(data.getString("postTwenty"));
                TextView TwentyHalf2 = findViewById(R.id.TwentyHalf2);
                TwentyHalf2.setText(data.getString("postTwentyHalf"));
                TextView TwentyOne2 = findViewById(R.id.Twentyone2);
                TwentyOne2.setText(data.getString("postTwentyOne"));
                TextView TwentyOneHalf2 = findViewById(R.id.TwentyoneHalf2);
                TwentyOneHalf2.setText(data.getString("postTwentyOneHalf"));
                if(!data.getString("postNine").equals("×")){
                    Nine2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNine2(v);
                        }
                    });
                }
                if(!data.getString("postNineHalf").equals("×")){
                    NineHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineHalf2(v);
                        }
                    });
                }
                if(!data.getString("postTen").equals("×")){
                    Ten2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTen2(v);
                        }
                    });
                }
                if(!data.getString("postTenHalf").equals("×")){
                    TenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postEleven").equals("×")){
                    Eleven2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEleven2(v);
                        }
                    });
                }
                if(!data.getString("postElevenHalf").equals("×")){
                    ElevenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickElevenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postTwelve").equals("×")){
                    Twelve2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelve2(v);
                        }
                    });
                }
                if(!data.getString("postTwelveHalf").equals("×")){
                    TwelveHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelveHalf2(v);
                        }
                    });
                }
                if(!data.getString("postThirteen").equals("×")){
                    Thirteen2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteen2(v);
                        }
                    });
                }
                if(!data.getString("postThirteenHalf").equals("×")){
                    ThirteenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postFourteen").equals("×")){
                    Fourteen2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteen2(v);
                        }
                    });
                }
                if(!data.getString("postFourteenHalf").equals("×")){
                    FourteenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postFifteen").equals("×")){
                    Fifteen2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteen2(v);
                        }
                    });
                }
                if(!data.getString("postFifteenHalf").equals("×")){
                    FifteenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postSixteen").equals("×")){
                    Sixteen2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteen2(v);
                        }
                    });
                }
                if(!data.getString("postSixteenHalf").equals("×")){
                    SixteenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postSeventeen").equals("×")){
                    Seventeen2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeen2(v);
                        }
                    });
                }
                if(!data.getString("postSeventeenHalf").equals("×")){
                    SeventeenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postEightteen").equals("×")){
                    Eightteen2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteen2(v);
                        }
                    });
                }
                if(!data.getString("postEightteenHalf").equals("×")){
                    EightteenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postNineteen").equals("×")){
                    Nineteen2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteen2(v);
                        }
                    });
                }
                if(!data.getString("postNineteenHalf").equals("×")){
                    NineteenHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteenHalf2(v);
                        }
                    });
                }
                if(!data.getString("postTwenty").equals("×")){
                    Twenty2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwenty2(v);
                        }
                    });
                }
                if(!data.getString("postTwentyHalf").equals("×")){
                    TwentyHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyHalf2(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOne").equals("×")){
                    TwentyOne2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyone2(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOneHalf").equals("×")){
                    TwentyOneHalf2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyoneHalf2(v);
                        }
                    });
                }

                //選択した日付＋２日のスケジュールセット
                data = datas.getJSONObject(2);

                //選択した日付＋２日のセット
                TextView date3 = findViewById(R.id.date3);
                date3.setText(data.getString("postDate"));

                datadate3 = data.getString("postDataTime");
                displaydate3 = data.getString("postDisplayTime");

                TextView Nine3 = findViewById(R.id.Nine3);
                Nine3.setText(data.getString("postNine"));
                TextView NineHalf3 = findViewById(R.id.NineHalf3);
                NineHalf3.setText(data.getString("postNineHalf"));
                TextView Ten3 = findViewById(R.id.Ten3);
                Ten3.setText(data.getString("postTen"));
                TextView TenHalf3 = findViewById(R.id.TenHalf3);
                TenHalf3.setText(data.getString("postTenHalf"));
                TextView Eleven3 = findViewById(R.id.Eleven3);
                Eleven3.setText(data.getString("postEleven"));
                TextView ElevenHalf3 = findViewById(R.id.ElevenHalf3);
                ElevenHalf3.setText(data.getString("postElevenHalf"));
                TextView Twelve3 = findViewById(R.id.Twelve3);
                Twelve3.setText(data.getString("postTwelve"));
                TextView TwelveHalf3 = findViewById(R.id.TwelveHalf3);
                TwelveHalf3.setText(data.getString("postTwelveHalf"));
                TextView Thirteen3 = findViewById(R.id.Thirteen3);
                Thirteen3.setText(data.getString("postThirteen"));
                TextView ThirteenHalf3 = findViewById(R.id.ThirteenHalf3);
                ThirteenHalf3.setText(data.getString("postThirteenHalf"));
                TextView Fourteen3 = findViewById(R.id.Fourteen3);
                Fourteen3.setText(data.getString("postFourteen"));
                TextView FourteenHalf3 = findViewById(R.id.FourteenHalf3);
                FourteenHalf3.setText(data.getString("postFourteenHalf"));
                TextView Fifteen3 = findViewById(R.id.Fifteen3);
                Fifteen3.setText(data.getString("postFifteen"));
                TextView FifteenHalf3 = findViewById(R.id.FifteenHalf3);
                FifteenHalf3.setText(data.getString("postFifteenHalf"));
                TextView Sixteen3 = findViewById(R.id.Sixteen3);
                Sixteen3.setText(data.getString("postSixteen"));
                TextView SixteenHalf3 = findViewById(R.id.SixteenHalf3);
                SixteenHalf3.setText(data.getString("postSixteenHalf"));
                TextView Seventeen3 = findViewById(R.id.Seventeen3);
                Seventeen3.setText(data.getString("postSeventeen"));
                TextView SeventeenHalf3 = findViewById(R.id.SeventeenHalf3);
                SeventeenHalf3.setText(data.getString("postSeventeenHalf"));
                TextView Eightteen3 = findViewById(R.id.Eightteen3);
                Eightteen3.setText(data.getString("postEightteen"));
                TextView EightteenHalf3 = findViewById(R.id.EightteenHalf3);
                EightteenHalf3.setText(data.getString("postEightteenHalf"));
                TextView Nineteen3 = findViewById(R.id.Nineteen3);
                Nineteen3.setText(data.getString("postNineteen"));
                TextView NineteenHalf3 = findViewById(R.id.NineteenHalf3);
                NineteenHalf3.setText(data.getString("postNineteenHalf"));
                TextView Twenty3 = findViewById(R.id.Twenty3);
                Twenty3.setText(data.getString("postTwenty"));
                TextView TwentyHalf3 = findViewById(R.id.TwentyHalf3);
                TwentyHalf3.setText(data.getString("postTwentyHalf"));
                TextView TwentyOne3 = findViewById(R.id.Twentyone3);
                TwentyOne3.setText(data.getString("postTwentyOne"));
                TextView TwentyOneHalf3 = findViewById(R.id.TwentyoneHalf3);
                TwentyOneHalf3.setText(data.getString("postTwentyOneHalf"));
                if(!data.getString("postNine").equals("×")){
                    Nine3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNine3(v);
                        }
                    });
                }
                if(!data.getString("postNineHalf").equals("×")){
                    NineHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineHalf3(v);
                        }
                    });
                }
                if(!data.getString("postTen").equals("×")){
                    Ten3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTen3(v);
                        }
                    });
                }
                if(!data.getString("postTenHalf").equals("×")){
                    TenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postEleven").equals("×")){
                    Eleven3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEleven3(v);
                        }
                    });
                }
                if(!data.getString("postElevenHalf").equals("×")){
                    ElevenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickElevenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postTwelve").equals("×")){
                    Twelve3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelve3(v);
                        }
                    });
                }
                if(!data.getString("postTwelveHalf").equals("×")){
                    TwelveHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelveHalf3(v);
                        }
                    });
                }
                if(!data.getString("postThirteen").equals("×")){
                    Thirteen3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteen3(v);
                        }
                    });
                }
                if(!data.getString("postThirteenHalf").equals("×")){
                    ThirteenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postFourteen").equals("×")){
                    Fourteen3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteen3(v);
                        }
                    });
                }
                if(!data.getString("postFourteenHalf").equals("×")){
                    FourteenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postFifteen").equals("×")){
                    Fifteen3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteen3(v);
                        }
                    });
                }
                if(!data.getString("postFifteenHalf").equals("×")){
                    FifteenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postSixteen").equals("×")){
                    Sixteen3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteen3(v);
                        }
                    });
                }
                if(!data.getString("postSixteenHalf").equals("×")){
                    SixteenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postSeventeen").equals("×")){
                    Seventeen3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeen3(v);
                        }
                    });
                }
                if(!data.getString("postSeventeenHalf").equals("×")){
                    SeventeenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postEightteen").equals("×")){
                    Eightteen3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteen3(v);
                        }
                    });
                }
                if(!data.getString("postEightteenHalf").equals("×")){
                    EightteenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postNineteen").equals("×")){
                    Nineteen3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteen3(v);
                        }
                    });
                }
                if(!data.getString("postNineteenHalf").equals("×")){
                    NineteenHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteenHalf3(v);
                        }
                    });
                }
                if(!data.getString("postTwenty").equals("×")){
                    Twenty3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwenty3(v);
                        }
                    });
                }
                if(!data.getString("postTwentyHalf").equals("×")){
                    TwentyHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyHalf3(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOne").equals("×")){
                    TwentyOne3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyone3(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOneHalf").equals("×")){
                    TwentyOneHalf3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyoneHalf3(v);
                        }
                    });
                }

                //選択した日付３日のスケジュールセット
                data = datas.getJSONObject(3);

                //選択した日付＋３日のセット
                TextView date4 = findViewById(R.id.date4);
                date4.setText(data.getString("postDate"));

                datadate4 = data.getString("postDataTime");
                displaydate4 = data.getString("postDisplayTime");

                TextView Nine4 = findViewById(R.id.Nine4);
                Nine4.setText(data.getString("postNine"));
                TextView NineHalf4 = findViewById(R.id.NineHalf4);
                NineHalf4.setText(data.getString("postNineHalf"));
                TextView Ten4 = findViewById(R.id.Ten4);
                Ten4.setText(data.getString("postTen"));
                TextView TenHalf4 = findViewById(R.id.TenHalf4);
                TenHalf4.setText(data.getString("postTenHalf"));
                TextView Eleven4 = findViewById(R.id.Eleven4);
                Eleven4.setText(data.getString("postEleven"));
                TextView ElevenHalf4 = findViewById(R.id.ElevenHalf4);
                ElevenHalf4.setText(data.getString("postElevenHalf"));
                TextView Twelve4 = findViewById(R.id.Twelve4);
                Twelve4.setText(data.getString("postTwelve"));
                TextView TwelveHalf4 = findViewById(R.id.TwelveHalf4);
                TwelveHalf4.setText(data.getString("postTwelveHalf"));
                TextView Thirteen4 = findViewById(R.id.Thirteen4);
                Thirteen4.setText(data.getString("postThirteen"));
                TextView ThirteenHalf4 = findViewById(R.id.ThirteenHalf4);
                ThirteenHalf4.setText(data.getString("postThirteenHalf"));
                TextView Fourteen4 = findViewById(R.id.Fourteen4);
                Fourteen4.setText(data.getString("postFourteen"));
                TextView FourteenHalf4 = findViewById(R.id.FourteenHalf4);
                FourteenHalf4.setText(data.getString("postFourteenHalf"));
                TextView Fifteen4 = findViewById(R.id.Fifteen4);
                Fifteen4.setText(data.getString("postFifteen"));
                TextView FifteenHalf4 = findViewById(R.id.FifteenHalf4);
                FifteenHalf4.setText(data.getString("postFifteenHalf"));
                TextView Sixteen4 = findViewById(R.id.Sixteen4);
                Sixteen4.setText(data.getString("postSixteen"));
                TextView SixteenHalf4 = findViewById(R.id.SixteenHalf4);
                SixteenHalf4.setText(data.getString("postSixteenHalf"));
                TextView Seventeen4 = findViewById(R.id.Seventeen4);
                Seventeen4.setText(data.getString("postSeventeen"));
                TextView SeventeenHalf4 = findViewById(R.id.SeventeenHalf4);
                SeventeenHalf4.setText(data.getString("postSeventeenHalf"));
                TextView Eightteen4 = findViewById(R.id.Eightteen4);
                Eightteen4.setText(data.getString("postEightteen"));
                TextView EightteenHalf4 = findViewById(R.id.EightteenHalf4);
                EightteenHalf4.setText(data.getString("postEightteenHalf"));
                TextView Nineteen4 = findViewById(R.id.Nineteen4);
                Nineteen4.setText(data.getString("postNineteen"));
                TextView NineteenHalf4 = findViewById(R.id.NineteenHalf4);
                NineteenHalf4.setText(data.getString("postNineteenHalf"));
                TextView Twenty4 = findViewById(R.id.Twenty4);
                Twenty4.setText(data.getString("postTwenty"));
                TextView TwentyHalf4 = findViewById(R.id.TwentyHalf4);
                TwentyHalf4.setText(data.getString("postTwentyHalf"));
                TextView TwentyOne4 = findViewById(R.id.Twentyone4);
                TwentyOne4.setText(data.getString("postTwentyOne"));
                TextView TwentyOneHalf4 = findViewById(R.id.TwentyoneHalf4);
                TwentyOneHalf4.setText(data.getString("postTwentyOneHalf"));
                if(!data.getString("postNine").equals("×")){
                    Nine4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNine4(v);
                        }
                    });
                }
                if(!data.getString("postNineHalf").equals("×")){
                    NineHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineHalf4(v);
                        }
                    });
                }
                if(!data.getString("postTen").equals("×")){
                    Ten4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTen4(v);
                        }
                    });
                }
                if(!data.getString("postTenHalf").equals("×")){
                    TenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postEleven").equals("×")){
                    Eleven4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEleven4(v);
                        }
                    });
                }
                if(!data.getString("postElevenHalf").equals("×")){
                    ElevenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickElevenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postTwelve").equals("×")){
                    Twelve4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelve4(v);
                        }
                    });
                }
                if(!data.getString("postTwelveHalf").equals("×")){
                    TwelveHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelveHalf4(v);
                        }
                    });
                }
                if(!data.getString("postThirteen").equals("×")){
                    Thirteen4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteen4(v);
                        }
                    });
                }
                if(!data.getString("postThirteenHalf").equals("×")){
                    ThirteenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postFourteen").equals("×")){
                    Fourteen4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteen4(v);
                        }
                    });
                }
                if(!data.getString("postFourteenHalf").equals("×")){
                    FourteenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postFifteen").equals("×")){
                    Fifteen4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteen4(v);
                        }
                    });
                }
                if(!data.getString("postFifteenHalf").equals("×")){
                    FifteenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postSixteen").equals("×")){
                    Sixteen4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteen4(v);
                        }
                    });
                }
                if(!data.getString("postSixteenHalf").equals("×")){
                    SixteenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postSeventeen").equals("×")){
                    Seventeen4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeen4(v);
                        }
                    });
                }
                if(!data.getString("postSeventeenHalf").equals("×")){
                    SeventeenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postEightteen").equals("×")){
                    Eightteen4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteen4(v);
                        }
                    });
                }
                if(!data.getString("postEightteenHalf").equals("×")){
                    EightteenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postNineteen").equals("×")){
                    Nineteen4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteen4(v);
                        }
                    });
                }
                if(!data.getString("postNineteenHalf").equals("×")){
                    NineteenHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteenHalf4(v);
                        }
                    });
                }
                if(!data.getString("postTwenty").equals("×")){
                    Twenty4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwenty4(v);
                        }
                    });
                }
                if(!data.getString("postTwentyHalf").equals("×")){
                    TwentyHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyHalf4(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOne").equals("×")){
                    TwentyOne4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyone4(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOneHalf").equals("×")){
                    TwentyOneHalf4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyoneHalf4(v);
                        }
                    });
                }


                //選択した日付＋４日のスケジュールセット
                data = datas.getJSONObject(4);

                //選択した日付＋４日のセット
                TextView date5 = findViewById(R.id.date5);
                date5.setText(data.getString("postDate"));

                datadate5 = data.getString("postDataTime");
                displaydate5 = data.getString("postDisplayTime");

                TextView Nine5 = findViewById(R.id.Nine5);
                Nine5.setText(data.getString("postNine"));
                TextView NineHalf5 = findViewById(R.id.NineHalf5);
                NineHalf5.setText(data.getString("postNineHalf"));
                TextView Ten5 = findViewById(R.id.Ten5);
                Ten5.setText(data.getString("postTen"));
                TextView TenHalf5 = findViewById(R.id.TenHalf5);
                TenHalf5.setText(data.getString("postTenHalf"));
                TextView Eleven5 = findViewById(R.id.Eleven5);
                Eleven5.setText(data.getString("postEleven"));
                TextView ElevenHalf5 = findViewById(R.id.ElevenHalf5);
                ElevenHalf5.setText(data.getString("postElevenHalf"));
                TextView Twelve5 = findViewById(R.id.Twelve5);
                Twelve5.setText(data.getString("postTwelve"));
                TextView TwelveHalf5 = findViewById(R.id.TwelveHalf5);
                TwelveHalf5.setText(data.getString("postTwelveHalf"));
                TextView Thirteen5 = findViewById(R.id.Thirteen5);
                Thirteen5.setText(data.getString("postThirteen"));
                TextView ThirteenHalf5 = findViewById(R.id.ThirteenHalf5);
                ThirteenHalf5.setText(data.getString("postThirteenHalf"));
                TextView Fourteen5 = findViewById(R.id.Fourteen5);
                Fourteen5.setText(data.getString("postFourteen"));
                TextView FourteenHalf5 = findViewById(R.id.FourteenHalf5);
                FourteenHalf5.setText(data.getString("postFourteenHalf"));
                TextView Fifteen5 = findViewById(R.id.Fifteen5);
                Fifteen5.setText(data.getString("postFifteen"));
                TextView FifteenHalf5 = findViewById(R.id.FifteenHalf5);
                FifteenHalf5.setText(data.getString("postFifteenHalf"));
                TextView Sixteen5 = findViewById(R.id.Sixteen5);
                Sixteen5.setText(data.getString("postSixteen"));
                TextView SixteenHalf5 = findViewById(R.id.SixteenHalf5);
                SixteenHalf5.setText(data.getString("postSixteenHalf"));
                TextView Seventeen5 = findViewById(R.id.Seventeen5);
                Seventeen5.setText(data.getString("postSeventeen"));
                TextView SeventeenHalf5 = findViewById(R.id.SeventeenHalf5);
                SeventeenHalf5.setText(data.getString("postSeventeenHalf"));
                TextView Eightteen5 = findViewById(R.id.Eightteen5);
                Eightteen5.setText(data.getString("postEightteen"));
                TextView EightteenHalf5 = findViewById(R.id.EightteenHalf5);
                EightteenHalf5.setText(data.getString("postEightteenHalf"));
                TextView Nineteen5 = findViewById(R.id.Nineteen5);
                Nineteen5.setText(data.getString("postNineteen"));
                TextView NineteenHalf5 = findViewById(R.id.NineteenHalf5);
                NineteenHalf5.setText(data.getString("postNineteenHalf"));
                TextView Twenty5 = findViewById(R.id.Twenty5);
                Twenty5.setText(data.getString("postTwenty"));
                TextView TwentyHalf5 = findViewById(R.id.TwentyHalf5);
                TwentyHalf5.setText(data.getString("postTwentyHalf"));
                TextView TwentyOne5 = findViewById(R.id.Twentyone5);
                TwentyOne5.setText(data.getString("postTwentyOne"));
                TextView TwentyOneHalf5 = findViewById(R.id.TwentyoneHalf5);
                TwentyOneHalf5.setText(data.getString("postTwentyOneHalf"));
                if(!data.getString("postNine").equals("×")){
                    Nine5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNine5(v);
                        }
                    });
                }
                if(!data.getString("postNineHalf").equals("×")){
                    NineHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineHalf5(v);
                        }
                    });
                }
                if(!data.getString("postTen").equals("×")){
                    Ten5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTen5(v);
                        }
                    });
                }
                if(!data.getString("postTenHalf").equals("×")){
                    TenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postEleven").equals("×")){
                    Eleven5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEleven5(v);
                        }
                    });
                }
                if(!data.getString("postElevenHalf").equals("×")){
                    ElevenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickElevenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postTwelve").equals("×")){
                    Twelve5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelve5(v);
                        }
                    });
                }
                if(!data.getString("postTwelveHalf").equals("×")){
                    TwelveHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelveHalf5(v);
                        }
                    });
                }
                if(!data.getString("postThirteen").equals("×")){
                    Thirteen5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteen5(v);
                        }
                    });
                }
                if(!data.getString("postThirteenHalf").equals("×")){
                    ThirteenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postFourteen").equals("×")){
                    Fourteen5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteen5(v);
                        }
                    });
                }
                if(!data.getString("postFourteenHalf").equals("×")){
                    FourteenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postFifteen").equals("×")){
                    Fifteen5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteen5(v);
                        }
                    });
                }
                if(!data.getString("postFifteenHalf").equals("×")){
                    FifteenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postSixteen").equals("×")){
                    Sixteen5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteen5(v);
                        }
                    });
                }
                if(!data.getString("postSixteenHalf").equals("×")){
                    SixteenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postSeventeen").equals("×")){
                    Seventeen5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeen5(v);
                        }
                    });
                }
                if(!data.getString("postSeventeenHalf").equals("×")){
                    SeventeenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postEightteen").equals("×")){
                    Eightteen5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteen5(v);
                        }
                    });
                }
                if(!data.getString("postEightteenHalf").equals("×")){
                    EightteenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postNineteen").equals("×")){
                    Nineteen5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteen5(v);
                        }
                    });
                }
                if(!data.getString("postNineteenHalf").equals("×")){
                    NineteenHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteenHalf5(v);
                        }
                    });
                }
                if(!data.getString("postTwenty").equals("×")){
                    Twenty5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwenty5(v);
                        }
                    });
                }
                if(!data.getString("postTwentyHalf").equals("×")){
                    TwentyHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyHalf5(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOne").equals("×")){
                    TwentyOne5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyone5(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOneHalf").equals("×")){
                    TwentyOneHalf5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyoneHalf5(v);
                        }
                    });
                }

                //選択した日付＋５日のスケジュールセット
                data = datas.getJSONObject(5);

                //選択した日付＋５日のセット
                TextView date6 = findViewById(R.id.date6);
                date6.setText(data.getString("postDate"));

                datadate6 = data.getString("postDataTime");
                displaydate6 = data.getString("postDisplayTime");

                TextView Nine6 = findViewById(R.id.Nine6);
                Nine6.setText(data.getString("postNine"));
                TextView NineHalf6 = findViewById(R.id.NineHalf6);
                NineHalf6.setText(data.getString("postNineHalf"));
                TextView Ten6 = findViewById(R.id.Ten6);
                Ten6.setText(data.getString("postTen"));
                TextView TenHalf6 = findViewById(R.id.TenHalf6);
                TenHalf6.setText(data.getString("postTenHalf"));
                TextView Eleven6 = findViewById(R.id.Eleven6);
                Eleven6.setText(data.getString("postEleven"));
                TextView ElevenHalf6 = findViewById(R.id.ElevenHalf6);
                ElevenHalf6.setText(data.getString("postElevenHalf"));
                TextView Twelve6 = findViewById(R.id.Twelve6);
                Twelve6.setText(data.getString("postTwelve"));
                TextView TwelveHalf6 = findViewById(R.id.TwelveHalf6);
                TwelveHalf6.setText(data.getString("postTwelveHalf"));
                TextView Thirteen6 = findViewById(R.id.Thirteen6);
                Thirteen6.setText(data.getString("postThirteen"));
                TextView ThirteenHalf6 = findViewById(R.id.ThirteenHalf6);
                ThirteenHalf6.setText(data.getString("postThirteenHalf"));
                TextView Fourteen6 = findViewById(R.id.Fourteen6);
                Fourteen6.setText(data.getString("postFourteen"));
                TextView FourteenHalf6 = findViewById(R.id.FourteenHalf6);
                FourteenHalf6.setText(data.getString("postFourteenHalf"));
                TextView Fifteen6 = findViewById(R.id.Fifteen6);
                Fifteen6.setText(data.getString("postFifteen"));
                TextView FifteenHalf6 = findViewById(R.id.FifteenHalf6);
                FifteenHalf6.setText(data.getString("postFifteenHalf"));
                TextView Sixteen6 = findViewById(R.id.Sixteen6);
                Sixteen6.setText(data.getString("postSixteen"));
                TextView SixteenHalf6 = findViewById(R.id.SixteenHalf6);
                SixteenHalf6.setText(data.getString("postSixteenHalf"));
                TextView Seventeen6 = findViewById(R.id.Seventeen6);
                Seventeen6.setText(data.getString("postSeventeen"));
                TextView SeventeenHalf6 = findViewById(R.id.SeventeenHalf6);
                SeventeenHalf6.setText(data.getString("postSeventeenHalf"));
                TextView Eightteen6 = findViewById(R.id.Eightteen6);
                Eightteen6.setText(data.getString("postEightteen"));
                TextView EightteenHalf6 = findViewById(R.id.EightteenHalf6);
                EightteenHalf6.setText(data.getString("postEightteenHalf"));
                TextView Nineteen6 = findViewById(R.id.Nineteen6);
                Nineteen6.setText(data.getString("postNineteen"));
                TextView NineteenHalf6 = findViewById(R.id.NineteenHalf6);
                NineteenHalf6.setText(data.getString("postNineteenHalf"));
                TextView Twenty6 = findViewById(R.id.Twenty6);
                Twenty6.setText(data.getString("postTwenty"));
                TextView TwentyHalf6 = findViewById(R.id.TwentyHalf6);
                TwentyHalf6.setText(data.getString("postTwentyHalf"));
                TextView TwentyOne6 = findViewById(R.id.Twentyone6);
                TwentyOne6.setText(data.getString("postTwentyOne"));
                TextView TwentyOneHalf6 = findViewById(R.id.TwentyoneHalf6);
                TwentyOneHalf6.setText(data.getString("postTwentyOneHalf"));
                if(!data.getString("postNine").equals("×")){
                    Nine6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNine6(v);
                        }
                    });
                }
                if(!data.getString("postNineHalf").equals("×")){
                    NineHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineHalf6(v);
                        }
                    });
                }
                if(!data.getString("postTen").equals("×")){
                    Ten6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTen6(v);
                        }
                    });
                }
                if(!data.getString("postTenHalf").equals("×")){
                    TenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postEleven").equals("×")){
                    Eleven6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEleven6(v);
                        }
                    });
                }
                if(!data.getString("postElevenHalf").equals("×")){
                    ElevenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickElevenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postTwelve").equals("×")){
                    Twelve6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelve6(v);
                        }
                    });
                }
                if(!data.getString("postTwelveHalf").equals("×")){
                    TwelveHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelveHalf6(v);
                        }
                    });
                }
                if(!data.getString("postThirteen").equals("×")){
                    Thirteen6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteen6(v);
                        }
                    });
                }
                if(!data.getString("postThirteenHalf").equals("×")){
                    ThirteenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postFourteen").equals("×")){
                    Fourteen6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteen6(v);
                        }
                    });
                }
                if(!data.getString("postFourteenHalf").equals("×")){
                    FourteenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postFifteen").equals("×")){
                    Fifteen6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteen6(v);
                        }
                    });
                }
                if(!data.getString("postFifteenHalf").equals("×")){
                    FifteenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postSixteen").equals("×")){
                    Sixteen6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteen6(v);
                        }
                    });
                }
                if(!data.getString("postSixteenHalf").equals("×")){
                    SixteenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postSeventeen").equals("×")){
                    Seventeen6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeen6(v);
                        }
                    });
                }
                if(!data.getString("postSeventeenHalf").equals("×")){
                    SeventeenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postEightteen").equals("×")){
                    Eightteen6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteen6(v);
                        }
                    });
                }
                if(!data.getString("postEightteenHalf").equals("×")){
                    EightteenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postNineteen").equals("×")){
                    Nineteen6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteen6(v);
                        }
                    });
                }
                if(!data.getString("postNineteenHalf").equals("×")){
                    NineteenHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteenHalf6(v);
                        }
                    });
                }
                if(!data.getString("postTwenty").equals("×")){
                    Twenty6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwenty6(v);
                        }
                    });
                }
                if(!data.getString("postTwentyHalf").equals("×")){
                    TwentyHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyHalf6(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOne").equals("×")){
                    TwentyOne6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyone6(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOneHalf").equals("×")){
                    TwentyOneHalf6.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyoneHalf6(v);
                        }
                    });
                }


                //選択した日付＋６日のスケジュールセット
                data = datas.getJSONObject(6);

                //選択した日付＋６日のセット
                TextView date7 = findViewById(R.id.date7);
                date7.setText(data.getString("postDate"));

                datadate7 = data.getString("postDataTime");
                displaydate7 = data.getString("postDisplayTime");

                TextView Nine7 = findViewById(R.id.Nine7);
                Nine7.setText(data.getString("postNine"));
                TextView NineHalf7 = findViewById(R.id.NineHalf7);
                NineHalf7.setText(data.getString("postNineHalf"));
                TextView Ten7 = findViewById(R.id.Ten7);
                Ten7.setText(data.getString("postTen"));
                TextView TenHalf7 = findViewById(R.id.TenHalf7);
                TenHalf7.setText(data.getString("postTenHalf"));
                TextView Eleven7 = findViewById(R.id.Eleven7);
                Eleven7.setText(data.getString("postEleven"));
                TextView ElevenHalf7 = findViewById(R.id.ElevenHalf7);
                ElevenHalf7.setText(data.getString("postElevenHalf"));
                TextView Twelve7 = findViewById(R.id.Twelve7);
                Twelve7.setText(data.getString("postTwelve"));
                TextView TwelveHalf7 = findViewById(R.id.TwelveHalf7);
                TwelveHalf7.setText(data.getString("postTwelveHalf"));
                TextView Thirteen7 = findViewById(R.id.Thirteen7);
                Thirteen7.setText(data.getString("postThirteen"));
                TextView ThirteenHalf7 = findViewById(R.id.ThirteenHalf7);
                ThirteenHalf7.setText(data.getString("postThirteenHalf"));
                TextView Fourteen7 = findViewById(R.id.Fourteen7);
                Fourteen7.setText(data.getString("postFourteen"));
                TextView FourteenHalf7 = findViewById(R.id.FourteenHalf7);
                FourteenHalf7.setText(data.getString("postFourteenHalf"));
                TextView Fifteen7 = findViewById(R.id.Fifteen7);
                Fifteen7.setText(data.getString("postFifteen"));
                TextView FifteenHalf7 = findViewById(R.id.FifteenHalf7);
                FifteenHalf7.setText(data.getString("postFifteenHalf"));
                TextView Sixteen7 = findViewById(R.id.Sixteen7);
                Sixteen7.setText(data.getString("postSixteen"));
                TextView SixteenHalf7 = findViewById(R.id.SixteenHalf7);
                SixteenHalf7.setText(data.getString("postSixteenHalf"));
                TextView Seventeen7 = findViewById(R.id.Seventeen7);
                Seventeen7.setText(data.getString("postSeventeen"));
                TextView SeventeenHalf7 = findViewById(R.id.SeventeenHalf7);
                SeventeenHalf7.setText(data.getString("postSeventeenHalf"));
                TextView Eightteen7 = findViewById(R.id.Eightteen7);
                Eightteen7.setText(data.getString("postEightteen"));
                TextView EightteenHalf7 = findViewById(R.id.EightteenHalf7);
                EightteenHalf7.setText(data.getString("postEightteenHalf"));
                TextView Nineteen7 = findViewById(R.id.Nineteen7);
                Nineteen7.setText(data.getString("postNineteen"));
                TextView NineteenHalf7 = findViewById(R.id.NineteenHalf7);
                NineteenHalf7.setText(data.getString("postNineteenHalf"));
                TextView Twenty7 = findViewById(R.id.Twenty7);
                Twenty7.setText(data.getString("postTwenty"));
                TextView TwentyHalf7 = findViewById(R.id.TwentyHalf7);
                TwentyHalf7.setText(data.getString("postTwentyHalf"));
                TextView TwentyOne7 = findViewById(R.id.Twentyone7);
                TwentyOne7.setText(data.getString("postTwentyOne"));
                TextView TwentyOneHalf7 = findViewById(R.id.TwentyoneHalf7);
                TwentyOneHalf7.setText(data.getString("postTwentyOneHalf"));
                if(!data.getString("postNine").equals("×")){
                    Nine7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNine7(v);
                        }
                    });
                }
                if(!data.getString("postNineHalf").equals("×")){
                    NineHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineHalf7(v);
                        }
                    });
                }
                if(!data.getString("postTen").equals("×")){
                    Ten7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTen7(v);
                        }
                    });
                }
                if(!data.getString("postTenHalf").equals("×")){
                    TenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postEleven").equals("×")){
                    Eleven7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEleven7(v);
                        }
                    });
                }
                if(!data.getString("postElevenHalf").equals("×")){
                    ElevenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickElevenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postTwelve").equals("×")){
                    Twelve7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelve7(v);
                        }
                    });
                }
                if(!data.getString("postTwelveHalf").equals("×")){
                    TwelveHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwelveHalf7(v);
                        }
                    });
                }
                if(!data.getString("postThirteen").equals("×")){
                    Thirteen7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteen7(v);
                        }
                    });
                }
                if(!data.getString("postThirteenHalf").equals("×")){
                    ThirteenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickThirteenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postFourteen").equals("×")){
                    Fourteen7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteen7(v);
                        }
                    });
                }
                if(!data.getString("postFourteenHalf").equals("×")){
                    FourteenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFourteenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postFifteen").equals("×")){
                    Fifteen7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteen7(v);
                        }
                    });
                }
                if(!data.getString("postFifteenHalf").equals("×")){
                    FifteenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickFifteenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postSixteen").equals("×")){
                    Sixteen7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteen7(v);
                        }
                    });
                }
                if(!data.getString("postSixteenHalf").equals("×")){
                    SixteenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSixteenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postSeventeen").equals("×")){
                    Seventeen7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeen7(v);
                        }
                    });
                }
                if(!data.getString("postSeventeenHalf").equals("×")){
                    SeventeenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickSeventeenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postEightteen").equals("×")){
                    Eightteen7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteen7(v);
                        }
                    });
                }
                if(!data.getString("postEightteenHalf").equals("×")){
                    EightteenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickEightteenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postNineteen").equals("×")){
                    Nineteen7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteen7(v);
                        }
                    });
                }
                if(!data.getString("postNineteenHalf").equals("×")){
                    NineteenHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickNineteenHalf7(v);
                        }
                    });
                }
                if(!data.getString("postTwenty").equals("×")){
                    Twenty7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwenty7(v);
                        }
                    });
                }
                if(!data.getString("postTwentyHalf").equals("×")){
                    TwentyHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyHalf7(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOne").equals("×")){
                    TwentyOne7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyone7(v);
                        }
                    });
                }
                if(!data.getString("postTwentyOneHalf").equals("×")){
                    TwentyOneHalf7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onClickTwentyoneHalf7(v);
                        }
                    });
                }

            }
            catch (JSONException ex) {
                Log.e(DEBUG_TAG, "JSON解析失敗", ex);
            }
        }
    }

    public void onClickNine1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "0900");
        intent.putExtra("display", displaydate1 + "09時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNine2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "0900");
        intent.putExtra("display", displaydate2 + "09時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNine3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "0900");
        intent.putExtra("display", displaydate3 + "09時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNine4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "0900");
        intent.putExtra("display", displaydate4 + "09時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNine5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "0900");
        intent.putExtra("display", displaydate5 + "09時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNine6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "0900");
        intent.putExtra("display", displaydate6 + "09時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNine7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "0900");
        intent.putExtra("display", displaydate7 + "09時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickNineHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0930　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "0930");
        intent.putExtra("display", displaydate1 + "09時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "0930");
        intent.putExtra("display", displaydate2 + "09時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "0930");
        intent.putExtra("display", displaydate3 + "09時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "0930");
        intent.putExtra("display", displaydate4 + "09時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "0930");
        intent.putExtra("display", displaydate5 + "09時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "0930");
        intent.putExtra("display", displaydate6 + "09時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "0930");
        intent.putExtra("display", displaydate7 + "09時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickTen1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1000");
        intent.putExtra("display", displaydate1 + "10時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTen2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1000");
        intent.putExtra("display", displaydate2 + "10時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTen3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1000");
        intent.putExtra("display", displaydate3 + "10時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTen4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1000");
        intent.putExtra("display", displaydate4 + "10時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTen5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1000");
        intent.putExtra("display", displaydate5 + "10時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTen6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1000");
        intent.putExtra("display", displaydate6 + "10時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTen7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1000");
        intent.putExtra("display", displaydate7 + "10時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickTenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1030");
        intent.putExtra("display", displaydate1 + "10時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1030");
        intent.putExtra("display", displaydate2 + "10時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1030");
        intent.putExtra("display", displaydate3 + "10時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1030");
        intent.putExtra("display", displaydate4 + "10時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1030");
        intent.putExtra("display", displaydate5 + "10時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1030");
        intent.putExtra("display", displaydate6 + "10時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1030");
        intent.putExtra("display", displaydate7 + "10時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickEleven1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1100");
        intent.putExtra("display", displaydate1 + "11時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEleven2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1100");
        intent.putExtra("display", displaydate2 + "11時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEleven3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1100");
        intent.putExtra("display", displaydate3 + "11時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEleven4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1100");
        intent.putExtra("display", displaydate4 + "11時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEleven5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1100");
        intent.putExtra("display", displaydate5 + "11時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEleven6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1100");
        intent.putExtra("display", displaydate6 + "11時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEleven7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1100");
        intent.putExtra("display", displaydate7 + "11時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickElevenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1130");
        intent.putExtra("display", displaydate1 + "11時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickElevenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1130");
        intent.putExtra("display", displaydate2 + "11時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickElevenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1130");
        intent.putExtra("display", displaydate3 + "11時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickElevenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1130");
        intent.putExtra("display", displaydate4 + "11時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickElevenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1130");
        intent.putExtra("display", displaydate5 + "11時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickElevenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1130");
        intent.putExtra("display", displaydate6 + "11時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickElevenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1130");
        intent.putExtra("display", displaydate7 + "11時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickTwelve1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1200");
        intent.putExtra("display", displaydate1 + "12時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelve2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1200");
        intent.putExtra("display", displaydate2 + "12時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelve3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1200");
        intent.putExtra("display", displaydate3 + "12時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelve4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1200");
        intent.putExtra("display", displaydate4 + "12時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelve5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1200");
        intent.putExtra("display", displaydate5 + "12時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelve6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1200");
        intent.putExtra("display", displaydate6 + "12時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelve7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1200");
        intent.putExtra("display", displaydate7 + "12時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickTwelveHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1230");
        intent.putExtra("display", displaydate1 + "12時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelveHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1230");
        intent.putExtra("display", displaydate2 + "12時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelveHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1230");
        intent.putExtra("display", displaydate3 + "12時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelveHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1230");
        intent.putExtra("display", displaydate4 + "12時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelveHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1230");
        intent.putExtra("display", displaydate5 + "12時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelveHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1230");
        intent.putExtra("display", displaydate6 + "12時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwelveHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1230");
        intent.putExtra("display", displaydate7 + "12時30分");
        setResult(RESULT_OK, intent);
        finish();
    }


    public void onClickThirteen1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1300");
        intent.putExtra("display", displaydate1 + "13時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteen2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1300");
        intent.putExtra("display", displaydate2 + "13時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteen3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1300");
        intent.putExtra("display", displaydate3 + "13時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteen4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1300");
        intent.putExtra("display", displaydate4 + "13時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteen5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1300");
        intent.putExtra("display", displaydate5 + "13時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteen6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1300");
        intent.putExtra("display", displaydate6 + "13時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteen7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1300");
        intent.putExtra("display", displaydate7 + "13時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickThirteenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1330");
        intent.putExtra("display", displaydate1 + "13時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1330");
        intent.putExtra("display", displaydate2 + "13時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1330");
        intent.putExtra("display", displaydate3 + "13時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1330");
        intent.putExtra("display", displaydate4 + "13時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1330");
        intent.putExtra("display", displaydate5 + "13時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1330");
        intent.putExtra("display", displaydate6 + "13時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickThirteenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1330");
        intent.putExtra("display", displaydate7 + "13時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickFourteen1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1400");
        intent.putExtra("display", displaydate1 + "14時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteen2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1400");
        intent.putExtra("display", displaydate2 + "14時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteen3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1400");
        intent.putExtra("display", displaydate3 + "14時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteen4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1400");
        intent.putExtra("display", displaydate4 + "14時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteen5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1400");
        intent.putExtra("display", displaydate5 + "14時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteen6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1400");
        intent.putExtra("display", displaydate6 + "14時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteen7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1400");
        intent.putExtra("display", displaydate7 + "14時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickFourteenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1430");
        intent.putExtra("display", displaydate1 + "14時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1430");
        intent.putExtra("display", displaydate2 + "14時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1430");
        intent.putExtra("display", displaydate3 + "14時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1430");
        intent.putExtra("display", displaydate4 + "14時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1430");
        intent.putExtra("display", displaydate5 + "14時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1430");
        intent.putExtra("display", displaydate6 + "14時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFourteenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1430");
        intent.putExtra("display", displaydate7 + "14時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickFifteen1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1500");
        intent.putExtra("display", displaydate1 + "15時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteen2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1500");
        intent.putExtra("display", displaydate2 + "15時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteen3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1500");
        intent.putExtra("display", displaydate3 + "15時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteen4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1500");
        intent.putExtra("display", displaydate4 + "15時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteen5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1500");
        intent.putExtra("display", displaydate5 + "15時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteen6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1500");
        intent.putExtra("display", displaydate6 + "15時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteen7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1500");
        intent.putExtra("display", displaydate7 + "15時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickFifteenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1530");
        intent.putExtra("display", displaydate1 + "15時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1530");
        intent.putExtra("display", displaydate2 + "15時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1530");
        intent.putExtra("display", displaydate3 + "15時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1530");
        intent.putExtra("display", displaydate4 + "15時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1530");
        intent.putExtra("display", displaydate5 + "15時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1530");
        intent.putExtra("display", displaydate6 + "15時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickFifteenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1530");
        intent.putExtra("display", displaydate7 + "15時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickSixteen1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1600");
        intent.putExtra("display", displaydate1 + "16時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteen2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1600");
        intent.putExtra("display", displaydate2 + "16時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteen3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1600");
        intent.putExtra("display", displaydate3 + "16時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteen4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1600");
        intent.putExtra("display", displaydate4 + "16時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteen5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1600");
        intent.putExtra("display", displaydate5 + "16時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteen6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1600");
        intent.putExtra("display", displaydate6 + "16時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteen7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1600");
        intent.putExtra("display", displaydate7 + "16時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickSixteenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1630");
        intent.putExtra("display", displaydate1 + "16時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1630");
        intent.putExtra("display", displaydate2 + "16時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1630");
        intent.putExtra("display", displaydate3 + "16時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1630");
        intent.putExtra("display", displaydate4 + "16時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1630");
        intent.putExtra("display", displaydate5 + "16時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1630");
        intent.putExtra("display", displaydate6 + "16時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSixteenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1630");
        intent.putExtra("display", displaydate7 + "16時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickSeventeen1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1700");
        intent.putExtra("display", displaydate1 + "17時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeen2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1700");
        intent.putExtra("display", displaydate2 + "17時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeen3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1700");
        intent.putExtra("display", displaydate3 + "17時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeen4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1700");
        intent.putExtra("display", displaydate4 + "17時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeen5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1700");
        intent.putExtra("display", displaydate5 + "17時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeen6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1700");
        intent.putExtra("display", displaydate6 + "17時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeen7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1700");
        intent.putExtra("display", displaydate7 + "17時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickSeventeenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1730");
        intent.putExtra("display", displaydate1 + "17時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1730");
        intent.putExtra("display", displaydate2 + "17時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1730");
        intent.putExtra("display", displaydate3 + "17時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1730");
        intent.putExtra("display", displaydate4 + "17時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1730");
        intent.putExtra("display", displaydate5 + "17時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1730");
        intent.putExtra("display", displaydate6 + "17時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickSeventeenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1730");
        intent.putExtra("display", displaydate7 + "17時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickEightteen1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1800");
        intent.putExtra("display", displaydate1 + "18時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteen2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1800");
        intent.putExtra("display", displaydate2 + "18時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteen3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1800");
        intent.putExtra("display", displaydate3 + "18時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteen4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1800");
        intent.putExtra("display", displaydate4 + "18時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteen5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1800");
        intent.putExtra("display", displaydate5 + "18時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteen6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1800");
        intent.putExtra("display", displaydate6 + "18時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteen7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1800");
        intent.putExtra("display", displaydate7 + "18時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickEightteenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1830");
        intent.putExtra("display", displaydate1 + "18時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1830");
        intent.putExtra("display", displaydate2 + "18時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1830");
        intent.putExtra("display", displaydate3 + "18時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1830");
        intent.putExtra("display", displaydate4 + "18時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1830");
        intent.putExtra("display", displaydate5 + "18時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1830");
        intent.putExtra("display", displaydate6 + "18時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickEightteenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1830");
        intent.putExtra("display", displaydate7 + "18時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickNineteen1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1900");
        intent.putExtra("display", displaydate1 + "19時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteen2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1900");
        intent.putExtra("display", displaydate2 + "19時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteen3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1900");
        intent.putExtra("display", displaydate3 + "19時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteen4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1900");
        intent.putExtra("display", displaydate4 + "19時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteen5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1900");
        intent.putExtra("display", displaydate5 + "19時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteen6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1900");
        intent.putExtra("display", displaydate6 + "19時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteen7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1900");
        intent.putExtra("display", displaydate7 + "19時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickNineteenHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "1930");
        intent.putExtra("display", displaydate1 + "19時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteenHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "1930");
        intent.putExtra("display", displaydate2 + "19時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteenHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "1930");
        intent.putExtra("display", displaydate3 + "19時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteenHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "1930");
        intent.putExtra("display", displaydate4 + "19時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteenHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "1930");
        intent.putExtra("display", displaydate5 + "19時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteenHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "1930");
        intent.putExtra("display", displaydate6 + "19時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickNineteenHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "1930");
        intent.putExtra("display", displaydate7 + "19時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickTwenty1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "2000");
        intent.putExtra("display", displaydate1 + "20時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwenty2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "2000");
        intent.putExtra("display", displaydate2 + "20時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwenty3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "2000");
        intent.putExtra("display", displaydate3 + "20時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwenty4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "2000");
        intent.putExtra("display", displaydate4 + "20時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwenty5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "2000");
        intent.putExtra("display", displaydate5 + "20時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwenty6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "2000");
        intent.putExtra("display", displaydate6 + "20時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwenty7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "2000");
        intent.putExtra("display", displaydate7 + "20時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickTwentyHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "2030");
        intent.putExtra("display", displaydate1 + "20時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "2030");
        intent.putExtra("display", displaydate2 + "20時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "2030");
        intent.putExtra("display", displaydate3 + "20時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "2030");
        intent.putExtra("display", displaydate4 + "20時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "2030");
        intent.putExtra("display", displaydate5 + "20時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "2030");
        intent.putExtra("display", displaydate6 + "20時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "2030");
        intent.putExtra("display", displaydate7 + "20時30分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickTwentyone1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 1000　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "2100");
        intent.putExtra("display", displaydate1 + "21時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyone2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "2100");
        intent.putExtra("display", displaydate2 + "21時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyone3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "2100");
        intent.putExtra("display", displaydate3 + "21時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyone4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "2100");
        intent.putExtra("display", displaydate4 + "21時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyone5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "2100");
        intent.putExtra("display", displaydate5 + "21時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyone6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "2100");
        intent.putExtra("display", displaydate6 + "21時00分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyone7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "2100");
        intent.putExtra("display", displaydate7 + "21時00分");
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onClickTwentyoneHalf1(View view) {
        //◯年◯月◯日◯時◯分　（表示用）  dispalydate1
        //yyyyMMdd と 0900　（データ格納用） datadate1 固定
        // クリック時の処理
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate1);
        intent.putExtra("datatime", "2130");
        intent.putExtra("display", displaydate1 + "21時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyoneHalf2(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate2);
        intent.putExtra("datatime", "2130");
        intent.putExtra("display", displaydate2 + "21時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyoneHalf3(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate3);
        intent.putExtra("datatime", "2130");
        intent.putExtra("display", displaydate3 + "21時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyoneHalf4(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate4);
        intent.putExtra("datatime", "2130");
        intent.putExtra("display", displaydate4 + "21時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyoneHalf5(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate5);
        intent.putExtra("datatime", "2130");
        intent.putExtra("display", displaydate5 + "21時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyoneHalf6(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate6);
        intent.putExtra("datatime", "2130");
        intent.putExtra("display", displaydate6 + "21時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
    public void onClickTwentyoneHalf7(View view) {
        Intent intent = new Intent();
        intent.putExtra("datadate", datadate7);
        intent.putExtra("datatime", "2130");
        intent.putExtra("display", displaydate7 + "21時30分");
        setResult(RESULT_OK, intent);
        finish();
    }
}
