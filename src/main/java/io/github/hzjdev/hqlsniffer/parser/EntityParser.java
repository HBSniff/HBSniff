package io.github.hzjdev.hqlsniffer.parser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.hzjdev.hqlsniffer.model.Declaration;
import io.github.hzjdev.hqlsniffer.model.ParametreOrField;
import io.github.hzjdev.hqlsniffer.utils.Const;
import io.github.hzjdev.hqlsniffer.utils.Utils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.hzjdev.hqlsniffer.utils.Const.IDENT_ANNOT_EXPR;
import static io.github.hzjdev.hqlsniffer.utils.Const.LEVEL_TO_POPULATE_DECLARATION;
import static io.github.hzjdev.hqlsniffer.utils.Utils.extractTypeFromCollection;

public class EntityParser {


    public static Map<String, Declaration> declarationCache = new HashMap<>(); // using a cache will optimize performance for big projects
    public static List<CompilationUnit> cusCache;

    public static void setCusCache(List<CompilationUnit> cus) {
        cusCache = cus;
    }

    /**
     * filter the classes without @Entity annotation
     * @param cus CompilationUnits to filter
     * @return list of results
     */
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

    /**
     * parse all classes in a subdirectories of a path
     * @param dirPath path to parse
     * @param results list of compilation units
     * @return  list of compilation units
     */
    public static List<CompilationUnit> parseFromDir(String dirPath, List<CompilationUnit> results) {
        File[] files = new File(dirPath).listFiles();
        if (files == null) {
            return results;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                parseFromDir(file.getAbsolutePath(), results);
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

    /**
     * generate a list of Declaration from a list of CompilationUnits
     * @param cus input of CompilationUnits
     * @return a list of Declarations
     */
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

    /**
     * get the identifier field of an @Entity
     * @param entity entity class
     * @return identifier field
     */
    public static ParametreOrField getIdentifierProperty(final Declaration entity) {
        if (entity == null) return null;
        List<ParametreOrField> declaredFields = entity.getFields();
        for (ParametreOrField fieldNode : declaredFields) {
            List<String> annotations = fieldNode.getAnnotations();
            if (annotations.contains(IDENT_ANNOT_EXPR)) {
                return fieldNode; // any field is annotated by @Id
            }
        }
        for (Declaration superClassEntity : getSuperClassDeclarations(entity)) {
            ParametreOrField fieldNode = getIdentifierProperty(superClassEntity);
            if (fieldNode != null) {
                return fieldNode; // any field of superclasses is annotated by @Id
            }
        }
        for (Declaration method : entity.getMembers()) {
            // any getter is annotated by @Id
            List<String> annotations = method.getAnnotations();
            if (annotations.contains(IDENT_ANNOT_EXPR)) {
                String type = method.getReturnTypeName();
                String fieldName;
                if (type != null && (type.equals(Utils.BOOLEAN_PRIMITIVE) || type.equals(Utils.BOOLEAN_CLASS))) {
                    fieldName = method.getName().replaceFirst("is", "");
                } else {
                    fieldName = method.getName().replaceFirst("get", "");
                }
                String realFieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
                ParametreOrField field = entity.getFields().stream().filter(i -> i.getName().equals(realFieldName)).findFirst().orElse(null);
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

    /**
     * get superclasses of a class
     * @param dec  the considered class
     * @param result result of Declaration list
     * @return result of Declaration list
     */
    private static List<Declaration> getSuperClassDeclarations(Declaration dec, List<Declaration> result) {
        if (dec == null) {
            return result;
        }
        List<String> superClasses = dec.getSuperClass();
        for (String superClass : superClasses) {
            Declaration superClassD = findTypeDeclaration(superClass, cusCache, 1);
            if (superClassD == null) continue;
            result.add(superClassD);
            getSuperClassDeclarations(superClassD, result);
        }
        return result;
    }

    /**
     * the public entrance of getSuperClassDeclarations
     * @param dec result of Declaration list
     * @return result of Declaration list
     */
    public static List<Declaration> getSuperClassDeclarations(Declaration dec) {
        List<Declaration> result = new ArrayList<>();
        return getSuperClassDeclarations(dec, result);
    }

    /**
     * populate declarations with parsed information
     * @param cus list of the scope of classes
     * @param level level to parse
     * @param d declaration to populate
     */
    public static void populateDeclaration(List<CompilationUnit> cus, Integer level, Declaration d) {
        if (level <= LEVEL_TO_POPULATE_DECLARATION) {
            d.setFields(d.getFields().stream().map(
                    i -> i.setTypeDeclaration(findTypeDeclaration(i.getName(), cus, level + 1)))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * extract Member types of a TypeDeclaration
     * @param td TypeDeclaration to extract
     * @return a list of ClassOrInterfaceDeclaration
     */
    public static List<ClassOrInterfaceDeclaration> extractMemberTypes(TypeDeclaration td) {
        List<ClassOrInterfaceDeclaration> res = new ArrayList<>();
        for (Object member : td.getMembers()) {
            if (member instanceof ClassOrInterfaceDeclaration) {
                res.add((ClassOrInterfaceDeclaration) member);
            }
        }
        return res;
    }


    /**
     * Check any method is called in a scope CompilationUnits
     * @param methodName method name
     * @param cus search scope of CompilationUnits
     * @return List of the file which called the method
     */
    public static List<Declaration> findCalledIn(String methodName, List<CompilationUnit> cus) {
        List<Declaration> calledIn = new ArrayList<>();
        if (methodName != null) {
            for (CompilationUnit cu : cus) {
                List<MethodCallExpr> mces = cu.findAll(MethodCallExpr.class);
                for (MethodCallExpr mce : mces) {
                    if (mce.getNameAsString().equals(methodName)) {
                        Optional<Node> parentMethod = mce.getParentNode();
                        while (parentMethod.isPresent() && !(parentMethod.get() instanceof MethodDeclaration)) {
                            parentMethod = parentMethod.get().getParentNode();
                        }
                        MethodDeclaration parent = (MethodDeclaration) parentMethod.orElse(null);
                        if (parent != null) {
                            calledIn.add(new Declaration(cu, parent));
                        }
                    }
                }
            }
        }
        return calledIn;
    }

    /**
     * Check any method is called in a scope CompilationUnits
     * @param methodName method name
     * @param cus search scope of CompilationUnits
     * @return List of the file which called the method
     */
    public static List<Declaration> findCalledIn(String methodName, String typeName, List<CompilationUnit> cus) {
        List<Declaration> calledIn = new ArrayList<>();
        if (methodName != null) {
            for (CompilationUnit cu : cus) {
                boolean imported = false;
                for(ImportDeclaration id: cu.getImports()){
                    if (id.getNameAsString().contains(typeName)){
                        imported = true;
                    }
                }
                List<MethodCallExpr> mces = cu.findAll(MethodCallExpr.class);
                for (MethodCallExpr mce : mces) {
                    if (imported && mce.getNameAsString().equals(methodName)) {
                        Optional<Node> parentMethod = mce.getParentNode();
                        while (parentMethod.isPresent() && !(parentMethod.get() instanceof MethodDeclaration)) {
                            parentMethod = parentMethod.get().getParentNode();
                        }
                        MethodDeclaration parent = (MethodDeclaration) parentMethod.orElse(null);
                        if (parent != null) {
                            calledIn.add(new Declaration(cu, parent));
                        }
                    }
                }
            }
        }
        return calledIn;
    }

    /**
     * locate a type in CompilationUnits and generate a Declaration for it
     * @param toFind type name of the Declaration
     * @param cus scope to find
     * @param level level to populate the declaration
     * @return the result Declaration
     */
    public static Declaration findTypeDeclaration(String toFind, List<CompilationUnit> cus, Integer level) {
        Declaration d = null;
        if (cusCache == null) {
            cusCache = cus;
        }
        if (Const.builtinTypes.contains(toFind)) {
            return null;
        }
        if (toFind != null) {
            if (declarationCache.get(toFind) != null) {
                return declarationCache.get(toFind);
            }
            toFind = extractTypeFromCollection(toFind);
            for (CompilationUnit cu : cus) {
                for (TypeDeclaration td : cu.getTypes()) {
                    if (td.getNameAsString().equals(toFind)) {
                        d = new Declaration(cu, td);
                        populateDeclaration(cus, level, d);
                    }
                    if (d == null) {
                        for (ClassOrInterfaceDeclaration cid : extractMemberTypes(td)) {
                            if (cid.getNameAsString().equals(toFind)) {
                                d = new Declaration(cu, cid);
                                populateDeclaration(cus, level, d);
                            }
                        }
                    }
                }
            }
        }
        filterCyclicDeclaration(d, new ArrayList<>());
        declarationCache.put(toFind, d);
        return d;
    }

    /**
     * exclude cyclic dependency in a Declaration
     * @param d declaration to check
     * @param visited visited Declaration
     */
    public static void filterCyclicDeclaration(Declaration d, List<Declaration> visited) {
        if (d == null || d.getFields() == null) return;
        visited.add(d);
        List<ParametreOrField> newParams = new ArrayList<>();
        for (ParametreOrField SubP : d.getFields()) {
            Declaration subType = SubP.getTypeDeclaration();
            if (subType == null) {
                newParams.add(SubP);
                continue;
            }
            if (visited.contains(subType)) {
                continue;
            } else {
                newParams.add(SubP);
            }
            filterCyclicDeclaration(subType, visited);
        }
        d.setFields(newParams);
    }


}
