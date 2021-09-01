package io.github.hzjdev.hqlsniffer.detector;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SmellDetectorFactory {

    public static SmellDetector create(SmellType smellType) {
        String className = SmellDetectorFactory.class.getPackage().getName() + ".rules." + smellType.name();
        try {
            return (SmellDetector) Class.forName(className)
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public static List<SmellDetector> createAll(List<CompilationUnit> cus, List<HqlAndContext> hqls, List<CompilationUnit> entities, ProjectSmellJSONReport psr) {
        List<SmellDetector> detectors = new ArrayList<>();
        for (SmellType s : SmellType.values()) {
            detectors.add(Objects.requireNonNull(create(s))
                    .populateContext(cus, hqls, entities, psr));
        }
        return detectors;
    }

    public enum SmellType {
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

}
