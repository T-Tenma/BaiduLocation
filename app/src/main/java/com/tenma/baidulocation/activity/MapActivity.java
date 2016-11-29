package com.tenma.baidulocation.activity;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.inner.GeoPoint;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.tenma.baidulocation.R;
import com.tenma.baidulocation.adapter.PlaceListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tenma on 2016/11/27.
 */

public class MapActivity extends AppCompatActivity implements BDLocationListener,OnGetGeoCoderResultListener,BaiduMap.OnMapTouchListener {

    /** 标识图片 */
    private ImageView mMarker;
    /** 地址列表视图 */
    private ListView mAddressList;
    /** 地图视图 */
    private MapView mMapView;
    /** 列表adapter */
    private PlaceListAdapter customListAdpter;
    /**  */
    private BaiduMap mBaiduMap;
    /** 定位用*/
    private LocationClient mLocationClient;
    /** 地址列表数据*/
    private List<PoiInfo> mInfoList;
    /** 地图中心点*/
    private Point mCenterPoint;
    /** 地理坐标点*/
    private LatLng mLoactionLatLng;
    /** 地理编码*/
    private GeoCoder mGeoCoder;
    /** 经度*/
    private double mLongitude;
    /** 纬度*/
    private double mLatitude;
    /** 地址*/
    private String mAddress;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initView();
        initMap();
        getimgxy();
    }

    /**
     * 初始化地图
     */
    private void initMap() {
        mMapView.showZoomControls(true);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
        mBaiduMap.setMapStatus(msu);
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setOnMapTouchListener(this);
        mLocationClient = new LocationClient(getApplicationContext());
        setLocationOption();
    }



    /**
     * 设置定位参数
     */
    private void setLocationOption() {
        mLocationClient.registerLocationListener(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(3000); // 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mMarker = (ImageView) findViewById(R.id.iv_marker);
        mAddressList = (ListView) findViewById(R.id.lv_map);

        setTitle("定位");
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        viewResult(bdLocation);
        if (bdLocation == null || mMapView == null)
            return;
        mLatitude = bdLocation.getLatitude();
        mLongitude = bdLocation.getLongitude();
        MyLocationData locData = new MyLocationData.Builder()
        .accuracy(bdLocation.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);    //设置定位数据

        // 获取当前MapView中心屏幕坐标对应的地理坐标
        LatLng currentLatLng;
        currentLatLng = new LatLng(mLatitude,mLongitude);
        // 发起反地理编码检索
        mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption())
                .location(currentLatLng));
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(currentLatLng,20);
        mBaiduMap.animateMapStatus(u);
    }


    /**
     * 初始化地图物理坐标
     */
    private void getimgxy() {
        // 初始化POI信息列表
        mInfoList = new ArrayList<PoiInfo>();
        mCenterPoint = mBaiduMap.getMapStatus().targetScreen;
        mLoactionLatLng = mBaiduMap.getMapStatus().target;
        // 地理编码
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(this);
        customListAdpter = new PlaceListAdapter(getLayoutInflater(), mInfoList);
        mAddressList.setAdapter(customListAdpter);
        mAddressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                customListAdpter.clearSelection(i);
                customListAdpter.notifyDataSetChanged();
                mLocationClient.stop();
                mBaiduMap.clear();
                PoiInfo info = (PoiInfo) customListAdpter.getItem(i);
                LatLng la = info.location;
                mLatitude = la.latitude;
                mLongitude = la.longitude;
                mAddress = info.address;

                MyLocationData locData = new MyLocationData.Builder()
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(mLatitude)
                        .longitude(mLongitude).build();
                mBaiduMap.setMyLocationData(locData);
                //设置定位数据
                mLoactionLatLng = new LatLng(mLatitude,
                        mLongitude);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(mLoactionLatLng, 20);    //设置地图中心点以及缩放级别
                mBaiduMap.animateMapStatus(u);
            }
        });

    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有找到检索结果
        }
        // 获取反向地理编码结果
        else {
            // 当前位置信息
            PoiInfo mCurentInfo = new PoiInfo();
            mCurentInfo.address = reverseGeoCodeResult.getAddress();
            mCurentInfo.location = reverseGeoCodeResult.getLocation();
            mCurentInfo.name = "[位置]";
            mInfoList.clear();
            mInfoList.add(mCurentInfo);

            // 将周边信息加入表
            if (reverseGeoCodeResult.getPoiList() != null) {
                mInfoList.addAll(reverseGeoCodeResult.getPoiList());
            }
            // 通知适配数据已改变
            customListAdpter.notifyDataSetChanged();
       /* mLoadBar.setVisibility(View.GONE);*/

        }
    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

            if (mCenterPoint == null) {
                return;
            }
            // 获取当前MapView中心屏幕坐标对应的地理坐标
            LatLng currentLatLng;
            currentLatLng = mBaiduMap.getProjection().fromScreenLocation(
                    mCenterPoint);
            // 发起反地理编码检索
            mGeoCoder.reverseGeoCode((new ReverseGeoCodeOption())
                    .location(currentLatLng));
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    /**
     * 通过Log查看具体定位信息
     * @param location 位置
     */
    private void viewResult(BDLocation location){
        //Receive Location
        StringBuffer sb = new StringBuffer(256);
        sb.append("time : ");
        sb.append(location.getTime());
        sb.append("\nerror code : ");
        sb.append(location.getLocType());
        sb.append("\nlatitude : ");
        sb.append(location.getLatitude());
        sb.append("\nlontitude : ");
        sb.append(location.getLongitude());
        sb.append("\nradius : ");
        sb.append(location.getRadius());
        if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
            sb.append("\nspeed : ");
            sb.append(location.getSpeed());// 单位：公里每小时
            sb.append("\nsatellite : ");
            sb.append(location.getSatelliteNumber());
            sb.append("\nheight : ");
            sb.append(location.getAltitude());// 单位：米
            sb.append("\ndirection : ");
            sb.append(location.getDirection());// 单位度
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());
            sb.append("\ndescribe : ");
            sb.append("gps定位成功");

        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());
            //运营商信息
            sb.append("\noperationers : ");
            sb.append(location.getOperators());
            sb.append("\ndescribe : ");
            sb.append("网络定位成功");
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");
        } else if (location.getLocType() == BDLocation.TypeServerError) {
            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        sb.append("\nlocationdescribe : ");
        sb.append(location.getLocationDescribe());// 位置语义化信息
        List<Poi> list = location.getPoiList();// POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
            }
        }
        Log.i("BaiduLocationApiDem", sb.toString());
    }
}
