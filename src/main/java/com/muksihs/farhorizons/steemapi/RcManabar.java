package com.muksihs.farhorizons.steemapi;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RcManabar {
	@JsonProperty("current_mana")
	private BigDecimal currentMana;
	@JsonProperty("last_update_time")
	private long lastUpdateTime;
	public BigDecimal getCurrentMana() {
		return currentMana;
	}
	public void setCurrentMana(BigDecimal currentMana) {
		this.currentMana = currentMana;
	}
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
}