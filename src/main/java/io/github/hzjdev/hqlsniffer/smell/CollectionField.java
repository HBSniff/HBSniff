package io.github.hzjdev.hqlsniffer.smell;

import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.Parametre;
import io.github.hzjdev.hqlsniffer.Smell;
import io.github.hzjdev.hqlsniffer.Utils;

import java.util.List;
import java.util.Set;

public class CollectionField extends SmellDetector{

    public List<Smell> useInterfaceSetOrListRule(Set<Declaration> allModelClasses) {

        for (Declaration entityNode : allModelClasses) {

            List<Parametre> declaredFields = entityNode.getFields();
            boolean passed = true;

            for (Parametre fieldNode : declaredFields) {
                String type = fieldNode.getType();
                if(type.contains("<")){
                    type = type.split("<")[0];
                }
                if (Utils.isCollection(type) && !type.equals(Utils.SET_NAME)
                        && !type.equals(Utils.LIST_NAME)) {
                    addReport("The field <"
                            + fieldNode.getName()
                            + "> of the class <" + entityNode.getName()
                            + " implements interface Collection but it "
                            + "doesn't implements interface Set or interface List.\n");
                    passed = false;
                    addResultFalse(entityNode);
                }
            }

            if (!passed) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return isEmptyReport();
    }

    public List<Smell> useListCollectionRule(Set<Declaration> allModelClasses) {

        for (Declaration entityNode : allModelClasses) {

            List<Parametre> declaredFields = entityNode.getFields();
            boolean passed = true;

            for (Parametre fieldNode : declaredFields) {
                String type = fieldNode.getType();
                if(type.contains("<")){
                    type = type.split("<")[0];
                }
                if (Utils.isCollection(type) && !Utils.isList(type)) {
                    addReport("The field <" + fieldNode.getName()
                            + "> of the class <" + fieldNode.getName()
                            + " implements interface Collection but "
                            + "it doesn't implements interface Set.\n");
                    passed = false;
                    addResultFalse(entityNode);
                }
            }

            if (!passed) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return isEmptyReport();
    }

    public List<Smell> useSetCollectionRule(Set<Declaration> allModelClasses) {

        for (Declaration entityNode : allModelClasses) {

            List<Parametre> declaredFields = entityNode.getFields();
            boolean passed = true;

            for (Parametre fieldNode : declaredFields) {
                String type = fieldNode.getType();
                if(type.contains("<")){
                    type = type.split("<")[0];
                }
                if (Utils.isCollection(type) && !Utils.isSet(type)) {
                    addReport("The field <" + fieldNode.getName()
                            + "> of the class <" + fieldNode.getName()
                            + " implements interface Collection but "
                            + "it doesn't implements interface Set.\n");
                    passed = false;
                    addResultFalse(entityNode);
                }
            }

            if (!passed) {
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return isEmptyReport();
    }

    public List<Smell> exec() {
        List<Smell> interfaces = useInterfaceSetOrListRule(declarations);
        interfaces.addAll(useSetCollectionRule(declarations));
        return interfaces;
    }

}
