package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static io.github.hzjdev.hqlsniffer.Main.*;

public class SmellDetection {

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

    public static Declaration generateDeclarationFromCU(CompilationUnit cu){
        for (TypeDeclaration td : cu.getTypes()) {
            Declaration d = new Declaration(cu, td);
//            for (Object m : td.getMembers()) {
//                if (m instanceof FieldDeclaration) {
//                    for(VariableDeclarator vd: ((FieldDeclaration) m).asFieldDeclaration().getVariables()) {
//                        if(vd!=null) {
//                            Parametre p = new Parametre(vd.getTypeAsString(), vd.getNameAsString());
//                            if (d.getFields() == null) {
//                                d.setFields(new ArrayList<>());
//                            }
//                            d.getFields().add(p);
//                        }
//                    }
//                }
//            }
            return d;
        }
        return null;
    }
    public static String cleanHql(String hql){
//        if(hql.startsWith("\"")){
//            hql = hql.replaceFirst("\"","");
//        }
        hql = hql.replaceAll("\\+","");
        hql = hql.replaceAll("\"","");
        Pattern pattern = Pattern.compile(" +");
        hql = pattern.matcher(hql).replaceAll(" ");
        return hql;
    }
    public static List<Smell> getEagerFetches(List<CompilationUnit> cus, ProjectSmellReport psr) {
        List<Smell> eagerFetches = new ArrayList<>();
        for (CompilationUnit cu: cus){
            List<NormalAnnotationExpr> annotations = cu.findAll(NormalAnnotationExpr.class);
            for (NormalAnnotationExpr annotation: annotations) {
                for(MemberValuePair mvp : annotation.getPairs()){
                    if(mvp.getValue().toString().contains("EAGER")){
                        Optional<Node> parentField = mvp.getParentNode();
                        while(parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)){
                            parentField = parentField.get().getParentNode();
                        }
                        if(parentField.isPresent()) {
                            FieldDeclaration pf = (FieldDeclaration) parentField.get();
                            Declaration d;
                            final Smell smell = new Smell();
                            for (VariableDeclarator vd : pf.getVariables()) {
                                Type t = vd.getType();
                                if (t != null) {
                                    String type = extractTypeFromExpression(t.toString());
                                    d = findTypeDeclaration(type, cus, 1);
                                    if (d != null) {
                                        List<Declaration> relatedComponent = new ArrayList<>();
                                        relatedComponent.add(d);
                                        smell.setComponent(parentField.toString())
                                                .setName("Eager")
                                                .setRelatedComponent(relatedComponent);
                                        break;
                                    }
                                }
                            }
                            if (smell.getName() != null) {
                                mvp.getRange().ifPresent(s -> smell.setPosition(s.toString()));

                                cu.getStorage().ifPresent(s -> smell.setFile(s.getPath().toString()));

                                Declaration parentDeclaration = generateDeclarationFromCU(cu);
                                if(parentDeclaration != null) {
                                    smell.setClassName(parentDeclaration.getName());

                                    List<Smell> smells = psr.getSmells().get(parentDeclaration);
                                    if (smells == null) {
                                        smells = new ArrayList<>();
                                    }
                                    smells.add(smell);
                                    eagerFetches.add(smell);
                                    psr.getSmells().put(generateDeclarationFromCU(cu), smells);
                                }
                            }
                        }
                    }
                }
            }
        }
        return eagerFetches;
    }

    public static List<Smell> getOneByOne(List<CompilationUnit> cus, ProjectSmellReport psr) {
        List<Smell> lazyFetches = new ArrayList<>();
        for (CompilationUnit cu: cus){
            List<NormalAnnotationExpr> annotations = cu.findAll(NormalAnnotationExpr.class);
            for (NormalAnnotationExpr annotation: annotations) {
                for(MemberValuePair mvp : annotation.getPairs()){
                    if(annotation.getNameAsString().contains("ToMany") && mvp.getValue().toString().contains("LAZY")) {
                        Optional<Node> parentField = mvp.getParentNode();
                        while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
                            parentField = parentField.get().getParentNode();
                        }
                        if (parentField.isPresent()) {
                            FieldDeclaration pf = (FieldDeclaration) parentField.get();
                            boolean batchSizeExists = false;
                            for (AnnotationExpr fieldAnnotations : pf.getAnnotations()) {
                                if (fieldAnnotations.getNameAsString().equals("BatchSize")) {
                                    batchSizeExists = true;
                                }
                            }
                            if (batchSizeExists) continue;
                            Declaration d;
                            final Smell smell = new Smell();
                            for (VariableDeclarator vd : pf.getVariables()) {
                                Type t = vd.getType();
                                if (t != null) {
                                    String type = extractTypeFromExpression(t.toString());
                                    d = findTypeDeclaration(type, cus, 1);
                                    if (d != null) {
                                        List<Declaration> relatedComponent = new ArrayList<>();
                                        relatedComponent.add(d);
                                        smell.setComponent(parentField.toString())
                                                .setName("One-By-One Candidate")
                                                .setRelatedComponent(relatedComponent);
                                        mvp.getRange().ifPresent(s -> smell.setPosition(s.toString()));

                                        cu.getStorage().ifPresent(s -> smell.setFile(s.getPath().toString()));

                                        Declaration parentDeclaration = generateDeclarationFromCU(cu);
                                        if(parentDeclaration!=null) {
                                            smell.setClassName(parentDeclaration.getName());
                                            lazyFetches.add(smell);
                                            List<Smell> smells = psr.getSmells().get(parentDeclaration);
                                            if (smells == null) {
                                                smells = new ArrayList<>();
                                            }
                                            smells.add(smell);
                                            psr.getSmells().put(parentDeclaration, smells);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return lazyFetches;
    }

    public static List<Smell> getOneToManyNPlusOne(List<CompilationUnit> cus, ProjectSmellReport psr) {
        List<Smell> result = new ArrayList<>();
        for (CompilationUnit cu: cus){
            for(TypeDeclaration cuType: cu.getTypes()) {
                String typeName = cuType.getNameAsString();
                List<NormalAnnotationExpr> annotations = cuType.findAll(NormalAnnotationExpr.class);
                for (NormalAnnotationExpr annotation : annotations) {
                    for (MemberValuePair mvp : annotation.getPairs()) {
                        if (annotation.getNameAsString().contains("OneToMany")) {
                            Optional<Node> parentField = mvp.getParentNode();
                            while (parentField.isPresent() && !(parentField.get() instanceof FieldDeclaration)) {
                                parentField = parentField.get().getParentNode();
                            }
                            if (parentField.isPresent()) {
                                FieldDeclaration pf = (FieldDeclaration) parentField.get();
                                Declaration d;
                                final Smell smell = new Smell();
                                for (VariableDeclarator vd : pf.getVariables()) {
                                    Type t = vd.getType();
                                    if (t != null) {
                                        String type = extractTypeFromExpression(t.toString());
                                        d = findTypeDeclaration(type, cus, 1);
                                        if (d != null) {
                                            for(Parametre targetField :d.getFields()){

                                                    if(!targetField.getType().equals(typeName)){
                                                        continue;
                                                    }
                                                        if (!targetField.annotationIncludes("ManyToOne")) {
                                                            List<Declaration> relatedComponent = new ArrayList<>();
                                                            relatedComponent.add(d);
                                                            smell.setComponent(parentField.toString())
                                                                    .setName("MissingOneToMany")
                                                                    .setRelatedComponent(relatedComponent);
                                                            smell.setPosition(targetField.getPosition());

                                                            cu.getStorage().ifPresent(s -> smell.setFile(s.getPath().toString()));

                                                            Declaration parentDeclaration = generateDeclarationFromCU(cu);
                                                            if (parentDeclaration != null) {
                                                                smell.setClassName(parentDeclaration.getName());
                                                                result.add(smell);
                                                                List<Smell> smells = psr.getSmells().get(parentDeclaration);
                                                                if (smells == null) {
                                                                    smells = new ArrayList<>();
                                                                }
                                                                smells.add(smell);
                                                                psr.getSmells().put(parentDeclaration, smells);
                                                        }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }


    public static List<Smell> getPaged(List<Result> hqls, List<CompilationUnit> cus, ProjectSmellReport psr) {
        List<Smell> pagedSmell = new ArrayList<>();
        for (Result hql: hqls) {
            for (Declaration calledIn: hql.getCalledIn()){
                String body = calledIn.getBody();
                if(body.toLowerCase().contains("limit") || body.toLowerCase().contains("page")){
                    if(!hql.getMethodBody().contains(".setFirstResult(") || !hql.getMethodBody().contains(".setMaxResults(")){
                        Smell smell = new Smell();
                        String path = calledIn.getFullPath();
                        smell.setPosition(calledIn.getPosition());
                        smell.setFile(path).setComponent(calledIn.getName());
                        Declaration parentDeclaration = null;
                        for(CompilationUnit cu: cus){
                            String cuPath = null;
                            if(cu.getStorage().isPresent()){
                                cuPath = cu.getStorage().get().getPath().toString();
                                if (path.equals(cuPath)){
                                    parentDeclaration = generateDeclarationFromCU(cu);
                                    break;
                                }
                            }
                        }
                        if(parentDeclaration!=null) {
                            List<Smell> smells = psr.getSmells().get(parentDeclaration);
                            if (smells == null) {
                                smells = new ArrayList<>();
                            }
                            smells.add(smell.setClassName(parentDeclaration.getName()));
                            pagedSmell.add(smell);
                            psr.getSmells().put(parentDeclaration, smells);
                        }

                    }
                }
            }
        }
        return pagedSmell;
    }

    public static List<Smell> getJoinFetch(List<Result> hqls, List<CompilationUnit> cus, List<Smell> eagerFetches, ProjectSmellReport psr) {
        List<Smell> joinFetchSmell = new ArrayList<>();
        for (Result hql_: hqls) {
            StringBuilder hql = new StringBuilder();
            for (String hql__: hql_.getHql()){
                hql.append(hql__).append(' ');
            }
            String hql_s = hql.toString().toLowerCase();
            if(!hql_s.contains("join fetch")){
                String from_entity = null;
                hql_s = cleanHql(hql_s);
                if(!hql_s.startsWith("delete") && !hql_s.startsWith("update") && !hql_s.startsWith("insert")) {
                    try {
                        from_entity = hql_s.split("from ")[1].split(" ")[0];
                    } catch (Exception e) {
                        from_entity = hql_.getReturnType();
                    }
                    Declaration parentDeclaration = null;
                    if (from_entity != null) {
                        for (Smell eagerFetch : eagerFetches) {
                            if (eagerFetch.getClassName().toLowerCase().equals(from_entity)) {
                                for (CompilationUnit cu : cus) {
                                    String cuPath = null;
                                    if (cu.getStorage().isPresent()) {
                                        cuPath = cu.getStorage().get().getPath().toString();
                                        if (hql_.getFullPath().equals(cuPath)) {
                                            parentDeclaration = generateDeclarationFromCU(cu);
                                            break;
                                        }
                                    }
                                }
                                List<Smell> smells = psr.getSmells().get(parentDeclaration);
                                if (smells == null) {
                                    smells = new ArrayList<>();
                                }
                                Smell smell = new Smell();
                                String path = hql_.getFullPath();
                                smell.setPosition(hql_.getCreateQueryPosition());
                                smell.setFile(path)
                                        .setComponent(hql_.getMethodName() + "+" + eagerFetch.getClassName() + ":" + hql_s)
                                        .setClassName(parentDeclaration.getName());
                                smell.setName("Join Fetch");
                                smells.add(smell);
                                joinFetchSmell.add(smell);
                                psr.getSmells().put(parentDeclaration, smells);
                            }
                        }
                    }
                }
            }
        }
        return joinFetchSmell;
    }


    public static void output(String path, Object results) throws FileNotFoundException {
        Gson gs = new GsonBuilder()
                .setPrettyPrinting()
//                .disableHtmlEscaping()
                .create();
        try (PrintWriter out = new PrintWriter(path)) {
            out.println(gs.toJson(results));
        }
    }

    public static void exec(String project, String root_path, String output_path) throws FileNotFoundException {
        ProjectSmellReport psr = new ProjectSmellReport();
        List<CompilationUnit> cus = new ArrayList<>();
        parseFrom(root_path+"\\"+project, cus);
        List<CompilationUnit> entities = getEntities(cus);
        getOneToManyNPlusOne(entities, psr);
        List<Smell> eagerFetches = getEagerFetches(entities, psr);
        List<Result> hqls = getHqlNodes(cus);
        populateContext(hqls, cus);
        getPaged(hqls, cus, psr);
        getJoinFetch(hqls, cus, eagerFetches, psr);
        getOneByOne(entities, psr);
        output(output_path+"\\"+project+"_smells.json", psr);
    }
}
