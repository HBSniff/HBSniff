package tests.io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.detector.rules.MissingNoArgumentConstructor;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import io.github.hzjdev.hqlsniffer.model.output.Smell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
* MissingNoArgumentConstructor Tester. 
*
*/ 
public class MissingNoArgumentConstructorTest {
    Set<Declaration> toInput;
    ProjectSmellJSONReport psr = new ProjectSmellJSONReport();
    MissingNoArgumentConstructor c;
    @Before
    public void before(){
        String rootPath = "src/test/resources/entities/";
        Declaration missingNoArgConstructorEntity = Declaration.fromPath(rootPath+"MissingNoArgConstructorEntity.java");
        Declaration noArgConstructorEntity =  Declaration.fromPath(rootPath+"NoArgConstructorEntity.java");

        toInput = new HashSet<>();
        toInput.add(missingNoArgConstructorEntity);
        toInput.add(noArgConstructorEntity);

        psr.getSmells().put(missingNoArgConstructorEntity,new ArrayList<>());
        psr.getSmells().put(noArgConstructorEntity,new ArrayList<>());

        c = (MissingNoArgumentConstructor)new MissingNoArgumentConstructor().populateContext(null,null,null, psr);
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
    public void testNoArgumentConstructorRule() {
        List<Smell> s = c.noArgumentConstructorRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(),"MissingNoArgConstructorEntity");
    }

    /**
     *
     * Method: exec()
     *
     */
    @Test
    public void testExec(){
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        assertEquals(c.exec().size(),c.noArgumentConstructorRule(toInput).size());
        assertEquals(c.exec().size(),1);
        assertEquals(c.exec().get(0).getClassName(),c.noArgumentConstructorRule(toInput).get(0).getClassName());

    }

} 
