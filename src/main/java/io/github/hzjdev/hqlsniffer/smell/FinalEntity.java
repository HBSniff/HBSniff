package io.github.hzjdev.hqlsniffer.smell;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.Smell;

import java.util.List;
import java.util.Set;

public class FinalEntity extends SmellDetector{
    public List<Smell> noFinalClassRule(Set<Declaration> classes) {

        for (Declaration entityNode : classes) {

            ClassOrInterfaceDeclaration cid = entityNode.getClassDeclr();
            if (cid.getModifiers()!=null && cid.getModifiers().contains(Modifier.finalModifier())) {
                addReport("The class <" + entityNode.getName()
                        + "> can't to be a final class.\n");
                addResultFalse(entityNode);
            } else {
                addResultTrue(entityNode);
            }
        }
        return isEmptyReport();
    }

    public List<Smell> exec() {
        return noFinalClassRule(declarations);
    }

}
