package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.model.output.Smell;
import io.github.hzjdev.hqlsniffer.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GetterSetter extends SmellDetector {


    /**
     * Verifies if exists set method for the field.
     *
     * @param fieldNode  The field contained in the class.
     * @param entityNode Entity that contains the field.
     * @return True if exists set method in the entity for the field.
     */
    protected final boolean hasSetMethod(final Parametre fieldNode, final Declaration entityNode) {
        String fieldName = fieldNode.getName();
        String type = fieldNode.getType();

        String setName = "set" + fieldName.substring(0, 1).toUpperCase()
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

        return methodName.equals(setName) && (methodType.equals("void") || methodType.equals(entityNode.getName()));
    }

    /**
     * Verifies if exists get method for the field.
     *
     * @param fieldNode  The field contained in the class.
     * @param entityNode Entity that contains the field.
     * @return True if exists get method in the entity for the field.
     */
    protected final boolean hasGetMethod(final Parametre fieldNode, final Declaration entityNode) {

        String shortFieldName = fieldNode.getName();
        String type = fieldNode.getType();
        String strGetOrIs;

        if (type != null && (type.equals(Utils.BOOLEAN_PRIMITIVE) || type.equals(Utils.BOOLEAN_CLASS))) {
            strGetOrIs = "is";
        } else {
            strGetOrIs = "get";
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


    public final List<Smell> provideGetsSetsFieldsRule(Set<Declaration> classes) {

        List<Smell> smells = new ArrayList<>();

        for (Declaration entityNode : classes) {
            StringBuilder comment = new StringBuilder();
            List<Parametre> declaredFields = entityNode.getFields();

            boolean passed = true;

            for (Parametre fieldNode : declaredFields) {

                if (fieldNode.isStatic()) {
                    continue;
                }

                if (!hasGetMethod(fieldNode, entityNode)) {
                    comment.append("The field <").append(fieldNode.getName()).append("> of the class <").append(fieldNode.getName()).append(" doesn't implement the get method.\n");
                    passed = false;
                }
                if (!hasSetMethod(fieldNode, entityNode)) {
                    comment.append("The field <").append(fieldNode.getName()).append("> of the class <").append(fieldNode.getName()).append(" doesn't implement the set method.\n");
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

    public List<Smell> exec() {
        return provideGetsSetsFieldsRule(entityDeclarations);
    }

}
