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

package io.github.hzjdev.hbsniff.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.HqlAndContext;
import io.github.hzjdev.hbsniff.model.ParametreOrField;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.hzjdev.hbsniff.parser.EntityParser.findTypeDeclaration;
import static io.github.hzjdev.hbsniff.utils.Const.*;
import static io.github.hzjdev.hbsniff.utils.Utils.*;

public class HqlExtractor {

    /**
     * populate hql contextual info
     * @param parent method declaration
     * @param cus scope of classes
     * @param hqlAndContext result
     */
    private static void populateHqlContextInfo(MethodDeclaration parent, TypeDeclaration td, List<CompilationUnit> cus, HqlAndContext hqlAndContext){
        if (parent != null) {
            List<ParametreOrField> params = new ArrayList<>();
            String methodName = parent.getNameAsString();
            parent.getRange().ifPresent(s -> hqlAndContext.setMethodPosition(s.toString()));
            List<ReturnStmt> rstmt = parent.findAll(ReturnStmt.class);
            if (rstmt.size() > 0) {
                rstmt.get(rstmt.size() - 1).asReturnStmt().getExpression().ifPresent(s ->
                        hqlAndContext.setReturnExpression(s.toString())
                );
            }
            for (Parameter p : parent.getParameters()) {
                String type = p.getTypeAsString();
                String name = p.getNameAsString();
                Declaration declaration = findTypeDeclaration(type, cus, 1);
                params.add(new ParametreOrField(type, name)
                        .setTypeDeclaration(declaration)
                        .setPosition(extractParametrePosition(p))
                        .populateModifiers(p.getModifiers())
                        .populateAnnotations(p.getAnnotations())
                );
            }
            String typeName = null;
            if(td != null){
                typeName = td.getNameAsString();
            }
            hqlAndContext.setTypeName(typeName).setMethodName(methodName).setParams(params).setMethodBody(parent.toString());
        }
    }

    /**
     * Extract Hql by hqlVariableNameCandidate from a Initializer of a VariableDeclarator
     * @param init Initializer expression to extract
     * @param hqls result list
     */
    private static void extractFromInitializerExpression(Expression init, List<String> hqls){
        String tmp = null;
        if (init.isBinaryExpr()) {
            tmp = concatBinaryExpr(init.asBinaryExpr());
        } else if (init.isLiteralExpr()) {
            tmp = extractLiteralExpr(init.asLiteralExpr());
        } else if (init.isObjectCreationExpr()) {
            for(Expression argument: init.asObjectCreationExpr().getArguments()){
                extractFromInitializerExpression(argument, hqls);
            }
        } else {
            if (!init.toString().contains(CLASS_SUFFIX)) {
                tmp = init.toString();
            }
        }
        if (tmp != null) {
            hqls.add(tmp);
        }
    }
    /**
     * Extract Hql by hqlVariableNameCandidate from a VariableDeclarator
     * @param vd VariableDeclarator to extract
     * @param hqls result list
     * @param hqlVariableNameCandidate hql variable name
     */
    private static void extractHqlFromVariableDeclarators(VariableDeclarator vd,  List<String> hqls, String hqlVariableNameCandidate){
        if (vd != null && vd.getNameAsString().equals(hqlVariableNameCandidate)) {
            vd.getInitializer().ifPresent(init -> {
                extractFromInitializerExpression(init, hqls);
            });
        }
    }

