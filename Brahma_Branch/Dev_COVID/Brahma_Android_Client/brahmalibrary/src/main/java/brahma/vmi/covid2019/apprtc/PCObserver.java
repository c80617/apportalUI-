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
package brahma.vmi.covid2019.apprtc;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import brahma.vmi.covid2019.R;

//import org.webrtc.Logging;

//Implementation detail: observe ICE & stream changes and react accordingly.
public class PCObserver implements PeerConnection.Observer {
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";
    static final String TAG = "PCObserver";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    CallActivity activity;
    PeerConnection pc;
    LinkedList<IceCandidate> queuedRemoteCandidates;
    PeerConnectionFactory factory;
    boolean quit;
    private AudioTrack localAudioTrack;
    private List<VideoRenderer.Callbacks> remoteRenders;
    private VideoTrack remoteVideoTrack;
    private AudioTrack remoteAudioTrack;
    private PeerConnectionEvents events;
    private MediaStream mediaStream;
    private AudioSource audioSource;
    private MediaConstraints audioConstraints;
    // enableAudio is set to true if audio should be sent.
    private boolean enableAudio = true;
    private EglBase rootEglBase;

    public PCObserver(CallActivity activity) {
        this.activity = activity;
        queuedRemoteCandidates = new LinkedList<IceCandidate>();
        quit = false;
        remoteVideoTrack = null;
        events = activity;
    }

    // Just for fun (and to regression-test bug 2302) make sure that DataChannels
    // can be created, queried, and disposed.
//    public static void createDataChannelToRegressionTestBug2302(PeerConnection pc) {
//        DataChannel dc = pc.createDataChannel("dcLabel", new DataChannel.Init());
//        AppRTCHelper.abortUnless("dcLabel".equals(dc.label()), "Unexpected label corruption?");
//        dc.close();
//        dc.dispose();
//    }

    public PeerConnection getPC() {
        return pc;
    }

    public void addIceCandidate(IceCandidate candidate) {
        if (queuedRemoteCandidates != null)
            queuedRemoteCandidates.add(candidate);
        else
            pc.addIceCandidate(candidate);
    }

    public void onIceServers(List<PeerConnection.IceServer> iceServers) {
        //WebRTC63
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.networkIgnoreMask = 0;
        factory = new PeerConnectionFactory(options);

        MediaConstraints pcConstraints = activity.getPCConstraints();
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
        //The constraints that I specify
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googCpuOveruseDetection", "false"));
        //pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("iceRestart", "true"));

        // Create peer connection constraints.
        pcConstraints = new MediaConstraints();

        // Enable DTLS for normal calls and disable for loopback calls.
//        pcConstraints.optional.add(new MediaConstraints.KeyValuePair(DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "false"));

//        rootEglBase = EglBase.create();
//        factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());

        //add RTC config
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        pc = factory.createPeerConnection(iceServers, pcConstraints, this);
        // Create audio constraints.
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(AUDIO_LEVEL_CONTROL_CONSTRAINT, "true"));

        //add local mediastream
        mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(createAudioTrack());
        pc.addStream(mediaStream);

        // Set default WebRTC tracing and INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
//        Logging.enableTracing("logcat:", EnumSet.of(Logging.TraceLevel.TRACE_DEFAULT));
//        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO);

//        createDataChannelToRegressionTestBug2302(pc);

        final PeerConnection finalPC = pc;
        final Runnable repeatedStatsLogger = new Runnable() {
            public void run() {
                if (quit)
                    return;

                final Runnable runnableThis = this;
                boolean success = finalPC.getStats(new StatsObserver() {
                    public void onComplete(StatsReport[] reports) {
                        for (StatsReport report : reports)
                            Log.d(TAG, "Stats: " + report.toString());
                        activity.getSVR().postDelayed(runnableThis, 10000);
                    }
                }, null);
                if (!success) {
                    throw new RuntimeException("getStats() return false!");
                }
            }
        };
        activity.getSVR().postDelayed(repeatedStatsLogger, 10000);
        activity.logAndToast(R.string.appRTC_toast_getIceServers_start);
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        if (!quit) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    JSONObject json = new JSONObject();
                    AppRTCHelper.jsonPut(json, "type", "candidate");
                    AppRTCHelper.jsonPut(json, "label", candidate.sdpMLineIndex);
                    AppRTCHelper.jsonPut(json, "id", candidate.sdpMid);
                    AppRTCHelper.jsonPut(json, "candidate", candidate.sdp);

