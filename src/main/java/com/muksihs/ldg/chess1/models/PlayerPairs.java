package com.muksihs.ldg.chess1.models;

import eu.bittrade.libs.steemj.base.models.AccountName;

public class PlayerPairs {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((challenged == null) ? 0 : challenged.hashCode());
		result = prime * result + ((challenger == null) ? 0 : challenger.hashCode());
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
}