    /**
     * Extract Hql by hqlVariableNameCandidate from a statement
     * @param statement statement to extract
     * @param hqls result list
     * @param hqlVariableNameCandidate hql variable name
     */
    private static void extractHqlFromStatement(Expression statement, List<String> hqls, String hqlVariableNameCandidate){
        if (statement instanceof VariableDeclarationExpr) {
            for (VariableDeclarator vd : statement.asVariableDeclarationExpr().getVariables()) {
                extractHqlFromVariableDeclarators(vd, hqls, hqlVariableNameCandidate);
            }
        } else if (statement instanceof AssignExpr) {
            // variable assignment
            List<String> extractedHql = extractHqlExpr(hqlVariableNameCandidate, statement.asAssignExpr());
            hqls.addAll(extractedHql);
        } else if (statement instanceof MethodCallExpr) {
            // hql.append("...")
            List<NameExpr> nodes = statement.asMethodCallExpr().findAll(NameExpr.class);
            if(nodes.size() < 1) return;
            String methodCallName = statement.asMethodCallExpr().getNameAsString();
            String variableName = nodes.get(0).getNameAsString();
            if(methodCallName != null && (methodCallName.equals(APPEND_METHOD_CALL_NAME) || methodCallName.equals(CONCAT_METHOD_CALL_NAME)) && variableName.equals(hqlVariableNameCandidate)){
                List<LiteralExpr> lexprs = statement.asMethodCallExpr().findAll(LiteralExpr.class);
                if(lexprs == null) return;
                for (LiteralExpr lexpr: lexprs){
                    String expr = extractLiteralExpr(lexpr);
                    hqls.add(expr);
                }
            }
        }
    }

    /**
     * locate and concat hql in a method body or type declaration
     * @param parent method body  or type declaration
     * @param hqlVariableNameCandidate the name of the hql variable
     * @return list of hqls (unconcatenated)
     */
    private static List<String> locateHqlInContext(BodyDeclaration parent, String hqlVariableNameCandidate){
        List<String> hqls = new ArrayList<>();
        if (parent != null) {
            List<FieldDeclaration> fieldDeclarations = parent.findAll(FieldDeclaration.class);
            for (FieldDeclaration declaration : fieldDeclarations) {
                for(VariableDeclarator vd: declaration.getVariables()){
                    extractHqlFromVariableDeclarators(vd, hqls, hqlVariableNameCandidate);
                }
            }
            List<ExpressionStmt> statements = parent.findAll(ExpressionStmt.class);
            for (ExpressionStmt statement : statements) {
                extractHqlFromStatement(statement.getExpression(), hqls, hqlVariableNameCandidate);
            }
        }
        return hqls;
    }

