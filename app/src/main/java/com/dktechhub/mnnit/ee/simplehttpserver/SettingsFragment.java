package com.dktechhub.mnnit.ee.simplehttpserver;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.preference.SwitchPreferenceCompat;

import java.io.File;

public class SettingsFragment extends PreferenceFragmentCompat {
public SettingsFragment()
{

}
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        SwitchPreferenceCompat dark= findPreference("dark_theme");
        dark.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().recreate();
                return true;
            }
        });

        EditTextPreference documentRoot= findPreference("document_root");
        documentRoot.setDefaultValue(Environment.getExternalStorageDirectory().getAbsolutePath());
        documentRoot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                
                    File f = new File((String) newValue);
                    if(f.exists()&&f.isDirectory()&&f.canRead())
                        return true;

                Toast.makeText(getContext(), "Directory not readable/not found", Toast.LENGTH_SHORT).show();
                    return false;
                    
            }
        });

        EditTextPreference port= findPreference("port");
port.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
    @Override
    public void onBindEditText(@NonNull EditText editText) {
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        
    }
});
port.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try{
            int port =Integer.parseInt((String) newValue);

            if(port>1024&&port<65536)
                return true;
            else Toast.makeText(getContext(), "port must be between 1024 and 65535", Toast.LENGTH_SHORT).show();
        }catch (Exception e)
        {
            Toast.makeText(getContext(), "port must be between 1024 and 65535", Toast.LENGTH_SHORT).show();
        }


        return false;
    }
});
    }
   
}