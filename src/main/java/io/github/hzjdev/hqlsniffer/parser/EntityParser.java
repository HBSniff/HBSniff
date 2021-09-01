package io.github.hzjdev.hqlsniffer.parser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import io.github.hzjdev.hqlsniffer.utils.Const;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import java.io.File;
import java.util.*;

import static io.github.hzjdev.hqlsniffer.utils.Const.LEVEL_TO_PARSE;
import static io.github.hzjdev.hqlsniffer.utils.Utils.extractParametrePosition;
import static io.github.hzjdev.hqlsniffer.utils.Utils.extractTypeFromExpression;

public class EntityParser {


    public static Map<String, Declaration> declarationCache = new HashMap<>();
    public static List<CompilationUnit> cusCache;

    public static void setCusCache(List<CompilationUnit> cus){
        cusCache = cus;
    }

    public static List<CompilationUnit> getEntities(List<CompilationUnit> cus) {
        List<CompilationUnit> results = new ArrayList<>();
        for (CompilationUnit cu: cus){
            List<AnnotationExpr> annotations = cu.findAll(AnnotationExpr.class);
            for (AnnotationExpr annotation: annotations) {
                if(annotation.getNameAsString().equals("Entity")){
                    results.add(cu);
                }
            }
        }
        return results;
    }

    public static List<CompilationUnit> parseFromDir(String dirPath, List<CompilationUnit> results) {
        File dirfile = new File(dirPath);//根据DirPath实例化一个File对象
        File[] files = dirfile.listFiles();//listFiles():以相对路径返回该目录下所有的文件名的一个File对象数组
        if ( files == null ) {
            return results;//[]
        }
        //遍历目录-2
        for (File file : files) {
            // isDirectory()是检查一个对象是否是文件夹,如果是则返回true，否则返回false
            if (file.isDirectory()) {
                parseFromDir(file.getAbsolutePath(), results);// getAbsolutePath(): 返回的是定义时的路径对应的相对路径
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
                        e.printStackTrace();
                    }
                }
            }
        }
        return results;
    }

    public static List<Declaration> genDeclarationsFromCompilationUnits(List<CompilationUnit> cus){
        List<Declaration> entities = new ArrayList<>();
        if(cus==null) return entities;
        for(CompilationUnit cu: cus){
            for(TypeDeclaration td: cu.getTypes()){
                Declaration d = findTypeDeclaration(td.getNameAsString(), cus, 1);
                if(d!=null) {
                    entities.add(d);
                }
            }
        }
        return entities;
    }
    public static Parametre getIdentifierProperty(final Declaration entity) {
        if(entity == null) return null;
        List<Parametre> declaredFields = entity.getFields();
        for (Parametre fieldNode : declaredFields) {
            List<String> annotations = fieldNode.getAnnotations();
            String ID="@Id";
            if (annotations.contains(ID)) {
                return fieldNode;
            }
        }
        for(Declaration superClassEntity : getSuperClassDeclarations(entity)){
            Parametre fieldNode = getIdentifierProperty(superClassEntity);
            if(fieldNode != null){
                return fieldNode;
            }
        }
        return null;
    }

    public static List<Declaration> getSuperClassDeclarations(Declaration classNode){
        List<Declaration> result = new ArrayList<>();
        if(classNode == null){
            return result;
        }
        List<String> superClasses = classNode.getSuperClass();
        for(String superClass : superClasses) {
            Declaration superClassD = findTypeDeclaration(superClass);
            if(superClassD == null) continue;
            result.add(superClassD);
            getSuperClassDeclarations(superClassD);
        }
        return result;
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


    public static Declaration findTypeDeclaration(String retType){
        return findTypeDeclaration(retType, cusCache, 1);
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


}
