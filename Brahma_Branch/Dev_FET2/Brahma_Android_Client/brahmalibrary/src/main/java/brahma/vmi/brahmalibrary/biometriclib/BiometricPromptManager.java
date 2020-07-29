//package brahma.vmi.brahmalibrary.biometriclib;
//
//import android.app.Activity;
//import android.app.KeyguardManager;
//import android.content.Context;
//import android.hardware.biometrics.BiometricManager;
//import android.os.Build;
//import android.os.CancellationSignal;
//import androidx.annotation.NonNull;
//import android.util.Log;
//
//import moe.feng.support.biometricprompt.BiometricPromptCompat;
//
//public class BiometricPromptManager {
//
//    private static final String TAG = "BiometricPromptManager";
//    private IBiometricPromptImpl mImpl;
//    private Activity mActivity;
//    BiometricManager biometricManager;
//    public BiometricPromptManager(Activity activity) {
//        mActivity = activity;
//        if (isAboveApi29()) {
//            biometricManager = mActivity.getSystemService(BiometricManager.class);
//        }
//        if (isAboveApi28() && isBiometricPromptEnable()) {
//            mImpl = new BiometricPromptApi28(activity);
//        } else if (isAboveApi23()) {
//            mImpl = new BiometricPromptApi23(activity);
//        } else {
//            Log.d("isUsingBio", "Can't use bio.");
//        }
//    }
//
//    public static BiometricPromptManager from(Activity activity) {
//        return new BiometricPromptManager(activity);
//    }
//    private boolean isAboveApi29() {
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
//    }
//    private boolean isAboveApi28() {
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
//    }
//    private boolean isAboveApi23() {
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
//    }
//
//    public void authenticate(@NonNull OnBiometricIdentifyCallback callback) {
//        mImpl.authenticate(new CancellationSignal(), callback);
//    }
//
//    public void authenticate(@NonNull CancellationSignal cancel,
//                             @NonNull OnBiometricIdentifyCallback callback) {
//        mImpl.authenticate(cancel, callback);
//    }
//
//    /**
//     * Determine if there is at least one fingerprint enrolled.
//     *
//     * @return true if at least one fingerprint is enrolled, false otherwise
//     */
//    public boolean hasEnrolledFingerprints() {
//        if(isAboveApi29()){
//            if(biometricManager.canAuthenticate() == 0)
//                return true;
//            else
//                return false;
//        }else if (isAboveApi28()) {
//            return BiometricPromptCompat.hasEnrolledFingerprints(mActivity);
//        } else if (isAboveApi23()) {
//            return ((BiometricPromptApi23) mImpl).hasEnrolledFingerprints();
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * Determine if fingerprint hardware is present and functional.
//     *
//     * @return true if hardware is present and functional, false otherwise.
//     */
//    public boolean isHardwareDetected() {
//        if(isAboveApi29()){
//            if(biometricManager.canAuthenticate() == 0)
//                return true;
//            else
//                return false;
//        }else if (isAboveApi28()) {
//            BiometricPromptCompat.isHardwareDetected(mActivity);
//            return BiometricPromptCompat.isHardwareDetected(mActivity);
//        } else if (isAboveApi23()) {
//            return ((BiometricPromptApi23) mImpl).isHardwareDetected();
//        } else {
//            return false;
//        }
//    }
//
//    public boolean isKeyguardSecure() {
//        KeyguardManager keyguardManager = (KeyguardManager) mActivity.getSystemService(Context.KEYGUARD_SERVICE);
//        if (keyguardManager.isKeyguardSecure()) {
//            return true;
//        }
//
//        return false;
//    }
//
//    /**
//     * Whether the device support biometric.
//     *
//     * @return
//     */
//    public boolean isBiometricPromptEnable() {
//        Log.d("isUsingBio", "isAboveApi23 >>>>> " + isAboveApi23());
//        Log.d("isUsingBio", "isHardwareDetected >>>>> " + isHardwareDetected());
//        Log.d("isUsingBio", "hasEnrolledFingerprints >>>>> " + hasEnrolledFingerprints());
//        Log.d("isUsingBio", "isKeyguardSecure >>>>> " + isKeyguardSecure());
//        return isAboveApi23()
//                && isHardwareDetected()
//                && hasEnrolledFingerprints()
//                && isKeyguardSecure();
//    }
//
//    /**
//     * Whether fingerprint identification is turned on in app setting.
//     *
//     * @return
//     */
//    public boolean isBiometricSettingEnable() {
//        return SPUtils.getBoolean(mActivity, SPUtils.KEY_BIOMETRIC_SWITCH_ENABLE, false);
//    }
//
//    /**
//     * Set fingerprint identification enable in app setting.
//     *
//     * @return
//     */
//    public void setBiometricSettingEnable(boolean enable) {
//        SPUtils.put(mActivity, SPUtils.KEY_BIOMETRIC_SWITCH_ENABLE, enable);
//    }
//
//    public interface OnBiometricIdentifyCallback {
//        void onUsePassword();
//        void onSucceeded();
//        void onFailed();
//        void onError(int code, String reason);
//        void onCancel();
//
//    }
//}
