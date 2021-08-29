package io.github.hzjdev.hqlsniffer;

import com.github.javaparser.ast.expr.AnnotationExpr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Parametre implements Serializable {
    String type;
    String name;
    String position;
    List<String> annotations;
    Declaration typeDeclaration;

    public String getPosition() {
        return position;
    }

    public Parametre setPosition(String position) {
        this.position = position;
        return this;
    }

    public Declaration getTypeDeclaration() {
        return typeDeclaration;
    }

    public Parametre setTypeDeclaration(Declaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
        return this;
    }

    public Parametre(String type, String name) {
        this.type = type;
        this.name = name;
        this.annotations = new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public Parametre setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public Parametre setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public Parametre populateAnnotations(List<AnnotationExpr> annotationExprs){
        for (AnnotationExpr expr: annotationExprs){
            annotations.add(expr.toString());
        }
        return this;

    }

    public boolean annotationIncludes(String s){
        for(String annotation: annotations){
            if (annotation.contains(s)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parametre parametre = (Parametre) o;
        return Objects.equals(type, parametre.type) && Objects.equals(name, parametre.name) && Objects.equals(position, parametre.position) && Objects.equals(typeDeclaration, parametre.typeDeclaration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, position, typeDeclaration);
    }
}
