/*
Copyright 2013 The MITRE Corporation, All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package brahma.vmi.covid2019.auth;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import brahma.vmi.covid2019.common.ConnectionInfo;

/**
 * @developer Ian
 * Stores authentication information to be sent to the proxy
 * If we have a session token, we first try to use it for authentication in lieu of prompting for input
 * If we do NOT have a session token (e.g. we have a password, security token, it is cleared after being accessed
 */
public final class AuthData {
    public static String pd = "";
    public static boolean ad = false;
    // maps ConnectionID to Request objects that contain auth info (password, etc)
    private static HashMap<Integer, JSONObject> authDataMap = new HashMap<Integer, JSONObject>();

    // no public instantiations[private AuthData() {}]
    public AuthData() {
    }

    // used to add auth data (password, security token, etc)
    public static void setAuthJSON(ConnectionInfo connectionInfo, JSONObject jsonObject) {
        // store this auth data
        Log.d("BrahmaActivity", "3 jsonObject:" + jsonObject.toString());
        authDataMap.put(connectionInfo.getConnectionID(), jsonObject);
    }

    public static JSONObject getJSON(ConnectionInfo connectionInfo) {
        // get the JSON and remove it from the map (returns null value if it doesn't exist)
        return authDataMap.remove(connectionInfo.getConnectionID());
    }

    public static String getPd() {
        return pd;
    }

    // used to add auth data (password, security token, etc)
    public static void setPd(String pass) {
        // store this auth data
        pd = pass;
    }

    public static boolean getAD() {
        return ad;
    }

    public static void setAD(boolean ada) {
        // store this auth data
        ad = ada;
    }

}
