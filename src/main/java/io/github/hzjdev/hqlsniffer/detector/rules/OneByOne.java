package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OneByOne extends SmellDetector {


    private boolean batchSizeExists(FieldDeclaration pf) {
        for (AnnotationExpr fieldAnnotations : pf.getAnnotations()) {
            if (fieldAnnotations.getNameAsString().equals("BatchSize")) {
                return true;
            }
        }
        return false;
    }

    public List<Smell> getOneByOne(Set<Declaration> entities) {
        List<Smell> lazyFetches = new ArrayList<>();
        for (Declaration parentDeclaration : entities) {
            CompilationUnit cu = parentDeclaration.getRawCU();
            List<NormalAnnotationExpr> annotations = cu.findAll(NormalAnnotationExpr.class);
            for (NormalAnnotationExpr annotation : annotations) {
                for (MemberValuePair mvp : annotation.getPairs()) {
                    if (annotation.getNameAsString().contains("ToMany") && mvp.getValue().toString().contains("LAZY")) {
                        Optional<Node> parentField = mvp.getParentNode();
                        while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
                            parentField = parentField.get().getParentNode();
                        }
                        if (parentField.isPresent()) {
                            FieldDeclaration pf = (FieldDeclaration) parentField.get();
                            if (batchSizeExists(pf)) continue;
                            final Smell smell = initSmell(parentDeclaration);
                            for (VariableDeclarator vd : pf.getVariables()) {
                                Type t = vd.getType();
                                if (t != null) {
                                    smell.setComment(parentField.toString())
                                            .setName("One-By-One");
                                    mvp.getRange().ifPresent(s -> smell.setPosition(s.toString()));
                                    lazyFetches.add(smell);
                                    psr.getSmells().get(parentDeclaration).add(smell);
                                }
                            }
                        }
                    }
                }
            }
        }
        return lazyFetches;
    }

    public List<Smell> exec() {
        return getOneByOne(entityDeclarations);
    }
}
