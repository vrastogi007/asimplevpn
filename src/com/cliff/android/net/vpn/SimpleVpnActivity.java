package com.cliff.android.net.vpn;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cliff.net.vpn.R;
import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

public class SimpleVpnActivity extends Activity implements OnClickListener {

	protected static final String TAG = "SimpleVPNActivity";
	protected static final String DB4OFILENAME = "db4o-config.db";
	protected String mDatabaseFullName = null;
	ArrayList<SimpleProfile> mProfileArrayList = null;

	protected static final int CODE_CREATE_PROFILE = 0;
	protected static final int CODE_MODIFY_PROFILE = 1;
	
	protected SimpleVpnManager mSimpleVpnManager = null;
	//protected SimpleProfile mConnectedProfile = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mDatabaseFullName = getFilesDir().getPath() + "/" + DB4OFILENAME;

		((Button) findViewById(R.id.addProfileButton)).setOnClickListener(this);
		((Button) findViewById(R.id.aboutButton)).setOnClickListener(this);

		refreshProfileList();
		
		mSimpleVpnManager = new SimpleVpnManager(this);
	}

	protected void refreshProfileList() {
		mProfileArrayList = getProfileArrayList();
		((ListView) findViewById(R.id.profileListView))
				.setAdapter(new SimpleProfileArrayAdapter(this,
						R.id.profileListView, mProfileArrayList));
		if (mProfileArrayList.size() > 0) {
			findViewById(android.R.id.empty).setVisibility(TextView.INVISIBLE);
		} else {
			findViewById(android.R.id.empty).setVisibility(TextView.VISIBLE);
		}
	}

	protected ArrayList<SimpleProfile> getProfileArrayList() {
		ObjectContainer db = Db4oEmbedded.openFile(
				Db4oEmbedded.newConfiguration(), mDatabaseFullName);
		ArrayList<SimpleProfile> profileArrayList = new ArrayList<SimpleProfile>();

		try {
			ObjectSet<SimpleProfile> resultlist = db.query(SimpleProfile.class);
			for (Object o : resultlist) {
				profileArrayList.add((SimpleProfile) o);
			}
		} finally {
			db.close();
		}
		return profileArrayList;
	}

	protected boolean saveProfile(SimpleProfile prof) {
		ObjectContainer db = Db4oEmbedded.openFile(
				Db4oEmbedded.newConfiguration(), mDatabaseFullName);
		try {
			db.store(prof);
		} catch (Exception e) {
			return false;
		} finally {
			db.close();
		}
		return true;
	}

	protected boolean removeProfile(SimpleProfile prof) {
		ObjectContainer db = Db4oEmbedded.openFile(
				Db4oEmbedded.newConfiguration(), mDatabaseFullName);
		try {
			ObjectSet result = db.queryByExample(prof);
			SimpleProfile found = (SimpleProfile)result.next();
			db.delete(found);
		} catch (Exception e) {
			return false;
		} finally {
			db.close();
		}
		return true;
	}

	protected boolean updateProfile(SimpleProfile prof, SimpleProfile example) {
		ObjectContainer db = Db4oEmbedded.openFile(
				Db4oEmbedded.newConfiguration(), mDatabaseFullName);
		try {
			ObjectSet result = db.queryByExample(example);
			SimpleProfile found = (SimpleProfile)result.next();
			found.update(prof);
			db.store(found);
		} catch (Exception e) {
			return false;
		} finally {
			db.close();
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (R.id.addProfileButton == v.getId()) {
			startActivityForResult(new Intent(this, AddProfileActivity.class), CODE_CREATE_PROFILE);
		} else if (R.id.aboutButton == v.getId()) {

		} else if (R.id.buttonDeleteItem == v.getId()) {
			// Log.d(TAG,
			// "position="+((ListView)findViewById(R.id.profileListView)).getPositionForView(v));
			int iPos = ((ListView)findViewById(R.id.profileListView)).getPositionForView(v);
			SimpleProfile prof = mProfileArrayList.get(iPos);
			String strId = mProfileArrayList.get(iPos).id;
			new AlertDialog.Builder(this).setTitle("Delete profile")
					.setMessage("Id = "+strId+"\nSure?").setPositiveButton("OK", new ListClickListener(prof){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							removeProfile(mProf);
							refreshProfileList();
						}
						
					})
					.setNegativeButton("Cancel", null).show();
		} else if (R.id.buttonEditItem == v.getId()) {
			int iPos = ((ListView)findViewById(R.id.profileListView)).getPositionForView(v);
			SimpleProfile prof = mProfileArrayList.get(iPos);
			Intent intent = new Intent(this, AddProfileActivity.class);
			intent.putExtra(AddProfileActivity.PARAM_PROFILE, prof);
			startActivityForResult(intent, CODE_MODIFY_PROFILE);
			
		} else if (R.id.ClickLinearLayout == v.getId()) {
			int iPos = ((ListView)findViewById(R.id.profileListView)).getPositionForView(v);
			SimpleProfile prof = mProfileArrayList.get(iPos);
			//try to connect
			if (!mSimpleVpnManager.isConnected()) {
				//connect
				mSimpleVpnManager.connectVpn(prof);
			} else {
				if (mSimpleVpnManager.getConnectedProfile().getId().equals(prof.getId())) {
					//disconnect
					mSimpleVpnManager.disconnectVpn();
				} else {
					//reconnect
					mSimpleVpnManager.disconnectVpn();
					mSimpleVpnManager.connectVpn(prof);					
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			SimpleProfile sprof = (SimpleProfile) data
					.getSerializableExtra(AddProfileActivity.RESULT_PROFILE);
			if (null != sprof) {
				if (requestCode == CODE_CREATE_PROFILE) {
					if (saveProfile(sprof)) {
						Toast.makeText(this, "Save succeeded", Toast.LENGTH_SHORT)
								.show();
						refreshProfileList();
					} else
						Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT)
								.show();
				} else if (requestCode == CODE_MODIFY_PROFILE) {
					SimpleProfile example = (SimpleProfile) data
							.getSerializableExtra(AddProfileActivity.PARAM_PROFILE);
					if (updateProfile(sprof, example)) {
						Toast.makeText(this, "Modify succeeded", Toast.LENGTH_SHORT)
								.show();
						refreshProfileList();
					} else
						Toast.makeText(this, "Modify failed", Toast.LENGTH_SHORT)
								.show();
				} 
			}
		}
		((Button) findViewById(R.id.aboutButton)).setText(requestCode + ":"
				+ resultCode);
	}

	private class SimpleProfileArrayAdapter extends ArrayAdapter<SimpleProfile> {

		private ArrayList<SimpleProfile> items;

		public SimpleProfileArrayAdapter(Context context,
				int textViewResourceId, ArrayList<SimpleProfile> objects) {
			super(context, textViewResourceId, objects);

			// TODO Auto-generated constructor stub
			items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.profilelist_item, null);
			}
			SimpleProfile o = items.get(position);
			if (o != null) {
				TextView tt = (TextView) v
						.findViewById(R.id.profileNameTextView);
				if (tt != null) {
					tt.setText(o.getName());
				}
				((LinearLayout) v.findViewById(R.id.ClickLinearLayout))
						.setOnClickListener((SimpleVpnActivity) getContext());
				((Button) v.findViewById(R.id.buttonEditItem))
						.setOnClickListener((SimpleVpnActivity) getContext());
				((Button) v.findViewById(R.id.buttonDeleteItem))
						.setOnClickListener((SimpleVpnActivity) getContext());
			}
			return v;
		}

	}

	abstract class ListClickListener implements DialogInterface.OnClickListener{

		SimpleProfile mProf = null;
		public ListClickListener(SimpleProfile prof) {
			mProf = prof;
		}

		@Override
		abstract public void onClick(DialogInterface dialog, int which);
		
	}

}