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

package brahma.vmi.covid2019.client;

import android.content.res.Configuration;

import brahma.vmi.covid2019.activities.AppRTCActivity;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.Config;
import brahma.vmi.covid2019.protocol.BRAHMAProtocol.Request;

/**
 * @developer Ian
 * Sends configuration updates to the remote Brahma instance.
 * Initially only used to track whether a hardware keyboard is connected
 */
public class ConfigHandler {
    private AppRTCActivity activity;

    public ConfigHandler(AppRTCActivity activity) {
        this.activity = activity;
    }

    public void handleConfiguration(Configuration config) {
        // whenever the configuration changes, catch it and track it
        if (activity.isConnected()) {
            Request request = makeRequest(config);
            activity.sendMessage(request);
        }
    }

    // transforms Configuration into a Request
    private Request makeRequest(Configuration config) {
        // create a Config protobuf
        Config.Builder cBuilder = Config.newBuilder();
        // set whether or not a hard keyboard is attached
        cBuilder.setHardKeyboard(config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);

        // wrap the Config in a Request and return it
        Request.Builder rBuilder = Request.newBuilder();
        rBuilder.setType(Request.RequestType.CONFIG);
        rBuilder.setConfig(cBuilder);
        return rBuilder.build();
    }
}
