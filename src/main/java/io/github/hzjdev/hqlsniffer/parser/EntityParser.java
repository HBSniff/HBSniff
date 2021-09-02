package io.github.hzjdev.hqlsniffer.parser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.Parametre;
import io.github.hzjdev.hqlsniffer.utils.Const;
import io.github.hzjdev.hqlsniffer.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.hzjdev.hqlsniffer.utils.Const.LEVEL_TO_PARSE;
import static io.github.hzjdev.hqlsniffer.utils.Utils.extractTypeFromExpression;

public class EntityParser {


    public static Map<String, Declaration> declarationCache = new HashMap<>();
    public static List<CompilationUnit> cusCache;

    public static void setCusCache(List<CompilationUnit> cus) {
        cusCache = cus;
    }

    public static List<CompilationUnit> getEntities(List<CompilationUnit> cus) {
        List<CompilationUnit> results = new ArrayList<>();
        for (CompilationUnit cu : cus) {
            List<AnnotationExpr> annotations = cu.findAll(AnnotationExpr.class);
            for (AnnotationExpr annotation : annotations) {
                if (annotation.getNameAsString().equals("Entity")) {
                    results.add(cu);
                }
            }
        }
        return results;
    }

    public static List<CompilationUnit> parseFromDir(String dirPath, List<CompilationUnit> results) {
        File dirfile = new File(dirPath);//根据DirPath实例化一个File对象
        File[] files = dirfile.listFiles();//listFiles():以相对路径返回该目录下所有的文件名的一个File对象数组
        if (files == null) {
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
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(new File(path));
                        results.add(cu);
                    } catch (ParseProblemException e) {
                        // We can do nothing for parse error.
                        // System.out.println(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return results;
    }

    public static List<Declaration> genDeclarationsFromCompilationUnits(List<CompilationUnit> cus) {
        List<Declaration> entities = new ArrayList<>();
        if (cus == null) return entities;
        for (CompilationUnit cu : cus) {
            for (TypeDeclaration td : cu.getTypes()) {
                Declaration d = findTypeDeclaration(td.getNameAsString(), cus, 1);
                if (d != null) {
                    entities.add(d);
                }
            }
        }
        return entities;
    }

    public static Parametre getIdentifierProperty(final Declaration entity) {
        if (entity == null) return null;
        List<Parametre> declaredFields = entity.getFields();
        for (Parametre fieldNode : declaredFields) {
            List<String> annotations = fieldNode.getAnnotations();
            String ID = "@Id";
            if (annotations.contains(ID)) {
                return fieldNode;
            }
        }
        for (Declaration superClassEntity : getSuperClassDeclarations(entity)) {
            Parametre fieldNode = getIdentifierProperty(superClassEntity);
            if (fieldNode != null) {
                return fieldNode;
            }
        }
        for (Declaration method : entity.getMembers()) {
            List<String> annotations = method.getAnnotations();
            String ID = "@Id";
            if (annotations.contains(ID)) {
                String type = method.getReturnTypeName();
                String fieldName;
                if (type != null && (type.equals(Utils.BOOLEAN_PRIMITIVE) || type.equals(Utils.BOOLEAN_CLASS))) {
                    fieldName = method.getName().replaceFirst("is", "");
                } else {
                    fieldName = method.getName().replaceFirst("get", "");
                }
                String realFieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                Parametre field = entity.getFields().stream().filter(i -> i.getName().equals(realFieldName)).findFirst().orElse(null);
                if (field != null) {
                    return field;
                } else {
                    for (Declaration superClassEntity : getSuperClassDeclarations(entity)) {
                        field = superClassEntity.getFields().stream().filter(i -> i.getName().equals(realFieldName)).findFirst().orElse(null);
                        if (field != null) {
                            return field;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static List<Declaration> getSuperClassDeclarations(Declaration classNode) {
        List<Declaration> result = new ArrayList<>();
        if (classNode == null) {
            return result;
        }
        List<String> superClasses = classNode.getSuperClass();
        for (String superClass : superClasses) {
            Declaration superClassD = findTypeDeclaration(superClass);
            if (superClassD == null) continue;
            result.add(superClassD);
            getSuperClassDeclarations(superClassD);
        }
        return result;
    }


    public static void populateDeclaration(List<CompilationUnit> cus, Integer level, Declaration d) {
        if (level <= LEVEL_TO_PARSE) {
            d.setFields(d.getFields().stream().map(
                    i -> i.setTypeDeclaration(findTypeDeclaration(i.getName(), cus, level + 1)))
                    .collect(Collectors.toList()));
        }
    }

    public static List<ClassOrInterfaceDeclaration> extractSubTypes(TypeDeclaration td) {
        List<ClassOrInterfaceDeclaration> res = new ArrayList<>();
        for (Object member : td.getMembers()) {
            if (member instanceof ClassOrInterfaceDeclaration) {
                res.add((ClassOrInterfaceDeclaration) member);
            }
        }
        return res;
    }


    public static Declaration findTypeDeclaration(String retType) {
        return findTypeDeclaration(retType, cusCache, 1);
    }

    public static Declaration findTypeDeclaration(String retType, List<CompilationUnit> cus, Integer level) {
        Declaration d = null;
        if (cusCache == null) {
            cusCache = cus;
        }
        if (Const.builtinTypes.contains(retType)) {
            return null;
        }
        if (retType != null) {
            if (declarationCache.get(retType) != null) {
                return declarationCache.get(retType);
            }
            retType = extractTypeFromExpression(retType);
            for (CompilationUnit cu : cus) {
                for (TypeDeclaration td : cu.getTypes()) {
                    if (td.getNameAsString().equals(retType)) {
                        d = new Declaration(cu, td);
                        populateDeclaration(cus, level, d);
                    }
                    if (d == null) {
                        for (ClassOrInterfaceDeclaration cid : extractSubTypes(td)) {
                            if (cid.getNameAsString().equals(retType)) {
                                d = new Declaration(cu, cid);
                                populateDeclaration(cus, level, d);
                            }
                        }
                    }
                }
            }
        }
        filterCycloDeclaration(d, new ArrayList<>(), new ArrayList<>());
        declarationCache.put(retType, d);
        return d;
    }

    public static void filterCycloDeclaration(Declaration d, List<Parametre> legacy, List<Declaration> legacyDec) {
        if (d == null || d.getFields() == null) return;
        legacyDec.add(d);
        List<Parametre> newParams = new ArrayList<>();
        for (Parametre SubP : d.getFields()) {
            Declaration subType = SubP.getTypeDeclaration();
            if (subType == null) {
                newParams.add(SubP);
                continue;
            }
            if (legacyDec.contains(subType)) {
                continue;
            } else {
                newParams.add(SubP);
            }
            filterCycloDeclaration(subType, legacy, legacyDec);
        }
        d.setFields(newParams);
    }


}
