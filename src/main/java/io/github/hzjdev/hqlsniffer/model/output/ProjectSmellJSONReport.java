package io.github.hzjdev.hqlsniffer.model.output;

import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.annotations.Expose;
import io.github.hzjdev.hqlsniffer.model.Declaration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.genDeclarationsFromCompilationUnits;

public class ProjectSmellJSONReport implements Serializable {

    @Expose
    Map<Declaration,List<Smell>> smells;

    public ProjectSmellJSONReport() {
        smells = new HashMap<>();
    }

    public Map<Declaration, List<Smell>> getSmells() {
        return smells;
    }

    public static ProjectSmellJSONReport fromCompilationUnits(List<CompilationUnit> entities){
        ProjectSmellJSONReport toReturn = new ProjectSmellJSONReport();
        for(Declaration d: genDeclarationsFromCompilationUnits(entities)){
            toReturn.getSmells().put(d,new ArrayList<>());
        }
        return toReturn;
    }
    public ProjectSmellJSONReport setSmells(Map<Declaration, List<Smell>> smells) {
        this.smells = smells;
        return this;
    }
}
