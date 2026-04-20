package com.digivahan.data.model;

import java.io.Serializable;
import java.util.List;

public class TrendingCarsModel implements Serializable {

    public String id;
    public String car_id;
    public String brandName;
    public String modelName;
    public String type;
    public double price;
    public String priceDisplay;
    public String mileage;
    public String topSpeed;
    public String imageUrl,createdAt, updatedAt;

    public Specifications specifications;
    public DetailedSpecifications detailedSpecifications;
    public Dimensions dimensions;
    public Features features;

    // Nested classes
    public static class Specifications implements Serializable {
        public String engine_capacity;
        public String transmission;
        public String fuel_tank_capacity;
        public String seat_height;
        public String kerb_weight;
    }

    public static class DetailedSpecifications implements Serializable {
        public String max_power;
        public String max_torque;
        public String riding_mode;
        public String gear_shifting_pattern;
    }

    public static class Dimensions implements Serializable {
        public String bootspace;
        public String ground_clearance;
        public String length;
        public String width;
        public String height;
    }

    public static class Features implements Serializable {
        public boolean air_conditioner;
        public String central_locking;
        public String power_windows;
        public String headrest;
        public String parking_assist;
        public boolean cruise_control;
        public int music_system_count;
        public String apple_carplay;
        public String android_auto;
        public boolean abs;
        public boolean sunroof;
        public boolean third_row_ac;
        public List<String> airbags;

        public String getAirbagsAsString() {
            if (airbags == null || airbags.isEmpty()) {
                return "No airbags information available";
            }
            return String.join(", ", airbags);
        }
    }

    // Getters and setters
    public String getBrandName() { return brandName; }
    public String getModelName() { return modelName; }
    public String getImageUrl() { return imageUrl; }
    public double getPrice() { return price; }
    public String getPriceDisplay() { return priceDisplay; }
    public Features getFeatures() { return features; }

}

