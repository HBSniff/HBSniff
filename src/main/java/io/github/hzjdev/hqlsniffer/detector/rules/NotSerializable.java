package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getSuperClassDeclarations;
import static io.github.hzjdev.hqlsniffer.utils.Const.SERIALIZABLE_ANNOT_EXPR;

public class NotSerializable extends SmellDetector {

    /**
     * detection methods
     * @param classes Entity Declarations
     * @return results
     */
    public final List<Smell> checkRule(Set<Declaration> classes) {
        List<Smell> smells = new ArrayList<>();
        for (Declaration entityNode : classes) {
            boolean pass = false;
            String serializable = SERIALIZABLE_ANNOT_EXPR;
            List<Declaration> toDetect = getSuperClassDeclarations(entityNode);
            toDetect.add(entityNode);
            for (Declaration superclass : toDetect) {
                if (pass) {
                    break;
                }
                for (String i : superclass.getImplementedInterface()) {
                    if (i.equals(serializable)) {
                        pass = true;
                        break;
                    }
                }
            }
            if (!pass) {
                Smell smell = initSmell(entityNode).setName("NotSerializable");
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
        return checkRule(entityDeclarations);
    }

}
