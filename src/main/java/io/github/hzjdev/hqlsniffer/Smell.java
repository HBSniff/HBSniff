package io.github.hzjdev.hqlsniffer;

import java.io.Serializable;
import java.util.List;

public class Smell implements Serializable {
    String name;
    String file;
    String position;
    String className;
    String component;
    Double intensity;
    List<Declaration> relatedComponent;

    public Double getIntensity() {
        return intensity;
    }

    public Smell setIntensity(Double intensity) {
        this.intensity = intensity;
        return this;
    }

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

    public String getComponent() {
        return component;
    }

    public Smell setComponent(String component) {
        this.component = component;
        return this;
    }
}
