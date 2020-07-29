package brahma.vmi.brahmalibrary.wcitui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.Objects;

import brahma.vmi.brahmalibrary.R;

import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.context;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.device_id;
import static brahma.vmi.brahmalibrary.wcitui.BrahmaMainActivity.myAppVerison;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    static SharedPreferences sharedPreferences;
    static String tempIP = "61.20.215.170";
    static String tempPort = "9768";

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            Log.d(TAG, "stringValue >>>>> " + stringValue);
            Log.d(TAG, "OnPreferenceChangeListener >>>>> " + preference);

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                if (index == -1) {
                    sharedPreferences.edit().putString("resolution_ratio", "0.5").apply();
                } else {
                    sharedPreferences.edit().putString("resolution_ratio", stringValue).apply();
                }
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : listPreference.getEntries()[1]);

            } else if (preference instanceof SwitchPreference) {
                if (stringValue.equals("true")) {
                    sharedPreferences.edit().putString("setting_bandwidth", "true").apply();
                } else {
                    sharedPreferences.edit().putString("setting_bandwidth", "false").apply();
                }
            } else if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("Host")) {
//                    if (stringValue.trim() == "") {
//                        preference.setSummary(tempIP);
//                        sharedPreferences.edit().putString("normal_IP", tempIP).apply();
//                    } else {
                    preference.setSummary(stringValue);
                    sharedPreferences.edit().putString("normal_IP", stringValue).apply();
//                    }
                } else if (preference.getKey().equals("Port")) {
//                    if (stringValue.trim() == "") {
//                        preference.setSummary(tempPort);
//                        sharedPreferences.edit().putString("normal_Port", tempPort).apply();
//                    } else {
                    preference.setSummary(stringValue);
                    sharedPreferences.edit().putString("normal_Port", stringValue).apply();
//                    }
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        if (preference instanceof SwitchPreference) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false));
        } else {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MainPreferenceFragment())
                .commit();

//        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.pref_main, rootKey);

            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_resolution)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_bandwidth)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_host)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_port)));

            Preference preference = (Preference) findPreference(getString(R.string.title_version));
            preference.setSummary(myAppVerison);

            Preference preference2 = (Preference) findPreference(getString(R.string.key_deviceID));
            preference2.setSummary(device_id);/**/
            preference2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ClipboardManager cmb = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                    assert cmb != null;
                    cmb.setPrimaryClip(ClipData.newPlainText(null, preference.getSummary()));
                    if (cmb.hasPrimaryClip()) {
//                        Objects.requireNonNull(cmb.getPrimaryClip()).getItemAt(0).getText();
                        Toast.makeText(context, getResources().getString(R.string.copy_success), Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
        }
    }
}