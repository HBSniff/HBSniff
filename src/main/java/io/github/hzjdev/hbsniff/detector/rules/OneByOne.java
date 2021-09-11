/*
 * This file is part of HBSniff.
 *
 *     HBSniff is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     HBSniff is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with HBSniff.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hbsniff.detector.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.github.hzjdev.hbsniff.detector.SmellDetector;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.hzjdev.hbsniff.utils.Const.*;

public class OneByOne extends SmellDetector {

    /**
     * insert smell to psr
     * @param n corresponding node
     * @param oneByOne result list
     * @param parentDeclaration parent declaration
     */
    public void genSmell(Node n, List oneByOne, Declaration parentDeclaration){
        Optional<Node> parentField = n.getParentNode();
        while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
            parentField = parentField.get().getParentNode();
        }
        if (parentField.isPresent()) {
            FieldDeclaration pf = (FieldDeclaration) parentField.get();
            if (batchSizeExists(pf)) return;
            final Smell smell = initSmell(parentDeclaration);
            for (VariableDeclarator vd : pf.getVariables()) {
                Type t = vd.getType();
                if (t != null) {
                    smell.setComment(vd.getNameAsString())
                            .setName("One-By-One");
                    n.getRange().ifPresent(s -> smell.setPosition(s.toString()));
                    oneByOne.add(smell);
                    psr.getSmells().get(parentDeclaration).add(smell);
                    break;
                }
            }
        }
    }

    /**
     * check if batchSize annotation exists
     * @param pf field
     * @return true if exists
     */
    private boolean batchSizeExists(FieldDeclaration pf) {
        for (AnnotationExpr fieldAnnotations : pf.getAnnotations()) {
            if (fieldAnnotations.getNameAsString().contains(BATCH_SIZE_ANNOT_EXPR)) {
                return true;
            }
        }
        return false;
    }

    /**
     * detection methods
     * @param entities Entity Declarations
     * @return results
     */
    public List<Smell> getOneByOne(Set<Declaration> entities) {
        List<Smell> lazyFetches = new ArrayList<>();
        for (Declaration parentDeclaration : entities) {
            CompilationUnit cu = parentDeclaration.getRawCU();
            List<NormalAnnotationExpr> annotations = cu.findAll(NormalAnnotationExpr.class);
            for (NormalAnnotationExpr annotation : annotations) {
                boolean fetchTypeExists = false;
                for (MemberValuePair mvp : annotation.getPairs()) {
                    if(mvp.getNameAsString().equals(FETCH_ANNOT_EXPR)){
                        fetchTypeExists = true;
                    }
                    if (fetchTypeExists && annotation.getNameAsString().contains(TO_MANY_ANNOT_EXPR) && mvp.getValue().toString().contains(LAZY_ANNOT_EXPR)) {
                        genSmell(annotation, lazyFetches, parentDeclaration);
                    }
                }
                if(!fetchTypeExists && annotation.getNameAsString().contains(TO_MANY_ANNOT_EXPR)){
                    genSmell(annotation, lazyFetches, parentDeclaration);
                }
            }
            List<MarkerAnnotationExpr> annotationsMarker = cu.findAll(MarkerAnnotationExpr.class);
            for (MarkerAnnotationExpr marker : annotationsMarker) {
                if (marker.getNameAsString().contains(TO_MANY_ANNOT_EXPR)) {
                    //@ManyToMany or @OneToMany with default fetch type LAZY
                    genSmell(marker, lazyFetches, parentDeclaration);
                }
            }
        }
        return lazyFetches;
    }

    /**
     * execute detection
     * @return list of smells
     */
    public List<Smell> exec() {
        return getOneByOne(entityDeclarations);
    }
}
