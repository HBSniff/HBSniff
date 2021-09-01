package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getIdentifierProperty;

public class MissingIdentifier extends SmellDetector {

    public List<Smell> provideIdentifierPropertyRule(Set<Declaration> classes) {
        List<Smell> smells = new ArrayList<>();
        for (Declaration entityNode : classes) {
            Parametre field = getIdentifierProperty(entityNode);
            if (field == null) {
                Smell smell = initSmell(entityNode).setName("MissingId");
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }
        return smells;
    }


    public List<Smell> exec() {
        return provideIdentifierPropertyRule(entityDeclarations);
    }


}
