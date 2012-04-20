package com.cliff.android.net.vpn;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.vpn.IVpnService;
import android.net.vpn.VpnManager;
import android.net.vpn.VpnProfile;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import android.net.vpn.VpnState;

public class SimpleVpnManager {
	private VpnManager mVpnManager;
	protected Context mContext;
	protected final String TAG = "SimpleVpnManager";
	protected SimpleProfile mConnectedProfile = null;
	protected boolean connected = false;
	protected VpnProfile mProfile = null;
	
	// protected SimpleVpnManager instance = null;

	public SimpleVpnManager(Context mContext) {
		super();
		this.mContext = mContext;
		mVpnManager = new VpnManager(mContext);
		BroadcastReceiver r = new ContextBroadcastReceiver(mContext, this) {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				//Log.d(TAG, intent.getAction());
	            String profileName = intent.getStringExtra(
	                    VpnManager.BROADCAST_PROFILE_NAME);
	            if (profileName == null) return;
	            String error = null;

	            VpnState s = (VpnState) intent.getSerializableExtra(
	                    VpnManager.BROADCAST_CONNECTION_STATE);

	            if (s == null) {
	                Log.e(TAG, "received null connectivity state");
	                return;
	            }
	            
	            mProfile.setState(s);

	            int connectingErrorCode = intent.getIntExtra(
	                    VpnManager.BROADCAST_ERROR_CODE, VpnManager.VPN_ERROR_NO_ERROR);

                Log.d(TAG, "received connectivity: " + profileName
	                        + ": connected? " + s
	                        + "   err=" + connectingErrorCode);

                switch (connectingErrorCode){
                case VpnManager.VPN_ERROR_NO_ERROR:
                	if (VpnState.CONNECTED.equals(s))
                		connected = true;
                	break;
                case VpnManager.VPN_ERROR_AUTH:
                	error = "VPN_ERROR_AUTH";
                	connected = false;
                	break;
                case VpnManager.VPN_ERROR_CONNECTION_FAILED:
                	error = "VPN_ERROR_CONNECTION_FAILED";
                	connected = false;
                	break;
                case VpnManager.VPN_ERROR_UNKNOWN_SERVER:
                	error = "VPN_ERROR_UNKNOWN_SERVER";
                	connected = false;
                	break;
                case VpnManager.VPN_ERROR_CHALLENGE:
                	error = "VPN_ERROR_CHALLENGE";
                	connected = false;
                	break;
                case VpnManager.VPN_ERROR_REMOTE_HUNG_UP:
                	error = "VPN_ERROR_REMOTE_HUNG_UP";
                	connected = false;
                	break;
                case VpnManager.VPN_ERROR_REMOTE_PPP_HUNG_UP:
                	error = "VPN_ERROR_REMOTE_PPP_HUNG_UP";
                	connected = false;
                	break;
                case VpnManager.VPN_ERROR_PPP_NEGOTIATION_FAILED:
                	error = "VPN_ERROR_PPP_NEGOTIATION_FAILED";
                	connected = false;
                	break;
                case VpnManager.VPN_ERROR_CONNECTION_LOST:
                	error = "VPN_ERROR_CONNECTION_LOST";
                	connected = false;
                	break;
                case VpnManager.VPN_ERROR_LARGEST:
                	error = "VPN_ERROR_LARGEST";
                	connected = false;
                	break;
                }
                
                if (null != error)
					Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
                
			}
			
		};
		mVpnManager.registerConnectivityReceiver(r);
	}

	public void connectVpn(SimpleProfile profile) {
		mVpnManager.startVpnService();
		mConnectedProfile = profile;
		mProfile = profile.getProfile();
		ServiceConnection c = new SimpleVpnManagerServiceConnection(mProfile, mConnectedProfile.getUserName(), mConnectedProfile.getPassword()) {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				try {
					boolean success = IVpnService.Stub.asInterface(service)
							.connect(mProfile, mUsername, mPassword);
					if (!success) {
						Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(mContext, "Succeeded", Toast.LENGTH_SHORT).show();
					}
				} catch (Throwable e) {
					// Log.e(TAG, "connect() exception");
				} finally {
					mContext.unbindService(this);
				}

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				checkStatus();

			}
		};
		if (!bindService(c)) {
			Toast.makeText(mContext, "bind Failed", Toast.LENGTH_SHORT);
			// TextView testText = (TextView) findViewById(R.id.testText);
			// testText.setText("bind not ok");
		}
		mConnectedProfile = profile;
	}

	public void disconnectVpn() {
		ServiceConnection c = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				try {
					IVpnService.Stub.asInterface(service).disconnect();
					//Log.e(TAG, "disconnect called");

				} catch (Throwable e) {
                    checkStatus();
					//Log.e(TAG, "connect() exception");
				} finally {
					mContext.unbindService(this);
				}

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				checkStatus();

			}
		};
		if (!bindService(c)) {
			//TextView testText = (TextView) findViewById(R.id.testText);
			//testText.setText("bind not ok");
		}
		mVpnManager.stopVpnService();
		mConnectedProfile = null;
	}

	public SimpleProfile getConnectedProfile() {
		return mConnectedProfile;
	}

	public boolean isConnected() {
		return connected;
	}

    public void checkStatus() {
        final ConditionVariable cv = new ConditionVariable();
        cv.close();
        ServiceConnection c = new ServiceConnection() {
            public synchronized void onServiceConnected(ComponentName className,
                    IBinder service) {
                cv.open();
                try {
                    IVpnService.Stub.asInterface(service).checkStatus(mProfile);
                } catch (RemoteException e) {
                    Log.e(TAG, "checkStatus()", e);
                    broadcastConnectivity(VpnState.IDLE);
                } finally {
                    mContext.unbindService(this);
                }
            }

            public void onServiceDisconnected(ComponentName className) {
                cv.open();
                broadcastConnectivity(VpnState.IDLE);
                mContext.unbindService(this);
            }
        };
        if (bindService(c)) {
            // wait for a second, let status propagate
            if (!cv.block(1000)) broadcastConnectivity(VpnState.IDLE);
        }
    }

    private boolean bindService(ServiceConnection c) {
        return mVpnManager.bindVpnService(c);
    }

    private void broadcastConnectivity(VpnState s) {
        mVpnManager.broadcastConnectivity(mProfile.getName(), s);
    }

    abstract class SimpleVpnManagerServiceConnection implements
			ServiceConnection {
		VpnProfile mProfile;
		String mUsername;
		String mPassword;

		public SimpleVpnManagerServiceConnection(VpnProfile profile, String username, String password) {
			mProfile = profile;
			mUsername = username;
			mPassword = password;
		}
	}

	abstract class ContextBroadcastReceiver extends BroadcastReceiver{

		Context mContext = null;
		SimpleVpnManager mParent = null;
		public ContextBroadcastReceiver(Context context, SimpleVpnManager parent) {
			mContext = context;
			mParent = parent;
		}
		
	}

}
