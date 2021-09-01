package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.hzjdev.hqlsniffer.utils.Utils.extractTypeFromExpression;
import static io.github.hzjdev.hqlsniffer.parser.EntityParser.findTypeDeclaration;

public class MissingOneToMany extends SmellDetector {



    public List<Smell> getOneToManyNPlusOne(List<CompilationUnit> cus) {
        List<Smell> result = new ArrayList<>();
        for (CompilationUnit cu: cus){
            for(TypeDeclaration cuType: cu.getTypes()) {
                String typeName = cuType.getNameAsString();
                List<NormalAnnotationExpr> annotations = cuType.findAll(NormalAnnotationExpr.class);
                for (NormalAnnotationExpr annotation : annotations) {
                    for (MemberValuePair mvp : annotation.getPairs()) {
                        if (annotation.getNameAsString().contains("OneToMany")) {
                            Optional<Node> parentField = mvp.getParentNode();
                            while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
                                parentField = parentField.get().getParentNode();
                            }
                            if (parentField.isPresent()) {
                                FieldDeclaration pf = (FieldDeclaration) parentField.get();
                                Declaration d;
                                final Smell smell = new Smell();
                                for (VariableDeclarator vd : pf.getVariables()) {
                                    Type t = vd.getType();
                                    if (t != null) {
                                        String type = extractTypeFromExpression(t.toString());
                                        d = findTypeDeclaration(type, cus, 1);
                                        if (d != null) {
                                            for(Parametre targetField :d.getFields()){

                                                if(!targetField.getType().equals(typeName)){
                                                    continue;
                                                }
                                                if (!targetField.annotationIncludes("ManyToOne")) {
                                                    List<Declaration> relatedComponent = new ArrayList<>();
                                                    relatedComponent.add(d);
                                                    smell.setComment(parentField.toString())
                                                            .setName("MissingOneToMany")
                                                            .setRelatedComponent(relatedComponent);
                                                    smell.setPosition(targetField.getPosition());

                                                    cu.getStorage().ifPresent(s -> smell.setFile(s.getPath().toString()));

                                                    Declaration parentDeclaration = new Declaration(cu);
                                                    if (parentDeclaration != null) {
                                                        smell.setClassName(parentDeclaration.getName());
                                                        result.add(smell);
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
                    }
                }
            }
        }
        return result;
    }

    public List<Smell> exec() {
        return getOneToManyNPlusOne(cus);
    }

}
