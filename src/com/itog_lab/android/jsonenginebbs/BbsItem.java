package com.itog_lab.android.jsonenginebbs;

public class BbsItem {
	private String docId;
	private String message;

	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return message;
	}
}
