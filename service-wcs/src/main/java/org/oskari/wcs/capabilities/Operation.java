package org.oskari.wcs.capabilities;

import java.util.Optional;

public class Operation {

    private String name;
    private Optional<String> get;
    private Optional<String> post;

    public Operation(String name, Optional<String> get, Optional<String> post) {
        this.name = name;
        this.get = get;
        this.post = post;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getGet() {
        return get;
    }

    public Optional<String> getPost() {
        return post;
    }

}
