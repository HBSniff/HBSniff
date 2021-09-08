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
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.Type;
import io.github.hzjdev.hbsniff.detector.SmellDetector;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.ParametreOrField;
import io.github.hzjdev.hbsniff.model.output.Smell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.hzjdev.hbsniff.utils.Const.MANY_TO_ONE_ANNOT_EXPR;
import static io.github.hzjdev.hbsniff.utils.Const.ONE_TO_MANY_ANNOT_EXPR;
import static io.github.hzjdev.hbsniff.utils.Utils.extractTypeFromCollection;

public class MissingManyToOne extends SmellDetector {


    /**
     * locate field of targetTypeName in typeName without ManyToOne
     * @param targetTypeName target type name to search
     * @param typeName type of the searched class
     * @return result field
     */
    private ParametreOrField locateMissingManyToOneField(String targetTypeName, String typeName) {
        if (targetTypeName != null) {
            String type = extractTypeFromCollection(targetTypeName);
            Declaration d = entityDeclarations.stream().filter(i -> i.getName().equals(type)).findFirst().orElse(null);
            if (d != null) {
                for (ParametreOrField targetField : d.getFields()) {
                    if (!targetField.getType().equals(typeName)) {
                        continue;
                    }
                    if (!targetField.annotationIncludes(MANY_TO_ONE_ANNOT_EXPR)) {
                        return targetField;
                    }
                }
            }
        }
        return null;
    }

    /**
     * detection methods
     * @param entities Entity Declarations
     * @return results
     */
    public List<Smell> getOneToManyNPlusOne(Set<Declaration> entities) {
        List<Smell> result = new ArrayList<>();
        for (Declaration parentDeclaration : entities) {
            CompilationUnit cu = parentDeclaration.getRawCU();
            for (TypeDeclaration cuType : cu.getTypes()) {
                String typeName = cuType.getNameAsString();
                List<AnnotationExpr> annotations = cuType.findAll(AnnotationExpr.class);
                for (AnnotationExpr annotation : annotations) {
                    if (annotation.getNameAsString().contains(ONE_TO_MANY_ANNOT_EXPR)) {
                        Optional<Node> parentField = annotation.getParentNode();
                        while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
                            parentField = parentField.get().getParentNode();
                        }
                        if (parentField.isPresent()) {
                            FieldDeclaration pf = (FieldDeclaration) parentField.get();
                            final Smell smell = initSmell(parentDeclaration);
                            for (VariableDeclarator vd : pf.getVariables()) {
                                Type t = vd.getType();
                                ParametreOrField targetField = locateMissingManyToOneField(t.toString(), typeName);
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

    /**
     * execute detection
     * @return list of smells
     */
    public List<Smell> exec() {
        return getOneToManyNPlusOne(entityDeclarations);
    }

}
