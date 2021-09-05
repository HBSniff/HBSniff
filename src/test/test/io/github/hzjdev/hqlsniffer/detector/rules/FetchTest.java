package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import io.github.hzjdev.hqlsniffer.model.output.Smell;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.HqlExtractor.getHqlNodes;
import static org.junit.Assert.assertEquals;

public class FetchTest {

    Set<Declaration> toInput;
    ProjectSmellJSONReport psr = new ProjectSmellJSONReport();
    Fetch d = new Fetch();
    List<CompilationUnit> cus = new ArrayList<>();

    List<HqlAndContext> hqls = new ArrayList<>();

    @Before
    public void before() throws FileNotFoundException {
        String rootPath = "src/test/resources/entities/";
        Declaration smelly = Declaration.fromPath(rootPath + "ManyToOneEagerEntity.java");
        Declaration cleanCall = Declaration.fromPath(rootPath + "UsedJoinFetch.java");
        Declaration smellyCall = Declaration.fromPath(rootPath + "LackJoinFetch.java");

        toInput = new HashSet<>();
        toInput.add(smelly);
        toInput.add(cleanCall);
        toInput.add(smellyCall);

        psr.getSmells().put(smelly, new ArrayList<>());
        psr.getSmells().put(cleanCall, new ArrayList<>());
        psr.getSmells().put(smellyCall, new ArrayList<>());

        cus.add(StaticJavaParser.parse(new File(rootPath + "ManyToOneEagerEntity.java")));
        cus.add(StaticJavaParser.parse(new File(rootPath + "UsedJoinFetch.java")));
        cus.add(StaticJavaParser.parse(new File(rootPath + "LackJoinFetch.java")));

        hqls = getHqlNodes(cus);

        d.populateContext(cus, hqls, null, psr);
        d.setEntityDeclarations(toInput);
    }

    @Test
    public void getEagerFetches() {
        List<Smell> result = d.getEagerFetches(cus);
        assertEquals(result.size(),1);
        assertEquals(result.get(0).getClassName(),"ManyToOneEagerEntity");
    }

    @Test
    public void getJoinFetch() {
        List<Smell> result = d.getJoinFetch(hqls, d.getEagerFetches(cus));
        assertEquals(result.size(),1);
        assertEquals(result.get(0).getClassName(),"LackJoinFetch");

    }

    @Test
    public void exec() {
        List<Smell> result = d.getEagerFetches(cus);
        result.addAll(d.getJoinFetch(hqls, result));
        assertEquals(result.size(),d.exec().size());
    }
}