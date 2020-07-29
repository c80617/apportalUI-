/*
* Copyright (c) 2013 The MITRE Corporation, All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this work except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package brahma.vmi.brahmalibrary.client;

import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;

import org.webrtc.MediaStream;

import brahma.vmi.brahmalibrary.activities.AppRTCActivity;
import brahma.vmi.brahmalibrary.common.Constants;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol;
import brahma.vmi.brahmalibrary.protocol.BRAHMAProtocol.Request;

import static android.media.AudioManager.FLAG_PLAY_SOUND;
import static android.media.AudioManager.FLAG_SHOW_UI;
import static brahma.vmi.brahmalibrary.apprtc.CallActivity.getMediaStream;

//import static brahma.vmi.brahmalibrary.activities.AppRTCVideoActivity.getMediaStream;

/**
 * @developer Ian
 * Captures key events to send to the remote Brahma instance.
 */
public class KeyHandler implements Constants {
    private AppRTCActivity activity;
    private String TAG = "KeyHandler";
    private AudioManager audioMgr;
    private int stepVolume = 1, maxVolume;
    private MediaStream ms = null;
    private int CurrentVolume = -1;
    private boolean notMute = false;
    private float volumeRatio = 0;
    public static boolean isKeyEvent = false;

    public KeyHandler(AppRTCActivity activity, AudioManager audioMgr) {
        this.activity = activity;
        this.audioMgr = audioMgr;
    }

    public boolean tryConsume(KeyEvent event) {
        //Log.d(TAG,"tryConsume event.getAction:"+event.getAction());
        // whenever any key is pressed, catch the event and track it
        // note: can't catch Home, Search, and App Switch keys within an app
        Log.d(TAG,"KeyCode = " + event.getKeyCode());
        if (activity.isConnected()) {
            Request request = makeRequest(event);
            activity.sendMessage(request);
            return true; // consume the event
        }
        return false; // don't consume the event, pass it onto other handler(s)
    }


