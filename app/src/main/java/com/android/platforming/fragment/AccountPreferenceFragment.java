package com.android.platforming.fragment;

import static com.android.platforming.clazz.FirestoreManager.getFirebaseAuth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.android.platforming.clazz.CustomDialog;
import com.android.platforming.clazz.User;
import com.example.platforming.R;
import com.google.firebase.auth.UserInfo;

public class AccountPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preference_account, rootKey);

        Preference uid = findPreference("uid");
        Preference email = findPreference("email");
        Preference changOfPassword = findPreference("changOfPassword");
        Preference signOut = findPreference("signOut");

        uid.setSummary(User.getUser().getUid());
        email.setSummary(User.getUser().getEmail());

        String provider = getFirebaseAuth().getCurrentUser().getProviderData().get(0).getProviderId();

        boolean isPassword = false;
        for(UserInfo data: getFirebaseAuth().getCurrentUser().getProviderData()){
            if(data.getProviderId().equals("password"))
                isPassword = true;
            Log.d("FirebaseAuth","provider: "+data.getProviderId());
        }

        if (isPassword) {
            changOfPassword.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    CustomDialog customDialog = new CustomDialog();
                    customDialog.verificationDialog(getActivity());
                    return true;
                }
            });
        } else {
            PreferenceScreen account = findPreference("account");
            account.removePreference(changOfPassword);
        }

        signOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                CustomDialog customDialog = new CustomDialog();
                customDialog.signOutDialog(getActivity());
                return true;
            }
        });
    }
}