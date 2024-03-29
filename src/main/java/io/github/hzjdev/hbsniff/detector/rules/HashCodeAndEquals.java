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
     * @param clazz entity class
     * @return hashCode method Declaration
     */
    protected final Declaration getEqualsMethod(final Declaration clazz) {
        if (clazz == null) return null;
        boolean check = false;
        Declaration toJudge = clazz.findDeclaration(EQUALS_METHOD_NAME);
        if (toJudge != null) {
            List<ParametreOrField> params = toJudge.getParametres();
            if (params != null && params.size() == 1) {
                ParametreOrField p = params.get(0);
                check = p.getType().equals(Object_TYPE_EXPR);
            }
        }
        if (!check) {
            List<Declaration> superClassDeclarations = getSuperClassDeclarations(clazz);
            for (Declaration superClassEntity : superClassDeclarations) {
                if(superClassEntity.equals(clazz)) continue;
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
                if(superClassEntity.equals(classNode)) continue;
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
    public final List<Smell> hashCodeAndEqualsUseIdentifierPropertyRule(Set<Declaration> classes) {
        List<Smell> result = new ArrayList<>();
        for (Declaration clazz : classes) {
            String comment = "";
            boolean equalsSmelly = false;
            boolean hashCodeSmelly = false;

            boolean annotationData = clazz.annotationIncludes(EQUALS_AND_HASH_CODE_ANNOT_EXPR) || clazz.annotationIncludes(DATA_ANNOT_EXPR);;
            if(annotationData){
                equalsSmelly = true;
                hashCodeSmelly = true;
                comment += ("Using Lombok Annotation.");
            };

            Declaration equalsMethod = getEqualsMethod(clazz);
            Declaration hashCodeMethod = getHashCodeMethod(clazz);

            ParametreOrField field = getIdentifierProperty(clazz);

            Set<String> accessedFieldsEquals = null;
            Set<String> accessedFieldsHash = null;
            List<Declaration> scopeOfFieldAccess = new ArrayList<>();
            scopeOfFieldAccess.add(clazz);
            scopeOfFieldAccess.addAll(getSuperClassDeclarations(clazz));
            if (equalsMethod != null) {
                equalsSmelly = equalsSmelly && equalsMethod.checkMethodCalled(REFLECTION_EQUALS_CALL);
                if (!equalsSmelly) {
                    accessedFieldsEquals = equalsMethod.getAccessedFieldNames(scopeOfFieldAccess);
                    equalsSmelly = accessedFieldsEquals != null && field != null && accessedFieldsEquals.contains(field.getName());
                    if (equalsSmelly) {
                        comment += ("Using ID <"
                                + field.getName() + "> from equals. ");
                    }
                } else {
                    comment += ("Using Reflection Equals.");
                }
            }
            if (hashCodeMethod != null) {
                hashCodeSmelly = hashCodeSmelly && hashCodeMethod.checkMethodCalled(REFLECTION_HASHCODE_CALL);

                if (!hashCodeSmelly) {
                    accessedFieldsHash = hashCodeMethod.getAccessedFieldNames(scopeOfFieldAccess);
                    hashCodeSmelly = accessedFieldsHash != null && field != null && accessedFieldsHash.contains(field.getName());
                    if (hashCodeSmelly) {
                        comment += ("Using ID <"
                                + field.getName() + "> from hashCode. ");
                    }
                } else {
                    comment += ("Using Reflection Hashcode.");
                }
            }

            if ((equalsSmelly || hashCodeSmelly) && !comment.equals("")) {
                Smell smell = initSmell(clazz).setName("UsingIdInHashCodeOrEquals").setComment(comment);
                psr.getSmells().get(clazz).add(smell);
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
        for (Declaration clazz : classes) {
            boolean annotationData = clazz.annotationIncludes(EQUALS_AND_HASH_CODE_ANNOT_EXPR) || clazz.annotationIncludes(DATA_ANNOT_EXPR);
            if(annotationData) continue;

            Declaration equalsMethod = getEqualsMethod(clazz);
            Declaration hashCodeMethod = getHashCodeMethod(clazz);
            if (equalsMethod == null) {
                Smell smell = initSmell(clazz).setName("MissingEquals");
                psr.getSmells().get(clazz).add(smell);
                result.add(smell);
            }
            if (hashCodeMethod == null) {
                Smell smell = initSmell(clazz).setName("MissingHashCode");
                psr.getSmells().get(clazz).add(smell);
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
        List<Smell> idRule = hashCodeAndEqualsUseIdentifierPropertyRule(entityDeclarations);
        idRule.addAll(hashCodeAndEqualsRule(entityDeclarations));
        return idRule;
    }
}
