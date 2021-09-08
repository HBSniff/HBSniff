/*
 * This file is part of hqlSniffer.
 *
 *     hqlSniffer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     hqlSniffer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with hqlSniffer.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hbsniff.detector.rules;

import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellReport;
import io.github.hzjdev.hbsniff.model.output.Smell;
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
    ProjectSmellReport psr = new ProjectSmellReport();
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