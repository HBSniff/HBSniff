/*
 * This file is part of HBSniff.
 *
 *     HBSniff is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     HBSniff is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with HBSniff.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hbsniff.detector.rules;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.HqlAndContext;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellReport;
import io.github.hzjdev.hbsniff.model.output.Smell;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hbsniff.parser.EntityParser.setCusCache;
import static io.github.hzjdev.hbsniff.parser.HqlExtractor.getHqlNodes;
import static org.junit.Assert.assertEquals;

public class FetchTest {

    Set<Declaration> toInput;
    ProjectSmellReport psr = new ProjectSmellReport();
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
        setCusCache(cus);
        hqls = getHqlNodes(cus);

        d.populateContext(cus, hqls, cus, psr);
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
        assertEquals(result.get(0).getClassName(),"ManyToOneEagerEntity");

    }

    @Test
    public void exec() {
        List<Smell> result = d.getEagerFetches(cus);
        result.addAll(d.getJoinFetch(hqls, result));
        assertEquals(result.size(),d.exec().size());
    }
}