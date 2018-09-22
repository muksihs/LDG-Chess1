package com.muksihs.ldg.chess1.models;

import java.util.List;

import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.Permlink;

public class PlayerChallenge {
	
	public boolean isMatchFor(PlayerChallenge otherChallenge) {
		if (otherChallenge==null) {
			return false;
		}
		if (challenged==null) {
			return false;
		}
		AccountName otherChallenged = otherChallenge.getChallenged();
		AccountName otherChallenger = otherChallenge.getChallenger();
		if (!challenged.equals(otherChallenger)) {
			return false;
		}
		if (otherChallenged==null) {
			return true;
		}
		return challenger.equals(otherChallenged);
	}
	
	public String getSemaphore() {
		StringBuilder sb = new StringBuilder();
		if (challenger!=null) {
			sb.append("@"+challenger.getName());
		}
		if (challenged!=null) {
			sb.append("-@"+challenged.getName());
		}
		return sb.toString();
	}
	
	public String getInverseSemaphore() {
		StringBuilder sb = new StringBuilder();
		if (challenged!=null) {
			sb.append("@"+challenged.getName());
		}
		if (challenger!=null) {
			sb.append("-@"+challenger.getName());
		}
		return sb.toString();
	}

	
	private List<String> tags;
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((challenged == null) ? 0 : challenged.hashCode());
		result = prime * result + ((challenger == null) ? 0 : challenger.hashCode());
		result = prime * result + ((permlink == null) ? 0 : permlink.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
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
		if (!(obj instanceof PlayerChallenge)) {
			return false;
		}
		PlayerChallenge other = (PlayerChallenge) obj;
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
		if (tags == null) {
			if (other.tags != null) {
				return false;
			}
		} else if (!tags.equals(other.tags)) {
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
