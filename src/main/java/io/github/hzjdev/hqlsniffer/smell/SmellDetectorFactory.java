package io.github.hzjdev.hqlsniffer.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.ProjectSmellReport;
import io.github.hzjdev.hqlsniffer.Result;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.findTypeDeclaration;

public class SmellDetectorFactory {

    public ProjectSmellReport psr;

    public List<CompilationUnit> cus;

    public List<Result> hqls;

    public List<CompilationUnit> entities;

    public HashSet<Declaration> declarations;

    public enum SmellType{
        //SBES 2020
        OneByOne,
        Pagination,
        Fetch,
        MissingOneToMany,
        //SBES 2019
        CollectionField,
        FinalEntity,
        GetterSetter,
        HashCodeAndEquals,
        MissingIdentifier,
        MissingNoArgumentConstructor,
        NotSerializable,
    }

    public static SmellDetector create(SmellType smellType){
        String smellName = smellType.name();
        try {
            Class toInit = Class.forName(SmellDetectorFactory.class.getPackage().getName() +"."+ smellName);
            return (SmellDetector) toInit.getDeclaredConstructor().newInstance();
        }catch (Exception e){
            return null;
        }
    }

    public static List<SmellDetector> createAll(List<CompilationUnit> cus, List<Result> hqls, List<CompilationUnit> entities, ProjectSmellReport psr){
        List<SmellDetector> detectors = new ArrayList<>();
        for(SmellType s: SmellType.values()){
            detectors.add(create(s).populateContext(cus, hqls, entities, psr));
        }
        return detectors;
    }

}
