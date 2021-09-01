package io.github.hzjdev.hqlsniffer.model.output;

import io.github.hzjdev.hqlsniffer.model.Declaration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectSmellCSVLine implements Serializable {
    String[] line;

    public ProjectSmellCSVLine(Smell smell) {
        line = new String[]{
                smell.getName(), smell.getFile(), smell.getClassName(), smell.getComment(), smell.getPosition()
        };
    }

    public static List<ProjectSmellCSVLine> fromProjectSmellJSONReport(ProjectSmellJSONReport psr) {
        List<ProjectSmellCSVLine> results = new ArrayList<>();
        if (psr == null) {
            return results;
        }
        for (HashMap.Entry<Declaration, List<Smell>> kv : psr.getSmells().entrySet()) {
            results.addAll(kv.getValue().stream().map(ProjectSmellCSVLine::new).collect(Collectors.toList()));
        }
        return results;
    }

    public static List<String[]> toCSV(List<ProjectSmellCSVLine> lines) {
        String[] heads = {"smell", "file", "className", "comment", "position"};
        List<String[]> result = new ArrayList<>();
        result.add(heads);
        result.addAll(lines.stream().map(ProjectSmellCSVLine::getLine).collect(Collectors.toList()));
        return result;
    }

    public String[] getLine() {
        return line;
    }
}
