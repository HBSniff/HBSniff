package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import io.github.hzjdev.hqlsniffer.model.output.Smell;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MissingManyToOneTest {

    Set<Declaration> toInput;
    ProjectSmellJSONReport psr = new ProjectSmellJSONReport();
    MissingManyToOne d = new MissingManyToOne();

    @Before
    public void before() {
        String rootPath = "src/test/resources/entities/";
        Declaration oneToManyEntity1 = Declaration.fromPath(rootPath + "NonListOrSetCollectionEntity.java");
        Declaration missingManyToOneEntity = Declaration.fromPath(rootPath + "NoArgConstructorEntity.java");
        Declaration oneToManyEntity2 = Declaration.fromPath(rootPath + "OneToManyEntity.java");
        Declaration manyToOneEntity = Declaration.fromPath(rootPath + "ManyToOneEntity.java");

        toInput = new HashSet<>();
        toInput.add(oneToManyEntity1);
        toInput.add(missingManyToOneEntity);
        toInput.add(oneToManyEntity2);
        toInput.add(manyToOneEntity);

        psr.getSmells().put(oneToManyEntity1, new ArrayList<>());
        psr.getSmells().put(missingManyToOneEntity, new ArrayList<>());
        psr.getSmells().put(oneToManyEntity2, new ArrayList<>());
        psr.getSmells().put(manyToOneEntity, new ArrayList<>());

        d.populateContext(null, null, null, psr);
        d.setEntityDeclarations(toInput);

    }

    @Test
    public void getOneToManyNPlusOne() {
        List<Smell> result = d.getOneToManyNPlusOne(toInput);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getClassName(), "NonListOrSetCollectionEntity");
        assertTrue(result.get(0).getComment().contains("missingManyToOne") && result.get(0).getComment().contains("lst"));
    }

    @Test
    public void exec() {
        assertEquals(d.exec().size(), d.getOneToManyNPlusOne(toInput).size());
    }
}