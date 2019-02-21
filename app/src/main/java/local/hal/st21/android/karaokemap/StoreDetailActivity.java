package local.hal.st21.android.karaokemap;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;

public class StoreDetailActivity extends AppCompatActivity {

    private String id = "";
    private String name = "";
    private String latitude = "";
    private String longitude = "";
    private String icon = "";
    private String roomNum = "";
    private String isImmediately = "";
    private String isAte = "";
    private String isHalf = "";
    private String moneyUrl = "";
    private String reservationUrl = "";
    private String tel = "";
    private String address = "";
    private String holiday = "";
    private String access = "";
    private String business = "";
    private String photo = "";
    private Dialog dialog;

    /**
     * WebViewのフィールド。
     */
    private WebView _wvBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        Intent intent = getIntent();
        id = (intent.getStringExtra("postId"));
        latitude = (intent.getStringExtra("postLatitude"));
        longitude = (intent.getStringExtra("postLongitude"));
        icon = (intent.getStringExtra("postIcon"));

        name = (intent.getStringExtra("postName"));
        TextView tvPostName = findViewById(R.id.tv_Name);
        tvPostName.setText(name);

        address = (intent.getStringExtra("postAddress"));
        TextView tvPostAddress = findViewById(R.id.tx_Address);
        tvPostAddress.setText(address);

        access = (intent.getStringExtra("postAccess"));
        TextView tvPostAccess = findViewById(R.id.tx_Access);
        tvPostAccess.setText(access);

        business = (intent.getStringExtra("postBusiness"));
        TextView tvPostBusiness = findViewById(R.id.tx_Business);
        tvPostBusiness.setText(business);

        tel = (intent.getStringExtra("postTel"));
        TextView tvPostTel = findViewById(R.id.tx_Tel);
        tvPostTel.setText(tel);

        holiday = (intent.getStringExtra("postHoliday"));
        TextView tvPostHoliday = findViewById(R.id.tx_Holiday);
        tvPostHoliday.setText(holiday);

        roomNum = (intent.getStringExtra("postRoomNum"));
        TextView tvPostRoomNum = findViewById(R.id.tx_Room_Num);
        tvPostRoomNum.setText(roomNum + "部屋");

        isImmediately = (intent.getStringExtra("postIsImmediately"));
        if(Integer.parseInt(isImmediately) == 0){
            isImmediately = "可";
        }else{
            isImmediately = "不可";
        }
        TextView tvPostIsImmediately = findViewById(R.id.tx_Immediately);
        tvPostIsImmediately.setText(isImmediately);

        isAte = (intent.getStringExtra("postIsAte"));
        if(Integer.parseInt(isAte) == 0){
            isAte = "可";
        }else{
            isAte = "不可";
        }
        TextView tvPostIsAte = findViewById(R.id.tx_Ate);
        tvPostIsAte.setText(isAte);

        isHalf = (intent.getStringExtra("postIsHalf"));
            isHalf = "下記サイトの価格" + isHalf;
        TextView tvPostIsHalf = findViewById(R.id.tx_Half);
        tvPostIsHalf.setText(isHalf);


        moneyUrl = (intent.getStringExtra("postMoneyUrl"));
        reservationUrl = (intent.getStringExtra("postReservationUrl"));

        photo = (intent.getStringExtra("postPhoto"));
        WebView wvPostPhoto = findViewById(R.id.wv_Photo);
        wvPostPhoto.setWebViewClient(new WebViewClient());
        wvPostPhoto.getSettings().setUseWideViewPort(true);
        wvPostPhoto.getSettings().setLoadWithOverviewMode(true);
        wvPostPhoto.loadUrl(GetUrl.photoUrl + "?img=" + photo);

        //ツールバー(レイアウトを変更可)。
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("店舗詳細");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_activity_store_detail, menu);

        return true;
    }

    public void onMoneyButtonClick(View view) {
        Uri uri = Uri.parse(moneyUrl);

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    public void onReservationButtonClick(View view) {
        Intent intent = new Intent(StoreDetailActivity.this, ReservationActivity.class);
        intent.putExtra("id",id);
        intent.putExtra("name",name);
        startActivity(intent);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menuCall:
                Uri uri = Uri.parse("tel:" + tel);
                Intent i = new Intent(Intent.ACTION_DIAL,uri);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