                    activity.sendMessage(AppRTCHelper.makeWebRTCRequest(json));
                }
            });
        }
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

    }

    public void drainRemoteCandidates() {
        if (queuedRemoteCandidates != null)
            for (IceCandidate candidate : queuedRemoteCandidates)
                pc.addIceCandidate(candidate);
        queuedRemoteCandidates = null;
    }

    //WebRTC 48
    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
    }//end

    @Override
    public void onSignalingChange(PeerConnection.SignalingState newState) {
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "IceConnectionState: " + newState);
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    events.onIceConnected();
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    events.onIceDisconnected();
                } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                    //reportError("ICE connection failed.");
                }
            }
        });
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
    }

    @Override
    public void onAddStream(final MediaStream stream) {//取得視訊與音訊的串流
//        new Thread(new Runnable() {
//            public void run() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                AppRTCHelper.abortUnless(//stream.audioTracks.size() == 1 &&
                        stream.videoTracks.size() <= 1,
                        "Weird-looking stream: " + stream);
                if (stream.audioTracks.size() == 1) {
                    boolean i = stream.audioTracks.get(0).enabled();
                    remoteAudioTrack = stream.audioTracks.get(0);
                    activity.setMediaStream(stream);
                }
                if (stream.videoTracks.size() == 1) {
                    remoteVideoTrack = stream.videoTracks.get(0);
                    remoteVideoTrack.setEnabled(true);
                    for (VideoRenderer.Callbacks remoteRender : remoteRenders) {
                        remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
                    }
                }
                activity.stopProgressDialog(); // stop the Progress Dialog
                activity.getSVR().setBackgroundColor(Color.TRANSPARENT); // video should be started now, remove the background color
            }
        });
//            }
//        }).start();
    }

    @Override
    public void onRemoveStream(final MediaStream stream) {
        new Thread(new Runnable() {
            public void run() {
                stream.videoTracks.get(0).dispose();
            }
        }).start();
    }

    @Override
    public void onDataChannel(final DataChannel dc) {
        new Thread(new Runnable() {
            public void run() {
                throw new RuntimeException(
                        "AppRTC doesn't use data channels, but got: " + dc.label() +
                                " anyway!");
            }
        }).start();
    }

    @Override
    public void onRenegotiationNeeded() {
        // No need to do anything; AppRTC follows a pre-agreed-upon
        // signaling/negotiation protocol.
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

    }

    public void quit() {
        quit = true;
        if (pc != null) {
            pc.dispose();
            pc = null;
        }
        if (factory != null) {
            factory.dispose();
            factory = null;

        }
//        if(rootEglBase != null){
//            rootEglBase.release();
//            rootEglBase = null;
//        }

        //WebRTC63
        PeerConnectionFactory.shutdownInternalTracer();
    }

    public void addRender(List<VideoRenderer.Callbacks> remoteRenderers) {
        this.remoteRenders = remoteRenderers;
    }

    private AudioTrack createAudioTrack() {
        Log.d(TAG, "AudioTrack createAudioTrack");
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(enableAudio);
        return localAudioTrack;
    }

    public interface PeerConnectionEvents {
        /**
         * Callback fired once local SDP is created and set.
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is closed (IceConnectionState is
         * DISCONNECTED).
         */
        void onIceDisconnected();

        /**
         * Callback fired once peer connection is closed.
         */
        void onPeerConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);
    }
}
