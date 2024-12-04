package com.sandesh.note_app;

public class ErrorItem {
    private String title;
    private String fix;

    public ErrorItem(String title, String fix) {
        this.title = title;
        this.fix = fix;
    }

    public String getTitle() {
        return title;
    }

    public String getFix() {
        return fix;
    }
}
