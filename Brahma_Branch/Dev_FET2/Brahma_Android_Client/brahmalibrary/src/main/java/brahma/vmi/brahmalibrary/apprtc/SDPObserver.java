/*
 Copyright (c) 2013 The MITRE Corporation, All Rights Reserved.

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
// Derived from AppRTCActivity from the libjingle / webrtc AppRTCDemo
// example application distributed under the following license.
/*
 * libjingle
 * Copyright 2013, Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package brahma.vmi.brahmalibrary.apprtc;

import android.util.Log;
import org.json.JSONObject;
import brahma.vmi.brahmalibrary.R;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Implementation detail: handle offer creation/signaling and answer setting,
// as well as adding remote ICE candidates once the answer SDP is set.


public class SDPObserver implements SdpObserver {
    protected static final String TAG = "SDPObserver";
    //AppRTCVideoActivity activity;
    private CallActivity activity;
    private static final String VIDEO_CODEC_H264 = "H264";
    private SessionDescription sdp;
    SDPObserver(CallActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreateSuccess(final SessionDescription origSdp) {
        Log.d(TAG,"onCreateSuccess!!!!!============"+origSdp.description);
        sdp = origSdp;
        final SDPObserver parent = this;
        new Thread(new Runnable() {
            public void run() {
                String sdpDescription = origSdp.description;
                sdpDescription = preferCodec(sdpDescription, VIDEO_CODEC_H264, false);

//                activity.logAndToast(R.string.appRTC_toast_sdpObserver_sendOffer);
                activity.setProgressDegree(100,activity.getResources().getString(R.string.progressBar_100));
                final SessionDescription sdp = new SessionDescription(origSdp.type, sdpDescription);
                activity.getPCObserver().getPC().setLocalDescription(parent, sdp);

            }
        }).start();
    }

    private void sendLocalDescription(PeerConnection pc) {
        SessionDescription sdp = pc.getLocalDescription();
        JSONObject json = new JSONObject();
        AppRTCHelper.jsonPut(json, "type", sdp.type.canonicalForm());
        AppRTCHelper.jsonPut(json, "sdp", sdp.description);
        activity.sendMessage(AppRTCHelper.makeWebRTCRequest(json));
    }

    @Override public void onSetSuccess() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                PCObserver pcObserver = activity.getPCObserver();
                if (pcObserver.getPC().getRemoteDescription() != null) {
                    pcObserver.drainRemoteCandidates();
                } else {
                    sendLocalDescription(pcObserver.getPC());
                }
            }
        });
    }

    @Override
    public void onCreateFailure(final String error) {
        new Thread(new Runnable() {
            public void run() {
                Log.e(TAG, "createSDP failed: " + error);
                activity.changeToErrorState("createSDP failed: " + error);
            }
        }).start();
    }

    @Override
    public void onSetFailure(final String error) {
        new Thread(new Runnable() {
            public void run() {
                Log.e(TAG, "setSDP failed: " + error);
                activity.changeToErrorState("setSDP failed: " + error);
            }
        }).start();
    }

    /** Returns the line number containing "m=audio|video", or -1 if no such line exists. */
    private static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        final String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length;  i++) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }

    private static String joinString(
            Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }

    private static String movePayloadTypesToFront(List<String> preferredPayloadTypes, String mLine) {
        // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
        final List<String> origLineParts = Arrays.asList(mLine.split(" "));
        if (origLineParts.size() <= 3) {
            Log.e(TAG, "Wrong SDP media description format: " + mLine);
            return null;
        }
        final List<String> header = origLineParts.subList(0, 3);
        final List<String> unpreferredPayloadTypes =
                new ArrayList<String>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
        // types.
        final List<String> newLineParts = new ArrayList<String>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, " ", false /* delimiterAtEnd */);
    }

    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        Log.d(TAG, "preferCodec: " + codec);
        //Log.d("preferCodec","sdpDescription:"+sdpDescription);

        final String[] lines = sdpDescription.split("\r\n");
        final int mLineIndex = findMediaDescriptionLine(isAudio, lines);
        Log.d(TAG, "mLineIndex:" + mLineIndex);
        if (mLineIndex == -1) {
            Log.w(TAG, "No mediaDescription line, so can't prefer " + codec);
            return sdpDescription;
        }

        Log.d(TAG,"mLineIndex:"+mLineIndex);

        // A list with all the payload types with name |codec|. The payload types are integers in the
        // range 96-127, but they are stored as strings here.
        final List<String> codecPayloadTypes = new ArrayList<String>();
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        final Pattern codecPattern = Pattern.compile("^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$");
        Log.d(TAG,"codecPattern:"+codecPattern);
        Log.d(TAG,"lines[i]:"+lines.length);
        for (String line : lines) {
            Matcher codecMatcher = codecPattern.matcher(line);
            //Log.d("YiWen","lines[i]:"+lines[i]);
            if (codecMatcher.matches()) {
                codecPayloadTypes.add(codecMatcher.group(1));
                Log.d("preferCodec", "codecMatcher");
            }
        }
        if (codecPayloadTypes.isEmpty()) {
            Log.w(TAG, "No payload types with name " + codec);
            return sdpDescription;
        }

        final String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
        if (newMLine == null) {
            return sdpDescription;
        }
        Log.d(TAG, "Change media description from: " + lines[mLineIndex] + " to " + newMLine);
        lines[mLineIndex] = newMLine;
        Log.d(TAG,"VALUE:"+joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */));

        return joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */);
    }
    public SessionDescription getSdp(){
        return sdp;
    }
}
