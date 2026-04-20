package com.digivahan.data.model;

import java.io.Serializable;

public class NewsStoriesItemModel implements Serializable {
    private String _id, banner, banner_public_id, news_type, heading, sub_heading, news, createdAt;

    public NewsStoriesItemModel() {}

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getBanner_public_id() {
        return banner_public_id;
    }

    public void setBanner_public_id(String banner_public_id) {
        this.banner_public_id = banner_public_id;
    }

    public String getNews_type() {
        return news_type;
    }

    public void setNews_type(String news_type) {
        this.news_type = news_type;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getSub_heading() {
        return sub_heading;
    }

    public void setSub_heading(String sub_heading) {
        this.sub_heading = sub_heading;
    }

    public String getNews() {
        return news;
    }

    public void setNews(String news) {
        this.news = news;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
