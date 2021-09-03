package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.findTypeDeclaration;
import static io.github.hzjdev.hqlsniffer.utils.Utils.cleanHql;
import static io.github.hzjdev.hqlsniffer.utils.Utils.extractTypeFromExpression;

public class Fetch extends SmellDetector {

    public List<Smell> getEagerFetches(List<CompilationUnit> cus) {
        List<Smell> eagerFetches = new ArrayList<>();
        for (CompilationUnit cu : cus) {
            List<NormalAnnotationExpr> annotations = cu.findAll(NormalAnnotationExpr.class);
            for (NormalAnnotationExpr annotation : annotations) {
                for (MemberValuePair mvp : annotation.getPairs()) {
                    if (mvp.getValue().toString().contains("EAGER")) {
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
                                        List<Declaration> relatedComponent = new ArrayList<>();
                                        relatedComponent.add(d);
                                        smell.setComment(parentField.toString())
                                                .setName("Eager Fetch")
                                                .setRelatedComponent(relatedComponent);
                                        break;
                                    }
                                }
                            }
                            if (smell.getName() != null) {
                                mvp.getRange().ifPresent(s -> smell.setPosition(s.toString()));

                                cu.getStorage().ifPresent(s -> smell.setFile(s.getPath().toString()));

                                Declaration parentDeclaration = new Declaration(cu);
                                if (parentDeclaration != null) {
                                    smell.setClassName(parentDeclaration.getName());

                                    List<Smell> smells = psr.getSmells().get(parentDeclaration);
                                    if (smells == null) {
                                        smells = new ArrayList<>();
                                    }
                                    smells.add(smell);
                                    eagerFetches.add(smell);
                                    psr.getSmells().put(new Declaration(cu), smells);
                                }
                            }
                        }
                    }
                }
            }
        }
        return eagerFetches;
    }

    public List<Smell> getJoinFetch(List<HqlAndContext> hqls, List<CompilationUnit> cus, List<Smell> eagerFetches) {
        List<Smell> joinFetchSmell = new ArrayList<>();
        for (HqlAndContext hql_ : hqls) {
            StringBuilder hql = new StringBuilder();
            for (String hql__ : hql_.getHql()) {
                hql.append(hql__).append(' ');
            }
            String hql_s = hql.toString().toLowerCase();
            if (!hql_s.contains("join fetch")) {
                String from_entity = null;
                hql_s = cleanHql(hql_s);
                if (!hql_s.startsWith("delete") && !hql_s.startsWith("update") && !hql_s.startsWith("insert")) {
                    try {
                        from_entity = hql_s.split("from ")[1].split(" ")[0];
                    } catch (Exception e) {
                        from_entity = hql_.getReturnType();
                    }
                    Declaration parentDeclaration = null;
                    if (from_entity != null) {
                        for (Smell eagerFetch : eagerFetches) {
                            if (eagerFetch.getClassName().toLowerCase().equals(from_entity)) {
                                for (CompilationUnit cu : cus) {
                                    String cuPath = null;
                                    if (cu.getStorage().isPresent()) {
                                        cuPath = cu.getStorage().get().getPath().toString();
                                        if (hql_.getFullPath().equals(cuPath)) {
                                            parentDeclaration = new Declaration(cu);
                                            break;
                                        }
                                    }
                                }
                                List<Smell> smells = psr.getSmells().get(parentDeclaration);
                                if (smells == null) {
                                    smells = new ArrayList<>();
                                }
                                Smell smell = new Smell();
                                String path = hql_.getFullPath();
                                smell.setPosition(hql_.getCreateQueryPosition());
                                smell.setFile(path)
                                        .setComment(hql_.getMethodName() + "+" + eagerFetch.getClassName() + ":" + hql_s)
                                        .setClassName(parentDeclaration.getName());
                                smell.setName("Join Fetch");
                                smells.add(smell);
                                joinFetchSmell.add(smell);
                                psr.getSmells().put(parentDeclaration, smells);
                            }
                        }
                    }
                }
            }
        }
        return joinFetchSmell;
    }

    public List<Smell> exec() {
        List<Smell> eagerFetches = getEagerFetches(cus);
        List<Smell> joinFetches = getJoinFetch(hqls, cus, eagerFetches);
        joinFetches.addAll(eagerFetches);
        return joinFetches;
    }
}
