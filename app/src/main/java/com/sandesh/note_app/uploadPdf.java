package com.sandesh.note_app;

public class uploadPdf {
    String name;
    String url;
    long uploadedDate;
    long size;

    public uploadPdf() {
        // Default constructor required for Firebase
    }

    public uploadPdf(String name, String url, long uploadedDate, long size) {
        this.name = name;
        this.url = url;
        this.uploadedDate = uploadedDate;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public long getUploadedDate() {
        return uploadedDate;
    }

    public long getSize() {
        return size;
    }
}
