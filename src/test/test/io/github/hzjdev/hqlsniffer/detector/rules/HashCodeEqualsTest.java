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
import io.github.hzjdev.hqlsniffer.model.output.ProjectSmellJSONReport;
import io.github.hzjdev.hqlsniffer.model.output.Smell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * GetterSetter Tester.
 */
public class HashCodeEqualsTest {
    Set<Declaration> toInput;
    ProjectSmellJSONReport psr = new ProjectSmellJSONReport();
    HashCodeAndEquals c;

    @Before
    public void before() {
        String rootPath = "src/test/resources/entities/";
        Declaration noHashCodeEqualsEntity = Declaration.fromPath(rootPath + "InCompleteGetterSetterEntity.java");
        Declaration idNotInHashCodeEqualsEntity = Declaration.fromPath(rootPath + "IdNotInHashCodeEqualsEntity.java");
        Declaration idInHashCodeEqualsEntity = Declaration.fromPath(rootPath + "IdInHashCodeEqualsEntity.java");

        toInput = new HashSet<>();
        toInput.add(noHashCodeEqualsEntity);
        toInput.add(idNotInHashCodeEqualsEntity);
        toInput.add(idInHashCodeEqualsEntity);

        psr.getSmells().put(noHashCodeEqualsEntity, new ArrayList<>());
        psr.getSmells().put(idNotInHashCodeEqualsEntity, new ArrayList<>());
        psr.getSmells().put(idInHashCodeEqualsEntity, new ArrayList<>());

        c = (HashCodeAndEquals) new HashCodeAndEquals().populateContext(null, null, null, psr);
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testHashCodeAndEqualsNotUseIdentifierPropertyRule() {
        List<Smell> s = c.hashCodeAndEqualsNotUseIdentifierPropertyRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals("IdNotInHashCodeEqualsEntity", s.get(0).getClassName());
        List<String> comments = Arrays.asList(s.get(0).getComment().split("\n"));
        assertTrue((comments.get(0).contains("hashCode") && comments.get(1).contains("equals")) ||
                (comments.get(1).contains("hashCode") && comments.get(0).contains("equals")));
    }


    @Test
    public void testHashCodeAndEqualsRule() {
        List<Smell> s = c.hashCodeAndEqualsRule(toInput);
        assertEquals(s.size(), 2);
        assertEquals("InCompleteGetterSetterEntity", s.get(0).getClassName());
        assertEquals("InCompleteGetterSetterEntity", s.get(1).getClassName());

        assertTrue((s.get(0).getName().contains("HashCode") && s.get(1).getName().contains("Equals")) ||
                (s.get(1).getName().contains("HashCode") && s.get(0).getName().contains("Equals")));
    }

    /**
     * Method: exec()
     */
    @Test
    public void testExec() {
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        List<Smell> collectionSmell = c.hashCodeAndEqualsNotUseIdentifierPropertyRule(toInput);
        collectionSmell.addAll(c.hashCodeAndEqualsRule(toInput));
        List<Smell> execSmell = c.exec();
        assertEquals(c.exec().size(), collectionSmell.size());
        assertEquals(c.exec().size(), 3);

        Set<String> execNames = execSmell.stream().map(Smell::getClassName).collect(Collectors.toSet());
        Set<String> smellNames = collectionSmell.stream().map(Smell::getClassName).collect(Collectors.toSet());
        assertEquals(execNames, smellNames);
    }

} 
