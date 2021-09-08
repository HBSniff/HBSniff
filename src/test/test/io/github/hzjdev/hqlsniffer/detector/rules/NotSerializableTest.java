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
 * GetterSetter Tester.
 */
public class NotSerializableTest {
    Set<Declaration> toInput;
    ProjectSmellReport psr = new ProjectSmellReport();
    NotSerializable c;

    @Before
    public void before() {
        String rootPath = "src/test/resources/entities/";
        Declaration notSerializableEntity = Declaration.fromPath(rootPath + "InCompleteGetterSetterEntity.java");
        Declaration serializableEntity = Declaration.fromPath(rootPath + "SerializableEntity.java");

        toInput = new HashSet<>();
        toInput.add(notSerializableEntity);
        toInput.add(serializableEntity);

        psr.getSmells().put(notSerializableEntity, new ArrayList<>());
        psr.getSmells().put(serializableEntity, new ArrayList<>());

        c = (NotSerializable) new NotSerializable().populateContext(null, null, null, psr);
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void checkRule() {
        List<Smell> s = c.checkRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(), "InCompleteGetterSetterEntity");
    }

    /**
     * Method: exec()
     */
    @Test
    public void testExec() {
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        assertEquals(c.exec().size(), c.checkRule(toInput).size());
        assertEquals(c.exec().size(), 1);
        assertEquals(c.exec().get(0).getClassName(), c.checkRule(toInput).get(0).getClassName());

    }

} 
