package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FinalEntity extends SmellDetector {
    public List<Smell> noFinalClassRule(Set<Declaration> classes) {
        List<Smell> smells = new ArrayList<>();
        for (Declaration entityNode : classes) {
            ClassOrInterfaceDeclaration cid = entityNode.getClassDeclr();
            if (cid == null) continue;
            if (cid.getModifiers() != null && cid.getModifiers().contains(Modifier.finalModifier())) {
                Smell smell = initSmell(entityNode).setName("HashCodeAndEqualsRule");
                psr.getSmells().get(entityNode).add(smell);
                smells.add(smell);
            }
        }
        return smells;
    }

    public List<Smell> exec() {
        return noFinalClassRule(entityDeclarations);
    }

}
