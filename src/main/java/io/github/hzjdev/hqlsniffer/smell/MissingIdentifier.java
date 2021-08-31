package io.github.hzjdev.hqlsniffer.smell;

import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.Parametre;
import io.github.hzjdev.hqlsniffer.Smell;

import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getIdentifierProperty;

public class MissingIdentifier extends SmellDetector{


    public List<Smell> provideIdentifierPropertyRule(Set<Declaration> classes) {


        for (Declaration entityNode : classes) {

            Parametre field = getIdentifierProperty(entityNode);

            if (field == null) {
                addReport("The class <" + entityNode.getName()
                        + " doesn't provide identifier property.\n");
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return isEmptyReport();
    }


    public List<Smell> exec() {
        return provideIdentifierPropertyRule(declarations);
    }


}
