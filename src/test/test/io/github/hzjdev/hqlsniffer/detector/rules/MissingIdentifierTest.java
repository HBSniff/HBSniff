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

package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellReport;
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
 */
public class MissingIdentifierTest {
    Set<Declaration> toInput;
    ProjectSmellReport psr = new ProjectSmellReport();
    MissingIdentifier c;

    @Before
    public void before() {
        String rootPath = "src/test/resources/entities/";
        Declaration fieldIdEntity = Declaration.fromPath(rootPath + "IdInHashCodeEqualsEntity.java");
        Declaration getterIdEntity = Declaration.fromPath(rootPath + "GetterIdentifierEntity.java");
        Declaration noIdEntity = Declaration.fromPath(rootPath + "MissingIdentifierEntity.java");

        toInput = new HashSet<>();
        toInput.add(fieldIdEntity);
        toInput.add(getterIdEntity);
        toInput.add(noIdEntity);

        psr.getSmells().put(fieldIdEntity, new ArrayList<>());
        psr.getSmells().put(getterIdEntity, new ArrayList<>());
        psr.getSmells().put(noIdEntity, new ArrayList<>());

        c = (MissingIdentifier) new MissingIdentifier().populateContext(null, null, null, psr);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: noArgumentConstructorRule(Set<Declaration> classes)
     */
    @Test
    public void testProvideIdentifierPropertyRule() {
        List<Smell> s = c.provideIdentifierPropertyRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(), "MissingIdentifierEntity");
    }

    /**
     * Method: exec()
     */
    @Test
    public void testExec() {
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        assertEquals(c.exec().size(), c.provideIdentifierPropertyRule(toInput).size());
        assertEquals(c.exec().size(), 1);
        assertEquals(c.exec().get(0).getClassName(), c.provideIdentifierPropertyRule(toInput).get(0).getClassName());

    }

} 
