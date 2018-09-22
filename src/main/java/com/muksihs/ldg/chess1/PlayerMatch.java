package com.muksihs.ldg.chess1;

import com.muksihs.ldg.chess1.models.PlayerChallenge;

public class PlayerMatch {
	private PlayerChallenge player1;
	private PlayerChallenge player2;
	public PlayerChallenge getPlayer1() {
		return player1;
	}
	public void setPlayer1(PlayerChallenge player1) {
		this.player1 = player1;
	}
	public PlayerChallenge getPlayer2() {
		return player2;
	}
	public void setPlayer2(PlayerChallenge player2) {
		this.player2 = player2;
	}
	
	public boolean isSameMatch(PlayerMatch match) {
		if (match==null) {
			return false;
		}
		if (getSemaphore().equals(match.getSemaphore())) {
			return true;
		}
		return getSemaphore().equals(match.getInverseSemaphore());
	}
	
	public String getSemaphore() {
		StringBuilder sb = new StringBuilder();
		if (player1!=null) {
			sb.append("@"+player1.getChallenger().getName());
		}
		if (player2!=null) {
			sb.append("-@"+player2.getChallenger().getName());
		}
		return sb.toString();
	}
	
	public String getInverseSemaphore() {
		StringBuilder sb = new StringBuilder();
		if (player2!=null) {
			sb.append("@"+player2.getChallenger().getName());
		}
		if (player1!=null) {
			sb.append("-@"+player1.getChallenger().getName());
		}
		return sb.toString();
	}
}