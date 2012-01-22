package org.wikipedia;

public class PictureEntry {

	public PictureEntry(String wikipediaUrl, String title, String summary) {
		super();
		this.wikipediaUrl = wikipediaUrl;
		this.title = title;
		this.summary = summary;
	}

	private String wikipediaUrl;
	private String title;
	private String summary;
	

	public String getWikipediaUrl() {
		return wikipediaUrl;
	}

	public void setWikipediaUrl(String wikipediaUrl) {
		this.wikipediaUrl = wikipediaUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}
