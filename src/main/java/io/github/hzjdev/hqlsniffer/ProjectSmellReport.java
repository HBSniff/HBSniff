package io.github.hzjdev.hqlsniffer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectSmellReport implements Serializable {
    Map<Declaration,List<Smell>> smells;

    public ProjectSmellReport() {
        smells = new HashMap<>();
    }

    public Map<Declaration, List<Smell>> getSmells() {
        return smells;
    }

    public ProjectSmellReport setSmells(Map<Declaration, List<Smell>> smells) {
        this.smells = smells;
        return this;
    }
}
