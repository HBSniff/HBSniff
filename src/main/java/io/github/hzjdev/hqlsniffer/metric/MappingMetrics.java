package io.github.hzjdev.hqlsniffer.metric;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.model.output.Metric;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.genDeclarationsFromCompilationUnits;
import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getSuperClassDeclarations;

/**
 * Implementing Mapping Metrics: {TATI,NCT,NCRF,ANV}
 * Paper: S. Holder, J. Buchan, S. G. MacDonell. Towards a Metrics Suite for Object-Relational Mappings. MBSDI 2008. 43-54
 */
public class MappingMetrics {
    /**
     * Table Accesses for Type Identification
     *
     * @param entities
     * @return
     */

    public static Map<String, Set<String>> NCRFInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> inheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> relatedInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> correspondingInheritanceMap = new HashMap<>();
    public static Map<String, Set<String>> referencedFields = new HashMap<>();

    private static Metric initMetric(Declaration entity) {
        return new Metric().setClassName(entity.getName()).setFile(entity.getFullPath()).setPosition(entity.getPosition());
    }

    public static void initInheritance(List<Declaration> entities) {
        for (Declaration entity : entities) {
            List<Declaration> superClasses = getEntitiesWithTableAnnotation(getSuperClassDeclarations(entity));
            String entityName = entity.getName();
            relatedInheritanceMap.computeIfAbsent(entityName, k -> new HashSet<>());
            for (Declaration superClass : superClasses) {
                String superClassName = superClass.getName();
                NCRFInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                inheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                correspondingInheritanceMap.computeIfAbsent(entityName, k -> new HashSet<>());

                if (superClass.getAnnotations().stream().noneMatch(i ->
                        i.contains("TABLE_PER_CLASS"))) {
                    correspondingInheritanceMap.get(entityName).add(superClassName);
                } else {
                    NCRFInheritanceMap.get(superClassName).add(entityName);
                }

                inheritanceMap.get(superClassName).add(entityName);
                relatedInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                relatedInheritanceMap.get(superClassName).add(entity.getName());
                relatedInheritanceMap.get(entityName).add(superClass.getName());


            }
        }
        for (Declaration entity : entities) {
            String typeName = entity.getName();
            Set<String> corresponding = correspondingInheritanceMap.get(typeName);
            if (corresponding == null) {
                corresponding = new HashSet<>();
            }
            for (Parametre p : entity.getFields()) {
                final String parametreTypeName = p.getType().split("<")[0];
                Declaration t = entities.stream().filter(i -> i.getName().equals(parametreTypeName)).findFirst().orElse(null);
                List<Declaration> superClasses = getSuperClassDeclarations(t);
                if (superClasses.size() > 0) {
                    t = superClasses.get(superClasses.size() - 1);
                }
                if (t != null) {
                    if (t.getAnnotations().stream().noneMatch(i ->
                            i.contains("TABLE_PER_CLASS"))) {
                        corresponding.add(parametreTypeName);
                    }
                }
            }
            referencedFields.put(typeName, corresponding);
        }
    }

    public static List<Declaration> getEntitiesWithTableAnnotation(List<Declaration> entities) {
        if (entities == null) return null;
        return entities.stream().filter(sc -> {
            boolean keep = false;
            for (String annotation : sc.getAnnotations()) {
                if (annotation.contains("@Table")) {
                    keep = true;
                    break;
                }
            }
            return keep;
        }).collect(Collectors.toList());
    }

    /**
     * Table Accesses for Type Identification
     *
     * @param entities
     * @return
     */
    public static List<Metric> TATI(List<Declaration> entities) {
        List<Metric> result = new ArrayList<>();
        for (Declaration entity : entities) {
            Set<String> correspondingTables = relatedInheritanceMap.get(entity.getName());
            if (correspondingTables != null && correspondingTables.size() > 0) {
                Metric s = initMetric(entity)
                        .setName("TATI")
                        .setComment(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 0.0);
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
     * Number of Corresponding Tables
     *
     * @param entities
     * @return
     */
    public static List<Metric> NCT(List<Declaration> entities) {
        List<Metric> result = new ArrayList<>();
        for (Declaration entity : entities) {
            Set<String> correspondingTables = referencedFields.get(entity.getName());
            if (correspondingTables != null) {
                Metric s = initMetric(entity)
                        .setName("NCT")
                        .setComment(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 1.0); //+self
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
     * Number of Corresponding Relational Fields
     *
     * @param entities
     * @return
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
     * @param entities
     * @return
     */
    public static List<Metric> ANV(List<Declaration> entities) {
        List<Metric> result = new ArrayList<>();
        String components = "";
        for (Declaration entity : entities) {
            List<Declaration> superClasses = getEntitiesWithTableAnnotation(getSuperClassDeclarations(entity));
            if (superClasses.size() < 1) continue;
            Declaration topClass = superClasses.get(superClasses.size() - 1);

            if (topClass.getAnnotations().stream().noneMatch(i ->
                    i.contains("SINGLE_TABLE"))) {
                continue;
            }

            Set<String> correspondingTables = inheritanceMap.get(topClass.getName());
            if (correspondingTables == null) continue;
            int numCorrespondingFields = 0;
            int numOwnFields = 0;

            for (String table : correspondingTables) {
                if (table.equals(entity.getName())) continue;
                Declaration t = entities.stream().filter(e -> e.getName().equals(table)).findFirst().orElse(null);

                List<Parametre> fields;
                if (t != null) {
                    fields = t.getFields();
                    List<String> fieldNames = fields.stream().filter(f -> !f.getAnnotations().contains("@Id")).map(ff -> table + "::" + ff.getName()).collect(Collectors.toList());
                    components += String.join(",", fieldNames);
                    numCorrespondingFields += fieldNames.size();
                }

            }
            List<Parametre> ownFields = entity.getFields();
            List<String> fieldNames = ownFields.stream().filter(f -> !f.getAnnotations().contains("@Id")).map(ff -> entity.getName() + "::" + ff.getName()).collect(Collectors.toList());
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

    public static List<Metric> exec(List<CompilationUnit> cus) {
        List<Declaration> entities = genDeclarationsFromCompilationUnits(cus);
//        entities = getEntitiesWithTableAnnotation(entities);
        initInheritance(entities);
        List<Metric> result = TATI(entities);
        result.addAll(NCT(entities));
        result.addAll(NCRF(entities));
        result.addAll(ANV(entities));
        return result;
    }
}
