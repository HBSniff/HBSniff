package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Declaration implements Serializable {
    String name;
    String position;
    String fullPath;
    String body;
    List<Parametre> fields;

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
    }

    public Declaration(CompilationUnit cu, MethodDeclaration td){
        setName(td.getNameAsString());
        cu.getStorage().ifPresent(s -> this.setFullPath(s.getPath().toString()));
        td.getRange().ifPresent(s -> this.setPosition(s.toString()));
        this.setBody(td.toString());
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


}
