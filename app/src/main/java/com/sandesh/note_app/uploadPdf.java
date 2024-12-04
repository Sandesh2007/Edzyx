package com.sandesh.note_app;

public class uploadPdf {
    private String name;
    private String url;
    private long uploadedDate;
    private long size;

    // Default constructor required for Firebase
    public uploadPdf() {
    }

    // Constructor with all fields
    public uploadPdf(String name, String url, long uploadedDate, long size) {
        this.name = name;
        this.url = url;
        this.uploadedDate = uploadedDate;
        this.size = size;
    }

    // Getter and setter for other fields
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(long uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
