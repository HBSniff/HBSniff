package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Declaration implements Serializable {
    String name;
    String position;
    String fullPath;
    String body;
    String declarationType;
    String returnTypeName;

    List<Parametre> fields;
    List<Parametre> parametres;
    List<Declaration> members;
    List<Declaration> constructors;

    CompilationUnit rawCU;
    BodyDeclaration rawBD;

    public List<String> getAccessedFieldNames(){
        List<FieldAccessExpr> fas = rawBD.findAll(FieldAccessExpr.class);
        List<String> result = new ArrayList<String>();
        for(FieldAccessExpr fa: fas){
            result.add(fa.getNameAsString());
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

    public Declaration(CompilationUnit cu, TypeDeclaration td){
        setName(td.getNameAsString());
        cu.getStorage().ifPresent(s -> this.setFullPath(s.getPath().toString()));
        td.getRange().ifPresent(s -> this.setPosition(s.toString()));
        this.setBody(td.toString());
        declarationType="class";
        constructors = new ArrayList<>();
        members = new ArrayList<>();
        fields = new ArrayList<>();
        for(Object bd: td.getMembers()){
            if (bd instanceof MethodDeclaration){
                members.add(new Declaration(cu, (MethodDeclaration)bd));
            }
        }
        for(Object cd: td.getConstructors()){
            constructors.add(new Declaration(cu, (ConstructorDeclaration)cd));
        }

        rawCU = cu;
        rawBD = td;
    }

    public Declaration(CompilationUnit cu, ConstructorDeclaration cd){
        setName(cd.getNameAsString());
        cu.getStorage().ifPresent(s -> this.setFullPath(s.getPath().toString()));
        cd.getRange().ifPresent(s -> this.setPosition(s.toString()));
        this.setBody(cd.toString());
        declarationType="constructor";
        parametres = new ArrayList<>();
        for(Parameter p :cd.getParameters()){
            parametres.add(new Parametre(p.getTypeAsString(),p.getNameAsString()));
        }

        rawCU = cu;
        rawBD = cd;
    }


    public Declaration findDeclaration(String name){
        for(Declaration d: members){
            if (d.getName().equals(name)){
                return d;
            }
        }
        return null;
    }

    public Declaration(CompilationUnit cu, MethodDeclaration md){
        setName(md.getNameAsString());
        cu.getStorage().ifPresent(s -> this.setFullPath(s.getPath().toString()));
        md.getRange().ifPresent(s -> this.setPosition(s.toString()));
        this.setBody(md.toString());
        returnTypeName = md.getTypeAsString();
        parametres = new ArrayList<>();
        for(Parameter p :md.getParameters()){
            parametres.add(new Parametre(p.getTypeAsString(),p.getNameAsString()));
        }
        declarationType="method";

        rawCU = cu;
        rawBD = md;
    }

    public List<String> getSuperClass(){
        List<String> result = new ArrayList<>();
        List<ClassOrInterfaceDeclaration> classes = getRawCU().findAll(ClassOrInterfaceDeclaration.class);
        for(ClassOrInterfaceDeclaration clazz: classes){
            for (ClassOrInterfaceType ct: clazz.getExtendedTypes()) {
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
