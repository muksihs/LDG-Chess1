package com.muksihs.ldg.chess1;

import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.Permlink;

public class SignupReject {
	private AccountName challenger;
	private Permlink permlink;
	private String[] tags;
	private String reason;
	public AccountName getChallenger() {
		return challenger;
	}
	public void setChallenger(AccountName challenger) {
		this.challenger = challenger;
	}
	public Permlink getPermlink() {
		return permlink;
	}
	public void setPermlink(Permlink permlink) {
		this.permlink = permlink;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
}