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

package io.github.hzjdev.hbsniff.detector;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.ParametreOrField;
import io.github.hzjdev.hbsniff.model.output.Metric;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.hzjdev.hbsniff.parser.EntityParser.*;
import static io.github.hzjdev.hbsniff.utils.Const.*;

/**
 * Implementing Mapping Metrics: {TATI,NCT,NCRF,ANV}
 * Paper: S. Holder, J. Buchan, S. G. MacDonell. Towards a Metrics Suite for Object-Relational Mappings. MBSDI 2008. 43-54
 */
public class MappingMetrics {

    public static Map<String, Set<String>> NCRFInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> ANVInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> TATIInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> NCTInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> NCRFFields = new HashMap<>();

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
            //  initialize
            TATIInheritanceMap.computeIfAbsent(entityName, k -> new HashSet<>());
            if(superClasses.size() < 1){
                continue;
            }
            Declaration topSuperClass = superClasses.get(superClasses.size() - 1);
            for (Declaration superClass : superClasses) {
                String superClassName = superClass.getName();
                //  initialize
                TATIInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());

                // Before an object can be completely retrieved, it is necessary to
                // identify the most specific class of this object (i.e., The bottom of the inheritance tree).
                // It should be noted that identifying the most
                // specific class is equivalent to identifying the tables that need to be queried in order to
                // retrieve the requested object.
                TATIInheritanceMap.get(superClassName).add(entity.getName());
                TATIInheritanceMap.get(entityName).add(superClass.getName());

                if (topSuperClass.getAnnotations().stream().anyMatch(i ->
                        i.contains(TABLE_PER_CLASS_ANNOT_EXPR))) {
                    // 2.3 One Inheritance Path – One Table
//                    PROS: non-null
//                    CONS: redundant data

                    // The ‘one inheritance path - one table’ mapping strategy only maps each concrete class to
                    // a table.
                    // NCRF(C) equals the number of relational fields in all tables (i.e., subclasses)
                    // that correspond to each non-inherited non-key attribute of C.
                    NCRFInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                    NCRFInheritanceMap.get(superClassName).add(entityName);
                } else if(topSuperClass.getAnnotations().stream().anyMatch(i ->
                        i.contains(SINGLE_TABLE_ANNOT_EXPR))){
                    // 2.2 One Inheritance Tree – One Table
//                    PROS: better performance, no join
//                    CONS: null values

                    //  all classes of an inheritance hierarchy are mapped to the same relational table.
                    // ANV measures additional storage space in terms of null values that result when different
                    //classes are stored together in the same table using the ‘union superclass’ mapping strategy
                    ANVInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                    ANVInheritanceMap.get(superClassName).add(entityName);
                }else if(topSuperClass.getAnnotations().stream().anyMatch(i ->
                        i.contains(JOINED_ANNOT_EXPR))){
                    // 2.1 One Class - One Table
                    // distributing object data over multiple tables.
                    // In order to link these tables, all tables share the same primary key.
                    // PROS: non-null, no redundant data
                    // CONS: low performance

                    // NCT equals the number of tables that contain data from instances of a class C.
                    // This number depends on the inheritance mapping
                    // strategies that are used for the inheritance relationships on the path of class C to the root
                    // class of the inheritance hierarchy.
                    NCTInheritanceMap.computeIfAbsent(entityName, k -> new HashSet<>());
                    NCTInheritanceMap.get(entityName).add(superClassName);
                }
            }
        }
        // NCRF Calculation depends on field numbers, so we need to identify corresponding fields
        for (Declaration entity : entities) {
            String typeName = entity.getName();
            Set<String> classesContainingFields = new HashSet<>();
            List<Declaration> superClasses = getSuperClassDeclarations(entity);
            if(superClasses != null && superClasses.size()>0){
                Declaration topSuperClass = superClasses.get(superClasses.size() - 1);
                if(NCRFInheritanceMap.containsKey(topSuperClass.getName())){
                    if(NCRFInheritanceMap.containsKey(typeName)) {
                        classesContainingFields.addAll(NCRFInheritanceMap.get(typeName));
                    }
                    classesContainingFields.add(typeName);
                }
            }else if(NCRFInheritanceMap.containsKey(typeName)){
                classesContainingFields.addAll(NCRFInheritanceMap.get(typeName));
                classesContainingFields.add(typeName);
            }
            NCRFFields.computeIfAbsent(typeName, k -> new HashSet<>());
            Set<String> fields = new HashSet<>();
            ParametreOrField field = getIdentifierProperty(entity);
            for (ParametreOrField p : entity.getFields()) {
                if(p.equals(field)) continue;
                fields.add(p.getName());
            }
            for(String c: classesContainingFields){
                for(String f: fields){
                    NCRFFields.get(typeName).add(c+"."+f);
                }
            }
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
            Set<String> correspondingTables = NCTInheritanceMap.get(entity.getName());
            if (correspondingTables != null) {
                Metric s = initMetric(entity)
                        .setName("NCT")
                        .setComment(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 1.0); //+self
                result.add(s);
            } else {
                Metric s = initMetric(entity)
                        .setName("NCT")
                        .setIntensity(1.0); //self
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
            Set<String> correspondingTables = NCRFFields.get(entity.getName());
            if (correspondingTables != null && correspondingTables.size() > 0) {
                Metric s = initMetric(entity)
                        .setName("NCRF")
                        .setComment(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size()+0.0); //self
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
