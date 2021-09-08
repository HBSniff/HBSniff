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
import io.github.hzjdev.hbsniff.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hbsniff.utils.Const.*;

public class GetterSetter extends SmellDetector {


    /**
     * Verifies if exists set method for the field.
     *
     * @param fieldNode  The field contained in the class.
     * @param entityNode Entity that contains the field.
     * @return True if exists set method in the entity for the field.
     */
    protected final boolean hasSetMethod(final ParametreOrField fieldNode, final Declaration entityNode) {
        String fieldName = fieldNode.getName();
        String type = fieldNode.getType();

        String setName = SETTER_METHOD_PREFIX + fieldName.substring(0, 1).toUpperCase()
                + fieldName.substring(1);

        Declaration methodNode = entityNode.findDeclaration(setName);

        if (methodNode == null) {
            return false;
        }

        if ((methodNode.getParametres() != null) && !(methodNode.getParametres().size() == 1 && methodNode.getParametres().get(0).getType().equals(type))) {
            return false;
        }

        String methodName = methodNode.getName();
        String methodType = methodNode.getReturnTypeName();

        return methodName.equals(setName) && (methodType.equals(VOID_TYPE_EXPR) || methodType.equals(entityNode.getName()));
    }

    /**
     * Verifies if exists get method for the field.
     *
     * @param fieldNode  The field contained in the class.
     * @param entityNode Entity that contains the field.
     * @return True if exists get method in the entity for the field.
     */
    protected final boolean hasGetMethod(final ParametreOrField fieldNode, final Declaration entityNode) {

        String shortFieldName = fieldNode.getName();
        String type = fieldNode.getType();
        String strGetOrIs;

        if (type != null && (type.equals(Utils.BOOLEAN_PRIMITIVE) || type.equals(Utils.BOOLEAN_CLASS))) {
            strGetOrIs = GETTER_METHOD_PREFIX_BOOL;
        } else {
            strGetOrIs = GETTER_METHOD_PREFIX_NORMAL;
        }

        String methodGetField = strGetOrIs + shortFieldName.substring(0, 1).toUpperCase()
                + shortFieldName.substring(1);

        Declaration methodNode = entityNode.findDeclaration(methodGetField);

        if (methodNode == null || (methodNode.getParametres() != null && methodNode.getParametres().size() > 0)) {
            return false;
        }

        String methodName = methodNode.getName();
        String methodType = methodNode.getReturnTypeName();

        return methodName.equals(methodGetField) && methodType.equals(type);
    }


    /**
     * detection methods
     * @param classes Entity Declarations
     * @return results
     */
    public final List<Smell> provideGetsSetsFieldsRule(Set<Declaration> classes) {

        List<Smell> smells = new ArrayList<>();
        boolean annotationGetter = false;
        boolean annotationSetter = false;

        for (Declaration entityNode : classes) {
            for(String node: entityNode.getAnnotations()){
                if(node.contains(GETTER_ANNOT_EXPR)){
                    annotationGetter = true;
                }
                if(node.contains(SETTER_ANNOT_EXPR)){
                    annotationSetter = true;
                }
            }

            StringBuilder comment = new StringBuilder();
            List<ParametreOrField> declaredFields = entityNode.getFields();

            boolean passed = true;

            for (ParametreOrField fieldNode : declaredFields) {

                if (fieldNode.isStatic()) {
                    continue;
                }

                if (!annotationGetter && !hasGetMethod(fieldNode, entityNode)) {
                    comment.append("The field <").append(fieldNode.getName()).append("> of the class <").append(entityNode.getName()).append(" doesn't implement the get method.\n");
                    passed = false;
                }
                if (!annotationSetter && !hasSetMethod(fieldNode, entityNode)) {
                    comment.append("The field <").append(fieldNode.getName()).append("> of the class <").append(entityNode.getName()).append(" doesn't implement the set method.\n");
                    passed = false;
                }
            }

            if (!passed) {
                Smell smell = initSmell(entityNode).setName("GetterSetter").setComment(comment.toString());
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }
        return smells;
    }

    /**
     * execute detection
     * @return list of smells
     */
    public List<Smell> exec() {
        return provideGetsSetsFieldsRule(entityDeclarations);
    }

}
