package brahma.vmi.covid2019.wcitui;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import brahma.vmi.covid2019.R;

import static brahma.vmi.covid2019.wcitui.BrahmaMainActivity.myAppVerison;

public class SettingActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingActivity.class.getSimpleName();
    static SharedPreferences sharedPreferences;
    /**
     * static
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            Log.d("setPerference", "stringValue >>>>> " + stringValue);

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                sharedPreferences.edit().putString("resolution_ratio", stringValue).apply();
                double resolution_ratio = 0.0;
                if (sharedPreferences.getString("resolution_ratio", "0.5") != "") {
                    resolution_ratio = Double.parseDouble(sharedPreferences.getString("resolution_ratio", "0.5"));
                }
                Log.d("setPerference", "resolution_ratio:" + resolution_ratio);
                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
//            preference.setSummary(R.string.app_version);
                Log.d("setPerference", "");

            } else if (preference instanceof SwitchPreference) {
                if (stringValue.equals("true")) {
                    sharedPreferences.edit().putString("setting_bandwidth", "true").apply();
                } else {
                    sharedPreferences.edit().putString("setting_bandwidth", "false").apply();
                }
                Log.d(TAG, "SwitchPreference >>>>> " + sharedPreferences.getString("setting_bandwidth", "false"));
            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));
                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(R.string.summary_choose_ringtone);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("key_gallery_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
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
        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            // gallery ListPerference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_resolution)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_bandwidth)));
            Preference preference = (Preference) findPreference(getString(R.string.title_version));
            preference.setSummary(myAppVerison);

        }
    }
}