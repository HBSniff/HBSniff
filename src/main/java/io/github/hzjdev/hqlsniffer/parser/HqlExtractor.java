package io.github.hzjdev.hqlsniffer.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.HqlAndContext;
import io.github.hzjdev.hqlsniffer.model.Parametre;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.findTypeDeclaration;
import static io.github.hzjdev.hqlsniffer.utils.Utils.extractParametrePosition;

public class HqlExtractor {
    public static List<HqlAndContext> getHqlNodes(List<CompilationUnit> cus) {
        List<HqlAndContext> hqlAndContexts = new ArrayList<>();
        for (CompilationUnit cu : cus) {
            List<MethodCallExpr> mces = cu.findAll(MethodCallExpr.class);
            for (MethodCallExpr mce : mces) {
                if (mce.getNameAsString().equals("createQuery")) {
                    String hql = null;
//                    String returnType = null;
                    String methodName = null;
                    String hqlCandidate = "";
                    Boolean foundStrLitr = false;
                    HqlAndContext hqlAndContext = new HqlAndContext();

                    cu.getStorage().ifPresent(s -> hqlAndContext.setFullPath(s.getPath().toString()));
                    mce.getRange().ifPresent(s -> hqlAndContext.setCreateQueryPosition(s.toString()));

                    List<Parametre> params = new ArrayList<>();

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
                                hqlCandidate = mce.getArgument(0).toString();
                            }
                            if (hqlCandidate.endsWith(".class")) {
                                // the case of .createQuery(Domain.class)
                                continue;
                            }
                        } catch (Exception e) {
                            System.out.println("698");
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
                    if (parent != null) {
//                        returnType = parent.getTypeAsString();
                        methodName = parent.getNameAsString();
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
                            params.add(new Parametre(type, name)
                                    .setTypeDeclaration(declaration)
                                    .setPosition(extractParametrePosition(p))
                                    .populateModifiers(p.getModifiers())
                                    .populateAnnotations(p.getAnnotations())
                            );
                        }
//                        result.setReturnType(returnType)
//                                .setMethodName(methodName).setParams(params).setMethodBody(parent.toString());
                        hqlAndContext.setMethodName(methodName).setParams(params).setMethodBody(parent.toString());

                    }

                    // find hql
                    if (foundStrLitr) {
                        // the easiest case
                        hql = hqlCandidate;
                        hqlAndContext.setHql(hql);
                    } else {
                        // looking at the method body
                        List<String> hqls = new ArrayList<>();
                        if (parent != null) {
                            List<ExpressionStmt> statements = parent.findAll(ExpressionStmt.class);
                            for (ExpressionStmt statement : statements) {
                                if (statement.getExpression() instanceof VariableDeclarationExpr) {
                                    for (VariableDeclarator vd : statement.getExpression().asVariableDeclarationExpr().getVariables()) {
                                        if (vd != null && vd.getNameAsString().equals(hqlCandidate)) {
                                            vd.getInitializer().ifPresent(init -> {
                                                String tmp = null;
                                                if (init.isBinaryExpr()) {
                                                    tmp = concatBinaryExpr(init.asBinaryExpr());
                                                } else if (init.isLiteralExpr()) {
                                                    tmp = extractLiteralExpr(init.asLiteralExpr());
                                                } else {
                                                    if (!init.toString().contains(".class")) {
                                                        tmp = init.toString();
                                                    }
                                                }
                                                if (tmp != null) {
                                                    hqls.add(tmp);
                                                }
                                            });
                                        }
                                    }
                                } else if (statement.getExpression() instanceof AssignExpr) {
                                    List<String> extractedHql = extractHqlExpr(hqlCandidate, statement.getExpression().asAssignExpr());
                                    hqls.addAll(extractedHql);
                                } else if (statement.getExpression() instanceof MethodCallExpr) {
                                    List<NameExpr> nodes = statement.getExpression().asMethodCallExpr().findAll(NameExpr.class);
                                    if(nodes.size() < 1) continue;
                                    String methodCallName = statement.getExpression().asMethodCallExpr().getNameAsString();
                                    String variableName = nodes.get(0).getNameAsString();
                                    if(methodCallName != null && (methodCallName.equals("append") || methodCallName.equals("concat")) && variableName.equals(hqlCandidate)){
                                        List<LiteralExpr> lexprs = statement.getExpression().asMethodCallExpr().findAll(LiteralExpr.class);
                                        if(lexprs == null) continue;
                                        for (LiteralExpr lexpr: lexprs){
                                            String expr = extractLiteralExpr(lexpr);
                                            hqls.add(expr);
                                        }
                                    }
                                }
//                                hqls.add(statement.getExpression().toString());
                            }
                        }
                        hqlAndContext.setHql(hqls);
                    }
                    // if hql is not found this entity is useless
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

    public static String extractLiteralExpr(LiteralExpr le) {
        if (le instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) le).getValue();
        } else if (le instanceof CharLiteralExpr) {
            return ((CharLiteralExpr) le).getValue();
        } else if (le instanceof TextBlockLiteralExpr) {
            return ((TextBlockLiteralExpr) le).getValue();
        } else {
            return le.toString();
        }
    }

