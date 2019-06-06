package com.example.GoogleMapAPI;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.programmingknowledge.mybalance_v11.DBHelper;
import com.example.programmingknowledge.mybalance_v11.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlacesListener;
import noman.googleplaces.Place;
import noman.googleplaces.PlacesException;

import static com.example.programmingknowledge.mybalance_v11.Category.c_exercise;
import static com.example.programmingknowledge.mybalance_v11.Category.c_leisure;
import static com.example.programmingknowledge.mybalance_v11.Category.c_study;
import static com.google.android.libraries.places.api.net.PlacesStatusCodes.OVER_QUERY_LIMIT;

public class GoogleMapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        PlacesListener,
        GoogleMap.OnMarkerClickListener{

    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 60000;  // 1초=1000  1분
    private static final int FASTEST_UPDATE_INTERVAL_MS = 60000; // 1분
    //설명
    //.setInterval(15000) // 15 seconds
    //.setFastestInterval(5000) // 5000ms
    //
    //이렇게 설정 하셨으면 기기는 15초마다 혹은 그보다 더 빠르거나 느리게 위치를 수집하지만 5초보다 빠르게 수집하진 않을것입니다

    private AppCompatActivity mActivity;
    boolean askPermissionOnceAgain = false;
    boolean mRequestingLocationUpdates = false;
    Location mCurrentLocatiion;
    boolean mMoveMapByUser = true;
    boolean mMoveMapByAPI = true;
    LatLng currentPosition;

    LatLng previousPosition = null;   //추가

    long now = System.currentTimeMillis();
    Date date = new Date(now);

    LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

    List<Marker> previous_marker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlemap);

        previous_marker = new ArrayList<Marker>();

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceInformation(currentPosition);
            }
        });

        /*Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });*/

        Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        Log.d(TAG, "onCreate");
        mActivity = this;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        MapFragment mapFragment = (MapFragment) getFragmentManager()     //지도 fragment 생성
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = (int) (display.getWidth()); //Display 사이즈의 95%
        int height = (int) (display.getHeight());  //Display 사이즈의 95%
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        /*mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                String text = "[장시간 클릭시 이벤트] latitude ="
                        + latLng.latitude + ", longitude ="
                        + latLng.longitude;
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG)
                        .show();
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, "onResume : call startLocationUpdates");
            if (!mRequestingLocationUpdates) startLocationUpdates();
        }

        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;

                checkPermissions();
            }
        }
    }

    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call FusedLocationApi.requestLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mRequestingLocationUpdates = true;

            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");
        mGoogleMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 경기대로 이동
        setDefaultLocation();

        //mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Log.d(TAG, "onMyLocationButtonClick : 위치에 따른 카메라 이동 활성화");
                mMoveMapByAPI = true;
                return true;
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick :");
            }
        });
        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (mMoveMapByUser == true && mRequestingLocationUpdates) {
                    Log.d(TAG, "onCameraMove : 위치에 따른 카메라 이동 비활성화");
                    mMoveMapByAPI = false;
                }
                mMoveMapByUser = true;
            }
        });
        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {  ////1분에 한번씩 호출
        double distance;
        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

        Log.d(TAG, "onLocationChanged : ");

        String markerTitle = getCurrentAddress(currentPosition);
        String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());
        System.out.println("현재 위치: " + markerSnippet + "  / 주소: " + markerTitle);
        //현재 위치에 마커 생성하고 이동

        setCurrentLocation(location, markerTitle, markerSnippet);   //마커 생성
        //mCurrentLocatiion = location;
       /* if(previousPosition == null){  //최초 실행했을 때 (앱을 아예 처음 켰을 때??)  //이 때 가까운 place들이 뜸 처음에만!  //이거 왜 처음에 안 뜨지..?상관은 없지만
            System.out.print("이전 위치가 없는 상황입니다.: ");
            previousPosition = currentPosition;
            showPlaceInformation(currentPosition);
        }

        else  {  //previousPosition이 null이 아니면 실행(이전 위치가 존재, 최초 실행이 아닐 때) (previousPosition != null)
            distance = SphericalUtil.computeDistanceBetween(currentPosition, previousPosition);  //이전 거리와 현재 거리 비교 (일단 10m로) //거리로 비교 왜 했냐 아오..
            System.out.println("이전 위치가 존재하는 상태입니다.: " + previousPosition.latitude + " " + previousPosition.longitude + " distance: " + distance); //최초 실행때는 실행되면 안됨, 이전 위치 존재x
            if(getCurrentAddress(previousPosition).equals(getCurrentAddress(currentPosition))) {  //현재 주소와 이전 주소가 같으면
                System.out.println("같은 위치입니다.: " + currentPosition.latitude + " " + currentPosition.longitude + " " + getCurrentAddress(currentPosition) + "/" + getCurrentAddress(previousPosition));
            }
            else{ //현재 주소가 이전 주소와 다르면
                if(distance > 15) { //이전 위치에서 15m를 벗어나면      //이 때 거리 변화 감지
                    System.out.println("15m를 벗어났습니다.");
                    previousPosition = currentPosition; //거리 범위를 넘었으니깐 현재 포지션은 다음 setCurrentLocation가 실행될 때 이전 포지션이 된다.
                    showPlaceInformation(currentPosition);
                }
                else{  //이전 위치에서 15를 벗어나지 않았으면(이전 위치에서 가까운 위치면)
                    System.out.println("비슷한 위치에 있습니다.");
                }
            }
        }*/
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() == false) {

            Log.d(TAG, "onStart: mGoogleApiClient connect");
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mRequestingLocationUpdates) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }

        if (mGoogleApiClient.isConnected()) {
            Log.d(TAG, "onStop : mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (mRequestingLocationUpdates == false) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                } else {
                    Log.d(TAG, "onConnected : 퍼미션 가지고 있음");
                    Log.d(TAG, "onConnected : call startLocationUpdates");
                    startLocationUpdates();
                    mGoogleMap.setMyLocationEnabled(true);
                }
            } else {
                Log.d(TAG, "onConnected : call startLocationUpdates");
                startLocationUpdates();
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        setDefaultLocation();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended");
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }

    public String getCurrentAddress(LatLng latlng) {  //현재 주소   ///1분에 6번씩 호출된다.
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {  //주소가 발견되지 않으면
            //Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();  //이게 꼭 필요할까?
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {  //현재 위치 가져오기
        mMoveMapByUser = false;

        if (currentMarker != null)
            currentMarker.remove();  //현재 마커가 null값이 아니면 지우기 (새로 가져오기 위해) (이 명령문 지우면 마커 그림이 계속 쌓임)

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());  //현재 경도와 위도 가져오기

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mGoogleMap.addMarker(markerOptions);  //현재 위치로 바뀐 마커 붙이기

        if (mMoveMapByAPI) {
            Log.d(TAG, "setCurrentLocation :  mGoogleMap moveCamera "
                    + location.getLatitude() + " " + location.getLongitude());
            //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15); //이건 뭐지?
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

    public void setDefaultLocation() {  //디폴트 위치 가져오기(경기대학교)
        mMoveMapByUser = false;

        LatLng DEFAULT_LOCATION = new LatLng(37.300962, 127.035782); //디폴트 위치, 경기대
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");

            if (mGoogleApiClient.isConnected() == false) {
                Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (permissionAccepted) {
                if (mGoogleApiClient.isConnected() == false) {
                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    mGoogleApiClient.connect();
                }
            } else {
                checkPermissions();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                askPermissionOnceAgain = true;
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : 퍼미션 가지고 있음");

                        if (mGoogleApiClient.isConnected() == false) {
                            Log.d(TAG, "onActivityResult : mGoogleApiClient connect ");
                            mGoogleApiClient.connect();
                        }
                        return;
                    }
                }
                break;
        }
    }

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {
    }

    ArrayList<MarkerInfo> mlist = new ArrayList<>();
    ArrayList<MarkerInfo> sort_mlist = new ArrayList<>();
    double placeDistance = 0;
    String[] placeType = null;
    String placeName = null;
    ArrayList<HashMap<String, String>> MarkerList;

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                System.out.println("placesuccess");
                ArrayList<MarkerInfo> mlist = new ArrayList<>();

                for (noman.googleplaces.Place place : places) {  //검색된 장소들에 대해 실행
                    LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    //String markerSnippet = getCurrentAddress(latLng);

                    placeType = place.getTypes();
                    placeName = place.getName();
                    placeDistance = SphericalUtil.computeDistanceBetween(currentPosition, latLng);

                    //placeAddr = markerSnippet;  //주소가 필요할까??
                    //tmp = place.getLocation();   //위도와 경도를 다시 받아오기 위해서(필요할까?)

                    if (placeDistance > 31) {  //시청이랑 이상한 길 없앨려고 넣었음
                        continue;
                    }
                    //미터로 반환
                    markerOptions.title(placeName);   //마커에 이름 저장
                    markerOptions.snippet(placeType[0]);  //마커에 타입 저장

                    MarkerInfo m = new MarkerInfo(placeName, placeType[0], placeDistance);
                    mlist.add(m);

                    Marker item = mGoogleMap.addMarker(markerOptions);
                    previous_marker.add(item);  //이 때 하나의 마커가 리스트에 저장된다.
                }  //places for문 끝(place 검색 끝)

                //중복 마커 제거
                LinkedHashSet<Marker> hashSet = new LinkedHashSet<Marker>();   // HashSet과 LinkedHashSet의 차이는 중복이 제거 될 때 HashSet은 기존 리스트의 구성요서의 순서가 지켜지지 않는다
                hashSet.addAll(previous_marker);   //hashset에 이전 previous_marker저장
                previous_marker.clear();
                previous_marker.addAll(hashSet);
                //검색된 마커들의 리스트 완성

                markerComparator comp = new markerComparator();
                Collections.sort(mlist, comp);
                //mlist에 저장된 객체들을 거리순으로 정렬

                MarkerList = new ArrayList<>();

                for (MarkerInfo m : mlist) {   //이 때 거리 순으로 정렬
                    HashMap<String, String> tmplist = new HashMap<>();
                    tmplist.put("place_name", m.getPlaceName());
                    tmplist.put("place_type", m.getPlaceType());
                    System.out.println("거리 확인: " + m.getDistance());
                    if (m.getDistance() > 31) {   //시청과 범위 벗어나는 장소를 리스트에 띄우지 않기 위해서
                        continue;
                    }
                    MarkerList.add(tmplist);  //리스트뷰에 띄울 리스트 생성
                    sort_mlist.add(m);
                }
                //리스트뷰 생성
                final ListView listView = (ListView) findViewById(R.id.listView);
                MarkerList = new ArrayList<>();
                SimpleAdapter simpleAdapter = new SimpleAdapter(GoogleMapActivity.this, MarkerList, android.R.layout.simple_list_item_2, new String[]{"place_name", "place_type"}, new int[]{android.R.id.text1, android.R.id.text2});


                for (MarkerInfo m : mlist) {   //이 때 거리 순으로 정렬
                    HashMap<String, String> tmplist = new HashMap<>();
                    tmplist.put("place_name", m.getPlaceName());
                    tmplist.put("place_type", m.getPlaceType());
                    if (m.getDistance() > 30) {   //시청과 범위 벗어나는 장소를 리스트에 띄우지 않기 위해서
                        continue;
                    }
                    MarkerList.add(tmplist);  //리스트뷰에 띄울 리스트 생성
                    sort_mlist.add(m);
                }
                listView.setAdapter(simpleAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  //db에 저장
                    @Override
                    public void onItemClick(AdapterView parent, View v, int position, long id) {   //리스트를 눌렀을 때  //여기서 DB로 저장
                        TextView tmp = (TextView) v.findViewById(android.R.id.text1);
                        TextView tmp2 = (TextView) v.findViewById(android.R.id.text2);
                        String title = tmp.getText().toString();
                        String type = tmp2.getText().toString();
                        System.out.println("클릭한 리스트에 장소 이름: " + title + "\n클릭한 리스트에 타입: " + type);

                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                        SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy/MM/dd");
                        SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm:ss");
                        final String formatDate = sdfdate.format(date);
                        final String formatTime = sdftime.format(date);

                        //db 삽입 부분
                        DBHelper helper = new DBHelper(getApplicationContext());
                        SQLiteDatabase db = helper.getWritableDatabase();
                        Cursor cursor = db.rawQuery("select * from tb_timeline where endtime is NULL", null);

                        //아예 처음시작하는 활동인 경우
                        if (cursor.getCount() == 0 ) {
                            db.execSQL("insert into tb_timeline (date, place, category, starttime) values (?,?,?,?)",
                                    new String[]{formatDate, title, type, formatTime});
                            return;
                        }
                        //전의 활동이 있는경우
                        else {
                            cursor.moveToFirst();
                            String place = cursor.getString(cursor.getColumnIndex("place"));
                            String category = cursor.getString(cursor.getColumnIndex("category"));
                            String starttime = cursor.getString(cursor.getColumnIndex("starttime"));

                            try {
                                //활동 중 날짜가 넘어가는 경우 ///단 한 활동이 24시간을 넘지 않는다는 가정하에
                                if (sdftime.parse(starttime).getTime() > sdftime.parse(formatTime).getTime()) {
                                    db.execSQL("update tb_timeline set endtime=? where endtime is NULL",
                                            new String[]{"24:00:00"});
                                    db.execSQL("insert into tb_timeline (date, place, category, starttime) values (?,?,?,?)",
                                            new String[]{formatDate, place, category, "00:00:00"});
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            //클릭 동시에 시작시간을 전에 활동 시작시간으로 update
                            db.execSQL("update tb_timeline set endtime=? where endtime is NULL",
                                    new String[]{formatTime});
                            //현재 장소, 카테고리, 현재시간 insert
                            db.execSQL("insert into tb_timeline (date, place, category, starttime) values (?,?,?,?)",
                                    new String[]{formatDate, title, type, formatTime});
                        }

                        db.close();
                    }
                });
            }
        });
    }
