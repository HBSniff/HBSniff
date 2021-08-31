package io.github.hzjdev.hqlsniffer.smell;

import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.Parametre;
import io.github.hzjdev.hqlsniffer.Smell;

import java.util.List;
import java.util.Set;

public class MissingNoArgumentConstructor extends SmellDetector{

    public final List<Smell> noArgumentConstructorRule(Set<Declaration> classes) {


        for (Declaration entityNode : classes) {

            // Checks the class and the inherited methods from the super class
            List<Declaration> constructors = entityNode.getConstructors();
            boolean passed = false;

            for (Declaration methodNode : constructors) {
                List<Parametre> parameters = methodNode.getParametres();
                if (parameters.isEmpty()) {
                    passed = true;
                    break;
                }
            }

            if (!passed) {
                addReport("The class <" + entityNode.getName()
                        + "> doesn't contain a default constructor.\n");
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return isEmptyReport();
    }

    public List<Smell> exec() {
        return noArgumentConstructorRule(declarations);
    }

}