    /**
     * get hqls from a set of CompilationUnits
     * @param cus set of CompilationUnits
     * @return hqls
     */
    public static List<HqlAndContext> getHqlNodes(List<CompilationUnit> cus) {
        List<HqlAndContext> hqlAndContexts = new ArrayList<>();
        for (CompilationUnit cu : cus) {
            List<MethodCallExpr> mces = cu.findAll(MethodCallExpr.class);

            for (MethodCallExpr mce : mces) {
                if (mce.getNameAsString().equals(CREATE_QUERY_METHOD_NAME)) {
                    String hql = null;
                    String hqlCandidate = "";
                    boolean foundStrLitr = false;
                    HqlAndContext hqlAndContext = new HqlAndContext();

                    cu.getStorage().ifPresent(s -> hqlAndContext.setFullPath(s.getPath().toString()));
                    mce.getRange().ifPresent(s -> hqlAndContext.setCreateQueryPosition(s.toString()));

                    List<ParametreOrField> params = new ArrayList<>();

                    // There may be no arguments
                    if (mce.getArguments().size() > 0) {
                        // the first parametre of createQuery method call, may be hql or an identifier
                        try {
                            if (mce.getArgument(0).isStringLiteralExpr()) {
                                hqlCandidate = mce.getArgument(0).asStringLiteralExpr().getValue();
                                foundStrLitr = true;
                            } else if (mce.getArgument(0).isBinaryExpr()) {
                                hqlCandidate = concatBinaryExpr(mce.getArgument(0).asBinaryExpr());
                                foundStrLitr = true;
                            } else {
                                if(mce.getArgument(0) instanceof MethodCallExpr){
                                    List<NameExpr> names = mce.getArgument(0).asMethodCallExpr().findAll(NameExpr.class);
                                    if(names.size()>0){
                                        hqlCandidate = names.get(0).getNameAsString();
                                    }
                                }else if(mce.getArgument(0) instanceof NameExpr){
                                    hqlCandidate = mce.getArgument(0).toString();
                                }else if(mce.getArgument(0) instanceof ClassExpr){
                                    continue; //.class
                                }else{
                                    hqlCandidate = mce.getArgument(0).toString();
                                    System.out.println("\n##Unexpected type");
                                    System.out.println("Candidate:" + hqlCandidate);
                                    System.out.println("File:" + hqlAndContext.getFullPath());
                                    System.out.println("Method:" + hqlAndContext.getMethodName());
                                    System.out.println("Position:" + hqlAndContext.getCreateQueryPosition());
                                    System.out.println("found .createQuery argument 1 of type "+mce.getClass().toString()+" "+hqlCandidate);
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("#getHqlNodes1");
                            System.out.println(hqlAndContext.toString());
                            e.printStackTrace();
                            continue;
                        }
                    }

                    // find the parent method body of createQuery statement
                    Optional<Node> parentMethod = mce.getParentNode();
                    while (parentMethod.isPresent() && !(parentMethod.get() instanceof MethodDeclaration)) {
                        parentMethod = parentMethod.get().getParentNode();
                    }
                    MethodDeclaration parent = (MethodDeclaration) parentMethod.orElse(null);
                    // gen method signature related info
                    TypeDeclaration td = cu.getPrimaryType().orElse(null);
                    populateHqlContextInfo(parent, td, cus, hqlAndContext);

                    // find hql
                    if (foundStrLitr) {
                        // the easiest case, hql is defined straightly with a literal expr
                        hql = hqlCandidate;
                        hqlAndContext.setHql(hql);
                    } else {
                        // looking at the method body
                        List<String> hqlFromContext = locateHqlInContext(parent, hqlCandidate);
                        if(hqlFromContext.size() < 1){
                            hqlFromContext = locateHqlInContext(td, hqlCandidate);
                        }
                        hqlAndContext.setHql(hqlFromContext);
                    }
                    // if hql is not found this entity should be dropped
                    if (hqlAndContext.getHql() != null && hqlAndContext.getHql().size() > 0) {
                        hqlAndContexts.add(hqlAndContext);
                    } else {
                        // TODO: LOGGING
                        System.out.println("\n##HQL Not Found");
                        System.out.println("Candidate:" + hqlCandidate);
                        System.out.println("File:" + hqlAndContext.getFullPath());
                        System.out.println("Method:" + hqlAndContext.getMethodName());
                        System.out.println("Position:" + hqlAndContext.getCreateQueryPosition());
                    }
                }
            }
        }
        return hqlAndContexts;
    }


    /**
     * extract a list of (unconcatenated) hqls from a Assign expression statement
     * @param variableName name of the assigned variable
     * @param statement assignment statement
     * @return a list of (unconcatenated) hqls
     */
    private static List<String> extractHqlExpr(String variableName, AssignExpr statement) {
        List<String> hqls = new ArrayList<>();
        Expression expr = statement.getValue();
        if (expr.isCastExpr()) {
            expr = expr.asCastExpr().getExpression();
        }
        if (expr.isLiteralExpr()) {
            String content = null;
            if (expr.isStringLiteralExpr()) {
                content = expr.asStringLiteralExpr().getValue();
            } else {
                content = expr.toString();
            }
            String target = statement.getTarget().toString();
            String op = statement.getOperator().toString();
            if (target.equals(variableName)) {
                if (op.equals(HQL_PLUS_OP)) {
                    hqls.add(content);
                } else if (op.equals(HQL_ASSIGN_OP)) {
                    hqls.add(content);
                }
            }
        } else if (expr.isBinaryExpr()) {
            String res = concatBinaryExpr(expr.asBinaryExpr());
            if (!res.isEmpty()) {
                hqls.add(res);
            }
        } else if (expr.isEnclosedExpr()) {
            // () dont know what to do with this expression
        } else if (expr.isMethodCallExpr()) {
            if (((MethodCallExpr) expr).getScope().isPresent()) {
                hqls.add(extractMethodCallExpr(expr.asMethodCallExpr()));
            }
        } else {
            System.out.println("#extractHqlExpr Failed" + expr);
        }
        return hqls;
    }
}