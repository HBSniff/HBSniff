package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.rules.GetterSetter;
import io.github.hzjdev.hqlsniffer.detector.rules.NotSerializable;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import io.github.hzjdev.hqlsniffer.model.output.Smell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
* GetterSetter Tester.
*
*/ 
public class NotSerializableTest {
    Set<Declaration> toInput;
    ProjectSmellJSONReport psr = new ProjectSmellJSONReport();
    NotSerializable c;
    @Before
    public void before(){
        String rootPath = "src/test/resources/entities/";
        Declaration notSerializableEntity = Declaration.fromPath(rootPath+ "InCompleteGetterSetterEntity.java");
        Declaration serializableEntity = Declaration.fromPath(rootPath+ "SerializableEntity.java");

        toInput = new HashSet<>();
        toInput.add(notSerializableEntity);
        toInput.add(serializableEntity);

        psr.getSmells().put(notSerializableEntity,new ArrayList<>());
        psr.getSmells().put(serializableEntity,new ArrayList<>());

        c = (NotSerializable)new NotSerializable().populateContext(null,null,null, psr);
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void checkRule() {
        List<Smell> s = c.checkRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(),"InCompleteGetterSetterEntity");
    }

    /**
     *
     * Method: exec()
     *
     */
    @Test
    public void testExec(){
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        assertEquals(c.exec().size(),c.checkRule(toInput).size());
        assertEquals(c.exec().size(),1);
        assertEquals(c.exec().get(0).getClassName(),c.checkRule(toInput).get(0).getClassName());

    }

} 
