package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.hzjdev.hqlsniffer.utils.Utils.extractTypeFromExpression;

public class MissingManyToOne extends SmellDetector {


    private Parametre locateMissingManyToOneField(String targetTypeName, String typeName) {
        if (targetTypeName != null) {
            String type = extractTypeFromExpression(targetTypeName);
            Declaration d = entityDeclarations.stream().filter(i -> i.getName().equals(type)).findFirst().orElse(null);
            if (d != null) {
                for (Parametre targetField : d.getFields()) {
                    if (!targetField.getType().equals(typeName)) {
                        continue;
                    }
                    if (!targetField.annotationIncludes("ManyToOne")) {
                        return targetField;
                    }
                }
            }
        }
        return null;
    }

    public List<Smell> getOneToManyNPlusOne(Set<Declaration> entities) {
        List<Smell> result = new ArrayList<>();
        for (Declaration parentDeclaration : entities) {
            CompilationUnit cu = parentDeclaration.getRawCU();
            for (TypeDeclaration cuType : cu.getTypes()) {
                String typeName = cuType.getNameAsString();
                List<AnnotationExpr> annotations = cuType.findAll(AnnotationExpr.class);
                for (AnnotationExpr annotation : annotations) {
                    if (annotation.getNameAsString().contains("OneToMany")) {
                        Optional<Node> parentField = annotation.getParentNode();
                        while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
                            parentField = parentField.get().getParentNode();
                        }
                        if (parentField.isPresent()) {
                            FieldDeclaration pf = (FieldDeclaration) parentField.get();
                            final Smell smell = initSmell(parentDeclaration);
                            for (VariableDeclarator vd : pf.getVariables()) {
                                Type t = vd.getType();
                                Parametre targetField = locateMissingManyToOneField(t.toString(), typeName);
                                if (targetField != null) {
                                    smell.setComment(targetField.getName() + "::" + parentField.toString())
                                            .setName("MissingManyToOne");
                                    smell.setPosition(targetField.getPosition());
                                    psr.getSmells().get(parentDeclaration).add(smell);
                                    result.add(smell);
                                }
                            }
                        }
                    }

                }
            }
        }
        return result;
    }

    public List<Smell> exec() {
        return getOneToManyNPlusOne(entityDeclarations);
    }

}
