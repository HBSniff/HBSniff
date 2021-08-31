package io.github.hzjdev.hqlsniffer.smell;

import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.Parametre;
import io.github.hzjdev.hqlsniffer.Smell;

import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getIdentifierProperty;
import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getSuperClassDeclarations;

public class HashCodeAndEquals extends SmellDetector{


    protected final Declaration getEqualsMethod(final Declaration classNode) {
        if(classNode==null) return null;
        boolean check = false;
        Declaration toJudge = classNode.findDeclaration("equals");
        if(toJudge!=null) {
            List<Parametre> params = toJudge.getParametres();
            if (params != null && params.size() == 1) {
                Parametre p = params.get(0);
                check = p.getType().equals("Object");
            }
        }
        if(!check){
            for(Declaration superClassEntity : getSuperClassDeclarations(classNode)){
                toJudge = getEqualsMethod(superClassEntity);
                if(toJudge != null){
                    return toJudge;
                }
            }
        }
        return toJudge;
    }


    protected final Declaration getHashCodeMethod(final Declaration classNode) {
        if(classNode==null) return null;
        boolean check = false;
        Declaration toJudge = classNode.findDeclaration("hashCode");
        if(toJudge!=null) {
            List<Parametre> params = toJudge.getParametres();
            check = params == null || params.size() == 0;
        }
        if(!check){
            for(Declaration superClassEntity : getSuperClassDeclarations(classNode)){
                toJudge = getEqualsMethod(superClassEntity);
                if(toJudge != null){
                    return toJudge;
                }
            }
        }
        return toJudge;
    }
    public final List<Smell> hashCodeAndEqualsNotUseIdentifierPropertyRule(Set<Declaration> classes) {
        for (Declaration entityNode : classes) {
            Declaration equalsMethod = getEqualsMethod(entityNode);
            Declaration hashCodeMethod = getHashCodeMethod(entityNode);

            Parametre field = getIdentifierProperty(entityNode);

            Set<String> accessedFieldsEquals = null;
            Set<String> accessedFieldsHash = null;
            List<Declaration> parents = getSuperClassDeclarations(entityNode);
            parents.add(entityNode);
            if (equalsMethod != null) {
                accessedFieldsEquals = equalsMethod.getAccessedFieldNames(parents);
            }
            if (hashCodeMethod != null) {
                accessedFieldsHash = hashCodeMethod.getAccessedFieldNames(parents);
            }

            boolean contem = false;
            if (accessedFieldsEquals != null && field!=null && accessedFieldsEquals.contains(field.getName())) {
                addReport("The class <" + entityNode.getName()
                        + "> contains the identifier property <"
                        + field.getName() + "> in the equals method.\n");
                contem = true;
            }

            if (accessedFieldsHash != null && field!=null && accessedFieldsHash.contains(field.getName())) {
                addReport("The class <" + entityNode.getName()
                        + "> contains the identifier property <"
                        + field.getName() + "> in the hashCode method.\n");
                contem = true;
            }
            if (contem) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return isEmptyReport();
    }

    public final List<Smell> hashCodeAndEqualsRule(Set<Declaration> classes) {
        for (Declaration entityNode : classes) {

            Declaration equalsMethod = getEqualsMethod(entityNode);
            Declaration hashCodeMethod = getHashCodeMethod(entityNode);

            boolean contem = equalsMethod != null && hashCodeMethod != null;

            if (!(contem)) {
                if (equalsMethod == null) {
                    addReport("The class <" + entityNode.getName()
                            + "> doesn't contain the equals method.\n");
                }
                if (hashCodeMethod == null) {
                    addReport("The class <" + entityNode.getName()
                            + "> doesn't contain the hashCode method.\n");
                }
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return isEmptyReport();
    }

    public List<Smell> exec() {
        List<Smell> idRule = hashCodeAndEqualsNotUseIdentifierPropertyRule(declarations);
        idRule.addAll(hashCodeAndEqualsRule(declarations));
        return idRule;
    }


}
