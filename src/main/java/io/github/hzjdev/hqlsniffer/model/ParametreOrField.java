/*
 * This file is part of hqlSniffer.
 *
 *     hqlSniffer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     hqlSniffer is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with hqlSniffer.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.hzjdev.hqlsniffer.model;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParametreOrField implements Serializable {

    @Expose
    String type;

    @Expose
    String name;

    @Expose
    List<String> modifiers;

    @Expose
    String position;

    @Expose
    List<String> annotations;

    @Expose(serialize = false)
    Declaration typeDeclaration;

    public ParametreOrField(String type, String name) {
        this.type = type;
        this.name = name;
        this.annotations = new ArrayList<>();
        this.modifiers = new ArrayList<>();
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
    }

    public String getPosition() {
        return position;
    }

    public ParametreOrField setPosition(String position) {
        this.position = position;
        return this;
    }

    public Declaration getTypeDeclaration() {
        return typeDeclaration;
    }

    public ParametreOrField setTypeDeclaration(Declaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
        return this;
    }

    public ParametreOrField populateModifiers(List<Modifier> ms) {
        for (Modifier m : ms) {
            modifiers.add(m.getKeyword().asString());
        }
        return this;
    }

    public String getType() {
        return type;
    }

    public ParametreOrField setType(String type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public ParametreOrField setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public ParametreOrField populateAnnotations(List<AnnotationExpr> annotationExprs) {
        for (AnnotationExpr expr : annotationExprs) {
            annotations.add(expr.toString());
        }
        return this;

    }

    /**
     * check if annotations includes string
     * @param s string to check
     * @return true if annotations includes string
     */
    public boolean annotationIncludes(String s) {
        for (String annotation : annotations) {
            if (annotation.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * check if the parametre/field is static
     * @return true if static
     */
    public boolean isStatic() {
        for (String modifier : modifiers) {
            if (Modifier.Keyword.STATIC.asString().equals(modifier)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParametreOrField parametre = (ParametreOrField) o;
        return Objects.equals(type, parametre.type) && Objects.equals(name, parametre.name) && Objects.equals(position, parametre.position) && Objects.equals(typeDeclaration, parametre.typeDeclaration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, position, typeDeclaration);
    }
}
