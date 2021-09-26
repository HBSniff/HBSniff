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

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import io.github.hzjdev.hbsniff.model.Declaration;
import io.github.hzjdev.hbsniff.model.ParametreOrField;
import io.github.hzjdev.hbsniff.utils.Const;
import io.github.hzjdev.hbsniff.utils.Utils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.hzjdev.hbsniff.utils.Const.*;
import static io.github.hzjdev.hbsniff.utils.Utils.extractTypeFromCollection;

public class EntityParser {


    public static Map<String, Declaration> declarationCache = new HashMap<>(); // using a cache will optimize performance for big projects
    public static Map<String, List<Declaration>> packageCache = new HashMap<>(); // this map classifies cus by their package
    public static List<CompilationUnit> cusCache;

    public static void setCusCache(List<CompilationUnit> cus) {
        cusCache = cus;
        declarationCache.clear();
        packageCache.clear();
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
                if (annotation.getNameAsString().equals(ENTITY_ANNOT_EXPR)) {
                    results.add(cu);
                    break;
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
    private static List<CompilationUnit> parseFromDir(String dirPath, List<CompilationUnit> results) {
        File[] files = new File(dirPath).listFiles();
        if (files == null) {
            return results;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                parseFromDir(file.getAbsolutePath(), results);
            } else {
                if (file.getName().toLowerCase().endsWith(JAVA_SUFFIX)) {
                    String path = file.getPath();
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(new File(path));
                        results.add(cu);
                    } catch (ParseProblemException e) {
//                      upstream parse error.
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return results;
    }

    /**
     * parse all classes in a subdirectories of a path
     * @param dirPath path to parse
     * @return  list of compilation units
     */
    public static List<CompilationUnit> parseFromDir(String dirPath) {
        List<CompilationUnit> results = new ArrayList<>();
        parseFromDir(dirPath, results);
        setCusCache(results);
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
                String path = "";
                if(cu.getStorage().isPresent()){
                    path = cu.getStorage().get().getPath().toString();
                }
                Declaration d = findTypeDeclaration(td.getNameAsString(), path, cus, 1);
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
            if (fieldNode.annotationIncludes(IDENT_ANNOT_EXPR)) {
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
            if (method.annotationIncludes(IDENT_ANNOT_EXPR)) {
                String type = method.getReturnTypeName();
                String fieldName;
                if (type != null && (type.equals(Utils.BOOLEAN_PRIMITIVE) || type.equals(Utils.BOOLEAN_CLASS))) {
                    fieldName = method.getName().replaceFirst(GETTER_METHOD_PREFIX_BOOL, "");
                } else {
                    fieldName = method.getName().replaceFirst(GETTER_METHOD_PREFIX_NORMAL, "");
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
            Declaration superClassD = findTypeDeclaration(superClass);
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
    private static void populateDeclaration(List<CompilationUnit> cus, String fullPath, Integer level, Declaration d) {
        if (level <= LEVEL_TO_POPULATE_DECLARATION) {
            d.setFields(d.getFields().stream().map(
                    i -> i.setTypeDeclaration(findTypeDeclaration(i.getName(), fullPath, cus, level + 1)))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * extract Member types of a TypeDeclaration
     * @param td TypeDeclaration to extract
     * @return a list of ClassOrInterfaceDeclaration
     */
    private static List<ClassOrInterfaceDeclaration> extractMemberTypes(TypeDeclaration td) {
        List<ClassOrInterfaceDeclaration> res = new ArrayList<>();
        for (Object member : td.getMembers()) {
            if (member instanceof ClassOrInterfaceDeclaration) {
                res.add((ClassOrInterfaceDeclaration) member);
            }
        }
        return res;
    }


    /**
     * Check any method is called in a scope CompilationUnits (not recommended because it only searches the method call having the same name regardless of their class)
     * @param methodName method name
     * @param cus search scope of CompilationUnits
     * @return List of the file which called the method
     */
    @Deprecated
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
     * @param md MethodDeclaration
     * @param typeName the name of the type containing the md
     * @param cus search scope of CompilationUnits
     * @return List of the file which called the method
     */
    public static List<Declaration> findCalledIn(MethodDeclaration md, String typeName, List<CompilationUnit> cus) {
        List<Declaration> calledIn = new ArrayList<>();
        Declaration toLocate = findTypeDeclaration(typeName);
        if (md != null) {
            // search if the considered cu and the cu containing method call is in the same package
            if(toLocate!=null) {
                for (Declaration dPkg : packageCache.get(getPackageName(toLocate.getRawCU()))) {
                    MethodDeclaration toAdd = findMethodCallInCompilationUnit(dPkg.getRawCU(), md);
                    if (toAdd != null) {
                        calledIn.add(new Declaration(dPkg.getRawCU(), toAdd));
                    }
                }
            }
            for (CompilationUnit cu : cus) {
                // check if typeName is imported
                boolean imported = false;
                TypeDeclaration primaryType = cu.getPrimaryType().orElse(null);
                if(primaryType!=null){
                    imported = primaryType.getNameAsString().equals(typeName);
                }
                if(!imported) {
                    for (ImportDeclaration id : cu.getImports()) {
                        if (id.getNameAsString().contains(typeName)) {
                            imported = true;
                        }
                    }
                }
                // we find method call if it is imported in the cu
                if(imported) {
                    MethodDeclaration toAdd = findMethodCallInCompilationUnit(cu,md);
                    if(toAdd!=null) {
                        calledIn.add(new Declaration(cu, toAdd));
                    }
                }
            }
        }
        return calledIn;
    }

    /**
     * helper method for findCalledIn locating method call in compilation unit
     * @param cu the compilation unit to process
     * @param md the method declaration to find
     * @return the method that contains the method call
     */
    private static MethodDeclaration findMethodCallInCompilationUnit(CompilationUnit cu, MethodDeclaration md){
        List<MethodCallExpr> mces = cu.findAll(MethodCallExpr.class);
        for (MethodCallExpr mce : mces) {
            if (mce.getNameAsString().equals(md.getNameAsString()) && mce.getArguments().size() == md.getParameters().size()) {
                Optional<Node> parentMethod = mce.getParentNode();
                while (parentMethod.isPresent() && !(parentMethod.get() instanceof MethodDeclaration)) {
                    parentMethod = parentMethod.get().getParentNode();
                }
                MethodDeclaration parent = (MethodDeclaration) parentMethod.orElse(null);
                if (parent != null) {
                    return parent;
                }
            }
        }
        return null;
    }

    /**
     * locate a type in CompilationUnits and generate a Declaration for it
     * @param toFind type name of the Declaration from cache
     * @return the result Declaration
     */
    public static Declaration findTypeDeclaration(String toFind) {
        initDeclarationCache();
        for(Map.Entry<String, Declaration> e: declarationCache.entrySet()){
            // path match
            if(e.getKey()!=null) {
                if (e.getKey().equals(toFind)) {
                    return e.getValue();
                }
            }
        }
        for(Map.Entry<String, Declaration> e: declarationCache.entrySet()){
            // type/class name match
            if(e.getValue()!=null) {
                if (e.getValue().getName().equals(toFind)) {
                    return e.getValue();
                }
            }
        }
        for(Map.Entry<String, Declaration> e: declarationCache.entrySet()){
            // type/class name and file name match
            if(e.getKey()!=null) {
                try {
                    if (e.getKey().split("\\\\")[0].split("/")[0].equals(toFind)){
                        return e.getValue();
                    }
                }catch(Exception ex){
                }
            }
        }
        return null;
    }

    /**
     * initialize declarationCache
     */
    private static void initDeclarationCache(){
        if(cusCache == null) return;
        if(declarationCache.size()<1){
            for (CompilationUnit cu : cusCache) {
                if(cu == null){
                    continue;
                }
                String path = "";
                if (cu.getStorage().isPresent()) {
                    path = cu.getStorage().get().getPath().toString();
                }
                for (TypeDeclaration td : cu.getTypes()) {
                    Declaration d = new Declaration(cu, td);
                    populateDeclaration(cusCache, path, 1, d);
                    declarationCache.put(path,d);
                    String packageName = getPackageName(d.getRawCU());
                    packageCache.computeIfAbsent(packageName, k -> new ArrayList<>());
                    packageCache.get(packageName).add(d);
                }
            }
        }
    }
    /**
     * locate a type in CompilationUnits and generate a Declaration for it
     * @param toFind type name of the Declaration
     * @param fullPath path of the type declaration
     * @param cus scope to find
     * @param level level to populate the declaration
     * @return the result Declaration
     */
    public static Declaration findTypeDeclaration(String toFind, String fullPath, List<CompilationUnit> cus, Integer level) {
        Declaration d = null;
        if (cusCache == null) {
            cusCache = cus;
        }
        if (Const.builtinTypes.contains(toFind)) {
            return null;
        }
        if (fullPath != null) {
            if (declarationCache.get(fullPath) != null) {
                return declarationCache.get(fullPath);
            }
            toFind = extractTypeFromCollection(toFind);
            for (CompilationUnit cu : cus) {
                if(!cu.getStorage().isPresent() || !(fullPath.equals(cu.getStorage().get().getPath().toString()))){
                   continue;
                }
                for (TypeDeclaration td : cu.getTypes()) {
                    if (td.getNameAsString().equals(toFind)) {
                        d = new Declaration(cu, td);
                        populateDeclaration(cus, fullPath, level, d);
                    }
                    if (d == null) {
                        for (ClassOrInterfaceDeclaration cid : extractMemberTypes(td)) {
                            if (cid.getNameAsString().equals(toFind)) {
                                d = new Declaration(cu, cid);
                                populateDeclaration(cus, fullPath, level, d);
                            }
                        }
                    }
                }
            }
        }
        if(d!=null) {
            filterCyclicDeclaration(d, new ArrayList<>());
            declarationCache.put(fullPath, d);
            String packageName = getPackageName(d.getRawCU());
            packageCache.computeIfAbsent(packageName, k -> new ArrayList<>());
            packageCache.get(packageName).add(d);
        }
        return d;
    }

    /**
     * get package name of compilation unit
     * @param cu compilation unit to process
     * @return package name
     */
    public static String getPackageName(CompilationUnit cu){
        if(cu.getPackageDeclaration().isPresent()){
            return cu.getPackageDeclaration().get().getNameAsString();
        }
        return null;
    }

    /**
     * exclude cyclic dependency in a Declaration
     * @param d declaration to check
     * @param visited visited Declaration
     */
    private static void filterCyclicDeclaration(Declaration d, List<Declaration> visited) {
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
