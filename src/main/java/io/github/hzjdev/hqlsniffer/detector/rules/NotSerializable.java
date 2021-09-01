package io.github.hzjdev.hqlsniffer.detector.rules;

import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getSuperClassDeclarations;

public class NotSerializable extends SmellDetector {


    public final List<Smell> checkRule(Set<Declaration> classes) {
        List<Smell> smells = new ArrayList<>();
        for (Declaration entityNode : classes) {
            boolean pass = false;
            String serializable = "Serializable";
            for(Declaration superclass: getSuperClassDeclarations(entityNode)){
                if(pass){
                    break;
                }
                for(String i:superclass.getImplementedInterface()){
                    if(i.equals(serializable)){
                        pass = true;
                        break;
                    }
                }
            }
            if(!pass) {
                Smell smell = initSmell(entityNode).setName("NotSerializable");
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }

        return smells;
    }

    public List<Smell> exec() {
        return checkRule(entityDeclarations);
    }

}
