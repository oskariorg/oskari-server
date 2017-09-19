package org.oskari.ows.capabilities;

public class Operation {

    private String name;
    private String get;
    private String post;

    public Operation(String name, String get, String post) {
        this.name = name;
        this.get = get;
        this.post = post;
    }

    public String getName() {
        return name;
    }

    public String getGet() {
        return get;
    }

    public String getPost() {
        return post;
    }

}
