package com.muksihs.farhorizons.steemapi;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RcAccount {
	private static final BigDecimal FIVE_DAYS_SECONDS = new BigDecimal((long) 5 * 24 * 60 * 60);
	private String account;
	@JsonProperty("rc_manabar")
	private RcManabar rcManabar;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public RcManabar getRcManabar() {
		return rcManabar;
	}

	public void setRcManabar(RcManabar rcManabar) {
		this.rcManabar = rcManabar;
	}

	public Map<String, Object> getMaxRcCreationAdjustment() {
		return maxRcCreationAdjustment;
	}

	public void setMaxRcCreationAdjustment(Map<String, Object> maxRcCreationAdjustment) {
		this.maxRcCreationAdjustment = maxRcCreationAdjustment;
	}

	public BigDecimal getMaxRc() {
		return maxRc;
	}

	public void setMaxRc(BigDecimal maxRc) {
		this.maxRc = maxRc;
	}

	@JsonProperty("max_rc_creation_adjustment")
	private Map<String, Object> maxRcCreationAdjustment;
	@JsonProperty("max_rc")
	private BigDecimal maxRc;

	/**
	 * estimated mana = (now_secs-last_secs)/(5 days seconds)*max_mana+current_mana;
	 * 
	 * @return
	 */
	public BigDecimal getEstimatedMana() {
		long now_secs = System.currentTimeMillis()/1000l;
		BigDecimal now = new BigDecimal(now_secs);
		BigDecimal last = new BigDecimal(getRcManabar().getLastUpdateTime());
		BigDecimal diff = now.subtract(last);
		System.out.println("diff: "+diff.toPlainString());
		BigDecimal percent = diff.divide(FIVE_DAYS_SECONDS, 3, RoundingMode.DOWN);
		System.out.println("percent: "+percent);
		BigDecimal currentMana = getRcManabar().getCurrentMana();
		BigDecimal estimateGain = maxRc.multiply(percent).setScale(0, RoundingMode.DOWN);
		System.out.println("gain: "+estimateGain.toPlainString());
		BigDecimal estimated = currentMana.add(estimateGain);
		return estimated.min(maxRc);
	}
}