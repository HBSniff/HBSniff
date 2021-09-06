package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.model.output.Smell;
import io.github.hzjdev.hqlsniffer.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollectionField extends SmellDetector {

    public List<Smell> useInterfaceSetOrListRule(Set<Declaration> allModelClasses) {
        List<Smell> smells = new ArrayList<>();

        for (Declaration entityNode : allModelClasses) {

            List<Parametre> declaredFields = entityNode.getFields();
            boolean passed = true;
            StringBuilder comment = new StringBuilder();
            for (Parametre fieldNode : declaredFields) {
                String type = fieldNode.getType();
                if (type.contains("<")) {
                    type = type.split("<")[0];
                }
                if (Utils.isCollection(type) && !type.equals(Utils.SET_NAME)
                        && !type.equals(Utils.LIST_NAME)) {
                    comment.append("The field <").append(fieldNode.getName()).append("> of the class <").append(entityNode.getName()).append("> implements interface Collection but it ").append("doesn't implements interface Set or interface List.\n");
                    passed = false;
                }
            }

            if (!passed) {
                Smell smell = initSmell(entityNode).setName("CollectionField").setComment(comment.toString());
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }
        return smells;
    }


    public List<Smell> useSetCollectionRule(Set<Declaration> allModelClasses) {
        List<Smell> smells = new ArrayList<>();

        for (Declaration entityNode : allModelClasses) {

            List<Parametre> declaredFields = entityNode.getFields();
            boolean passed = true;
            StringBuilder comment = new StringBuilder();

            for (Parametre fieldNode : declaredFields) {
                String type = fieldNode.getType();
                if (type.contains("<")) {
                    type = type.split("<")[0];
                }
                if (Utils.isCollection(type) && !Utils.isSet(type)) {
                    comment.append("The field <").append(fieldNode.getName()).append("> of the class <").append(entityNode.getName()).append(" implements interface Collection but ").append("it doesn't implements interface Set.\n");
                    passed = false;
                }
            }

            if (!passed) {
                Smell smell = initSmell(entityNode).setName("CollectionField").setComment(comment.toString());
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }
        return smells;
    }

    public List<Smell> exec() {
        List<Smell> interfaces = useInterfaceSetOrListRule(entityDeclarations);
        interfaces.addAll(useSetCollectionRule(entityDeclarations));
        return interfaces;
    }

}
