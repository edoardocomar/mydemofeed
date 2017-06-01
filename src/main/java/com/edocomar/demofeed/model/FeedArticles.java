package com.edocomar.demofeed.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedArticles   {

	private String feed = null;
	private List<Article> articles = new ArrayList<Article>();

    @JsonProperty
	public String getFeed() {
		return feed;
	}
    @JsonProperty
	public void setFeed(String feed) {
		this.feed = feed;
	}


    @JsonProperty
	public List<Article> getArticles() {
		return articles;
	}
    @JsonProperty
	public void setArticles(List<Article> articles) {
		this.articles = articles;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((articles == null) ? 0 : articles.hashCode());
		result = prime * result + ((feed == null) ? 0 : feed.hashCode());
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
		FeedArticles other = (FeedArticles) obj;
		if (articles == null) {
			if (other.articles != null)
				return false;
		} else if (!articles.equals(other.articles))
			return false;
		if (feed == null) {
			if (other.feed != null)
				return false;
		} else if (!feed.equals(other.feed))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Articles [feed=" + feed + ", articles=" + articles + "]";
	}

}
