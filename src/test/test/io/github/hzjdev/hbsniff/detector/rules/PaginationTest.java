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

import static io.github.hzjdev.hbsniff.parser.HqlExtractor.getHqlNodes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PaginationTest {


    Set<Declaration> toInput;
    ProjectSmellReport psr = new ProjectSmellReport();
    Pagination d = new Pagination();
    List<CompilationUnit> cus = new ArrayList<>();
    List<HqlAndContext> hqls = new ArrayList<>();

    @Before
    public void before() throws FileNotFoundException {
        String rootPath = "src/test/resources/entities/metric/";
        Declaration smelly = Declaration.fromPath(rootPath + "Paged.java");
        Declaration clean = Declaration.fromPath(rootPath + "PagedCorrect.java");

        toInput = new HashSet<>();
        toInput.add(smelly);
        toInput.add(clean);

        psr.getSmells().put(smelly, new ArrayList<>());
        psr.getSmells().put(clean, new ArrayList<>());


        cus.add(StaticJavaParser.parse(new File(rootPath + "Paged.java")));
        cus.add(StaticJavaParser.parse(new File(rootPath + "PagedCorrect.java")));

        hqls = getHqlNodes(cus);

        d.populateContext(cus, hqls, null, psr);
        d.setEntityDeclarations(toInput);
    }

    @Test
    public void getPaged() {
        List<Smell> result = d.getPaged(hqls, cus);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getClassName(), "Paged");
        assertTrue(result.get(0).getComment().contains("students"));
    }

    @Test
    public void exec() {
        assertEquals(d.exec().size(), d.getPaged(hqls, cus).size());
    }


}