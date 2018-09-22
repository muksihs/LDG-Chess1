package com.muksihs.ldg.chess1.models;

import eu.bittrade.libs.steemj.base.models.AccountName;

public class SteemAccountInformation {
	private String activeKey = null;
	private String postingKey = null;
	private AccountName accountName = null;

	public String getActiveKey() {
		return activeKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountName == null) ? 0 : accountName.hashCode());
		result = prime * result + ((activeKey == null) ? 0 : activeKey.hashCode());
		result = prime * result + ((postingKey == null) ? 0 : postingKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SteemAccountInformation)) {
			return false;
		}
		SteemAccountInformation other = (SteemAccountInformation) obj;
		if (accountName == null) {
			if (other.accountName != null) {
				return false;
			}
		} else if (!accountName.equals(other.accountName)) {
			return false;
		}
		if (activeKey == null) {
			if (other.activeKey != null) {
				return false;
			}
		} else if (!activeKey.equals(other.activeKey)) {
			return false;
		}
		if (postingKey == null) {
			if (other.postingKey != null) {
				return false;
			}
		} else if (!postingKey.equals(other.postingKey)) {
			return false;
		}
		return true;
	}

	public void setActiveKey(String activeKey) {
		this.activeKey = activeKey;
	}

	public String getPostingKey() {
		return postingKey;
	}

	public void setPostingKey(String postingKey) {
		this.postingKey = postingKey;
	}

	public AccountName getAccountName() {
		return accountName;
	}

	public void setAccountName(AccountName accountName) {
		this.accountName = accountName;
	}
}