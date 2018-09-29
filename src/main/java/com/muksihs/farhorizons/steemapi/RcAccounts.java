package com.muksihs.farhorizons.steemapi;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RcAccounts {
	@JsonProperty("rc_accounts")
	private ArrayList<RcAccount> rcAccounts;

	public ArrayList<RcAccount> getRcAccounts() {
		return rcAccounts;
	}

	public void setRcAccounts(ArrayList<RcAccount> rcAccounts) {
		this.rcAccounts = rcAccounts;
	}
}