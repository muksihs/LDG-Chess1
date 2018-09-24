package com.muksihs.ldg.chess1.models;

import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.Permlink;
import steem.models.ChessGameData;

public class MoveResponse {
	private ChessGameData cgd;
	private AccountName player;
	public AccountName getPlayer() {
		return player;
	}
	public void setPlayer(AccountName player) {
		this.player = player;
	}
	public Permlink getPermlink() {
		return permlink;
	}
	public void setPermlink(Permlink permlink) {
		this.permlink = permlink;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public ChessGameData getCgd() {
		return cgd;
	}
	public void setCgd(ChessGameData cgd) {
		this.cgd = cgd;
	}
	private Permlink permlink;
	private String[] tags;
	private String reason;
}