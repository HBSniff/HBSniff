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

public class OneByOneTest {

    Set<Declaration> toInput;
    ProjectSmellJSONReport psr = new ProjectSmellJSONReport();
    OneByOne d = new OneByOne();

    @Before
    public void before() {
        String rootPath = "src/test/resources/entities/";
        Declaration withLazy = Declaration.fromPath(rootPath + "OneToManyLazyEntity.java");
        Declaration withLazyBatchSize = Declaration.fromPath(rootPath + "OneToManyLazyBatchSizeEntity.java");

        Declaration noLazy = Declaration.fromPath(rootPath + "OneToManyEntity.java");

        toInput = new HashSet<>();
        toInput.add(withLazy);
        toInput.add(noLazy);
        toInput.add(withLazyBatchSize);

        psr.getSmells().put(withLazy, new ArrayList<>());
        psr.getSmells().put(noLazy, new ArrayList<>());
        psr.getSmells().put(withLazyBatchSize, new ArrayList<>());

        d.populateContext(null, null, null, psr);
        d.setEntityDeclarations(toInput);

    }

    @Test
    public void getOneByOne() {
        List<Smell> result = d.getOneByOne(toInput);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getClassName(), "OneToManyLazyEntity");
        assertTrue(result.get(0).getComment().contains("lst"));

    }

    @Test
    public void exec() {
        assertEquals(d.exec().size(), d.getOneByOne(toInput).size());
    }
}