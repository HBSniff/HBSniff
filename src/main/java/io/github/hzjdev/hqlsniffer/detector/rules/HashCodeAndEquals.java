package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getIdentifierProperty;
import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getSuperClassDeclarations;

public class HashCodeAndEquals extends SmellDetector {

    protected final Declaration getEqualsMethod(final Declaration classNode) {
        if (classNode == null) return null;
        boolean check = false;
        Declaration toJudge = classNode.findDeclaration("equals");
        if (toJudge != null) {
            List<Parametre> params = toJudge.getParametres();
            if (params != null && params.size() == 1) {
                Parametre p = params.get(0);
                check = p.getType().equals("Object");
            }
        }
        if (!check) {
            for (Declaration superClassEntity : getSuperClassDeclarations(classNode)) {
                toJudge = getEqualsMethod(superClassEntity);
                if (toJudge != null) {
                    return toJudge;
                }
            }
        }
        return toJudge;
    }


    protected final Declaration getHashCodeMethod(final Declaration classNode) {
        if (classNode == null) return null;
        boolean check = false;
        Declaration toJudge = classNode.findDeclaration("hashCode");
        if (toJudge != null) {
            List<Parametre> params = toJudge.getParametres();
            check = params == null || params.size() == 0;
        }
        if (!check) {
            for (Declaration superClassEntity : getSuperClassDeclarations(classNode)) {
                toJudge = getEqualsMethod(superClassEntity);
                if (toJudge != null) {
                    return toJudge;
                }
            }
        }
        return toJudge;
    }

    public final List<Smell> hashCodeAndEqualsNotUseIdentifierPropertyRule(Set<Declaration> classes) {
        List<Smell> result = new ArrayList<>();
        for (Declaration entityNode : classes) {
            String comment = "";
            boolean smelly = false;
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

            if (accessedFieldsEquals != null && field != null && !accessedFieldsEquals.contains(field.getName())) {
                comment += ("The class does not contain the identifier property <"
                        + field.getName() + "> in the equals method.\n");
                smelly = true;
            }

            if (accessedFieldsHash != null && field != null && !accessedFieldsHash.contains(field.getName())) {
                comment += ("The class does not contain the identifier property <"
                        + field.getName() + "> in the hashCode method.\n");
                smelly = true;
            }
            if (smelly) {
                Smell smell = initSmell(entityNode).setName("HashCodeAndEqualsRule").setComment(comment);
                psr.getSmells().get(entityNode).add(smell);
                result.add(smell);
            }
        }
        return result;
    }

    public final List<Smell> hashCodeAndEqualsRule(Set<Declaration> classes) {
        List<Smell> result = new ArrayList<>();
        for (Declaration entityNode : classes) {
            Declaration equalsMethod = getEqualsMethod(entityNode);
            Declaration hashCodeMethod = getHashCodeMethod(entityNode);
            String comment = "";
            if (equalsMethod == null) {
                comment += "Missing equals method.\n";
                Smell smell = initSmell(entityNode).setName("HashCodeAndEqualsRule").setComment(comment);
                psr.getSmells().get(entityNode).add(smell);
                result.add(smell);
            }
            if (hashCodeMethod == null) {
                comment += "Missing hashCode method.\n";
                Smell smell = initSmell(entityNode).setName("HashCodeAndEqualsRule").setComment(comment);
                psr.getSmells().get(entityNode).add(smell);
                result.add(smell);
            }
        }
        return result;
    }

    public List<Smell> exec() {
        List<Smell> idRule = hashCodeAndEqualsNotUseIdentifierPropertyRule(entityDeclarations);
        idRule.addAll(hashCodeAndEqualsRule(entityDeclarations));
        return idRule;
    }
}
