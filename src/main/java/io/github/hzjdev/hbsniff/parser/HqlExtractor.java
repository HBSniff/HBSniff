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
     * get hqls from a set of CompilationUnits
     * @param cus set of CompilationUnits
     * @return hqls
     */
    public static List<HqlAndContext> getHqlNodes(List<CompilationUnit> cus) {
        List<HqlAndContext> hqlAndContexts = new ArrayList<>();
        for (CompilationUnit cu : cus) {
            for (MethodCallExpr mce : cu.findAll(MethodCallExpr.class)) {
                if (mce.getNameAsString().equals(CREATE_QUERY_METHOD_NAME)) {
                    // init hqlAndContext Object
                    HqlAndContext hqlAndContext = new HqlAndContext();
                    cu.getStorage().ifPresent(s -> hqlAndContext.setFullPath(s.getPath().toString()));
                    mce.getRange().ifPresent(s -> hqlAndContext.setCreateQueryPosition(s.toString()));

                    // gen method signature/type related info
                    TypeDeclaration td = cu.getPrimaryType().orElse(null);
                    MethodDeclaration md = getParentMethodOfMethodCall(mce);

                    // populate contextual info for hqlAndContext Object
                    populateHqlContextInfo(md, td, hqlAndContext);

                    // We locate .createQuery method call/type dec where hqls are executed
                    String hql = findHqlExprInMethodCall(mce);
                    if(hql != null){
                        // hql is defined straightly with a literal expr
                        hqlAndContext.setHql(hql);
                        hqlAndContexts.add(hqlAndContext);
                    }else{
                        // TODO: find hql in larger scope (currently only in method call & type containing ".createQuery()")
                        // find the variable name of hql
                        String hqlVariableName = findHqlVariableNameFromMethodCall(mce);

                        // looking at the method body
                        List<String> hqlFromContext = locateHqlInContext(md, hqlVariableName);
                        if(hqlFromContext.size() < 1){
                            // looking at the type dec
                            hqlFromContext = locateHqlInContext(td, hqlVariableName);
                        }
                        if(hqlFromContext.size() > 0) {
                            // found
                            hqlAndContext.setHql(hqlFromContext);
                            hqlAndContexts.add(hqlAndContext);
                        }
                    }
                }
            }
        }
        return hqlAndContexts;
    }

    /**
     * find Method Declaration of a Method Call
     * @param mce method call expression
     * @return result MethodDeclaration
     */
    private static MethodDeclaration getParentMethodOfMethodCall(MethodCallExpr mce){
        // find the parent method body of createQuery statement
        Optional<Node> parentMethod = mce.getParentNode();
        while (parentMethod.isPresent() && !(parentMethod.get() instanceof MethodDeclaration)) {
            parentMethod = parentMethod.get().getParentNode();
        }
        return (MethodDeclaration)parentMethod.orElse(null);
    }

    /**
     * find hql defined directly in .createQuery()
     * @param mce createQuery() Method Call
     * @return hql
     */
    private static String findHqlExprInMethodCall(MethodCallExpr mce){
        String hqlCandidate = null;
        // There may be no arguments
        if (mce.getArguments().size() > 0) {
            // the first parametre of createQuery method call, may be hql or an identifier
            try {
                if (mce.getArgument(0).isStringLiteralExpr()) {
                    hqlCandidate = mce.getArgument(0).asStringLiteralExpr().getValue();
                } else if (mce.getArgument(0).isBinaryExpr()) {
                    hqlCandidate = concatBinaryExpr(mce.getArgument(0).asBinaryExpr());
                }
            } catch (Exception e) {
                return hqlCandidate;
            }
        }
        return hqlCandidate;
    }

    /**
     * infer hql variable name mentioned in .createQuery()
     * @param mce createQuery() Method Call
     * @return hql
     */
    private static String findHqlVariableNameFromMethodCall(MethodCallExpr mce){
        String hqlCandidate = null;
        if(mce.getArgument(0) instanceof MethodCallExpr){
            List<NameExpr> names = mce.getArgument(0).asMethodCallExpr().findAll(NameExpr.class);
            if(names.size()>0){
                hqlCandidate = names.get(0).getNameAsString();
            }
        }else if(mce.getArgument(0) instanceof NameExpr){
            hqlCandidate = mce.getArgument(0).toString();
        }else if(mce.getArgument(0) instanceof ClassExpr){
            hqlCandidate = null; //.class
        }else{
            hqlCandidate = mce.getArgument(0).toString();
        }
        return hqlCandidate;
    }



    /**
     * populate hql contextual info
     * @param md method declaration
     * @param hqlAndContext result
     */
    private static void populateHqlContextInfo(MethodDeclaration md, TypeDeclaration td, HqlAndContext hqlAndContext){
        if (md != null) {
            List<ParametreOrField> params = new ArrayList<>();
            String methodName = md.getNameAsString();
            md.getRange().ifPresent(s -> hqlAndContext.setMethodPosition(s.toString()));
            List<ReturnStmt> rstmt = md.findAll(ReturnStmt.class);
            if (rstmt.size() > 0) {
                rstmt.get(rstmt.size() - 1).asReturnStmt().getExpression().ifPresent(s ->
                        hqlAndContext.setReturnExpression(s.toString())
                );
            }
            for (Parameter p : md.getParameters()) {
                String type = p.getTypeAsString();
                String name = p.getNameAsString();
                Declaration declaration = findTypeDeclaration(type);
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
            hqlAndContext.setTypeName(typeName).setMethodName(methodName).setParams(params).setMethodBody(md.toString()).setDefinedIn(md);
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
            // TODO: deal with () expression
        } else if (expr.isMethodCallExpr()) {
            if (((MethodCallExpr) expr).getScope().isPresent()) {
                hqls.add(extractMethodCallExpr(expr.asMethodCallExpr()));
            }
        }
        return hqls;
    }
}