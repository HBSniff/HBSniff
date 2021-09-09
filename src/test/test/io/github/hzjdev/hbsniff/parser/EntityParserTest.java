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

package io.github.hzjdev.hbsniff.parser;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hbsniff.model.Declaration;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntityParserTest { 

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
    *
    * Method: parseFromDir(String dirPath, List<CompilationUnit> results)
    *
    */
    @Test
    public void testParseFromDir() throws Exception {
        String rootPath = "src/test/resources/entities/parser/parseFromDir/";
        List<CompilationUnit> results = EntityParser.parseFromDir(rootPath);
        assertEquals(results.size(), 3);
    }

    /**
    *
    * Method: genDeclarationsFromCompilationUnits(List<CompilationUnit> cus)
    *
    */
    @Test
    public void testGenDeclarationsFromCompilationUnits() throws Exception {
        String rootPath = "src/test/resources/entities/parser/parseFromDir/";
        List<CompilationUnit> cus = EntityParser.parseFromDir(rootPath);
        List<Declaration> declarations = EntityParser.genDeclarationsFromCompilationUnits(cus);
        assertEquals(declarations.size(), 3);
        List<String> names = declarations.stream().map(Declaration::getName).collect(Collectors.toList());
        assertTrue(names.contains("Entity1"));
        assertTrue(names.contains("Entity2"));
        assertTrue(names.contains("Normal"));

    }

    /**
    *
    * Method: getIdentifierProperty(final Declaration entity)
    *
    */
    @Test
    public void testGetIdentifierProperty() throws Exception {
        String rootPath1 = "src/test/resources/entities/parser/parseFromDir/";
        String rootPath2 = "src/test/resources/entities/parser/getIdentifierProperty/";

        List<CompilationUnit> cus1 = EntityParser.parseFromDir(rootPath1);
        EntityParser.setCusCache(cus1);
        Declaration idDeclaredInField = EntityParser.findTypeDeclaration("Entity1");
        Declaration idDeclaredInGetter = EntityParser.findTypeDeclaration("Entity2");
        Declaration withoutId = EntityParser.findTypeDeclaration("Normal");
        assertTrue(EntityParser.getIdentifierProperty(idDeclaredInField).getName().equals("id"));
        assertTrue(EntityParser.getIdentifierProperty(idDeclaredInGetter).getName().equals("id"));
        assertTrue(EntityParser.getIdentifierProperty(withoutId) == null);

        List<CompilationUnit> cus2 = EntityParser.parseFromDir(rootPath2);
        EntityParser.setCusCache(cus2);
        Declaration entityChild = EntityParser.findTypeDeclaration("EntityChild");
        Declaration entityChild2 = EntityParser.findTypeDeclaration("EntityChildChild");

        assertTrue(EntityParser.getIdentifierProperty(entityChild).getName().equals("id"));
        assertTrue(EntityParser.getIdentifierProperty(entityChild2).getName().equals("id"));


    }

    /**
    *
    * Method: getSuperClassDeclarations(Declaration dec)
    *
    */
    @Test
    public void testGetSuperClassDeclarations() throws Exception {
        String rootPath = "src/test/resources/entities/parser/getIdentifierProperty/";
        List<CompilationUnit> cus = EntityParser.parseFromDir(rootPath);
        EntityParser.setCusCache(cus);
        Declaration entityChild = EntityParser.findTypeDeclaration("EntityChild");
        Declaration entityChild2 = EntityParser.findTypeDeclaration("EntityChildChild");

        assertTrue(EntityParser.getSuperClassDeclarations(entityChild).size() == 1);
        assertTrue(EntityParser.getSuperClassDeclarations(entityChild).get(0).getName().equals("EntityParent"));
        assertTrue(EntityParser.getSuperClassDeclarations(entityChild2).size() == 2);

    }


    /**
    *
    * Method: findCalledIn(String methodName, String typeName, List<CompilationUnit> cus)
    *
    */
    @Test
    public void testFindCalledInForMethodNameTypeNameCus() throws Exception {
        String rootPath1 = "src/test/resources/entities/metric/";

        List<CompilationUnit> cus1 = EntityParser.parseFromDir(rootPath1);

        EntityParser.setCusCache(cus1);
        Declaration paged = EntityParser.findTypeDeclaration("PagedCorrect");
        List<Declaration> result = EntityParser.findCalledIn("findStudents", paged.getName(), cus1);
        assertEquals(result.size(),1);
        assertEquals(result.get(0).getName(),"students");

        List<Declaration> resultDeprecated = EntityParser.findCalledIn("findStudents", cus1);
        assertEquals(resultDeprecated.size(),2);
        assertEquals(resultDeprecated.get(0).getName(),"students");
        assertEquals(resultDeprecated.get(1).getName(),"students");


    }

    /**
    *
    * Method: findTypeDeclaration(String toFind, List<CompilationUnit> cus, Integer level)
    *
    */
    @Test
    public void testFindTypeDeclaration() throws Exception {
        String rootPath1 = "src/test/resources/entities/parser/parseFromDir/";

        List<CompilationUnit> cus1 = EntityParser.parseFromDir(rootPath1);
        EntityParser.setCusCache(cus1);

        Declaration idDeclaredInField = EntityParser.findTypeDeclaration("Entity1");
        Declaration idDeclaredInGetter = EntityParser.findTypeDeclaration("Entity2");

        assertEquals(idDeclaredInField.getName(),"Entity1");
        assertEquals(idDeclaredInGetter.getName(),"Entity2");

    }



} 


