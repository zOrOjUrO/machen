package com.todo.machen.listHelper;

public class setData {

    String title, description, image;
    public setData(String title, String desc, String image){
        this.title = title;
        this.description = desc;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getDescription() {
        return description;
    }
}
