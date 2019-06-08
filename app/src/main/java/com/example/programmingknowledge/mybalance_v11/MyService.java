package com.example.programmingknowledge.mybalance_v11;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.GoogleMapAPI.GoogleMapActivity;
import com.example.settings.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = MyService.class.getSimpleName();

    private Thread mThread;
    private int mCount = 0;
    //Intent intent;
    //Context context = this;

    LatLng currentPosition;
    LatLng previousPosition = null;   //추가

    GoogleApiClient mLocationClient;
    private IBinder mBinder = new MyBinder();

    private static final int UPDATE_INTERVAL_MS = 20000;  // 1초=1000  1분
    private static final int FASTEST_UPDATE_INTERVAL_MS = 20000; // 1분

    //LocationRequest mLocationRequest = new LocationRequest();

    public static final String ACTION_LOCATION_BROADCAST = MyService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

    LocationRequest mLocationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


    public MyService() {
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if ("startForeground".equals(intent.getAction())) {
            // 포그라운드 서비스 시작
            startForegroundService();

        } else if (mThread == null) {
            // 스레드 초기화 및 시작
            mThread = new Thread("My Thread") {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        try {
                            mCount++;
                            // 1초 마다 쉬기
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // 스레드에 인터럽트가 걸리면
                            // 오래 걸리는 처리 종료
                            break;
                        }
                        if(mCount==20){
                            Log.d("My Service", "지도 가져와야돼 " + mCount);
                            //getmap(intent);
                        }
                        // 1초 마다 로그 남기기
                        Log.d("My Service", "서비스 동작 중 " + mCount);

                    }
                }
            };
            mThread.start();
        }

        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
        //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes


        mLocationRequest.setPriority(priority);
        mLocationClient.connect();

        return START_NOT_STICKY;
    }

    public void getmap(){
        Intent intent = new Intent(this, GoogleMapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");

        // stopService 에 의해 호출 됨
        // 스레드를 정지시킴
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }

        super.onDestroy();
    }

    // MyService의 레퍼런스를 반환하는 Binder 객체

    @Override
    public void onLocationChanged(Location location) {
        double distance;
        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

        Log.d(TAG, "onLocationChanged : ");

        String markerTitle = getCurrentAddress(currentPosition);
        String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());
        System.out.println("Service: 현재 위치: " + markerSnippet + "  / 주소: " + markerTitle);
        //현재 위치에 마커 생성하고 이동
        //setCurrentLocation(location, markerTitle, markerSnippet);   //마커 생성
        //mCurrentLocatiion = location;
        if(previousPosition == null){  //최초 실행했을 때 지도 불러옴
            System.out.println("***Service: 이전 위치가 없는 상황입니다. ");
            previousPosition = currentPosition;
            getmap();
            //showPlaceInformation(currentPosition);
        }
        else  {  //previousPosition이 null이 아니면 실행(이전 위치가 존재, 최초 실행이 아닐 때) (previousPosition != null)
            distance = SphericalUtil.computeDistanceBetween(currentPosition, previousPosition);  //이전 거리와 현재 거리 비교 (일단 10m로) //거리로 비교 왜 했냐 아오..
            System.out.println("Service: 이전 위치가 존재하는 상태입니다.: " + getCurrentAddress(previousPosition) + " " + getCurrentAddress(currentPosition) + " distance: " + distance ); //최초 실행때는 실행되면 안됨, 이전 위치 존재x
            if(getCurrentAddress(previousPosition).equals(getCurrentAddress(currentPosition))) {  //현재 주소와 이전 주소가 같으면
                System.out.println("***Service: 같은 위치입니다.: " + currentPosition.latitude + " " + currentPosition.longitude + " " + getCurrentAddress(currentPosition) + "/" + getCurrentAddress(previousPosition));
            }
            else{ //현재 주소가 이전 주소와 다르면
                if(distance > 15) { //이전 위치에서 15m를 벗어나면      //이 때 거리 변화 감지
                    System.out.println("***Service: 15m를 벗어났습니다.");
                    previousPosition = currentPosition; //거리 범위를 넘었으니깐 현재 포지션은 다음 setCurrentLocation가 실행될 때 이전 포지션이 된다.
                    //showPlaceInformation(currentPosition);
                    //이 때 GoogleMapActivity가 불러와져야 됨됨됨
                    getmap();
                }
                else{  //이전 위치에서 15를 벗어나지 않았으면(이전 위치에서 가까운 위치면)
                    System.out.println("***Service: 비슷한 위치에 있습니다.");
                }
            }
        }
        //System.out.println("왜 안되지?");
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Log.d(TAG, "== Error On onConnected() Permission not granted");
            //Permission not granted by user so cancel the further execution.

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

        Log.d(TAG, "Connected to Google API");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");
    }

    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    // 바인드된 컴포넌트에 카운팅 변수 값을 제공
    public int getCount() {
        return mCount;
    }

    private void startForegroundService() {
        System.out.println("fore");
        // default 채널 ID로 알림 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("포그라운드 서비스");
        builder.setContentText("포그라운드 서비스 실행 중");
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        // 오레오에서는 알림 채널을 매니저에 생성해야 한다
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // 포그라운드로 시작
        startForeground(1, builder.build());
    }
}
