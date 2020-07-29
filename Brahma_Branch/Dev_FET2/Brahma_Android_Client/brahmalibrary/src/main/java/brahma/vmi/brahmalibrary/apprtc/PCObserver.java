package brahma.vmi.brahmalibrary.apprtc;

import android.graphics.Color;
import android.util.Log;

import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import brahma.vmi.brahmalibrary.R;

public class PCObserver implements PeerConnection.Observer {
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";
    private static final String AUDIO_TRACK_ID = "ARDAMSa0";
    private static final String TAG = "PCObserver";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final String EXTRA_VIDEO_FILE_AS_CAMERA = "public static final String EXTRA_VIDEO_FILE_AS_CAMERA = \"org.appspot.apprtc.VIDEO_FILE_AS_CAMERA\";";
    private CallActivity activity;
    private PeerConnection pc;
    private LinkedList<IceCandidate> queuedRemoteCandidates;
    private PeerConnectionFactory factory;
    private boolean quit;
    private List<VideoRenderer.Callbacks> remoteRenders;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    private AudioTrack remoteAudioTrack;
    private PeerConnectionEvents events;
    private MediaConstraints audioConstraints;
    private EglBase rootEglBase;
    private boolean renderVideo = true;
    private VideoSource videoSource;

    public PCObserver(CallActivity activity) {
        Log.d("yoyoyo", "PCObserver constrator");
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
    private VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
        String videoFileAsCamera = activity.getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                Log.d(TAG, "Failed to open video file for emulated camera");
                return null;
            }
//        } else if (screencaptureEnabled) {
//            return createScreenCapturer();
        } else if (true) {
//            if (!captureToTexture()) {
//                reportError(getString(R.string.camera2_texture_only_error));
//                return null;
//            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(activity));
        }
//        else {
//            Logging.d(TAG, "Creating capturer using camera1 API.");
//            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
//        }
        if (videoCapturer == null) {
            Log.d(TAG, "Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    PeerConnection getPC() {
        return pc;
    }

    public void setPC(PeerConnection peerConnection) {
        pc = peerConnection;
    }

    void addIceCandidate(IceCandidate candidate) {
        if (queuedRemoteCandidates != null)
            queuedRemoteCandidates.add(candidate);
        else
            pc.addIceCandidate(candidate);
    }

    void onIceServers(List<PeerConnection.IceServer> iceServers) {
        //WebRTC63
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.networkIgnoreMask = 0;
        factory = new PeerConnectionFactory(options);
        MediaConstraints pcConstraints = null;
        if (activity.getPCConstraints() != null) {
            pcConstraints = activity.getPCConstraints();
            pcConstraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels", "true"));
            //The constraints that I specify
            pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
            pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
            pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googCpuOveruseDetection", "false"));
            //pcConstraints.mandatory.add(new MediaConstraints.KeyValuePair("iceRestart", "true"));
        }
//        pcConstraints = new MediaConstraints();
        // Create peer connection constraints.

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
//        pc = factory.createPeerConnection(iceServers,this);
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
        MediaStream mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream.addTrack(createAudioTrack());
        if (pc != null)
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
        activity.connectionTimeout();
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

    void drainRemoteCandidates() {
        if (queuedRemoteCandidates != null)
            for (IceCandidate candidate : queuedRemoteCandidates)
                pc.addIceCandidate(candidate);
        queuedRemoteCandidates = null;
    }

    @Override
    public void onIceConnectionReceivingChange(boolean receiving) {
    }

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
                    remoteVideoTrack.setEnabled(false);
//                    remoteVideoTrack.setEnabled(true);
                    for (VideoRenderer.Callbacks remoteRender : remoteRenders) {
                        remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
                    }
                }
                activity.stopProgressDialog(); // stop the Progress Dialog
                activity.getSVR().setBackgroundColor(Color.TRANSPARENT); // video should be started now, remove the background color
                activity.stopTimeoutTimer();
            }
        });
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

    void quit() {
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

    void addRender(List<VideoRenderer.Callbacks> remoteRenderers) {
        this.remoteRenders = remoteRenderers;
    }


//    private VideoTrack createVideoTrack(VideoCapturer capturer) {
//        Log.d("yoyoyo","createVideoTrack");
//        videoSource = factory.createVideoSource(capturer);
//        capturer.startCapture(widthPixels_org, heightPixels_org, 30);
//        Log.d("yoyoyo","capturer startCapture");
//        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
//        Log.d("yoyoyo","factory createVideoTrack");
//        localVideoTrack.setEnabled(renderVideo);
//        Log.d("yoyoyo","setEnabled");
//        localVideoTrack.addSink(localRender);
//        Log.d("yoyoyo","addSink");
//        return localVideoTrack;
//    }

    private AudioTrack createAudioTrack() {
        Log.d(TAG, "AudioTrack createAudioTrack");
        AudioSource audioSource = factory.createAudioSource(audioConstraints);
        AudioTrack localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        // enableAudio is set to true if audio should be sent.
        boolean enableAudio = true;
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
