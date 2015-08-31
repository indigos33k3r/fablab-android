package de.fau.cs.mad.fablab.android.view.fragments.settings;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import javax.inject.Inject;

import de.fau.cs.mad.fablab.android.BuildConfig;
import de.fau.cs.mad.fablab.android.model.AutoCompleteModel;
import de.fau.cs.mad.fablab.android.model.PushModel;
import de.fau.cs.mad.fablab.android.model.SpaceApiModel;
import de.fau.cs.mad.fablab.android.model.VersionCheckModel;

public class SettingsFragmentViewModel implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private static final String KEY_PREF_ENABLE_PUSH = "enable_push";
    private static final String KEY_PREF_POLLING_FREQ = "spaceapi_polling_freq";
    private static final String KEY_PREF_FORCE_RELOAD_AUTOCOMPLETION = "force_reload_autocompletion";
    private static final String KEY_PREF_CHECK_FOR_UPDATES = "check_for_updates";

    @Inject
    SpaceApiModel mSpaceApiModel;
    @Inject
    PushModel mPushModel;
    @Inject
    AutoCompleteModel mAutoCompleteModel;
    @Inject
    VersionCheckModel mVersionCheckModel;

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_PREF_FORCE_RELOAD_AUTOCOMPLETION:
                mAutoCompleteModel.forceReloadProductNames();
                break;
            case KEY_PREF_CHECK_FOR_UPDATES:
                mVersionCheckModel.checkVersion();
                break;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case KEY_PREF_ENABLE_PUSH:
                if (sharedPreferences.getBoolean(key, false)) {
                    mPushModel.registerDeviceToGcm();
                } else {
                    mPushModel.unregisterDeviceFromGcm();
                }
                break;
            case KEY_PREF_POLLING_FREQ:
                long pollingFrequency = Integer.parseInt(sharedPreferences.getString(
                        key, "15")) * 60 * 1000;
                mSpaceApiModel.setPollingFrequency(pollingFrequency);
                break;
        }
    }

    public void initialize(PreferenceScreen preferenceScreen, SharedPreferences sharedPreferences) {
        if (BuildConfig.FLAVOR.equals("fdroid")) {
            Preference preference = preferenceScreen.findPreference(KEY_PREF_ENABLE_PUSH);
            if (preference != null) {
                preferenceScreen.removePreference(preference);
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        preferenceScreen.findPreference(KEY_PREF_FORCE_RELOAD_AUTOCOMPLETION)
                .setOnPreferenceClickListener(this);
        preferenceScreen.findPreference(KEY_PREF_CHECK_FOR_UPDATES)
                .setOnPreferenceClickListener(this);
    }
}