    public static String concatBinaryExpr(BinaryExpr expr) {
        StringBuilder hql_concatenated = new StringBuilder();
        String op = expr.asBinaryExpr().getOperator().toString();
        if (op.equals("PLUS")) {
            for (Node e : expr.getChildNodes()) {
                if (e instanceof LiteralExpr) {
                    hql_concatenated.append(extractLiteralExpr((LiteralExpr) e));
                } else if (e instanceof BinaryExpr) {
                    hql_concatenated.append(concatBinaryExpr(((BinaryExpr) e).asBinaryExpr()));
                } else if (e instanceof NameExpr) {
                    hql_concatenated.append(":" + ((NameExpr) e).asNameExpr().getNameAsString());
                } else if (e instanceof MethodCallExpr) {
                    hql_concatenated.append(extractMethodCallExpr((MethodCallExpr) e));
                } else if (e instanceof EnclosedExpr) {
                    Expression inner = ((EnclosedExpr) e).asEnclosedExpr().getInner();
                    if (inner.isConditionalExpr()) {
                        hql_concatenated.append(":" + ((EnclosedExpr) e).asEnclosedExpr().getInner().asConditionalExpr().getCondition().toString());
                    } else {
                        System.out.println("#99" + e.toString());
                    }
                } else {
                    System.out.println("#99" + e.toString());
                }
            }
        }
        return hql_concatenated.toString();
    }


    public static String extractMethodCallExpr(MethodCallExpr mce) {
        Optional<Expression> e = mce.getScope();
        if (e.isPresent() && e.get().isMethodCallExpr()) {
            if (e.get().asMethodCallExpr().getScope().isPresent()) {
                Expression expr = e.get().asMethodCallExpr().getScope().get();
                if (expr.isNameExpr()) {
                    return ":" + e.get().asMethodCallExpr().getScope().get().asNameExpr().getNameAsString();
                } else if (expr.isMethodCallExpr()) {
                    return extractMethodCallExpr(expr.asMethodCallExpr());
                } else {
                    return "";
                }
            }
        }
        return "";
    }

    public static List<String> extractHqlExpr(String hqlCandidate, AssignExpr statement) {
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
            if (target.equals(hqlCandidate)) {
                if (op.equals("PLUS")) {
                    hqls.add(content);
                } else if (op.equals("ASSIGN")) {
                    hqls.clear();
                    hqls.add(content);
                }
            }
        } else if (expr.isBinaryExpr()) {
            String res = concatBinaryExpr(expr.asBinaryExpr());
            if (!res.isEmpty()) {
                hqls.add(res);
            }
        } else if (expr.isEnclosedExpr()) {
            // ()
        } else if (expr.isMethodCallExpr()) {
            if (((MethodCallExpr) expr).getScope().isPresent()) {
                hqls.add(extractMethodCallExpr(expr.asMethodCallExpr()));
            }
        } else {
            System.out.println("#84" + expr);
        }
        return hqls;
    }
}