    // transforms KeyEvent into a Request
    private Request makeRequest(KeyEvent event) {
        BRAHMAProtocol.KeyEvent.Builder kBuilder = BRAHMAProtocol.KeyEvent.newBuilder();
        kBuilder.setEventTime(event.getEventTime());
        kBuilder.setDeviceId(event.getDeviceId());
        kBuilder.setFlags(event.getFlags());
        //Log.d(TAG,"makeRequest event.getKeyCode= "+event.getKeyCode());

        ms = getMediaStream();
        maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        int musicVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeRatio = (float)musicVolume / (float)maxVolume;

        Log.d(TAG,"voice call volume:" + maxVolume);
        Log.d(TAG,"music volume:" + musicVolume);
        Log.d(TAG,"volumeRatio:" + volumeRatio);

        if (event.getAction() == KeyEvent.ACTION_MULTIPLE && event.getKeyCode() == KeyEvent.KEYCODE_UNKNOWN) {
            // this attribute is used for the special case of a ACTION_MULTIPLE event with key code of KEYCODE_UNKNOWN
            kBuilder.setCharacters(event.getCharacters());
        }
        else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP){
            isKeyEvent = true;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    Log.d(TAG,"KEYCODE_VOLUME_UP");
                    Log.d(TAG,"getMediaVolume():"+getMediaVolume());
                    addMediaVolume(getMediaVolume());
                    // audioMgr.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    Log.d(TAG,"KEYCODE_VOLUME_DOWN");
                    Log.d(TAG,"getMediaVolume():"+getMediaVolume());
                    Log.d(TAG,"getMediaMusicVolume():"+getMediaMusicVolume());
                    if(getMediaMusicVolume() == 0){
                        cutMediaVolume(1);
                    }else{
                        cutMediaVolume(getMediaVolume());
                    }
                    // audioMgr.adjustVolume(AudioManager.ADJUST_LOWER, 0);
                    break;
                default:
                    break;
            }
        }
        else {
            // the following attributes are used whenever action is not ACTION_MULTIPLE, OR key code is not KEYCODE_UNKNOWN
            kBuilder.setDownTime(event.getDownTime());
            kBuilder.setAction(event.getAction());
            kBuilder.setCode(event.getKeyCode());
            kBuilder.setRepeat(event.getRepeatCount());
            kBuilder.setMetaState(event.getMetaState());
            kBuilder.setScanCode(event.getScanCode());
            kBuilder.setSource(event.getSource());
            //Log.d(TAG,"event.getAction():"+event.toString());
        }

        // wrap the KeyEvent in a Request and return it
        Request.Builder rBuilder = Request.newBuilder();
        rBuilder.setType(Request.RequestType.KEYEVENT);
        rBuilder.setKey(kBuilder);
        return rBuilder.build();
    }

    private int getMediaVolume(){
        return audioMgr.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
    }
    private int getMediaMusicVolume(){
        return audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    private void setMediaVolume(int volume) {
        Log.d(TAG,"set voice call Volume:"+volume);
        Log.d(TAG,"set Music Volume:"+volume*volumeRatio);
        int fakeVolume = (int)(volume*volumeRatio);
        if(volume <= 0){//靜音
            if (ms.audioTracks.size() == 1){
                ms.audioTracks.get(0).setEnabled(false);
                notMute = ms.audioTracks.get(0).enabled();
                //for show
                audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, fakeVolume, FLAG_SHOW_UI);
                audioMgr.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, FLAG_PLAY_SOUND);
                Log.d(TAG,"MUTE,audioTracks notMute:" + notMute + "\nCurrent volume:"+audioMgr.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
                CurrentVolume = 0;
                //handler.sendEmptyMessage(0);
            }
        }else{//調整音量
            audioMgr.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, FLAG_PLAY_SOUND);
            audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, fakeVolume, FLAG_SHOW_UI);
            ms.audioTracks.get(0).setEnabled(true);
            notMute = ms.audioTracks.get(0).enabled();
            Log.d(TAG,"NOT MUTE,audioTracks notMute:"+ notMute +"\nCurrent volume:"+audioMgr.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
            CurrentVolume = volume;
        }
    }


    public void setMediaVolumeOppsite(int volume) {

        float oppsiteRatio = 1/volumeRatio;
        int orignVolume = (int)(volume*oppsiteRatio);
        Log.d("isKeyEvent","fakeVolume:"+ volume);
        Log.d("isKeyEvent","orignalVolume:"+orignVolume);
        Log.d("isKeyEvent","oppsiteRatio:"+oppsiteRatio);

        if(orignVolume <= 0){//靜音
            if (ms.audioTracks.size() == 1){
                ms.audioTracks.get(0).setEnabled(false);
                notMute = ms.audioTracks.get(0).enabled();
                //for show
                audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, volume, FLAG_SHOW_UI);
                audioMgr.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, FLAG_PLAY_SOUND);
                Log.d("isKeyEvent","MUTE,audioTracks notMute:" + notMute + "\nCurrent volume:"+audioMgr.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
                CurrentVolume = 0;
                //handler.sendEmptyMessage(0);
            }
        }else{//調整音量
            audioMgr.setStreamVolume(AudioManager.STREAM_VOICE_CALL, orignVolume, FLAG_PLAY_SOUND);
            audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, volume, FLAG_SHOW_UI);
            ms.audioTracks.get(0).setEnabled(true);
            notMute = ms.audioTracks.get(0).enabled();
            Log.d("isKeyEvent","NOT MUTE,audioTracks notMute:"+ notMute +"\nCurrent volume:"+audioMgr.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
            CurrentVolume = orignVolume;
        }
        //isKeyEvent = true;
    }

    private void addMediaVolume(int current) {
        //提高音量
        if(CurrentVolume == 0){
            current = CurrentVolume + stepVolume;
            setMediaVolume(current);
        }else{
            current = current + stepVolume;
            if(current >= maxVolume)
                current = maxVolume;
            setMediaVolume(current);
            //volumeSeekBar.setProgress(current);
        }
    }

    private void cutMediaVolume(int current){
        //降低音量
        current = current - stepVolume;
        if (current <= 0)
            current = 0;
        setMediaVolume(current);
        //volumeSeekBar.setProgress(current);
    }
}