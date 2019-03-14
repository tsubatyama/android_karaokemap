package local.hal.st21.android.karaokemap;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.net.MalformedURLException;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.drawable.BitmapDrawable;
import android.view.Menu;
import android.support.v7.widget.SearchView;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuInflater;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;



public class StoreSearchMapsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private ListView lvStoreList;    // 内容エリア
    private LinearLayout linearLayoutArea;
    private ArrayList<Marker> markers;
    private Intent intent;
    private Double lat;
    private Double lng;

    private SearchView mSearchView;


    private Location location = null;
    private boolean isFirst = true;

    /**
     * アニメーションにかける時間（ミリ秒）
     */
    private final static int DURATION = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_search_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.storeMaps);
        mapFragment.getMapAsync(this);

        //ツールバー(レイアウトを変更可)。
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //DrawerLayout
        DrawerLayout drawer = findViewById(R.id.dlMainContent);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //レフトナビ本体。
        NavigationView navigationView = findViewById(R.id.nvSideMenuButton);
        navigationView.setNavigationItemSelectedListener(this);

        setTitle("店舗検索");

        // 内容エリアの結び付け
        lvStoreList = findViewById(R.id.lvStoreList);

        linearLayoutArea = findViewById(R.id.llStoreMapMain);

        // ExpandするViewの元のサイズを保持
        final int originalHeight = linearLayoutArea.getHeight() / 2;

        // 内容エリアを閉じるアニメーション
        StoreSearchMapsAnimation closeAnimation = new StoreSearchMapsAnimation(lvStoreList, -originalHeight, originalHeight);
        closeAnimation.setDuration(DURATION);
        lvStoreList.startAnimation(closeAnimation);

        //ユーザ名を表示する
        SharedPreferences pref = getSharedPreferences("prefUserId",0);
        if(Build.VERSION.SDK_INT < 23) {
            TextView navTvUserName = navigationView.findViewById(R.id.navTvUserName);
            navTvUserName.setText(pref.getString("name", "ユーザ名"));
        } else {
            View headerView = navigationView.getHeaderView(0);
            TextView navTvUserName = headerView.findViewById(R.id.navTvUserName);
            navTvUserName.setText(pref.getString("name", "ユーザ名"));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Set Menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        MenuItem menuItem = menu.findItem(R.id.toolbar_menu_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        // whether display Magnifying Glass Icon at first
        mSearchView.setIconifiedByDefault(true);

        // whether display Submit Button
        mSearchView.setSubmitButtonEnabled(false);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Geocoder gcoder = new Geocoder(StoreSearchMapsActivity.this, Locale.getDefault());
                List<Address> lstAddr;
                try {
                    // 位置情報の取得
                    lstAddr = gcoder.getFromLocationName(query, 1);
                    if (lstAddr != null && lstAddr.size() > 0) {
                        // 緯度/経度取得
                        Address addr = lstAddr.get(0);
                        double latitude = addr.getLatitude();
                        double longitude = addr.getLongitude();

                        LatLng mapLatLng = new LatLng(latitude, longitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLatLng,16));


                        //非同期処理を開始する。
                        StoreMapTaskReceiver receiver = new StoreMapTaskReceiver();
                        //ここで渡した引数はLoginTaskReceiverクラスのdoInBackground(String... params)で受け取れる。
                        receiver.execute(GetUrl.storeMapUrl, latitude + "", longitude + "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    /**
     * レフトナビ以外をクリックした時の動き。
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.dlMainContent);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * レフトナビをクリックした時。
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_mypage) {
//            intent = new Intent(StoreSearchMapsActivity.this,MypageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (id == R.id.nav_logout){
            //ユーザーID削除。
            SharedPreferences setting = getSharedPreferences("prefUserId" , 0);
            SharedPreferences.Editor editor = setting.edit();
            editor.remove("id");
            editor.commit();
//            intent = new Intent(StoreSearchMapsActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }
        DrawerLayout drawer = findViewById(R.id.dlMainContent);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /** fine location のリクエストコード（値は他のパーミッションと被らなければ、なんでも良い）*/
            final int requestCode = 1;

            // いずれも得られていない場合はパーミッションのリクエストを要求する
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode );
            return;
        }

        // 位置情報を管理している LocationManager のインスタンスを生成する
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String locationProvider = null;

        // GPSが利用可能になっているかどうかをチェック
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        }
        // GPSプロバイダーが有効になっていない場合は基地局情報が利用可能になっているかをチェック
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        // いずれも利用可能でない場合は、GPSを設定する画面に遷移する
        else {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            return;
        }
        // 最新の位置情報
        location = locationManager.getLastKnownLocation(locationProvider);
        lat = location.getLatitude();
        lng = location.getLongitude();


        if (location != null) {
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));

            //非同期処理を開始する。
            StoreMapTaskReceiver receiver = new StoreMapTaskReceiver();
            //ここで渡した引数はLoginTaskReceiverクラスのdoInBackground(String... params)で受け取れる。
            receiver.execute(GetUrl.storeMapUrl, location.getLatitude() + "", location.getLongitude() + "");
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,16));

            location = null;
            isFirst = false;
        } else if(isFirst) {
            Toast.makeText(StoreSearchMapsActivity.this, "現在地情報の取得に失敗しました。", Toast.LENGTH_SHORT).show();
            //非同期処理を開始する。
            StoreMapTaskReceiver receiver = new StoreMapTaskReceiver();
            //大阪市役所34.693835, 135.501929
            //ここで渡した引数はLoginTaskReceiverクラスのdoInBackground(String... params)で受け取れる。

            receiver.execute(GetUrl.storeMapUrl, "35.6915", "139.697");

            isFirst = false;
        } else {
            for(Marker marker : markers) {
                marker.remove();
            }
            List<Map<String, String>> storeList = new ArrayList<>();
            SimpleAdapter adapter = new SimpleAdapter(StoreSearchMapsActivity.this, storeList, R.layout.row_store_list, null, null);
            lvStoreList.setAdapter(adapter);
            //カメラの位置を取得する。
            CameraPosition cameraPosition = mMap.getCameraPosition();
            //非同期処理を開始する。
            StoreMapTaskReceiver receiver = new StoreMapTaskReceiver();
            //ここで渡した引数はLoginTaskReceiverクラスのdoInBackground(String... params)で受け取れる。
            receiver.execute(GetUrl.storeMapUrl, cameraPosition.target.latitude + "", cameraPosition.target.longitude + "");
            LatLng mapLatLng = new LatLng(lat, lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLatLng,16));

        }
    }


    /**
     * フローティングアクションボタンが押された時のイベント処理用メソッド.
     *
     * @param view 画面部品。
     */
    public void onFabOpenListClick(View view) {
    }

    /**
     * ボタンが押された時の処理.
     *
     * @param view 画面部品。
     */
    public void onButtonClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btSurroundingStore:
                Button btSurroundingStore = (Button) view;
                btSurroundingStore.setVisibility(View.INVISIBLE);
                for(Marker marker : markers) {
                    marker.remove();
                }
                List<Map<String, String>> storeList = new ArrayList<>();
                SimpleAdapter adapter = new SimpleAdapter(StoreSearchMapsActivity.this, storeList, R.layout.row_store_list, null, null);
                lvStoreList.setAdapter(adapter);
                //カメラの位置を取得する。
                CameraPosition position = mMap.getCameraPosition();
                lat = position.target.latitude;
                lng = position.target.latitude;

                //非同期処理を開始する。
                StoreMapTaskReceiver receiver = new StoreMapTaskReceiver();
                //ここで渡した引数はLoginTaskReceiverクラスのdoInBackground(String... params)で受け取れる。
                receiver.execute(GetUrl.storeMapUrl, position.target.latitude + "", position.target.longitude + "");
                break;
        }
    }



    /**
     * 非同期通信を行うAsyncTaskクラスを継承したメンバクラス.
     */
    private class StoreMapTaskReceiver extends AsyncTask<String, Void, String> {

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
            String lat = params[1];
            String lng = params[2];

            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";
            String postData = "lat=" + lat + "&lng=" + lng;

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
            final List<Map<String, String>> storeList = new ArrayList<>();
            try {
                JSONObject rootJson = new JSONObject(result);
                JSONArray datas = rootJson.getJSONArray("postsList");
                if(datas.length() == 0) {
                    Toast.makeText(StoreSearchMapsActivity.this, "この地域に指定された条件の店舗は在りません。", Toast.LENGTH_SHORT).show();
                    return;
                }
                for(int i = 0; i < datas.length(); i++) {
                    Map<String, String> map = new HashMap<>();
                    JSONObject restNow = datas.getJSONObject(i);
                    map.put("postId",restNow.getString("postId"));
                    map.put("postName", restNow.getString("postName"));
                    map.put("postLatitude", restNow.getString("postLatitude"));
                    map.put("postLongitude", restNow.getString("postLongitude"));
                    map.put("postIcon", restNow.getString("postIcon"));
                    map.put("postRoomNum", restNow.getString("postRoomNum"));
                    map.put("postIsImmediately", restNow.getString("postIsImmediately"));
                    map.put("postIsAte", restNow.getString("postIsAte"));
                    map.put("postIsHalf", restNow.getString("postIsHalf"));
                    map.put("postMoneyUrl", restNow.getString("postMoneyUrl"));
                    map.put("postReservationUrl", restNow.getString("postReservationUrl"));
                    map.put("postTel", restNow.getString("postTel"));
                    map.put("postAddress", restNow.getString("postAddress"));
                    map.put("postHoliday", restNow.getString("postHoliday"));
                    map.put("postAccess", restNow.getString("postAccess"));
                    map.put("postBusiness", restNow.getString("postBusiness"));
                    map.put("postPhoto", restNow.getString("postPhoto"));
                    storeList.add(map);
                }
            }
            catch (JSONException ex) {
                Log.e(DEBUG_TAG, "JSON解析失敗", ex);
            }

            markers = new ArrayList<>();

            for(Map<String, String> map : storeList) {


                InputStream is = null;
                String a = "/Users/tsubasakotani/upload/" + map.get("postIcon");
                try{
                    URL url = new URL(a);
                    is = url.openStream();
                }catch (MalformedURLException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                }

                BitmapDrawable bitmapdraw;
                if(map.get("postIcon").equals("ビッグエコー")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.bigecho);
                }else if(map.get("postIcon").equals("まねきねこ")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.maneki);
                }else if(map.get("postIcon").equals("カラオケBanBan")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.banban);
                }else if(map.get("postIcon").equals("シダックス")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.shidax);
                }else if(map.get("postIcon").equals("コート・ダジュール")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.cote);
                }else if(map.get("postIcon").equals("ジャンボカラオケ広場")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.jankara);
                }else if(map.get("postIcon").equals("カラオケ館")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.karakan);
                }else if(map.get("postIcon").equals("カラオケの鉄人")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.karatetsu);
                }else if(map.get("postIcon").equals("カラオケマック")){
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.karamac);
                }else {
                    bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.example);
                }
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 170, 170, false);


                //マーカー表示
                LatLng latLng = new LatLng(Float.parseFloat(map.get("postLatitude")), Float.parseFloat(map.get("postLongitude")));
                markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title(map.get("postName")).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))));

                markers.get(markers.size() - 1).setTag(map);
            }


            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(StoreSearchMapsActivity.this, StoreDetailActivity.class);
                    Map<String, String> map = (Map<String, String>) marker.getTag();
                    intent.putExtra("postId", map.get("postId"));
                    intent.putExtra("postName", map.get("postName"));
                    intent.putExtra("postLatitude", map.get("postLatitude"));
                    intent.putExtra("postLongitude", map.get("postLongitude"));
                    intent.putExtra("postIcon", map.get("postIcon"));
                    intent.putExtra("postRoomNum", map.get("postRoomNum"));
                    intent.putExtra("postIsImmediately", map.get("postIsImmediately"));
                    intent.putExtra("postIsAte", map.get("postIsAte"));
                    intent.putExtra("postIsHalf", map.get("postIsHalf"));
                    intent.putExtra("postMoneyUrl", map.get("postMoneyUrl"));
                    intent.putExtra("postReservationUrl", map.get("postReservationUrl"));
                    intent.putExtra("postTel", map.get("postTel"));
                    intent.putExtra("postAddress", map.get("postAddress"));
                    intent.putExtra("postHoliday", map.get("postHoliday"));
                    intent.putExtra("postAccess", map.get("postAccess"));
                    intent.putExtra("postBusiness", map.get("postBusiness"));
                    intent.putExtra("postPhoto", map.get("postPhoto"));
                    startActivity(intent);
                }
            });

            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                private boolean isNotFirst = false;

                @Override
                public void onCameraIdle() {
                    if(isNotFirst) {
                        Button btSurroundingStore = findViewById(R.id.btSurroundingStore);
                        btSurroundingStore.setVisibility(View.VISIBLE);
                    }
                    isNotFirst = true;
                }
            });

            String[] from = {"postName", "postRoomNum"};
            int[] to = {R.id.rowTvStoreTitle, R.id.rowTvStoreContent};
            final SimpleAdapter adapter = new SimpleAdapter(StoreSearchMapsActivity.this, storeList, R.layout.row_store_list, from, to);
            adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    int id = view.getId();
                    String strData = (String) data;
                    switch (id) {
                        case R.id.rowTvStoreTitle:
                            TextView rowTvStoreTitle = (TextView) view;
                            rowTvStoreTitle.setText(strData);
                            return true;
                        case R.id.rowTvStoreContent:
                            TextView rowTvStoreContent = (TextView) view;
                            rowTvStoreContent.setText(strData);
                            return true;
                    }
                    return false;
                }
            });
            lvStoreList.setAdapter(adapter);
            lvStoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(StoreSearchMapsActivity.this, StoreDetailActivity.class);
                    Map<String, String> map = (Map<String, String>) adapter.getItem(position);
                    intent.putExtra("postId", map.get("postId"));
                    intent.putExtra("postName", map.get("postName"));
                    intent.putExtra("postLatitude", map.get("postLatitude"));
                    intent.putExtra("postLongitude", map.get("postLongitude"));
                    intent.putExtra("postIcon", map.get("postIcon"));
                    intent.putExtra("postRoomNum", map.get("postRoomNum"));
                    intent.putExtra("postIsImmediately", map.get("postIsImmediately"));
                    intent.putExtra("postIsAte", map.get("postIsAte"));
                    intent.putExtra("postIsHalf", map.get("postIsHalf"));
                    intent.putExtra("postMoneyUrl", map.get("postMoneyUrl"));
                    intent.putExtra("postReservationUrl", map.get("postReservationUrl"));
                    intent.putExtra("postTel", map.get("postTel"));
                    intent.putExtra("postAddress", map.get("postAddress"));
                    intent.putExtra("postHoliday", map.get("postHoliday"));
                    intent.putExtra("postAccess", map.get("postAccess"));
                    intent.putExtra("postBusiness", map.get("postBusiness"));
                    intent.putExtra("postPhoto", map.get("postPhoto"));
                    startActivity(intent);
                }
            });

            mMap.setIndoorEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);
        }
    }


}
