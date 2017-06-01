package com.edocomar.demofeed.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Article   {

	private String title = null;
	private String content = null;

	public Article() {
	}

	public Article(String title, String content) {
		this.title = title;
		this.content = content;
	}
	
    @JsonProperty
	public String getTitle() {
		return title;
	}
    @JsonProperty
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * base64 encoded content of article
	 **/
    @JsonProperty
	public String getContent() {
		return content;
	}
    @JsonProperty
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Article other = (Article) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Article [title=" + title + ", content=" + content + "]";
	}

}
