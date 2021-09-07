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

import com.github.javaparser.ast.CompilationUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.github.hzjdev.hqlsniffer.parser.EntityParser.findCalledIn;

public class HqlAndContext implements Serializable {
    String id;
    List<String> hql;
    String returnType;
    String methodName;
    String typeName;
    String methodBody;
    String cleanedHql;
    List<Declaration> hqlFromType;
    Map<String, String> aliasMap;
    String fullPath;
    String createQueryPosition;
    String methodPosition;
    String returnExpression;
    List<ParametreOrField> params;
    Declaration returnTypeDeclaration;
    List<Declaration> calledIn;


    public HqlAndContext() {
        setId(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public HqlAndContext populateCalledIn(List<CompilationUnit> cus) {
        this.calledIn = findCalledIn(getMethodName(), getTypeName(), cus);
        return this;
    }

    public String getTypeName() {
        return typeName;
    }

    public HqlAndContext setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    public void setAliasMap(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public String getCleanedHql() {
        return cleanedHql;
    }

    public void setCleanedHql(String cleanedHql) {
        this.cleanedHql = cleanedHql;
    }

    public List<Declaration> getHqlFromType() {
        return hqlFromType;
    }

    public void setHqlFromType(List<Declaration> hqlFromType) {
        this.hqlFromType = hqlFromType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethodBody() {
        return methodBody;
    }

    public HqlAndContext setMethodBody(String methodBody) {
        this.methodBody = methodBody;
        return this;
    }

    public String getReturnExpression() {
        return returnExpression;
    }

    public HqlAndContext setReturnExpression(String returnExpression) {
        this.returnExpression = returnExpression;
        return this;
    }

    public String getMethodPosition() {
        return methodPosition;
    }

    public HqlAndContext setMethodPosition(String methodPosition) {
        this.methodPosition = methodPosition;
        return this;
    }

    public Declaration getReturnTypeDeclaration() {
        return returnTypeDeclaration;
    }

    public HqlAndContext setReturnTypeDeclaration(Declaration returnTypeDeclaration) {
        this.returnTypeDeclaration = returnTypeDeclaration;
        return this;
    }

    public String getFullPath() {
        return fullPath;
    }

    public HqlAndContext setFullPath(String fullPath) {
        this.fullPath = fullPath;
        return this;
    }

    public List<Declaration> getCalledIn() {
        return calledIn;
    }

    public HqlAndContext setCalledIn(List<Declaration> calledIn) {
        this.calledIn = calledIn;
        return this;
    }

    public String getCreateQueryPosition() {
        return createQueryPosition;
    }

    public HqlAndContext setCreateQueryPosition(String createQueryPosition) {
        this.createQueryPosition = createQueryPosition;
        return this;
    }

    public List<String> getHql() {
        return hql;
    }

    public HqlAndContext setHql(List<String> hql) {
        this.hql = hql;
        return this;
    }

    public HqlAndContext setHql(String hql) {
        if (this.hql == null) {
            this.hql = new ArrayList<>();
        }
        this.hql.add(hql);
        return this;
    }


    public String getReturnType() {
        return returnType;
    }

    public HqlAndContext setReturnType(String returnType) {
        this.returnType = returnType;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public HqlAndContext setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public List<ParametreOrField> getParams() {
        return params;
    }

    public HqlAndContext setParams(List<ParametreOrField> params) {
        this.params = params;
        return this;
    }

    @Override
    public String toString() {
        return "Result{" +
                "hql=" + hql +
                ", returnType='" + returnType + '\'' +
                ", methodName='" + methodName + '\'' +
                ", fullPath='" + fullPath + '\'' +
                ", position='" + createQueryPosition + '\'' +
                ", params=" + params +
                ", returnTypeDeclaration=" + returnTypeDeclaration +
                ", calledIn=" + calledIn +
                '}';
    }
}
