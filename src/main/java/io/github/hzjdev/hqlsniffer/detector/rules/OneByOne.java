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

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.findTypeDeclaration;
import static io.github.hzjdev.hqlsniffer.utils.Utils.extractTypeFromExpression;

public class OneByOne extends SmellDetector {

    public List<Smell> getOneByOne(List<CompilationUnit> cus) {
        List<Smell> lazyFetches = new ArrayList<>();
        for (CompilationUnit cu : cus) {
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
                            boolean batchSizeExists = false;
                            for (AnnotationExpr fieldAnnotations : pf.getAnnotations()) {
                                if (fieldAnnotations.getNameAsString().equals("BatchSize")) {
                                    batchSizeExists = true;
                                }
                            }
                            if (batchSizeExists) continue;
                            Declaration d;
                            final Smell smell = new Smell();
                            for (VariableDeclarator vd : pf.getVariables()) {
                                Type t = vd.getType();
                                if (t != null) {
                                    String type = extractTypeFromExpression(t.toString());
                                    d = findTypeDeclaration(type, cus, 1);
                                    if (d != null) {
                                        List<Declaration> relatedComponent = new ArrayList<>();
                                        relatedComponent.add(d);
                                        smell.setComment(parentField.toString())
                                                .setName("One-By-One Candidate")
                                                .setRelatedComponent(relatedComponent);
                                        mvp.getRange().ifPresent(s -> smell.setPosition(s.toString()));

                                        cu.getStorage().ifPresent(s -> smell.setFile(s.getPath().toString()));

                                        Declaration parentDeclaration = new Declaration(cu);
                                        smell.setClassName(parentDeclaration.getName());
                                        lazyFetches.add(smell);
                                        List<Smell> smells = psr.getSmells().get(parentDeclaration);
                                        if (smells == null) {
                                            smells = new ArrayList<>();
                                        }
                                        smells.add(smell);
                                        psr.getSmells().put(parentDeclaration, smells);
                                    }
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
        return getOneByOne(cus);
    }
}
