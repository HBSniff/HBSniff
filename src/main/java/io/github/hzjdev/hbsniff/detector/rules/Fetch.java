/*
 * This file is part of hqlSniffer.
 *
 *     hqlSniffer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     hqlSniffer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with hqlSniffer.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hbsniff.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import io.github.hzjdev.hbsniff.detector.SmellDetector;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.HqlAndContext;
import io.github.hzjdev.hbsniff.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.hzjdev.hbsniff.model.HqlAndContext.extractSelectedFields;
import static io.github.hzjdev.hbsniff.parser.EntityParser.findTypeDeclaration;
import static io.github.hzjdev.hbsniff.utils.Const.*;
import static io.github.hzjdev.hbsniff.utils.Utils.cleanHql;

public class Fetch extends SmellDetector {

    /**
     * insert smell to psr
     * @param n corresponding node
     * @param eagerFetches result list
     * @param cu compilationunit of file
     */
    public void genSmell(Node n, List eagerFetches, CompilationUnit cu){
        Optional<Node> parentField = n.getParentNode();
        while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
            parentField = parentField.get().getParentNode();
        }
        if (parentField.isPresent()) {
            final Smell smell;
            try {
                String field;
                try {
                    field = parentField.get().findAll(VariableDeclarator.class).get(0).getNameAsString();
                }catch(Exception e){
                    field = parentField.get().toString();
                }
                Declaration parentDeclaration = findTypeDeclaration(cu.getStorage().get().getPath().toString());
                smell = initSmell(parentDeclaration).setComment(field)
                        .setName("Eager Fetch");

                n.getRange().ifPresent(s -> smell.setPosition(s.toString()));
                psr.getSmells().get(parentDeclaration).add(smell);
                eagerFetches.add(smell);
            }catch(Exception e){
                e.printStackTrace();
                return;
            }
        }
    }
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
                if (annotation.getNameAsString().contains(TO_ONE_ANNOT_EXPR) || annotation.getNameAsString().contains(TO_MANY_ANNOT_EXPR)) {
                    boolean fetchTypeExists = false;
                    for (MemberValuePair mvp : annotation.getPairs()) {
                        // case of @ManyToOne(fetch = {FetchType.EAGER})
                        if(mvp.getNameAsString().equals(FETCH_ANNOT_EXPR)){
                            fetchTypeExists = true;
                        }
                        if (fetchTypeExists && mvp.getValue().toString().contains(EAGER_ANNOT_EXPR)) {
                            genSmell(mvp, eagerFetches, cu);
                        }
                    }
                    if(!fetchTypeExists && annotation.getNameAsString().contains(TO_ONE_ANNOT_EXPR)){
                        // case of @ManyToOne(cascade = { CascadeType.ALL })
                        genSmell(annotation, eagerFetches, cu);
                    }
                }
            }
            List<MarkerAnnotationExpr> annotationsMarker = cu.findAll(MarkerAnnotationExpr.class);
            for (MarkerAnnotationExpr marker : annotationsMarker) {
                if (marker.getNameAsString().contains(TO_ONE_ANNOT_EXPR)) {
                    //Direct marker like @ManyToOne or @OneToOne with default fetch type EAGER
                    genSmell(marker, eagerFetches, cu);
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
                        String[] from_entity_arr = from_entity.split("\\.");
                        from_entity = from_entity_arr[from_entity_arr.length - 1];
                        for (Smell eagerFetch : eagerFetches) {
                            if (eagerFetch.getClassName().toLowerCase().equals(from_entity)) {
                                Declaration dec = findTypeDeclaration(eagerFetch.getClassName());
                                Set<String> selected_fields = extractSelectedFields(hql_s, dec);
                                if(dec!=null) {
                                    if(selected_fields.size()>0 && !selected_fields.contains(eagerFetch.getComment().toLowerCase())){
                                        continue;
                                    }
                                    Smell smell = new Smell();
                                    String path = hql_.getFullPath();
                                    smell.setPosition(hql_.getCreateQueryPosition());
                                    smell.setFile(path)
                                            .setComment(eagerFetch.getComment() + "::"+eagerFetch.getClassName() + "::" +hql_.getMethodName() + ">" + hql.toString())
                                            .setClassName(eagerFetch.getClassName());
                                    smell.setName("Lacking Join Fetch");
                                    joinFetchSmell.add(smell);
                                    psr.getSmells().get(dec).add(smell);
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
        List<Smell> eagerFetches = getEagerFetches(entities);
        List<Smell> joinFetches = getJoinFetch(hqls, eagerFetches);
        joinFetches.addAll(eagerFetches);
        return joinFetches;
    }
}
