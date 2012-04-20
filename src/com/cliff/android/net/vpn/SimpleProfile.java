package com.cliff.android.net.vpn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.net.vpn.L2tpProfile;
import android.net.vpn.PptpProfile;
import android.net.vpn.VpnProfile;
import android.net.vpn.VpnState;

public class SimpleProfile implements Serializable {
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String result = "id="+id;
		result += ";name="+name;
		result += ";serverName="+serverName;
		result += ";userName="+userName;
		result += ";type="+type;
		return result;
	}

	/**
	 * 
	 */
	//private static final long serialVersionUID = 7297907062170461490L;
	public enum PROFILE_TYPE {
		PPTP, L2TP
	};

	PROFILE_TYPE type = PROFILE_TYPE.PPTP;
	public String domainSuffices = "";
	public String id = "";
	public String name = "";
	public String serverName = "";
	public String routeList = "";
	public boolean pptpEncryptionEnabled = false;
	public boolean l2tpSecretEnabled = false;
	public String l2tpSecretString = "";
	public String userName = "";
	public String password = "";

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public VpnProfile getProfile() {
		switch (type) {
		case PPTP:
			return getPptpProfile();
		case L2TP:
			return getL2tpProfile();
		}
		return null;
	}

	public String getDomainSuffices() {
		return domainSuffices;
	}

	public void setDomainSuffices(String domainSuffices) {
		this.domainSuffices = domainSuffices;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getRouteList() {
		return routeList;
	}

	public void setRouteList(String routeList) {
		this.routeList = routeList;
	}

	public boolean isPptpEncryptionEnabled() {
		return pptpEncryptionEnabled;
	}

	public void setPptpEncryptionEnabled(boolean pptpEncryptionEnabled) {
		this.pptpEncryptionEnabled = pptpEncryptionEnabled;
	}

	public boolean isL2tpSecretEnabled() {
		return l2tpSecretEnabled;
	}

	public void setL2tpSecretEnabled(boolean l2tpSecretEnabled) {
		this.l2tpSecretEnabled = l2tpSecretEnabled;
	}

	public String getL2tpSecretString() {
		return l2tpSecretString;
	}

	public void setL2tpSecretString(String l2tpSecretString) {
		this.l2tpSecretString = l2tpSecretString;
	}

	public PROFILE_TYPE getType() {
		return type;
	}

	public void setType(PROFILE_TYPE type) {
		this.type = type;
	}

	public void update(SimpleProfile prof) {
		//this.id = prof.getId();
		this.type = prof.getType();
		this.domainSuffices = prof.getDomainSuffices();
		this.name = prof.getName();
		this.serverName = prof.getServerName();
		this.routeList = prof.getRouteList();
		this.pptpEncryptionEnabled = prof.isPptpEncryptionEnabled();
		this.l2tpSecretEnabled = prof.isL2tpSecretEnabled();
		this.l2tpSecretString = prof.getL2tpSecretString();
		this.userName = prof.getUserName();
		this.password = prof.getPassword();		
	}

	public PptpProfile getPptpProfile() {
		PptpProfile profile = new PptpProfile();
		profile.setDomainSuffices(domainSuffices);
		profile.setEncryptionEnabled(pptpEncryptionEnabled);
		profile.setId(id);
		profile.setName(name);
		profile.setServerName(serverName);
		profile.setRouteList(routeList);
		profile.setState(VpnState.UNKNOWN);
		return profile;
	}

	public L2tpProfile getL2tpProfile() {
		L2tpProfile profile = new L2tpProfile();
		profile.setDomainSuffices(domainSuffices);
		profile.setSecretEnabled(l2tpSecretEnabled);
		profile.setSecretString(l2tpSecretString);
		profile.setId(id);
		profile.setName(name);
		profile.setServerName(serverName);
		profile.setRouteList(routeList);
		profile.setState(VpnState.UNKNOWN);
		return profile;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeInt(type.ordinal());
		oos.writeUTF(domainSuffices);
		oos.writeUTF(id);
		oos.writeUTF(name);
		oos.writeUTF(serverName);
		oos.writeUTF(routeList);
		oos.writeBoolean(pptpEncryptionEnabled);
		oos.writeBoolean(l2tpSecretEnabled);
		oos.writeUTF(l2tpSecretString);
		oos.writeUTF(userName);
		oos.writeUTF(password);
	}

	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {

		this.type = PROFILE_TYPE.values()[ois.readInt()];
		this.domainSuffices = ois.readUTF();
		this.id = ois.readUTF();
		this.name = ois.readUTF();
		this.serverName = ois.readUTF();
		this.routeList = ois.readUTF();
		this.pptpEncryptionEnabled = ois.readBoolean();
		this.l2tpSecretEnabled = ois.readBoolean();
		this.l2tpSecretString = ois.readUTF();
		this.userName = ois.readUTF();
		this.password = ois.readUTF();
	}

}
