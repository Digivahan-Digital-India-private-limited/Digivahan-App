package com.digivahan.data.model;

import java.io.Serializable;
import java.util.List;

public class TipsItemModel implements Serializable {

    public String _id, banner, banner_public_id, title, summary;
    public List<Point> points;

    public static class Point implements Serializable {
        public String icon;
        public String message;
        public String icon_public_id;
    }
}

