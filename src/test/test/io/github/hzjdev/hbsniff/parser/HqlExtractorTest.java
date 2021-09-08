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
 *     along with hqlSniffer.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hbsniff.parser;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hbsniff.model.HqlAndContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.hzjdev.hbsniff.parser.HqlExtractor.getHqlNodes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HqlExtractorTest {

    List<CompilationUnit> cus;
    @Before
    public void before() throws Exception {
        String rootPath = "src/test/resources/hqls/";
        cus = EntityParser.parseFromDir(rootPath);
    }

    private List<CompilationUnit> extractFromCus(String name){
        return cus.stream().filter(i->i.getPrimaryTypeName().get().equals(name)).collect(Collectors.toList());
    }
    @After
    public void after() throws Exception {
    }

    @Test
    public void testGetHqlNodes1() throws Exception {
        List<CompilationUnit> input = extractFromCus("HQL1");
        List<HqlAndContext> result = getHqlNodes(input);
        assertEquals(result.get(0).getHql().get(0),"SELECT ac from AdCategory ac");
    }

    @Test
    public void testGetHqlNodes2() throws Exception {
        List<CompilationUnit> input = extractFromCus("HQL2");
        List<HqlAndContext> result = getHqlNodes(input);
        assertEquals(result.get(0).getHql().size(),4);
    }


    @Test
    public void testGetHqlNodes3() throws Exception {
        List<CompilationUnit> input = extractFromCus("HQL3");
        List<HqlAndContext> result = getHqlNodes(input);
        assertEquals(result.get(0).getHql().size(),3);
    }



    @Test
    public void testGetHqlNodes4() throws Exception {
        List<CompilationUnit> input = extractFromCus("HQL4");
        List<HqlAndContext> result = getHqlNodes(input);
        assertEquals(result.get(0).getHql().get(0),"SELECT x FROM Feedback x WHERE 1=1");
    }

} 


