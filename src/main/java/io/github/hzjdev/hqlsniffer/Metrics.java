package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.hzjdev.hqlsniffer.Main.findTypeDeclaration;
import static io.github.hzjdev.hqlsniffer.Main.getSuperClassDeclarations;

/**
 * Implementing Mapping Metrics: {TATI,NCT,NCRF,ANV}
 * Paper: S. Holder, J. Buchan, S. G. MacDonell. Towards a Metrics Suite for Object-Relational Mappings. MBSDI 2008. 43-54
 */
public class Metrics {
    /**
     * Table Accesses for Type Identification
     * @param entities
     * @return
     */

    public Map<String, Set<String>> inheritanceMap = new HashMap<>();
    public Map<String, Set<String>> relatedInheritanceMap = new HashMap<>();
    public Map<String, Set<String>> correspondingInheritanceMap = new HashMap<>();
    public Map<String, Set<String>> referencedFields = new HashMap<>();

    private Smell initSmell(Declaration entity){
        return new Smell().setClassName(entity.getName()).setFile(entity.getFullPath()).setPosition(entity.getPosition());
    }

    public void initInheritance(List<Declaration> entities){
        for(Declaration entity: entities){
            List<Declaration> superClasses = getEntitiesWithTableAnnotation(getSuperClassDeclarations(entity));
            String entityName = entity.getName();
            relatedInheritanceMap.computeIfAbsent(entityName, k -> new HashSet<>());
            for(Declaration superClass: superClasses){
                String superClassName = superClass.getName();
                inheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                inheritanceMap.get(superClassName).add(entityName);

                relatedInheritanceMap.computeIfAbsent(superClassName, k -> new HashSet<>());
                relatedInheritanceMap.get(superClassName).add(entity.getName());
                relatedInheritanceMap.get(entityName).add(superClass.getName());

                correspondingInheritanceMap.computeIfAbsent(entityName, k -> new HashSet<>());
                correspondingInheritanceMap.get(entityName).add(superClassName);

            }
        }
        for(Declaration entity: entities) {
            String typeName = entity.getName();
            Set<String> corresponding =  correspondingInheritanceMap.get(typeName);
            if(corresponding == null){
                corresponding = new HashSet<>();
            }
            for(Parametre p :entity.getFields()){
                final String parametreTypeName = p.getType().split("<")[0];
                Declaration t = entities.stream().filter(i->i.getName().equals(parametreTypeName)).findFirst().orElse(null);
                if(t!=null){
                    corresponding.add(parametreTypeName);
                }
            }
            referencedFields.put(typeName,corresponding);
        }
    }

    public List<Declaration> getEntitiesWithTableAnnotation(List<Declaration> entities){
        if(entities == null) return null;
        return entities.stream().filter(sc->{
            boolean keep = false;
            for(String annotation: sc.getAnnotations()){
                if (annotation.contains("@Table")){
                    keep = true;
                    break;
                }
            }
            return keep;
        }).collect(Collectors.toList());
    }

    /**
     * Table Accesses for Type Identification
     * @param entities
     * @return
     */
    public List<Smell> TATI(List<Declaration> entities){
        List<Smell> result = new ArrayList<>();
        for(Declaration entity: entities){
            Set<String> correspondingTables = relatedInheritanceMap.get(entity.getName());
            if(correspondingTables != null) {
                Smell s = initSmell(entity)
                        .setName("TATI")
                        .setComponent(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 0.0);
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Number of Corresponding Tables
     * @param entities
     * @return
     */
    public List<Smell> NCT(List<Declaration> entities){
        List<Smell> result = new ArrayList<>();
        for(Declaration entity: entities){
            Set<String> correspondingTables = referencedFields.get(entity.getName());
            if(correspondingTables != null) {
                Smell s = initSmell(entity)
                        .setName("NCT")
                        .setComponent(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 0.0);
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Number of Corresponding Relational Fields
     * @param entities
     * @return
     */
    public List<Smell> NCRF(List<Declaration> entities){
        List<Smell> result = new ArrayList<>();
        for(Declaration entity: entities){
            Set<String> correspondingTables = inheritanceMap.get(entity.getName());
            if(correspondingTables != null) {
                Smell s = initSmell(entity)
                        .setName("NCRF")
                        .setComponent(String.join(",", correspondingTables))
                        .setIntensity(correspondingTables.size() + 0.0);
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Additional Null Values
     * @param entities
     * @return
     */
    public List<Smell> ANV(List<Declaration> entities){
        List<Smell> result = new ArrayList<>();
        String components = "";
        for(Declaration entity: entities){
            List<Declaration> superClasses = getEntitiesWithTableAnnotation(getSuperClassDeclarations(entity));
            if(superClasses.size() <1) continue;

            Declaration topClass = superClasses.get(superClasses.size()-1);
            Set<String> correspondingTables = inheritanceMap.get(topClass.getName());
            if(correspondingTables == null) continue;
            correspondingTables.remove(entity.getName());
            int numCorrespondingFields = 0;
            int numOwnFields = 0;

            for(String table: correspondingTables){
                Declaration t = entities.stream().filter(e->e.getName().equals(table)).findFirst().orElse(null);

                List<Parametre> fields;
                if(t!=null){
                    fields = t.getFields();
                    List<String> fieldNames = fields.stream().filter(f->f.getAnnotations().contains("@Id")).map(ff->entity.getName()+"::"+ff.getName()).collect(Collectors.toList());
                    components += String.join(",", fieldNames);
                    numCorrespondingFields += fieldNames.size();
                }

            }
            List<Parametre> ownFields = entity.getFields();
            List<String> fieldNames = ownFields.stream().filter(f->f.getAnnotations().contains("@Id")).map(ff->entity.getName()+"::"+ff.getName()).collect(Collectors.toList());
            components += " | ";
            components += String.join(",", fieldNames);
            numOwnFields = fieldNames.size();

            Smell s = initSmell(entity)
                    .setName("NCRF")
                    .setComponent(components)
                    .setIntensity(numOwnFields * numCorrespondingFields + 0.0);
            result.add(s);
        }
        return result;
    }

    public void exec(List<CompilationUnit> cus){
        List<Declaration> entities = new ArrayList<>();
        for(CompilationUnit cu: cus){
            for(TypeDeclaration td: cu.getTypes()){
                Declaration d = findTypeDeclaration(td.getNameAsString(), cus, 1);
                if(d!=null) {
                    entities.add(d);
                }
            }
        }
        entities = getEntitiesWithTableAnnotation(entities);
        initInheritance(entities);
        TATI(entities);
        NCT(entities);
        NCRF(entities);
        ANV(entities);

    }
}
