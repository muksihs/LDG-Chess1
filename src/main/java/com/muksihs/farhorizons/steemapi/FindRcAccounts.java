package com.muksihs.farhorizons.steemapi;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class FindRcAccounts extends ArrayList<Object> {
	public FindRcAccounts() {
		add("rc_api");
		add("find_rc_accounts");
		accountNameList=new AccountNameList();
		add(accountNameList);
	}
	public final AccountNameList accountNameList;
	public static class AccountNameList {
		private final List<String> accounts=new ArrayList<>();

		public List<String> getAccounts() {
			return accounts;
		}
	}
}