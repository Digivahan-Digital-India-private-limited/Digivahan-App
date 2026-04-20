package com.digivahan.data.model;

import java.util.ArrayList;
import java.util.List;

public class ChatItemModel {
    private String _id;
    private String sender_id;
    private String message;
    private List<String> images;
    private String latitude;
    private String longitude;
    private List<String> deleted_by;
    private String message_timestamp;

    private String localId;
    private boolean isSending;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public List<String> getDeleted_by() {
        return deleted_by;
    }

    public void setDeleted_by(List<String> deleted_by) {
        this.deleted_by = deleted_by;
    }

    public String getMessage_timestamp() {
        return message_timestamp;
    }

    public void setMessage_timestamp(String message_timestamp) {
        this.message_timestamp = message_timestamp;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public boolean isSending() {
        return isSending;
    }

    public void setSending(boolean sending) {
        isSending = sending;
    }

    public ChatItemModel() {
        images = new ArrayList<>();
        deleted_by = new ArrayList<>();
    }

    /* ---------------- Getters & Setters ---------------- */

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getSenderId() {
        return sender_id;
    }

    public void setSenderId(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images != null ? images : new ArrayList<>();
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public List<String> getDeletedBy() {
        return deleted_by;
    }

    public void setDeletedBy(List<String> deleted_by) {
        this.deleted_by = deleted_by != null ? deleted_by : new ArrayList<>();
    }

    public String getMessageTimestamp() {
        return message_timestamp;
    }

    public void setMessageTimestamp(String message_timestamp) {
        this.message_timestamp = message_timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessageModel{" +
                "id='" + _id + '\'' +
                ", sender_id='" + sender_id + '\'' +
                ", message='" + message + '\'' +
                ", images=" + images +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", deleted_by=" + deleted_by +
                ", message_timestamp='" + message_timestamp + '\'' +
                '}';
    }
}
