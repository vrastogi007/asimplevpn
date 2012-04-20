package com.cliff.android.net.vpn;

import java.util.UUID;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.cliff.android.net.vpn.SimpleProfile.PROFILE_TYPE;
import com.cliff.net.vpn.R;

public class AddProfileActivity extends PreferenceActivity implements
		OnClickListener, OnSharedPreferenceChangeListener {

	private final String TAG = "AddProfileActivity";
	
	public final String KEY_PROFILEID = "PROFILEID";
	public final String KEY_PROFILENAME = "PROFILENAME";
	public final String KEY_PROFILETYPE = "PROFILETYPE";
	public final String KEY_SERVERNAME = "SERVERNAME";
	public final String KEY_USERNAME = "USERNAME";
	public final String KEY_PASSWORD = "PASSWORD";
	public final String KEY_ISPPTPENCRYPT = "ISPPTPENCRYPT";
	public final String KEY_ISL2TPSECRETSTR = "ISL2TPSECRETSTR";
	public final String KEY_L2TPSECRETSTR = "L2TPSECRETSTR";
	public final String KEY_ROUTELIST = "ROUTELIST";
	public final String KEY_DOMAINSUFFIX = "DOMAINSUFFIX";

	public final String VALUE_PPTP = "PPTP";
	public final String VALUE_L2TP = "L2TP";

	public final static String PARAM_PROFILE = "PARAM_PROFILE";
	public final static String RESULT_PROFILE = "RESULT";
	//public final String PROFILE_ID = "PROFILE_ID";
	
	protected SimpleProfile mParamProfile = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.addprofile);
		((Button) findViewById(R.id.buttonAddProfileOK))
				.setOnClickListener(this);
		((Button) findViewById(R.id.buttonAddProfileCancel))
				.setOnClickListener(this);
		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);

		mParamProfile = (SimpleProfile)getIntent().getSerializableExtra(PARAM_PROFILE);
		initData(mParamProfile);
		Log.d(TAG, "onCreate");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onResume");
		super.onResume();
	}

	protected void clearData() {
		PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
	}
	
	protected void initData(SimpleProfile prof) {

		if (null == prof) {
			// new profile
			clearData();
			addPreferencesFromResource(R.xml.addprofile);
			ListPreference profileTypeList = (ListPreference)findPreference(KEY_PROFILETYPE);
			profileTypeList.setValueIndex(0);			
			profileTypeList.setSummary(profileTypeList.getValue());			
			findPreference(KEY_ISPPTPENCRYPT).setEnabled(true);
			findPreference(KEY_ISL2TPSECRETSTR).setEnabled(false);						

		} else {
			addPreferencesFromResource(R.xml.addprofile);
			//transProfileToPreference(prof);
			// edit existed profile
			String needSummaries[] = {KEY_PROFILENAME, KEY_SERVERNAME, KEY_USERNAME, KEY_ROUTELIST, KEY_DOMAINSUFFIX};
			String needSummariesValue[] = {prof.getName(), prof.getServerName(), prof.getUserName(), prof.getRouteList(), prof.getDomainSuffices()};
			String needHiddenSummaries[] = {KEY_PASSWORD, KEY_L2TPSECRETSTR};
			String needHiddenSummariesValue[] = {prof.getPassword(), prof.getL2tpSecretString()};
			
			for (int i = 0; i < needSummaries.length; i++) {
				EditTextPreference pref = (EditTextPreference)findPreference(needSummaries[i]);
				pref.setText(needSummariesValue[i]);
				pref.setSummary(needSummariesValue[i]);
			}

			for (int i = 0; i < needHiddenSummaries.length; i++) {
				EditTextPreference pref = (EditTextPreference)findPreference(needHiddenSummaries[i]);
				pref.setText(needHiddenSummariesValue[i]);
				pref.setSummary(needHiddenSummariesValue[i].replaceAll(".", "*"));
			}
			
			ListPreference profileTypeList = (ListPreference)findPreference(KEY_PROFILETYPE);
			profileTypeList.setValueIndex(prof.type.ordinal());			
			profileTypeList.setSummary(profileTypeList.getValue());
			
			if (VALUE_PPTP.equals(profileTypeList.getValue())){
				findPreference(KEY_ISPPTPENCRYPT).setEnabled(true);
				findPreference(KEY_ISL2TPSECRETSTR).setEnabled(false);						
			} else if (VALUE_L2TP.equals(profileTypeList.getValue())) {
				findPreference(KEY_ISPPTPENCRYPT).setEnabled(false);
				findPreference(KEY_ISL2TPSECRETSTR).setEnabled(true);									
			}
		}
		

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (R.id.buttonAddProfileOK == v.getId()) {

			// if
			// (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("checkboxtest0",
			// false))
			//String needSummaries[] = {KEY_PROFILENAME, KEY_SERVERNAME, KEY_USERNAME, KEY_ROUTELIST, KEY_DOMAINSUFFIX};
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			if (null == prefs.getString(KEY_PROFILENAME, null)) {
				Toast.makeText(this, "Empty profile name", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (null == prefs.getString(KEY_SERVERNAME, null)) {
				Toast.makeText(this, "Empty server name", Toast.LENGTH_SHORT).show();
				return;
			}

			SimpleProfile prof = new SimpleProfile();
			String strId = null;
			if (null == mParamProfile)
				strId = UUID.randomUUID().toString();
			else
				strId = mParamProfile.getId();
			prof.setId(strId);
			prof.setName(prefs.getString(KEY_PROFILENAME, ""));
			prof.setType(PROFILE_TYPE.valueOf(prefs.getString(KEY_PROFILETYPE, "")));
			prof.setServerName(prefs.getString(KEY_SERVERNAME, ""));
			prof.setUserName(prefs.getString(KEY_USERNAME, ""));
			prof.setPassword(prefs.getString(KEY_PASSWORD, ""));
			prof.setPptpEncryptionEnabled(prefs.getBoolean(KEY_ISPPTPENCRYPT, false));
			prof.setL2tpSecretEnabled(prefs.getBoolean(KEY_ISL2TPSECRETSTR, false));
			prof.setL2tpSecretString(prefs.getString(KEY_L2TPSECRETSTR, ""));
			prof.setDomainSuffices(prefs.getString(KEY_DOMAINSUFFIX, ""));
			prof.setRouteList(prefs.getString(KEY_ROUTELIST, ""));
			
			Intent resultIntent = new Intent();
			resultIntent.putExtra(RESULT_PROFILE, prof);
			this.setResult(RESULT_OK, resultIntent);
			
			if (null != mParamProfile)
				resultIntent.putExtra(PARAM_PROFILE, mParamProfile);

			this.finish();
		} else if (R.id.buttonAddProfileCancel == v.getId()) {
			this.setResult(RESULT_CANCELED);
			this.finish();
		}
	}
	
//	public void transProfileToPreference(SimpleProfile prof) {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		
//		prefs.edit().putString(KEY_PROFILEID, prof.getId());
//		prefs.edit().putString(KEY_PROFILENAME, prof.getName());
//		prefs.edit().putString(KEY_PROFILETYPE, prof.getType().toString());
//		prefs.edit().putString(KEY_USERNAME, prof.getUserName());
//		prefs.edit().putString(KEY_PASSWORD, prof.getPassword());
//		prefs.edit().putBoolean(KEY_ISPPTPENCRYPT, prof.isPptpEncryptionEnabled());
//		prefs.edit().putBoolean(KEY_ISL2TPSECRETSTR, prof.isL2tpSecretEnabled());
//		prefs.edit().putString(KEY_L2TPSECRETSTR, prof.getL2tpSecretString());
//		prefs.edit().putString(KEY_DOMAINSUFFIX, prof.getDomainSuffices());
//		prefs.edit().putString(KEY_ROUTELIST, prof.getRouteList());
//		prefs.edit().putString(KEY_SERVERNAME, prof.getServerName());
//		boolean b = prefs.edit().commit();
//		
//	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);		
		clearData();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences,
			String key) {
		// TODO Auto-generated method stub
		if (key.equals(KEY_PROFILETYPE)) {
			String pf = preferences.getString(KEY_PROFILETYPE, null);
			if (VALUE_PPTP.equals(pf)) {
				findPreference(KEY_ISPPTPENCRYPT).setEnabled(true);
				findPreference(KEY_ISL2TPSECRETSTR).setEnabled(false);				
			} else if (VALUE_L2TP.equals(pf)) {				
				findPreference(KEY_ISPPTPENCRYPT).setEnabled(false);
				findPreference(KEY_ISL2TPSECRETSTR).setEnabled(true);
			}
		} else if (key.equals(KEY_DOMAINSUFFIX)
				||key.equals(KEY_PROFILENAME)
				||key.equals(KEY_SERVERNAME)
				||key.equals(KEY_USERNAME)
				||key.equals(KEY_ROUTELIST)) {
			findPreference(key).setSummary(preferences.getString(key, ""));			
		} else if (key.equals(KEY_L2TPSECRETSTR)
				||key.equals(KEY_PASSWORD)) {
			findPreference(key).setSummary(preferences.getString(key, "").replaceAll(".", "*"));						
		}
	}

}
