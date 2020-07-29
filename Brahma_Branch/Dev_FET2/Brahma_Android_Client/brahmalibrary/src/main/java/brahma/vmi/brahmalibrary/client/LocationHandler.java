/*
 * Copyright (c) 2013 The MITRE Corporation, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this work except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package brahma.vmi.brahmalibrary.client;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import brahma.vmi.brahmalibrary.common.Utility;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.LocationResponse;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.LocationSubscribe;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.LocationUnsubscribe;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.Request;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.Response;
import brahma.vmi.brahmalibrary.services.SessionService;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.isOpenLocationPermission;

//public class LocationHandler implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
public class LocationHandler {
    private static final String TAG = LocationHandler.class.getName();
    //    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private SessionService service;
    private LocationManager lm;
    private Looper looper;
    // keeps track of what LocationListeners there are for a given LocationProvider
    private HashMap<String, BRAHMALocationListener> locationListeners = new HashMap<String, BRAHMALocationListener>();

    public LocationHandler(SessionService service) {
        this.service = service;
        lm = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
        looper = Looper.myLooper();
        // Create an instance of GoogleAPIClient.
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(context)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }
    }

    private void removeLUpdates(String provider) {
        if (locationListeners.containsKey(provider))
            lm.removeUpdates(locationListeners.get(provider));
    }

    public boolean initLocationUpdates() {
        return sendLocationProviderMessages();
    }

    public void cleanupLocationUpdates() {
        // loop through location listeners and remove subscriptions for each one
        Iterator it = locationListeners.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pairs = (HashMap.Entry) it.next();
            lm.removeUpdates((LocationListener) pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    private BRAHMALocationListener getListenerSingle(String provider) {
        // generate a unique name for this key (each single subscription is disposed after receiving one update)
        String uniqueName = provider + String.format("%.3f", System.currentTimeMillis() / 1000.0);

        // add a listener for this key
        locationListeners.put(uniqueName, new BRAHMALocationListener(uniqueName, true));

        return locationListeners.get(uniqueName);
    }

    private BRAHMALocationListener getListenerLongTerm(String provider) {
        Log.d(TAG,"getListenerLongTerm >>>>>> "+provider);
        // if the HashMap doesn't contain a listener for this key, add one
        if (!locationListeners.containsKey(provider))
            locationListeners.put(provider, new BRAHMALocationListener(provider, false));

        return locationListeners.get(provider);
    }

    // called when a LocationListener triggers, converts the data and sends it to the VM
    private void onLocationChanged(Location location) {
        Request request = Utility.toRequest_LocationUpdate(location);

        // send the Request to the VM
        service.sendMessage(request);
    }

    // called when a onProviderEnabled or onProviderDisabled triggers, converts the data and sends it to the VM
    private void onProviderEnabled(String s, boolean isEnabled) {
        Request request = Utility.toRequest_LocationProviderEnabled(s, isEnabled);

        // send the Request to the VM
        service.sendMessage(request);
    }

    // called when a onStatusChanged triggers, converts the data and sends it to the VM
    private void onStatusChanged(String s, int i, Bundle bundle) {
        Request request = Utility.toRequest_LocationProviderStatus(s, i, bundle);
        // send the Request to the VM
        service.sendMessage(request);
    }

    private boolean sendLocationProviderMessages() {
        //mGoogleApiClient.connect();
        List<String> providerNames = lm.getAllProviders();
        try {
            for (String providerName : providerNames) {
                Log.d(TAG, "Init provider => providerName = " + providerName);
                if (!providerName.equals(LocationManager.PASSIVE_PROVIDER)) {

                    if (isOpenLocationPermission()) {
                        Log.d(TAG, "location permission granted");
                        LocationProvider provider = lm.getProvider(providerName);
                        assert provider != null;
                        Request request = Utility.toRequest_LocationProviderInfo(provider);
                        service.sendMessage(request);
                        Log.d(TAG, "location request:" + request.toString());
                    } else {
                        Log.d(TAG, "location permission denied");
                        //fake data
                        Request request = Utility.toRequest_LocationProviderInfo();
                        service.sendMessage(request);
                        Log.d(TAG, "location request:" + request.toString());
                    }
                }
            }
            return true;
        } catch (NullPointerException e) {
            Log.d(TAG, "sendLocationProviderMessages NullPointerException");
            return false;
        }
    }

    public void handleLocationResponse(Response response) {
        Log.d(TAG, "handleLocationResponse start");
        if (isOpenLocationPermission()) {
            LocationResponse locationResponse = response.getLocationResponse();
            // a response can either be to subscribe or to unsubscribe
            if (locationResponse.getType() == LocationResponse.LocationResponseType.SUBSCRIBE) {
                LocationSubscribe locationSubscribe = locationResponse.getSubscribe();
                String provider = locationSubscribe.getProvider();
                Log.d(TAG, "provider = " + provider);
                try {
                    // a subscribe request can either be one-time or long-term
                    if (locationSubscribe.getType() == LocationSubscribe.LocationSubscribeType.SINGLE_UPDATE) {
                        Log.d(TAG, "SINGLE_UPDATE = " + provider);
                        LocationListener locationListener = getListenerSingle(provider);
                        lm.requestSingleUpdate(
                                provider,
                                locationListener,
                                looper);
                    } else if (locationSubscribe.getType() == LocationSubscribe.LocationSubscribeType.MULTIPLE_UPDATES) {
                        Log.d(TAG, "MULTIPLE_UPDATES = " + provider);
                        Log.d(TAG, "provider = " + provider);
                        Log.d(TAG, "locationSubscribe.getMinTime() = " + locationSubscribe.getMinTime());
                        Log.d(TAG, "locationSubscribe.getMinDistance() = " + locationSubscribe.getMinDistance());
                        LocationListener locationListener = getListenerLongTerm(provider);
//                        lm.requestLocationUpdates(GPS_PROVIDER, 5000, 0, this);
//                        lm.requestLocationUpdates(NETWORK_PROVIDER, 5000, 0, this);

                        lm.requestLocationUpdates(
                                provider,
                                locationSubscribe.getMinTime(),
                                locationSubscribe.getMinDistance(),
                                locationListener,
                                looper);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else if (locationResponse.getType() == LocationResponse.LocationResponseType.UNSUBSCRIBE) {
                LocationUnsubscribe locationUnsubscribe = locationResponse.getUnsubscribe();

                // unsubscribe from location updates for this provider
                // (we only get unsubscribe requests for long-term subscriptions)
                String provider = locationUnsubscribe.getProvider();
                removeLUpdates(provider);
            }

            //add by yiwen 20180321
            try {
                Location locationGPS = lm.getLastKnownLocation(GPS_PROVIDER); //使用GPS定位座標
                Location locationNetwork = lm.getLastKnownLocation(NETWORK_PROVIDER); //使用Network定位座標

                if (locationGPS != null) {
                    locationGPS.setAccuracy(30);//調整地圖的大小
                    Request request = Utility.toRequest_LocationUpdate(locationGPS);
                    Log.d(TAG, "GPS - request:" + request.toString());
                    service.sendMessage(request);
                } else if (locationNetwork != null) {
                    //changeProvider
                    locationNetwork.setProvider(GPS_PROVIDER);
                    locationNetwork.setAccuracy(30);//調整地圖的大小
                    Request request = Utility.toRequest_LocationUpdate(locationNetwork);
                    Log.d(TAG, "Network - request:" + request.toString());
                    service.sendMessage(request);
                } else {
                    Request request = Utility.toRequest_LocationUpdate();
                    service.sendMessage(request);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "location permission is denied!!");
            Request request = Utility.toRequest_LocationUpdate();
            service.sendMessage(request);
            Log.d(TAG, "location request:" + request.toString());
        }

    }

    class BRAHMALocationListener implements LocationListener {

        private String key;
        private boolean singleShot;

        BRAHMALocationListener(String key, boolean singleShot) {
            this.key = key;
            this.singleShot = singleShot;
        }

        @Override
        public void onLocationChanged(Location location) {
//            Log.d(TAG, "onLocationChanged: Provider(" + location.getProvider() + "), singleShot(" + singleShot + "), " + location.toString());
            LocationHandler.this.onLocationChanged(location);

            // if this is a singleshot update, we don't need this listener anymore; remove it
            if (singleShot) {
                lm.removeUpdates(this);
                locationListeners.remove(key);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "onStatusChanged: Provider(" + s + ") Status(" + i + ")");
            LocationHandler.this.onStatusChanged(s, i, bundle);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "onProviderEnabled: Provider(" + s + ")");
            LocationHandler.this.onProviderEnabled(s, true);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "onProviderDisabled: Provider(" + s + ")");
            LocationHandler.this.onProviderEnabled(s, false);
        }
    }
}
