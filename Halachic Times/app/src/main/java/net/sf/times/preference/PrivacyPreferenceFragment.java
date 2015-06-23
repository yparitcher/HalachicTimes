package net.sf.times.preference;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;

import net.sf.times.R;
import net.sf.times.ZmanimApplication;
import net.sf.times.location.AddressProvider;

/**
 * This fragment shows the preferences for the Privacy and Security header.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PrivacyPreferenceFragment extends AbstractPreferenceFragment {

    private Preference clearHistory;

    @Override
    protected int getPreferencesXml() {
        return R.xml.privacy_preferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clearHistory = findPreference("clear_history");
        clearHistory.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == clearHistory) {
            preference.setEnabled(false);
            deleteHistory();
            preference.setEnabled(true);
            return true;
        }
        return super.onPreferenceClick(preference);
    }

    /**
     * Clear the history of addresses.
     */
    private void deleteHistory() {
        ZmanimApplication app = (ZmanimApplication) getActivity().getApplication();
        AddressProvider provider = app.getAddresses();
        provider.deleteAddresses();
    }
}