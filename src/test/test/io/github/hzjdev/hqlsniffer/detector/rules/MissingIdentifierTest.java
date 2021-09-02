package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.rules.MissingIdentifier;
import io.github.hzjdev.hqlsniffer.detector.rules.MissingNoArgumentConstructor;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import io.github.hzjdev.hqlsniffer.model.output.Smell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
* MissingNoArgumentConstructor Tester. 
*
*/ 
public class MissingIdentifierTest {
    Set<Declaration> toInput;
    ProjectSmellJSONReport psr = new ProjectSmellJSONReport();
    MissingIdentifier c;
    @Before
    public void before(){
        String rootPath = "src/test/resources/entities/";
        Declaration fieldIdEntity = Declaration.fromPath(rootPath+ "IdInHashCodeEqualsEntity.java");
        Declaration getterIdEntity =  Declaration.fromPath(rootPath+"GetterIdentifierEntity.java");
        Declaration noIdEntity =  Declaration.fromPath(rootPath+"MissingIdentifierEntity.java");

        toInput = new HashSet<>();
        toInput.add(fieldIdEntity);
        toInput.add(getterIdEntity);
        toInput.add(noIdEntity);

        psr.getSmells().put(fieldIdEntity,new ArrayList<>());
        psr.getSmells().put(getterIdEntity,new ArrayList<>());
        psr.getSmells().put(noIdEntity,new ArrayList<>());

        c = (MissingIdentifier)new MissingIdentifier().populateContext(null,null,null, psr);
    }

    @After
    public void after() throws Exception {
    }

    /**
    *
    * Method: noArgumentConstructorRule(Set<Declaration> classes)
    *
    */
    @Test
    public void testProvideIdentifierPropertyRule() {
        List<Smell> s = c.provideIdentifierPropertyRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(),"MissingIdentifierEntity");
    }

    /**
     *
     * Method: exec()
     *
     */
    @Test
    public void testExec(){
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        assertEquals(c.exec().size(),c.provideIdentifierPropertyRule(toInput).size());
        assertEquals(c.exec().size(),1);
        assertEquals(c.exec().get(0).getClassName(),c.provideIdentifierPropertyRule(toInput).get(0).getClassName());

    }

} 
