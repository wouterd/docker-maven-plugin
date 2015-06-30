package net.wouterdanes.docker.provider.model;

import org.apache.maven.plugins.annotations.Parameter;

public class ContainerCommitConfiguration {
    @Parameter
    private String id;

    @Parameter
    private String repo;

    @Parameter
    private String tag;

    @Parameter
    private String comment;

    @Parameter
    private String author;

    @Parameter(defaultValue = "false")
    private boolean push;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }
}
