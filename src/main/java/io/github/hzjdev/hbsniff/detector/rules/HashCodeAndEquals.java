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

import io.github.hzjdev.hbsniff.detector.SmellDetector;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.ParametreOrField;
import io.github.hzjdev.hbsniff.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hbsniff.parser.EntityParser.getIdentifierProperty;
import static io.github.hzjdev.hbsniff.parser.EntityParser.getSuperClassDeclarations;
import static io.github.hzjdev.hbsniff.utils.Const.*;

public class HashCodeAndEquals extends SmellDetector {

    /**
     * get equals method of a class
     * @param classNode entity class
     * @return hashCode method Declaration
     */
    protected final Declaration getEqualsMethod(final Declaration classNode) {
        if (classNode == null) return null;
        boolean check = false;
        Declaration toJudge = classNode.findDeclaration(EQUALS_METHOD_NAME);
        if (toJudge != null) {
            List<ParametreOrField> params = toJudge.getParametres();
            if (params != null && params.size() == 1) {
                ParametreOrField p = params.get(0);
                check = p.getType().equals(Object_TYPE_EXPR);
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

    /**
     * get hashCode method of a class
     * @param classNode entity class
     * @return hashCode method Declaration
     */
    protected final Declaration getHashCodeMethod(final Declaration classNode) {
        if (classNode == null) return null;
        boolean check = false;
        Declaration toJudge = classNode.findDeclaration(HASHCODE_METHOD_NAME);
        if (toJudge != null) {
            List<ParametreOrField> params = toJudge.getParametres();
            check = params == null || params.size() == 0;
        }
        if (!check) {
            for (Declaration superClassEntity : getSuperClassDeclarations(classNode)) {
                toJudge = getHashCodeMethod(superClassEntity);
                if (toJudge != null) {
                    return toJudge;
                }
            }
        }
        return toJudge;
    }

    /**
     * lacking identifier field in hashcode or equals smell detection
     * @param classes entities
     * @return list of smells
     */
    public final List<Smell> hashCodeAndEqualsNotUseIdentifierPropertyRule(Set<Declaration> classes) {
        List<Smell> result = new ArrayList<>();
        for (Declaration entityNode : classes) {
            String comment = "";
            boolean annotationData = entityNode.annotationIncludes(EQUALS_AND_HASH_CODE_ANNOT_EXPR) || entityNode.annotationIncludes(DATA_ANNOT_EXPR);;
            if(annotationData) continue;

            Declaration equalsMethod = getEqualsMethod(entityNode);
            Declaration hashCodeMethod = getHashCodeMethod(entityNode);

            ParametreOrField field = getIdentifierProperty(entityNode);

            Set<String> accessedFieldsEquals = null;
            Set<String> accessedFieldsHash = null;
            List<Declaration> parents = getSuperClassDeclarations(entityNode);
            parents.add(entityNode);
            boolean equalsOk = false;
            boolean hashCodeOk = false;
            if(equalsMethod!=null) {
                equalsOk = equalsMethod.checkMethodCalled(REFLECTION_EQUALS_CALL);
                if (!equalsOk) {
                    accessedFieldsEquals = equalsMethod.getAccessedFieldNames(parents);
                    equalsOk = accessedFieldsEquals != null && field != null && accessedFieldsEquals.contains(field.getName());
                }
                if (!equalsOk) {
                    comment += ("The class does not contain the identifier property <"
                            + field.getName() + "> in the equals method.\n");
                }
            }else{
                comment += ("Equals method not implemented.\n");
            }
            if(hashCodeMethod!=null) {
                hashCodeOk = hashCodeMethod.checkMethodCalled(REFLECTION_HASHCODE_CALL);
                if (!hashCodeOk) {
                    accessedFieldsHash = hashCodeMethod.getAccessedFieldNames(parents);
                    hashCodeOk = accessedFieldsHash != null && field != null && accessedFieldsHash.contains(field.getName());
                }
                if (!hashCodeOk) {
                    comment += ("The class does not contain the identifier property <"
                            + field.getName() + "> in the hashCode method.\n");
                }
            }else{
                comment += ("HashCode method not implemented.\n");
            }

            if ((!equalsOk || !hashCodeOk )&& !comment.equals("")) {
                Smell smell = initSmell(entityNode).setName("MissingIdInHashCodeOrEquals").setComment(comment);
                psr.getSmells().get(entityNode).add(smell);
                result.add(smell);
            }
        }
        return result;
    }

    /**
     * lacking hashcode and equals smell detection
     * @param classes entities
     * @return list of smells
     */
    public final List<Smell> hashCodeAndEqualsRule(Set<Declaration> classes) {
        List<Smell> result = new ArrayList<>();
        for (Declaration entityNode : classes) {
            boolean annotationData = entityNode.annotationIncludes(EQUALS_AND_HASH_CODE_ANNOT_EXPR) || entityNode.annotationIncludes(DATA_ANNOT_EXPR);
            if(annotationData) continue;

            Declaration equalsMethod = getEqualsMethod(entityNode);
            Declaration hashCodeMethod = getHashCodeMethod(entityNode);
            if (equalsMethod == null) {
                Smell smell = initSmell(entityNode).setName("MissingEquals");
                psr.getSmells().get(entityNode).add(smell);
                result.add(smell);
            }
            if (hashCodeMethod == null) {
                Smell smell = initSmell(entityNode).setName("MissingHashCode");
                psr.getSmells().get(entityNode).add(smell);
                result.add(smell);
            }
        }
        return result;
    }

    /**
     * execute detection
     * @return list of smells
     */
    public List<Smell> exec() {
        List<Smell> idRule = hashCodeAndEqualsNotUseIdentifierPropertyRule(entityDeclarations);
        idRule.addAll(hashCodeAndEqualsRule(entityDeclarations));
        return idRule;
    }
}