/*
    public void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GoogleMapActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_placelist, null);
        builder.setView(view);

        final ListView listview = (ListView)view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        SimpleAdapter simpleAdapter = new SimpleAdapter(GoogleMapActivity.this, MarkerList, android.R.layout.simple_list_item_2, new String[]{"place_name", "place_type"}, new int[]{android.R.id.text1, android.R.id.text2});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                TextView tmp = (TextView) v.findViewById(android.R.id.text1);
                TextView tmp2 = (TextView) v.findViewById(android.R.id.text2);
                String title = tmp.getText().toString();
                String type = tmp2.getText().toString();
                System.out.println("클릭한 리스트에 장소 이름: " + title + "\n클릭한 리스트에 타입: " + type);

                long now = System.currentTimeMillis();
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfdate = new SimpleDateFormat("yyyy/MM/dd");
                SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm:ss");
                final String formatDate = sdfdate.format(date);
                final String formatTime = sdftime.format(date);
                DBHelper helper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = helper.getWritableDatabase();


                // 카테고리 장소타입별로 자동분류
                if (c_study.contains(type))
                    type = "study";
                else if (c_exercise.contains(type))
                    type = "exercise";
                else if (c_leisure.contains(type))
                    type = "leisure";
                else
                    type = "other";

                // 장소 이름에 특정 단어가 들어가면 type을 알맞게 분류
                if (title.contains("아파트") || title.contains("빌라"))
                    type = "rest";
                else if (title.contains("학교") || title.contains("도서관") || title.contains("학원"))
                    type = "study";

                //이미 저장된 장소인 경우 그 카테고리로 자동분류
                Cursor cursor2 = db.rawQuery("select category from tb_timeline where place=\"" + title + "\"", null);
                if (cursor2.getCount() != 0) {
                    cursor2.moveToLast();
                    type = cursor2.getString(cursor2.getColumnIndex("category"));
                }


                //db 삽입 부분
                Cursor cursor = db.rawQuery("select * from tb_timeline where endtime is NULL", null);

                //아예 처음시작하는 활동인 경우
                if (cursor.getCount() == 0 ) {
                    db.execSQL("insert into tb_timeline (date, place, category, starttime) values (?,?,?,?)",
                            new String[]{formatDate, title, type, formatTime});
                    return;
                }
                //전의 활동이 있는경우
                else {
                    cursor.moveToFirst();
                    String place = cursor.getString(cursor.getColumnIndex("place"));
                    String category = cursor.getString(cursor.getColumnIndex("category"));
                    String starttime = cursor.getString(cursor.getColumnIndex("starttime"));

                    try {
                        //활동 중 날짜가 넘어가는 경우 ///단 한 활동이 24시간을 넘지 않는다는 가정하에
                        if (sdftime.parse(starttime).getTime() > sdftime.parse(formatTime).getTime()) {
                            db.execSQL("update tb_timeline set endtime=? where endtime is NULL",
                                    new String[]{"24:00:00"});
                            db.execSQL("insert into tb_timeline (date, place, category, starttime) values (?,?,?,?)",
                                    new String[]{formatDate, place, category, "00:00:00"});
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //클릭 동시에 시작시간을 전에 활동 시작시간으로 update
                    db.execSQL("update tb_timeline set endtime=? where endtime is NULL",
                            new String[]{formatTime});
                    //현재 장소, 카테고리, 현재시간 insert
                    db.execSQL("insert into tb_timeline (date, place, category, starttime) values (?,?,?,?)",
                            new String[]{formatDate, title, type, formatTime});
                }

                db.close();
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
*/
    @Override
    public void onPlacesFinished() {
        //System.out.println("onPlacesFinished");
    }

    public void showPlaceInformation(LatLng location) {
        mGoogleMap.clear();//지도 클리어

        if (previous_marker != null)   //마커가 존재하면
            previous_marker.clear(); //지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(GoogleMapActivity.this)
                .key("AIzaSyBzMQMBkCT4TIyu5zpqVDxWUu9yAvlJE-k")
                .latlng(location.latitude, location.longitude)  //현재 위치
                .radius(30) //20 미터 내에서 검색
                //.type(PlaceType.BUS_STATION)  //모든 타입을 검색하면 시청이 검색 됨..흐규흐규...
                .build()
                .execute();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}

class MarkerInfo {
    String placeName;
    String placeType;
    double distance;

    MarkerInfo(String placeName, String placeType, double distance) {
        this.placeName = placeName;
        this.placeType = placeType;
        this.distance = distance;
    }

    String getPlaceName() {
        return placeName;
    }

    String getPlaceType() {
        return placeType;
    }

    double getDistance() {
        return distance;
    }
}

class markerComparator implements Comparator<MarkerInfo> {
    @Override
    public int compare(MarkerInfo first, MarkerInfo second) {
        double firstValue = first.getDistance();
        double secondValue = second.getDistance();

        if (firstValue > secondValue) {
            return 1;
        } else if (firstValue < secondValue) {
            return -1;
        } else {
            return 0;
        }
    }
}
