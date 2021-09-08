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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * CollectionField Tester.
 */
public class CollectionFieldTest {

    Set<Declaration> toInput;
    ProjectSmellReport psr = new ProjectSmellReport();
    CollectionField c;

    @Before
    public void before() {
        String rootPath = "src/test/resources/entities/";
        Declaration setCollectionEntity = Declaration.fromPath(rootPath + "SetCollectionEntity.java");
        Declaration listCollectionEntity = Declaration.fromPath(rootPath + "ListCollectionEntity.java");
        Declaration nonListOrSetCollectionEntity = Declaration.fromPath(rootPath + "NonListOrSetCollectionEntity.java");

        toInput = new HashSet<>();
        toInput.add(setCollectionEntity);
        toInput.add(listCollectionEntity);
        toInput.add(nonListOrSetCollectionEntity);

        psr.getSmells().put(setCollectionEntity, new ArrayList<>());
        psr.getSmells().put(listCollectionEntity, new ArrayList<>());
        psr.getSmells().put(nonListOrSetCollectionEntity, new ArrayList<>());

        c = (CollectionField) new CollectionField().populateContext(null, null, null, psr);
    }


    @After
    public void after() throws Exception {
    }

    /**
     * Method: useInterfaceSetOrListRule(Set<Declaration> allModelClasses)
     */
    @Test
    public void testUseInterfaceSetOrListRule() {
        List<Smell> s = c.useInterfaceSetOrListRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(), "NonListOrSetCollectionEntity");
    }

    /**
     * Method: useSetCollectionRule(Set<Declaration> allModelClasses)
     */
    @Test
    public void testUseSetCollectionRule() throws Exception {
        List<Smell> s = c.useSetCollectionRule(toInput);
        assertEquals(s.size(), 1);
        assertEquals(s.get(0).getClassName(), "NonListOrSetCollectionEntity");
    }

    /**
     * Method: exec()
     */
    @Test
    public void testExec() {
        c.setEntityDeclarations((HashSet<Declaration>) toInput);
        List<Smell> collectionSmell = c.useInterfaceSetOrListRule(toInput);
        collectionSmell.addAll(c.useSetCollectionRule(toInput));
        List<Smell> execSmell = c.exec();
        assertEquals(c.exec().size(), collectionSmell.size());
        assertEquals(c.exec().size(), 2);

        Set<String> execNames = execSmell.stream().map(Smell::getClassName).collect(Collectors.toSet());
        Set<String> smellNames = collectionSmell.stream().map(Smell::getClassName).collect(Collectors.toSet());
        assertEquals(execNames, smellNames);

    }


} 
