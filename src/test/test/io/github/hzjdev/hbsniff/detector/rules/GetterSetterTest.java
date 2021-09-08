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

import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellReport;
import io.github.hzjdev.hbsniff.model.output.Smell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * GetterSetter Tester.
 */
public class GetterSetterTest {
    Set<Declaration> toInput;
    ProjectSmellReport psr = new ProjectSmellReport();
    GetterSetter c;

    @Before
    public void before() {
        String rootPath = "src/test/resources/entities/";
        Declaration incompleteGetterSetterEntity = Declaration.fromPath(rootPath + "InCompleteGetterSetterEntity.java");

        toInput = new HashSet<>();
        toInput.add(incompleteGetterSetterEntity);

        psr.getSmells().put(incompleteGetterSetterEntity, new ArrayList<>());

        c = (GetterSetter) new GetterSetter().populateContext(null, null, null, psr);
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testProvideGetsSetsFieldsRule() {
        List<Smell> s = c.provideGetsSetsFieldsRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(), "InCompleteGetterSetterEntity");
        List<String> fields = Arrays.asList(s.get(0).getComment().split("\n"));
        for (String field : fields) {
            assertTrue(field.contains("missingGetterSetterField") && (field.contains("get") || field.contains("set")));
        }
        assertEquals(fields.size(), 2);
    }

    /**
     * Method: exec()
     */
    @Test
    public void testExec() {
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        assertEquals(c.exec().size(), c.provideGetsSetsFieldsRule(toInput).size());
        assertEquals(c.exec().size(), 1);
        assertEquals(c.exec().get(0).getClassName(), c.provideGetsSetsFieldsRule(toInput).get(0).getClassName());

    }

} 
