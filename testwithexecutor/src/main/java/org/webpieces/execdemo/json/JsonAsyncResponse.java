package org.webpieces.execdemo.json;

import java.util.ArrayList;
import java.util.List;

//@Jackson
public class JsonAsyncResponse {

	private int searchTime;
	private String something;

	public int getSearchTime() {
		return searchTime;
	}
	public void setSearchTime(int searchTime) {
		this.searchTime = searchTime;
	}

	public String getSomething() {
		return something;
	}

	public void setSomething(String something) {
		this.something = something;
	}
}
