package io.github.hzjdev.hqlsniffer.model;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static io.github.hzjdev.hqlsniffer.utils.Utils.extractParametrePosition;

public class Declaration implements Serializable {
    @Expose
    String name;

    @Expose
    String position;

    @Expose
    String fullPath;

    @Expose
    String body;

    @Expose
    String declarationType;

    @Expose
    String returnTypeName;

    @Expose
    List<Parametre> fields;

    @Expose
    List<Parametre> parametres;

    @Expose
    List<Declaration> members;

    @Expose
    List<Declaration> constructors;

    @Expose(serialize = false)
    CompilationUnit rawCU;

    @Expose(serialize = false)
    BodyDeclaration rawBD;

    public Declaration(CompilationUnit cu) {
        this(cu, cu.getTypes().get(0));
    }

    public Declaration(CompilationUnit cu, TypeDeclaration td) {
        setName(td.getNameAsString());
        cu.getStorage().ifPresent(s -> this.setFullPath(s.getPath().toString()));
        td.getRange().ifPresent(s -> this.setPosition(s.toString()));
        this.setBody(td.toString());
        declarationType = "class";
        constructors = new ArrayList<>();
        members = new ArrayList<>();
        fields = new ArrayList<>();
        for (Object bd : td.getMembers()) {
            if (bd instanceof MethodDeclaration) {
                members.add(new Declaration(cu, (MethodDeclaration) bd));
            }
            if (bd instanceof FieldDeclaration) {
                for (VariableDeclarator vd : ((FieldDeclaration) bd).asFieldDeclaration().getVariables()) {
                    if (vd != null) {
                        Parametre p = new Parametre(vd.getTypeAsString(), vd.getNameAsString())
                                .setPosition(extractParametrePosition(vd))
                                .populateAnnotations(((FieldDeclaration) bd).getAnnotations());
                        getFields().add(p);
                    }
                }
            }
        }
        for (Object cd : td.getConstructors()) {
            constructors.add(new Declaration(cu, (ConstructorDeclaration) cd));
        }

        rawCU = cu;
        rawBD = td;
    }


    public Declaration(CompilationUnit cu, ConstructorDeclaration cd) {
        setName(cd.getNameAsString());
        cu.getStorage().ifPresent(s -> this.setFullPath(s.getPath().toString()));
        cd.getRange().ifPresent(s -> this.setPosition(s.toString()));
        this.setBody(cd.toString());
        declarationType = "constructor";
        parametres = new ArrayList<>();
        for (Parameter p : cd.getParameters()) {
            parametres.add(new Parametre(p.getTypeAsString(), p.getNameAsString()));
        }

        rawCU = cu;
        rawBD = cd;
    }

    public Declaration(CompilationUnit cu, MethodDeclaration md) {
        setName(md.getNameAsString());
        cu.getStorage().ifPresent(s -> this.setFullPath(s.getPath().toString()));
        md.getRange().ifPresent(s -> this.setPosition(s.toString()));
        this.setBody(md.toString());
        returnTypeName = md.getTypeAsString();
        parametres = new ArrayList<>();
        for (Parameter p : md.getParameters()) {
            parametres.add(new Parametre(p.getTypeAsString(), p.getNameAsString()));
        }
        declarationType = "method";

        rawCU = cu;
        rawBD = md;
    }

    public static Declaration fromPath(String path) {
        File f = new File(path);
        try {
            CompilationUnit cu = StaticJavaParser.parse(f);
            return new Declaration(cu, cu.getType(0));
        } catch (IOException e) {
            return null;
        }
    }

    public List<String> getAnnotations() {
        List<String> results = new ArrayList<>();
        BodyDeclaration toProcess = null;
        if (rawBD instanceof TypeDeclaration) {
            toProcess = getClassDeclr();
        } else if (rawBD instanceof MethodDeclaration) {
            toProcess = rawBD.asMethodDeclaration();
        }
        if (toProcess != null) {
            for (Object annotation : toProcess.getAnnotations()) {
                results.add(annotation.toString());
            }
        }
        return results;
    }

