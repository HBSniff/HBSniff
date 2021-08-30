package io.github.hzjdev.hqlsniffer;

import antlr.collections.AST;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;

import com.github.javaparser.ast.stmt.ReturnStmt;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.hibernate.hql.internal.ast.HqlParser;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


import java.util.*;

import static io.github.hzjdev.hqlsniffer.Const.LEVEL_TO_PARSE;

public class Main {

    public static Map<String, Declaration> declarationCache = new HashMap<>();
    public static List<CompilationUnit> cusCache;
    // Copied from CSDN
    public static List<CompilationUnit> parseFrom(String DirPath, List<CompilationUnit> results ) {
        File dirfile = new File( DirPath );//根据DirPath实例化一个File对象
        File[] files = dirfile.listFiles();//listFiles():以相对路径返回该目录下所有的文件名的一个File对象数组
        if ( files == null ) {
            return results;//[]
        }
        //遍历目录-2
        for (File file : files) {
            // isDirectory()是检查一个对象是否是文件夹,如果是则返回true，否则返回false
            if (file.isDirectory()) {
                parseFrom(file.getAbsolutePath(), results);// getAbsolutePath(): 返回的是定义时的路径对应的相对路径
            } else {
                if (file.getName().toLowerCase().endsWith(".java")) {
                    String path = file.getPath();
                    try{
                        CompilationUnit cu = StaticJavaParser.parse(new File(path));
                        results.add(cu);
                    }catch (ParseProblemException e){
                        // We can do nothing for parse error.
                        // System.out.println(e);
                    }catch (Exception e){
                        System.out.println("58");
                        e.printStackTrace();
                    }
                }
            }
        }
        return results;
    }

