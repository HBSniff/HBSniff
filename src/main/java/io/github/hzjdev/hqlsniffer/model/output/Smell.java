package io.github.hzjdev.hqlsniffer.model.output;

import com.google.gson.annotations.Expose;
import io.github.hzjdev.hqlsniffer.model.Declaration;

import java.io.Serializable;
import java.util.List;

public class Smell implements Serializable {
    @Expose
    String name;

    @Expose
    String file;

    @Expose
    String position;

    @Expose
    String className;

    @Expose
    String comment;

    @Expose
    List<Declaration> relatedComponent;


    public String getClassName() {
        return className;
    }

    public Smell setClassName(String className) {
        this.className = className;
        return this;
    }

    public List<Declaration> getRelatedComponent() {
        return relatedComponent;
    }

    public Smell setRelatedComponent(List<Declaration> relatedComponent) {
        this.relatedComponent = relatedComponent;
        return this;
    }

    public String getName() {
        return name;
    }

    public Smell setName(String name) {
        this.name = name;
        return this;
    }

    public String getFile() {
        return file;
    }

    public Smell setFile(String file) {
        this.file = file;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public Smell setPosition(String position) {
        this.position = position;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Smell setComment(String comment) {
        this.comment = comment;
        return this;
    }
}
