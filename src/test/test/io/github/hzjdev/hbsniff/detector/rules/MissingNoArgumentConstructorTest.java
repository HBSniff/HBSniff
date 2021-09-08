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

package io.github.hzjdev.hbsniff.detector.rules;

import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellReport;
import io.github.hzjdev.hbsniff.model.output.Smell;
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
 */
public class MissingNoArgumentConstructorTest {
    Set<Declaration> toInput;
    ProjectSmellReport psr = new ProjectSmellReport();
    MissingNoArgumentConstructor c;

    @Before
    public void before() {
        String rootPath = "src/test/resources/entities/";
        Declaration missingNoArgConstructorEntity = Declaration.fromPath(rootPath + "MissingNoArgConstructorFinalEntity.java");
        Declaration noArgConstructorEntity = Declaration.fromPath(rootPath + "NoArgConstructorEntity.java");

        toInput = new HashSet<>();
        toInput.add(missingNoArgConstructorEntity);
        toInput.add(noArgConstructorEntity);

        psr.getSmells().put(missingNoArgConstructorEntity, new ArrayList<>());
        psr.getSmells().put(noArgConstructorEntity, new ArrayList<>());

        c = (MissingNoArgumentConstructor) new MissingNoArgumentConstructor().populateContext(null, null, null, psr);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: noArgumentConstructorRule(Set<Declaration> classes)
     */
    @Test
    public void testNoArgumentConstructorRule() {
        List<Smell> s = c.noArgumentConstructorRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(), "MissingNoArgConstructorFinalEntity");
    }

    /**
     * Method: exec()
     */
    @Test
    public void testExec() {
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        assertEquals(c.exec().size(), c.noArgumentConstructorRule(toInput).size());
        assertEquals(c.exec().size(), 1);
        assertEquals(c.exec().get(0).getClassName(), c.noArgumentConstructorRule(toInput).get(0).getClassName());

    }

} 
