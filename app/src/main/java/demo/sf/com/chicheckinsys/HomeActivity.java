package demo.sf.com.chicheckinsys;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Button chcekinBtn;
    private ConstraintLayout constraintLayout;

    private boolean isFirstLocate = true;

    public LocationClient mLocationClient;

    public static final int DISTANCE = 600;
    private LatLng companyAddr;
    private LatLng myAddr;

    private BDLocation currentBDLocation = new BDLocation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        mLocationClient = new LocationClient(getApplicationContext());
        BDAbstractLocationListener myListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myListener);

        setContentView(R.layout.activity_home);
        chcekinBtn = findViewById(R.id.checkIn_btn);
        chcekinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckInResultActivity.launchActivity(HomeActivity.this, currentBDLocation.getLatitude(), currentBDLocation.getLongitude(), isDistanceEffective());
            }
        });
        constraintLayout = findViewById(R.id.map_container);

        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);

        List<String> permissionList  = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(HomeActivity.this,android.Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(HomeActivity.this,android.Manifest.
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if(!permissionList.isEmpty()){
            String [] permissions= permissionList.toArray(new String[permissionList.
                    size()]);
            /*使用ActivityCompat 统一申请权限 */
            ActivityCompat.requestPermissions(HomeActivity.this,permissions,1);
        } else {
            genCompanyAddr();
            requestLocation();
        }
    }

    private void requestLocation() {
        startLocating();
        mLocationClient.start();
    }

    private void genCompanyAddr() {
        companyAddr = new LatLng(31.210093,121.612336);

        //构建Marker
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marker);
        OverlayOptions option = new MarkerOptions()
                .position(companyAddr)
                .icon(bitmap);
        mBaiduMap.addOverlay(option);

        OverlayOptions ooCircle = new CircleOptions().fillColor(0x000000FF)
                .center(companyAddr).stroke(new Stroke(5, 0xAA000000))
                .radius(DISTANCE);
        mBaiduMap.addOverlay(ooCircle);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Must grant all the permissions!", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    finish();
                }
                break;
            default:

        }
    }

    private void startLocating() {

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(10000);
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        option.setLocationNotify(true);
        //option.setWifiCacheTimeOut(5*60*1000);
//可选，7.2版本新增能力
//如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位
        option.setEnableSimulateGps(false);
//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        mLocationClient.setLocOption(option);
    }

    private boolean isDistanceEffective() {
        double dis = DistanceUtil.getDistance(myAddr, companyAddr);
        return dis < (double)DISTANCE;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            currentBDLocation = location;

            if (location == null || mMapView == null)
                return;

            if(isFirstLocate){
                //LatLng对象主要用来存放经纬度
                //zoomTo是用来设置百度地图的缩放级别，范围为3~19，数值越大越精确
                myAddr = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(myAddr, 18);
                mBaiduMap.setMapStatus(update);
//                mBaiduMap.animateMapStatus(update);
//                update = MapStatusUpdateFactory.zoomTo(16f);
//                mBaiduMap.animateMapStatus(update);
                isFirstLocate = false;
            }

            mBaiduMap.setMyLocationEnabled(true);
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    // 这里的方向需要用户通过传感器自定获取并设置
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            Log.i("纬度", String.valueOf(locData.latitude));
            Log.i("经度", String.valueOf(locData.longitude));
            Log.i("速度", String.valueOf(locData.speed));
            Log.i("方向", String.valueOf(locData.direction));
            Log.i("精度", String.valueOf(locData.accuracy));

            mBaiduMap.setMyLocationData(locData);

//            if (isDistanceEffective()) {
//                Snackbar.make(constraintLayout, "You can check in now!", Snackbar.LENGTH_SHORT).show();
//            } else {
//                Snackbar.make(constraintLayout, "You are far away from company!", Snackbar.LENGTH_SHORT).show();
//            }
        }
    }
}
