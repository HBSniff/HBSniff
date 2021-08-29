package io.github.hzjdev.hqlsniffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Result implements Serializable {
    String id;
    List<String> hql;
    String returnType;
    String methodName;
    String methodBody;
    String cleanedHql;
    List<Declaration> hqlFromType;
    Map<String,String> aliasMap;
    String fullPath;
    String createQueryPosition;
    String methodPosition;
    String returnExpression;
    List<Parametre> params;
    Declaration returnTypeDeclaration;
    List<Declaration> calledIn;


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

    public Result setMethodBody(String methodBody) {
        this.methodBody = methodBody;
        return this;
    }

    public String getReturnExpression() {
        return returnExpression;
    }

    public Result setReturnExpression(String returnExpression) {
        this.returnExpression = returnExpression;
        return this;
    }

    public String getMethodPosition() {
        return methodPosition;
    }

    public Result setMethodPosition(String methodPosition) {
        this.methodPosition = methodPosition;
        return this;
    }

    public Declaration getReturnTypeDeclaration() {
        return returnTypeDeclaration;
    }

    public Result setReturnTypeDeclaration(Declaration returnTypeDeclaration) {
        this.returnTypeDeclaration = returnTypeDeclaration;
        return this;
    }


    public String getFullPath() {
        return fullPath;
    }

    public Result setFullPath(String fullPath) {
        this.fullPath = fullPath;
        return this;
    }

    public List<Declaration> getCalledIn() {
        return calledIn;
    }

    public Result setCalledIn(List<Declaration> calledIn) {
        this.calledIn = calledIn;
        return this;
    }

    public String getCreateQueryPosition() {
        return createQueryPosition;
    }

    public Result setCreateQueryPosition(String createQueryPosition) {
        this.createQueryPosition = createQueryPosition;
        return this;
    }

    public Result() {
        setId(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    public List<String> getHql() {
        return hql;
    }

    public Result setHql(List<String> hql) {
        this.hql = hql;
        return this;
    }

    public Result setHql(String hql) {
        if(this.hql == null){
            this.hql = new ArrayList<>();
        }
        this.hql.add(hql);
        return this;
    }



    public String getReturnType() {
        return returnType;
    }

    public Result setReturnType(String returnType) {
        this.returnType = returnType;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public Result setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public List<Parametre> getParams() {
        return params;
    }

    public Result setParams(List<Parametre> params) {
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
