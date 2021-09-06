package io.github.hzjdev.hqlsniffer.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.github.hzjdev.hqlsniffer.detector.SmellDetector;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.hzjdev.hqlsniffer.utils.Const.EAGER_ANNOT_EXPR;
import static io.github.hzjdev.hqlsniffer.utils.Utils.cleanHql;

public class Fetch extends SmellDetector {

    /**
     * find fields annotated with eager fetches in entites
     * @param cus scope of detected files
     * @return list of smells
     */
    public List<Smell> getEagerFetches(List<CompilationUnit> cus) {
        List<Smell> eagerFetches = new ArrayList<>();
        for (CompilationUnit cu : cus) {
            List<NormalAnnotationExpr> annotations = cu.findAll(NormalAnnotationExpr.class);
            for (NormalAnnotationExpr annotation : annotations) {
                for (MemberValuePair mvp : annotation.getPairs()) {
                    if (mvp.getValue().toString().contains(EAGER_ANNOT_EXPR)) {
                        Optional<Node> parentField = mvp.getParentNode();
                        while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
                            parentField = parentField.get().getParentNode();
                        }
                        if (parentField.isPresent()) {
                            Declaration parentDeclaration = new Declaration(cu);
                            final Smell smell = initSmell(parentDeclaration);
                            smell.setComment(parentField.toString())
                                    .setName("Eager Fetch");

                            mvp.getRange().ifPresent(s -> smell.setPosition(s.toString()));
                            psr.getSmells().get(parentDeclaration).add(smell);
                            eagerFetches.add(smell);
                        }
                    }
                }
            }
        }
        return eagerFetches;
    }

    /**
     * find hqls affected by Join Fetch smell
     * @param hqls hqls
     * @param eagerFetches detection result of Eager Fetch smell
     * @return list of smells
     */
    public List<Smell> getJoinFetch(List<HqlAndContext> hqls, List<Smell> eagerFetches) {
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
                    if (from_entity != null) {
                        for (Smell eagerFetch : eagerFetches) {
                            if (eagerFetch.getClassName().toLowerCase().equals(from_entity)) {
                                Declaration parentDeclaration = Declaration.fromPath(hql_.getFullPath());
                                if(parentDeclaration!=null) {
                                    Smell smell = new Smell();
                                    String path = hql_.getFullPath();
                                    smell.setPosition(hql_.getCreateQueryPosition());
                                    smell.setFile(path)
                                            .setComment(eagerFetch.getClassName() + "::" +hql_.getMethodName() + ">" + hql.toString())
                                            .setClassName(parentDeclaration.getName());
                                    smell.setName("Lacking Join Fetch");
                                    joinFetchSmell.add(smell);
                                    psr.getSmells().get(parentDeclaration).add(smell);
                                }
                            }
                        }
                    }
                }
            }
        }
        return joinFetchSmell;
    }

    /**
     * execute detection
     * @return list of smells
     */
    public List<Smell> exec() {
        List<Smell> eagerFetches = getEagerFetches(cus);
        List<Smell> joinFetches = getJoinFetch(hqls, eagerFetches);
        joinFetches.addAll(eagerFetches);
        return joinFetches;
    }
}
