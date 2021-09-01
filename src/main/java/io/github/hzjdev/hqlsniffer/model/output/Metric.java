package io.github.hzjdev.hqlsniffer.model.output;

import com.google.gson.annotations.Expose;
import io.github.hzjdev.hqlsniffer.model.Declaration;

import java.util.List;

public class Metric extends Smell {
    @Expose
    Double intensity;

    public Double getIntensity() {
        return intensity;
    }

    public Metric setIntensity(Double intensity) {
        this.intensity = intensity;
        return this;
    }

    @Override
    public Metric setClassName(String className) {
        this.className = className;
        return this;
    }

    @Override
    public Metric setRelatedComponent(List<Declaration> relatedComponent) {
        this.relatedComponent = relatedComponent;
        return this;
    }

    @Override
    public Metric setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Metric setFile(String file) {
        this.file = file;
        return this;
    }

    @Override
    public Metric setPosition(String position) {
        this.position = position;
        return this;
    }

    @Override
    public Metric setComment(String comment) {
        this.comment = comment;
        return this;
    }

}
