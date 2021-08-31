package io.github.hzjdev.hqlsniffer.smell;

import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.Smell;

import java.util.List;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.getSuperClassDeclarations;

public class NotSerializable extends SmellDetector{


    public final List<Smell> checkRule(Set<Declaration> classes) {
        for (Declaration entityNode : classes) {
            boolean pass = false;
            String serializable = "Serializable";
            for(Declaration superclass: getSuperClassDeclarations(entityNode)){
                if(pass){
                    break;
                }
                for(String i:superclass.getImplementedInterface()){
                    if(i.equals(serializable)){
                        addResultTrue(entityNode);
                        pass = true;
                        break;
                    }
                }
            }
            if(!pass) {
                addReport("The class <" + entityNode.getName() + "> "
                        + "doesn't implements interface Serializable.\n");
                addResultFalse(entityNode);
            }
        }

        return isEmptyReport();
    }

    public List<Smell> exec() {
        return checkRule(declarations);
    }

}
