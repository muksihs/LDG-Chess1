package com.muksihs.ldg.chess1.models;

import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.Permlink;

public class PlayerPairs {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((challenged == null) ? 0 : challenged.hashCode());
		result = prime * result + ((challenger == null) ? 0 : challenger.hashCode());
		result = prime * result + ((permlink == null) ? 0 : permlink.hashCode());
		return result;
	}
	private Permlink permlink;
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PlayerPairs)) {
			return false;
		}
		PlayerPairs other = (PlayerPairs) obj;
		if (challenged == null) {
			if (other.challenged != null) {
				return false;
			}
		} else if (!challenged.equals(other.challenged)) {
			return false;
		}
		if (challenger == null) {
			if (other.challenger != null) {
				return false;
			}
		} else if (!challenger.equals(other.challenger)) {
			return false;
		}
		if (permlink == null) {
			if (other.permlink != null) {
				return false;
			}
		} else if (!permlink.equals(other.permlink)) {
			return false;
		}
		return true;
	}
	private AccountName challenger;
	private AccountName challenged;
	public AccountName getChallenger() {
		return challenger;
	}
	public void setChallenger(AccountName challenger) {
		this.challenger = challenger;
	}
	public AccountName getChallenged() {
		return challenged;
	}
	public void setChallenged(AccountName challenged) {
		this.challenged = challenged;
	}
	public Permlink getPermlink() {
		return permlink;
	}
	public void setPermlink(Permlink permlink) {
		this.permlink = permlink;
	}
}