    public static String extractMethodCallExpr(MethodCallExpr mce){
        Optional<Expression> e = mce.getScope();
        if(e.isPresent() && e.get().isMethodCallExpr()){
            if(e.get().asMethodCallExpr().getScope().isPresent() ){
                Expression expr = e.get().asMethodCallExpr().getScope().get();
                if(expr.isNameExpr()) {
                    return ":" + e.get().asMethodCallExpr().getScope().get().asNameExpr().getNameAsString();
                }
                else if(expr.isMethodCallExpr()){
                    return extractMethodCallExpr(expr.asMethodCallExpr());
                }
                else{
                    return "";
                }
            }
        }
        return "";
    }
    public static List<String> extractHqlExpr(String hqlCandidate, AssignExpr statement){
        List<String> hqls = new ArrayList<>();
        Expression expr = statement.getValue();
        if(expr.isCastExpr()){
            expr =expr.asCastExpr().getExpression();
        }
        if(expr.isLiteralExpr()) {
            String content = null;
            if(expr.isStringLiteralExpr()) {
                content = expr.asStringLiteralExpr().getValue();
            }else{
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
        }else if(expr.isBinaryExpr()){
           String res = concatBinaryExpr(expr.asBinaryExpr());
           if(!res.isEmpty()){
               hqls.add(res);
           }
        }else if(expr.isEnclosedExpr()){
            // ()
        }else if(expr.isMethodCallExpr()){
            if(((MethodCallExpr) expr).getScope().isPresent()) {
                hqls.add(extractMethodCallExpr(expr.asMethodCallExpr()));
            }
        }else{
            System.out.println("#84"+expr);
        }
        return hqls;
    }

    public static String extractLiteralExpr(LiteralExpr le){
        if(le instanceof StringLiteralExpr){
            return ((StringLiteralExpr) le).getValue();
        }else if(le instanceof CharLiteralExpr){
            return ((CharLiteralExpr) le).getValue();
        }else if(le instanceof TextBlockLiteralExpr){
            return ((TextBlockLiteralExpr) le).getValue();
        }else{
            return le.toString();
        }
    }
    public static String concatBinaryExpr(BinaryExpr expr){
        StringBuilder hql_concatenated = new StringBuilder();
        String op = expr.asBinaryExpr().getOperator().toString();
        if (op.equals("PLUS")) {
            for(Node e : expr.getChildNodes()){
                if(e instanceof LiteralExpr) {
                    hql_concatenated.append(extractLiteralExpr((LiteralExpr) e));
                }else if(e instanceof BinaryExpr){
                    hql_concatenated.append(concatBinaryExpr(((BinaryExpr) e).asBinaryExpr()));
                }else if(e instanceof NameExpr){
                    hql_concatenated.append(":"+((NameExpr) e).asNameExpr().getNameAsString());
                }else if(e instanceof MethodCallExpr) {
                    hql_concatenated.append(extractMethodCallExpr((MethodCallExpr) e));
                }else if(e instanceof EnclosedExpr) {
                    Expression inner =  ((EnclosedExpr) e).asEnclosedExpr().getInner();
                    if(inner.isConditionalExpr()){
                        hql_concatenated.append(":"+((EnclosedExpr) e).asEnclosedExpr().getInner().asConditionalExpr().getCondition().toString());
                    }else{
                        System.out.println("#99"+e.toString());
                    }
                }else{
                    System.out.println("#99"+e.toString());
                }
            }
        }
        return hql_concatenated.toString();
    }



    public static void getIdentAndAlias(AST ast,Map<String,String> result) {
        if ( ast == null ) {
            return;
        }
        if(ast.getType() == HqlTokenTypes.FROM ){
            AST rangeOfFrom = ast.getFirstChild();
            if(rangeOfFrom == null){
                return;
            }
            AST ident = rangeOfFrom.getFirstChild();
            if(ident == null){
                return;
            }
            AST alias = ident.getNextSibling();
            if(alias != null) {
                String identName = ident.getText();
                String aliasName = alias.getText();
                result.put(aliasName, identName);
            }else{
                result.put(ident.getText(), ident.getText());
            }
            getIdentAndAlias(ast.getFirstChild(), result);
            getIdentAndAlias(ast.getNextSibling(), result);
        }else{
            getIdentAndAlias(ast.getFirstChild(), result);
            getIdentAndAlias(ast.getNextSibling(), result);
        }
    }

    public static void processAst(AST ast, Map<String, String> aliasMaps) {
        if ( ast == null ) {
            return;
        }
        if(ast.getType() == HqlTokenTypes.IDENT ){
            if(aliasMaps.get(ast.getText()) != null) {
                ast.setText(aliasMaps.get(ast.getText()));
            }
        }else if(ast.getType() == HqlTokenTypes.ALIAS){
            ast.setText("");
        }
        processAst(ast.getNextSibling(), aliasMaps);
        processAst(ast.getFirstChild(), aliasMaps);
    }

    public static void printAST(HqlParser parser, AST ast){
        System.out.println( "AST  :  " + ast.toStringTree() + "" );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        parser.showAst( ast, new PrintStream( baos ) );
        System.out.println( baos.toString() );
    }



    public static boolean tokenNotExists(AST ast, int tokenType){
        boolean result = true;
        if(ast != null){
            if(ast.getType() == tokenType){
                result = false;
            }else{
                result = tokenNotExists(ast.getFirstChild(), tokenType);
                if(!result) return false;
                result = tokenNotExists(ast.getNextSibling(), tokenType);
                if(!result) return false;
            }
        }
        return result;
    }

    public static boolean checkTokenName(AST ast, String tokenName){
        boolean result = true;
        if(ast != null){
            if(ast.getText().toLowerCase(Locale.ROOT) == "foo"){
                result = false;
            }else{
                result = checkTokenName(ast.getFirstChild(), tokenName);
                if(!result) return false;
                result = checkTokenName(ast.getNextSibling(), tokenName);
                if(!result) return false;
            }
        }
        return result;
    }

    public static boolean isEasySelect(AST ast){
        AST selectToken = ast.getFirstChild().getFirstChild().getNextSibling();
        if(selectToken == null || selectToken.getType() != HqlTokenTypes.SELECT){
            return true;
        }
        else{
            return tokenNotExists(selectToken, HqlTokenTypes.DOT);
        }
    }

    public static boolean isComplexSelect(AST ast){
        if(ast == null || ast.getFirstChild() == null || ast.getFirstChild().getFirstChild() == null)
            return true;
        AST selectToken = ast.getFirstChild().getFirstChild().getNextSibling();
        if(selectToken == null || selectToken.getType() != HqlTokenTypes.SELECT){
            return true;
        }
        else{
            AST fc = selectToken.getFirstChild();
            if(fc == null) return false;
            else{
                if(fc.getText().toLowerCase().equals("distinct")){
                    fc = fc.getNextSibling();
                }
                AST ns = fc.getNextSibling();
                if(ns == null) return false;
                return !ns.getText().isEmpty();
            }
        }
    }

    public static boolean isComplexFrom(AST ast){
        AST selectToken = null;
        if(ast.getFirstChild()!=null && ast.getFirstChild().getType() == HqlTokenTypes.FROM){
            selectToken = ast.getFirstChild();
        }else if(ast.getFirstChild()!=null && ast.getFirstChild().getFirstChild()!=null && ast.getFirstChild().getFirstChild().getType() == HqlTokenTypes.FROM){
            selectToken = ast.getFirstChild().getFirstChild();
        }
        if(selectToken == null) return false;
        else{
//            return !tokenNotExists(selectToken, HqlTokenTypes.COMMA);
            AST fc = selectToken.getFirstChild();
            if(fc == null) return false;
            else{
                AST range = fc.getNextSibling();
                if(range == null) return false;
                else{
                    return !range.getText().isEmpty();
                }
            }
        }
    }

    public static boolean isMultipleSelect(AST ast, int level){
        boolean res = false;
        if(ast == null) return false;
        if(level > LEVEL_TO_PARSE && ast.getType() == HqlTokenTypes.SELECT){
            return true;
        }
        else{
            res = isMultipleSelect(ast.getFirstChild(), level+1);
            if(res){
                return res;
            }
            res = isMultipleSelect(ast.getNextSibling(), level);
        }
        return res;
    }


    public static boolean checkSelectOuter(AST ast){
        boolean keep = tokenNotExists(ast, HqlTokenTypes.UPDATE);
        if(!keep) return false;
        keep = tokenNotExists(ast, HqlTokenTypes.DELETE);
        if(!keep) return false;


//        boolean noWhere = tokenNotExists(ast, HqlTokenTypes.WHERE);
//        if(noWhere && isEasySelect(ast)) return false;

        if(isComplexSelect(ast)) return false;
        if(isMultipleSelect(ast, 1)) return false;
//        AST levelOneLeft = ast.getFirstChild();
//        AST levelOneRight = levelOneLeft.getNextSibling();
//        List<AST> levelTwo = new ArrayList<>();
//        if(levelOneLeft!=null){
//            AST levelTwoLeftLeft = levelOneLeft.getFirstChild();
//            levelTwo.add(levelTwoLeftLeft);
//            if(levelTwoLeftLeft!=null) {
//                levelTwo.add(levelTwoLeftLeft.getFirstChild());
//            }
//        }
//        if(levelOneRight!=null){
//            AST levelTwoLeftLeft = levelOneRight.getFirstChild();
//            levelTwo.add(levelTwoLeftLeft);
//            if(levelTwoLeftLeft!=null) {
//                levelTwo.add(levelTwoLeftLeft.getFirstChild());
//            }
//        }
//        for(AST node: levelTwo) {
//            keep = tokenNotExists(node, HqlTokenTypes.SELECT);
//            if(!keep) return false;
//        }
        return true;
    }

    public static boolean checkWhereLevel(AST ast, int level){
        boolean result = true;
        if(ast != null){
            if(level >= 4){
                result = false;
            }else{
                result = checkWhereLevel(ast.getFirstChild(), level+1);
                if(!result) return false;
                result = checkWhereLevel(ast.getNextSibling(), level);
                if(!result) return false;
            }
        }
        return result;
    }

    public static void cleanSubAST(AST ast){
        if(ast == null){
            return;
        }
        AST fc = ast.getFirstChild();
        if(fc == null){
            return;
        }
        fc.setText("");
        if(fc.getNextSibling() != null){
            fc.getNextSibling().setText("");
            cleanSubAST(fc.getNextSibling());
        }
        cleanSubAST(fc);
    }

    public static List<Declaration> getSuperClassDeclarations(Declaration classNode){
        List<Declaration> result = new ArrayList<>();
        if(classNode == null){
            return result;
        }
        List<String> superClasses = classNode.getSuperClass();
        for(String superClass : superClasses) {
            Declaration superClassD = findTypeDeclaration(superClass);
            result.add(superClassD);
            getSuperClassDeclarations(superClassD);
        }
        return result;
    }

    public static void extractValuesAST(AST ast,List<String> res){
        if(ast == null) return;
        if(ast.getFirstChild()!=null && ast.getFirstChild().getNextSibling() != null && ast.getFirstChild().getNextSibling().getType() == HqlTokenTypes.COLON){
            AST idt = ast.getFirstChild().getNextSibling().getFirstChild();
            if(!idt.getText().isEmpty()) {

                if (idt != null) {
                    res.add(idt.getText());
//            ast.getFirstChild().getNextSibling().setText("[");
                }
                ast.getFirstChild().getNextSibling().getFirstChild().setText("!value!");
            }
        }
        if(ast.getType() == HqlTokenTypes.IN_LIST){
            AST colon = ast.getFirstChild();
            if(colon!=null){
                AST idt = colon.getFirstChild();
                if(idt!=null){
                    if(!idt.getText().isEmpty()) {
                        res.add(idt.getText());
                        ast.getFirstChild().getFirstChild().setText("!value!");
                    }
                }
            }
        }
        if(ast.getType() == HqlTokenTypes.METHOD_CALL){
            AST fc = ast.getFirstChild();
            ast.setText("");
            if(fc !=null) {
                fc.setText("");

            }
        }

        if(ast.getType() == HqlTokenTypes.EXPR_LIST){
            ast.setText("");
        }

        if(ast.getType() == HqlTokenTypes.LIKE || ast.getType() == HqlTokenTypes.NOT_LIKE){
            AST fc = ast.getFirstChild();
            if(fc !=null) {
                AST ns = fc.getNextSibling();
                if(ns!=null) {
                    cleanSubAST(ns);
                    ns.setText("");
                }
            }
            ast.setText(ast.getText()+" :!value!");
        }
        if(ast.getType()>=99 && ast.getType()<=105 || ast.getType() == HqlTokenTypes.NUM_INT || ast.getType() == HqlTokenTypes.FLOAT_SUFFIX || ast.getType() == HqlTokenTypes.EXPONENT || ast.getType() == HqlTokenTypes.PARAM || ast.getType() == HqlTokenTypes.TRUE || ast.getType() == HqlTokenTypes.FALSE){
            if(!ast.getText().isEmpty()) {
                res.add(ast.getText());
                ast.setText(":!value!");
            }
        }
        extractValuesAST(ast.getFirstChild(),res);
        extractValuesAST(ast.getNextSibling(),res);
    }

    public static void populateFromToIdentifiers(AST ast,String from){
        if(ast == null) return;
        if(ast.getType() != HqlTokenTypes.DOT){
            AST fc = ast.getFirstChild();
            AST fcs = null;
            if(fc != null && fc.getType()==HqlTokenTypes.IDENT && !fc.getText().contains("!value!") && !fc.getText().contains(from)) {
                fc.setText(from+"."+fc.getText());
                fcs = fc.getNextSibling();
            }
            if(fcs != null && fc.getType()==HqlTokenTypes.IDENT && !fcs.getText().contains("!value!") && !fcs.getText().contains(from)) {
                fcs.setText(from+"."+fcs.getText());
            }
        }
        populateFromToIdentifiers(ast.getFirstChild(),from);
        populateFromToIdentifiers(ast.getNextSibling(),from);
    }

    public static String extractSingleFrom(AST ast){
        String res = null;
        if(ast == null) return null;
        if(ast.getType()==HqlTokenTypes.FROM){
            AST range = ast.getFirstChild();
            if(range != null){
                AST type = range.getFirstChild();
                if(type!=null){
                    return type.getText();
                }
            }
        }
        res = extractSingleFrom(ast.getFirstChild());
        if(res!=null) return res;
        res = extractSingleFrom(ast.getNextSibling());
        return res;
    }

    public static boolean checkWhereLevelOuter(AST ast){
        try {
            AST where = ast.getFirstChild().getNextSibling();
            if(where.getType() != HqlTokenTypes.WHERE){
                return false;
            }else{
                checkWhereLevel(where, 1);
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }

    public static boolean filterAST(AST ast){
        if(ast == null) return false;
        boolean keep = checkSelectOuter(ast);
        if(!keep) return false;
        keep = tokenNotExists(ast, HqlTokenTypes.JOIN);
        if(!keep) return false;
        keep = tokenNotExists(ast, HqlTokenTypes.KEY);
        if(!keep) return false;
        keep = tokenNotExists(ast, HqlTokenTypes.VALUE);
        if(!keep) return false;
        keep = tokenNotExists(ast, HqlTokenTypes.INDEX_OP);
        if(!keep) return false;
        keep = tokenNotExists(ast, HqlTokenTypes.EXISTS);
        if(!keep) return false;
        keep = tokenNotExists(ast, HqlTokenTypes.OR);
        if(!keep) return false;

        keep = checkTokenName(ast, "foo");
        if(!keep) return false;
//        AST whereCandidate = ast.getFirstChild().getNextSibling();
        keep = checkWhereLevelOuter(ast);
        if(!keep) return false;

        if(isComplexFrom(ast)) return false;

        String from = extractSingleFrom(ast);
        if(from!=null) {
            populateFromToIdentifiers(ast, from);
        }
        return keep;
    }

    public static String extractParametrePosition(Node p){
        Range a = p.getRange().orElse(null);
        return a== null ? "" : a.toString();
    }
    public static List<Result> getHqlNodes(List<CompilationUnit> cus) {
        List<Result> results = new ArrayList<>();
        for (CompilationUnit cu: cus){
            List<MethodCallExpr> mces = cu.findAll(MethodCallExpr.class);
            for (MethodCallExpr mce: mces) {
                if(mce.getNameAsString().equals("createQuery")){
                    String hql = null;
//                    String returnType = null;
                    String methodName = null;
                    String hqlCandidate = "";
                    Boolean foundStrLitr = false;
                    Result result = new Result();

                    cu.getStorage().ifPresent(s -> result.setFullPath(s.getPath().toString()));
                    mce.getRange().ifPresent(s -> result.setCreateQueryPosition(s.toString()));

                    List<Parametre> params = new ArrayList<>();

                    // There may be no arguments
                    if(mce.getArguments().size()>0) {
                        // the first parametre of createQuery method call, may be hql or an identifier
                        try {
                            if(mce.getArgument(0).isStringLiteralExpr()) {
                                hqlCandidate = mce.getArgument(0).asStringLiteralExpr().getValue();
                                foundStrLitr = true;
                            }else if(mce.getArgument(0).isBinaryExpr()){
                                hqlCandidate = concatBinaryExpr(mce.getArgument(0).asBinaryExpr());
                                foundStrLitr = true;
                            }else{
                                hqlCandidate = mce.getArgument(0).toString();
                            }
                            if(hqlCandidate.endsWith(".class")){
                                // the case of .createQuery(Domain.class)
                                continue;
                            }
                        } catch (Exception e) {
                            System.out.println("698");
                            System.out.println(result.toString());
                            e.printStackTrace();
                            continue;
                        }
                    }

                    // find the parent method body of createQuery statement
                    Optional<Node> parentMethod = mce.getParentNode();
                    while(parentMethod.isPresent() && !(parentMethod.get() instanceof MethodDeclaration)){
                        parentMethod = parentMethod.get().getParentNode();
                    }
                    MethodDeclaration parent = (MethodDeclaration) parentMethod.orElse(null);

                    // gen method signature related info
                    if (parent != null) {
//                        returnType = parent.getTypeAsString();
                        methodName = parent.getNameAsString();
                        parent.getRange().ifPresent(s -> result.setMethodPosition(s.toString()));
                        List<ReturnStmt> rstmt = parent.findAll(ReturnStmt.class);
                        if(rstmt.size()>0) {
                            rstmt.get(rstmt.size()-1).asReturnStmt().getExpression().ifPresent(s ->
                                    result.setReturnExpression(s.toString())
                            );
                        }
                        for(Parameter p: parent.getParameters()) {
                            String type = p.getTypeAsString();
                            String name = p.getNameAsString();
                            Declaration declaration = findTypeDeclaration(type, cus, 1);
                            params.add(new Parametre(type,name)
                                    .setTypeDeclaration(declaration)
                                    .setPosition(extractParametrePosition(p))
                                    .populateModifiers(p.getModifiers())
                                    .populateAnnotations(p.getAnnotations())
                            );
                        }
//                        result.setReturnType(returnType)
//                                .setMethodName(methodName).setParams(params).setMethodBody(parent.toString());
                        result.setMethodName(methodName).setParams(params).setMethodBody(parent.toString());

                    }

                    // find hql
                    if(foundStrLitr){
                        // the easiest case
                        hql = hqlCandidate;
                        result.setHql(hql);
                    }else{
                        // looking at the method body
                        List<String> hqls = new ArrayList<>();
                        if(parent!=null) {
                            List<ExpressionStmt> statements = parent.findAll(ExpressionStmt.class);
                            for(ExpressionStmt statement : statements){
                                if(statement.getExpression() instanceof VariableDeclarationExpr) {
                                    for (VariableDeclarator vd : statement.getExpression().asVariableDeclarationExpr().getVariables()) {
                                        if (vd != null && vd.getNameAsString().equals(hqlCandidate)) {
                                            vd.getInitializer().ifPresent(init -> {
                                                String tmp = null;
                                                if (init.isBinaryExpr()) {
                                                    tmp = concatBinaryExpr(init.asBinaryExpr());
                                                }else if (init.isLiteralExpr()) {
                                                    tmp = extractLiteralExpr(init.asLiteralExpr());
                                                }else{
                                                    if  (!init.toString().contains(".class")) {
                                                        tmp = init.toString();
                                                    }
                                                }
                                                if(tmp!=null){
                                                    hqls.add(tmp);
                                                }
                                            });
                                        }
                                    }
                                }
                                else if(statement.getExpression() instanceof AssignExpr) {
                                    List<String> extractedHql = extractHqlExpr(hqlCandidate, statement.getExpression().asAssignExpr());
                                    hqls.addAll(extractedHql);
                                }
//                                hqls.add(statement.getExpression().toString());
                            }
                        }
                        result.setHql(hqls);
                    }
                    // if hql is not found this entity is useless
                    if(result.getHql()!=null && result.getHql().size()>0) {
                        results.add(result);
                    }else{

                        // TODO: LOGGING
                        System.out.println("\n##HQL Not Found");
                        System.out.println("Candidate:"+hqlCandidate);
                        System.out.println("File:"+result.fullPath);
                        System.out.println("Method:"+result.methodName);
                        System.out.println("Position:"+result.createQueryPosition);
                    }
                }
            }
        }
        return results;
    }
    public static String extractTypeFromExpression(String expr){
        if(expr != null) {
            if (expr.contains("<")) {
                String[] tmp = expr.split("<");
                expr = tmp[tmp.length - 1].split(">")[0];
            }
            expr = expr.replaceAll("\\[]", "");
        }
        return expr;
    }

    public static Declaration populateDeclaration(TypeDeclaration td, List<CompilationUnit> cus, Integer level, Declaration d){
        for (Object m : td.getMembers()) {
            if (m instanceof FieldDeclaration) {
                for(VariableDeclarator vd: ((FieldDeclaration) m).asFieldDeclaration().getVariables()) {
                    if(vd!=null) {
                        Parametre p = new Parametre(vd.getTypeAsString(), vd.getNameAsString())
                                .setPosition(extractParametrePosition(vd))
                                .populateAnnotations(((FieldDeclaration) m).getAnnotations());
                        if (level <= LEVEL_TO_PARSE) {
                            Declaration dec = findTypeDeclaration(vd.getTypeAsString(), cus, level + 1);
                            p.setTypeDeclaration(dec);
                        }
                        if (d.getFields() == null) {
                            d.setFields(new ArrayList<>());
                        }
                        d.getFields().add(p);
                    }
                }
            }
        }
        return d;
    }

    public static List<ClassOrInterfaceDeclaration> extractSubTypes(TypeDeclaration td){
        List<ClassOrInterfaceDeclaration> res = new ArrayList<>();
        for(Object member: td.getMembers()){
            if(member instanceof ClassOrInterfaceDeclaration){
                res.add((ClassOrInterfaceDeclaration)member);
            }
        }
        return res;
    }
    // find type declaration

    public static Declaration getTypeFromCache(String retType){
        return declarationCache.get(retType);
    }


    public static Declaration findTypeDeclaration(String retType){
        return findTypeDeclaration(retType, cusCache, 1);
    }

    public static void setCusCache(List<CompilationUnit> cus){
        cusCache = cus;
    }
    public static Declaration findTypeDeclaration(String retType, List<CompilationUnit> cus, Integer level) {
        Declaration d = null;
        if(cusCache == null){
            cusCache = cus;
        }
        if(Const.builtinTypes.contains(retType)){
            return null;
        }
        if(retType != null) {
            if(declarationCache.get(retType)!=null){
                return declarationCache.get(retType);
            }
            retType = extractTypeFromExpression(retType);
            for (CompilationUnit cu : cus) {
                for (TypeDeclaration td : cu.getTypes()) {
                    if (td.getNameAsString().equals(retType)) {
                        d = new Declaration(cu, td);
                        populateDeclaration(td, cus, level, d);
                    }
                    if(d==null){
                        for(ClassOrInterfaceDeclaration cid: extractSubTypes(td)){
                            if (cid.getNameAsString().equals(retType)) {
                                d = new Declaration(cu, cid);
                                populateDeclaration(cid, cus, level, d);
                            }
                        }
                    }
                }
            }
        }
        filterCycloDeclaration(d, new ArrayList<>(), new ArrayList<>());
        declarationCache.put(retType,d);
        return d;
    }

    public static CompilationUnit findCompilationUnit(String retType, List<CompilationUnit> cus) {
        CompilationUnit d = null;
        if(Const.builtinTypes.contains(retType)){
            return null;
        }
        if(retType != null) {
            retType = extractTypeFromExpression(retType);
            for (CompilationUnit cu : cus) {
                for (TypeDeclaration td : cu.getTypes()) {
                    if (td.getNameAsString().equals(retType)) {
                        return cu;
                    }
                }
            }
        }
        return d;
    }

    public static void filterCycloDeclaration(Declaration d, List<Parametre> legacy, List<Declaration> legacyDec){
        if(d == null || d.getFields() == null) return;
        legacyDec.add(d);
        List<Parametre> newParams = new ArrayList<>();
        for(Parametre SubP: d.getFields()){
            Declaration subType = SubP.getTypeDeclaration();
            if(subType==null){
                newParams.add(SubP);
                continue;
            }
            if(legacyDec.contains(subType)){
                continue;
            }else{
                newParams.add(SubP);
            };
            filterCycloDeclaration(subType, legacy, legacyDec);
        }
        d.setFields(newParams);
    }

    // find called in
    public static List<Declaration> findCalledIn(String methodName, List<CompilationUnit> cus) {
        List<Declaration> calledIn = new ArrayList<>();
        if(methodName != null) {
            for (CompilationUnit cu : cus) {
                List<MethodCallExpr> mces = cu.findAll(MethodCallExpr.class);
                for (MethodCallExpr mce: mces) {
                    if (mce.getNameAsString().equals(methodName)) {
                        Optional<Node> parentMethod = mce.getParentNode();
                        while(parentMethod.isPresent() && !(parentMethod.get() instanceof MethodDeclaration)){
                            parentMethod = parentMethod.get().getParentNode();
                        }
                        MethodDeclaration parent = (MethodDeclaration) parentMethod.orElse(null);
                        if(parent != null){
                            calledIn.add(new Declaration(cu, parent));
                        }
                    }
                }
            }
        }
        return calledIn;
    }

    // populate context of hql nodes
    public static List<Result> populateContext(List<Result> toPopulate, List<CompilationUnit> cus){
        for(Result res: toPopulate){
            // Populate type
//            res.setReturnTypeDeclaration(findTypeDeclaration(res.getReturnType(), cus, 1));
            // Populate method call context
            res.setCalledIn(findCalledIn(res.getMethodName(),cus));
        }
        return toPopulate;
    }

    public static void main(String[] args) throws IOException {
        String project;
        String root_path;
        String output_path;
        try {
            project = args[0];
        }catch (Exception e){
            project = "SpringBlog";
        }
        try {
            root_path = args[1];
        }catch (Exception e){
            root_path = "D:\\tools\\hql\\projects";
        }
        try {
            output_path = args[2];
        }catch (Exception e){
            output_path = "D:\\tools\\hql\\projects";
        }
        SmellDetection.exec(project, root_path, output_path);
//        exec(project, root_path, output_path);
//        exec_post(project, root_path, output_path);

    }
}