    public ClassOrInterfaceDeclaration getClassDeclr() {
        return this.getRawCU().findFirst(ClassOrInterfaceDeclaration.class).orElse(null);
    }

    public Set<String> getAccessedFieldNames(List<Declaration> parents) {
        Set<String> result = new HashSet<>(getAccessedFieldNamesInner(parents));
        List<MethodCallExpr> mcs = rawBD.findAll(MethodCallExpr.class);
        for (MethodCallExpr mc : mcs) {
            boolean skip = false;
            for (Node n : mc.getChildNodes()) {
                if (n instanceof MethodCallExpr) {
                    skip = true;
                    break;
                }
            }
            if (skip) continue;
            for (Declaration parent : parents) {
                Declaration md = parent.findDeclaration(mc.getNameAsString());
                if (md != null) {
                    result.addAll(md.getAccessedFieldNamesInner(parents));
                }
            }
        }
        return result;
    }

    private List<String> getAccessedFieldNamesInner(List<Declaration> parents) {
        List<NameExpr> nes = rawBD.findAll(NameExpr.class);
        List<String> result = new ArrayList<>();
        Set<Parametre> fields = new HashSet<>();
        for (Declaration d : parents) {
            fields.addAll(d.getFields());
        }
        for (NameExpr ne : nes) {
            for (Parametre field : fields) {
                if (field.getName().equals(ne.getNameAsString())) {
                    result.add(ne.getNameAsString());
                }
            }
        }

        return result;
    }

    public List<Parametre> getFields() {
        return fields;
    }

    public Declaration setFields(List<Parametre> fields) {
        this.fields = fields;
        return this;
    }

    public Declaration findDeclaration(String name) {
        if (members == null) return null;
        for (Declaration d : members) {
            if (d.getName().equals(name)) {
                return d;
            }
        }
        return null;
    }

    public List<String> getSuperClass() {
        List<String> result = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classes = getRawCU().findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration clazz : classes) {
            for (ClassOrInterfaceType ct : clazz.getExtendedTypes()) {
                result.add(ct.getNameAsString());
            }
        }
        return result;
    }

    public List<String> getImplementedInterface() {
        List<String> result = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classes = getRawCU().findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration clazz : classes) {
            for (ClassOrInterfaceType ct : clazz.getImplementedTypes()) {
                result.add(ct.getNameAsString());
            }
        }
        return result;
    }

    public CompilationUnit getRawCU() {
        return rawCU;
    }

    public void setRawCU(CompilationUnit rawCU) {
        this.rawCU = rawCU;
    }

    public BodyDeclaration getRawBD() {
        return rawBD;
    }

    public void setRawBD(BodyDeclaration rawBD) {
        this.rawBD = rawBD;
    }

    public List<Parametre> getParametres() {
        return parametres;
    }

    public void setParametres(List<Parametre> parametres) {
        this.parametres = parametres;
    }

    public String getName() {
        return name;
    }

    public Declaration setName(String name) {
        this.name = name;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public Declaration setPosition(String position) {
        this.position = position;
        return this;
    }

    public String getFullPath() {
        return fullPath;
    }

    public Declaration setFullPath(String fullPath) {
        this.fullPath = fullPath;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Declaration setBody(String body) {
        this.body = body;
        return this;
    }

    public String getDeclarationType() {
        return declarationType;
    }

    public void setDeclarationType(String declarationType) {
        this.declarationType = declarationType;
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }

    public void setReturnTypeName(String returnTypeName) {
        this.returnTypeName = returnTypeName;
    }

    public List<Declaration> getMembers() {
        return members;
    }

    public void setMembers(List<Declaration> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return fullPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Declaration that = (Declaration) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(position, that.position) &&
                Objects.equals(fullPath, that.fullPath) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, position, fullPath, body);
    }

    public List<Declaration> getConstructors() {
        return constructors;
    }

    public void setConstructors(List<Declaration> constructors) {
        this.constructors = constructors;
    }

}
