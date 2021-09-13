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

package io.github.hzjdev.hbsniff.metric;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.ParametreOrField;
import io.github.hzjdev.hbsniff.model.output.Metric;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.hzjdev.hbsniff.parser.EntityParser.genDeclarationsFromCompilationUnits;
import static io.github.hzjdev.hbsniff.parser.EntityParser.getSuperClassDeclarations;
import static io.github.hzjdev.hbsniff.utils.Const.*;

/**
 * Implementing Mapping Metrics: {TATI,NCT,NCRF,ANV}
 * Paper: S. Holder, J. Buchan, S. G. MacDonell. Towards a Metrics Suite for Object-Relational Mappings. MBSDI 2008. 43-54
 */
public class MappingMetrics {

    public static Map<String, Set<String>> NCRFInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> ANVInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> TATIInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> correspondingInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> NCTFields = new HashMap<>();

    /**
     * \ Create a Metric Instance by Class Declaration
     * @param entity class Declaration
     * @return new empty Metric Instance
     */
    private static Metric initMetric(Declaration entity) {
        return new Metric().setClassName(entity.getName()).setFile(entity.getFullPath()).setPosition(entity.getPosition());
    }

    /**
     * \ Calculate Inheritance Relationships for the Metrics
     * @param entities class Declaration
     */
    public static void initInheritance(List<Declaration> entities) {
        // TATI NCRF ANV Calculation
        for (Declaration entity : entities) {
            List<Declaration> superClasses = getEntitiesWithTableAnnotation(getSuperClassDeclarations(entity));
            String entityName = entity.getName();
            TATIInheritanceMap.computeIfAbsent(entityName, k -> new HashSet<>());
            for (Declaration superClass : superClasses) {
                String superClassName = superClass.getName();
                NCRFInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                ANVInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                correspondingInheritanceMap.computeIfAbsent(entityName, k -> new HashSet<>());
                TATIInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());

                if (superClass.getAnnotations().stream().noneMatch(i ->
                        i.contains(TABLE_PER_CLASS_ANNOT_EXPR))) {
                    correspondingInheritanceMap.get(entityName).add(superClassName);
                } else {
                    NCRFInheritanceMap.get(superClassName).add(entityName);
                }

                ANVInheritanceMap.get(superClassName).add(entityName);
                TATIInheritanceMap.get(superClassName).add(entity.getName());
                TATIInheritanceMap.get(entityName).add(superClass.getName());
            }
        }
        // NCT Calculation
        for (Declaration entity : entities) {
            String typeName = entity.getName();
            Set<String> corresponding = correspondingInheritanceMap.get(typeName);
            if (corresponding == null) {
                corresponding = new HashSet<>();
            }
            for (ParametreOrField p : entity.getFields()) {
                final String parametreTypeName = p.getType().split("<")[0];
                // find type decs of parametre p
                Declaration t = entities.stream().filter(i -> i.getName().equals(parametreTypeName)).findFirst().orElse(null);
                // get names of types
                List<String> availableNames= entities.stream().map(Declaration::getName).collect(Collectors.toList());
                if(availableNames.size() <1) break;
                List<Declaration> superClasses = getSuperClassDeclarations(t).stream().filter(availableNames::contains).collect(Collectors.toList());
                if (superClasses.size() > 0) {
                    t = superClasses.get(superClasses.size() - 1);
                }else{
                    t = null;
                }
                if (t != null) {
                    if (t.getAnnotations().stream().noneMatch(i ->
                            i.contains(TABLE_PER_CLASS_ANNOT_EXPR))) {
                        corresponding.add(parametreTypeName);
                    }
                }
            }
            NCTFields.put(typeName, corresponding);
        }
    }

    /**
     * Find Classes with @Entity Annotation
     * @param entities declarations
     * @return List of @Entity classes
     */
    public static List<Declaration> getEntitiesWithTableAnnotation(List<Declaration> entities) {
        if (entities == null) return null;
        return entities.stream().filter(sc -> sc.annotationIncludes(TABLE_ANNOT_EXPR)).collect(Collectors.toList());
    }

    /**
     * Table Accesses for Type Identification
     *
     * @param entities input classes
     * @return results for every class
     */
    public static List<Metric> TATI(List<Declaration> entities) {
        List<Metric> result = new ArrayList<>();
        for (Declaration entity : entities) {
            Set<String> correspondingTables = TATIInheritanceMap.get(entity.getName());
            if (correspondingTables != null && correspondingTables.size() > 0) {
                Metric s = initMetric(entity)
                        .setName("TATI")
                        .setComment(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 0.0);
                result.add(s);
            } else {
                Metric s = initMetric(entity)
                        .setName("TATI")
                        .setIntensity(0.0); //self
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Number of Corresponding Tables
     *
     * @param entities input classes
     * @return results for every class
     */
    public static List<Metric> NCT(List<Declaration> entities) {
        List<Metric> result = new ArrayList<>();
        for (Declaration entity : entities) {
            Set<String> correspondingTables = NCTFields.get(entity.getName());
            if (correspondingTables != null) {
                Metric s = initMetric(entity)
                        .setName("NCT")
                        .setComment(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 1.0); //+self
                result.add(s);
            } else {
                Metric s = initMetric(entity)
                        .setName("NCT")
                        .setIntensity(0.0); //self
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Number of Corresponding Relational Fields
     *
     * @param entities input classes
     * @return results for every class
     */
    public static List<Metric> NCRF(List<Declaration> entities) {
        List<Metric> result = new ArrayList<>();
        for (Declaration entity : entities) {
            Set<String> correspondingTables = NCRFInheritanceMap.get(entity.getName());
            if (correspondingTables != null && correspondingTables.size() > 0) {
                Metric s = initMetric(entity)
                        .setName("NCRF")
                        .setComment(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 1.0); //self
                result.add(s);
            } else {
                Metric s = initMetric(entity)
                        .setName("NCRF")
                        .setIntensity(0.0); //self
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Additional Null Values
     *
     * @param entities input classes
     * @return results for every class
     */
    public static List<Metric> ANV(List<Declaration> entities) {
        List<Metric> result = new ArrayList<>();
        String components = "";
        for (Declaration entity : entities) {
            List<Declaration> superClasses = getEntitiesWithTableAnnotation(getSuperClassDeclarations(entity));
            if (superClasses.size() < 1) continue;
            Declaration topClass = superClasses.get(superClasses.size() - 1);

            if (topClass.getAnnotations().stream().noneMatch(i ->
                    i.contains(SINGLE_TABLE_ANNOT_EXPR))) {
                continue;
            }

            Set<String> correspondingTables = ANVInheritanceMap.get(topClass.getName());
            if (correspondingTables == null) continue;
            int numCorrespondingFields = 0;
            int numOwnFields = 0;

            for (String table : correspondingTables) {
                if (table.equals(entity.getName())) continue;
                Declaration t = entities.stream().filter(e -> e.getName().equals(table)).findFirst().orElse(null);

                List<ParametreOrField> fields;
                if (t != null) {
                    fields = t.getFields();
                    List<String> fieldNames = fields.stream().filter(f -> !f.annotationIncludes(IDENT_ANNOT_EXPR)).map(ff -> table + "::" + ff.getName()).collect(Collectors.toList());
                    components += String.join(",", fieldNames);
                    numCorrespondingFields += fieldNames.size();
                }

            }
            List<ParametreOrField> ownFields = entity.getFields();
            List<String> fieldNames = ownFields.stream().filter(f -> !f.annotationIncludes(IDENT_ANNOT_EXPR)).map(ff -> entity.getName() + "::" + ff.getName()).collect(Collectors.toList());
            components += " | ";
            components += String.join(",", fieldNames);
            numOwnFields = fieldNames.size();

            Metric s = initMetric(entity)
                    .setName("ANV")
                    .setComment(components)
                    .setIntensity(numOwnFields * numCorrespondingFields + 0.0);
            result.add(s);
        }
        return result;
    }

    /**
     * Execute
     * @param cus all classes to detect
     * @return metric values
     */
    public static List<Metric> exec(List<CompilationUnit> cus) {
        List<Declaration> entities = genDeclarationsFromCompilationUnits(cus);
        initInheritance(entities);
        List<Metric> result = TATI(entities);
        result.addAll(NCT(entities));
        result.addAll(NCRF(entities));
        result.addAll(ANV(entities));
        return result;
    }
}
