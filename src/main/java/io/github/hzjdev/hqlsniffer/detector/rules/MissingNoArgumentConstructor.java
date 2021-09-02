package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MissingNoArgumentConstructor extends SmellDetector {

    public final List<Smell> noArgumentConstructorRule(Set<Declaration> classes) {
        List<Smell> smells = new ArrayList<>();
        for (Declaration entityNode : classes) {

            // Checks the class and the inherited methods from the super class
            List<Declaration> constructors = entityNode.getConstructors();
            boolean passed = false;

            if(constructors != null) {
                for (Declaration methodNode : constructors) {
                    List<Parametre> parameters = methodNode.getParametres();
                    if (parameters.isEmpty()) {
                        passed = true;
                        break;
                    }
                }
            }

            if (!passed) {
                Smell smell = initSmell(entityNode).setName("MissingNoArgumentConstructor");
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }
        return smells;
    }

    public List<Smell> exec() {
        return noArgumentConstructorRule(entityDeclarations);
    }

}
