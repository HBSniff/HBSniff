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

package io.github.hzjdev.hbsniff.model;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.hzjdev.hbsniff.parser.EntityParser.findCalledIn;
import static io.github.hzjdev.hbsniff.utils.Const.*;

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
    MethodDeclaration definedIn;
    List<Declaration> calledIn;

    /**
     * constructor, initialize an UUID for every HqlAndContext Object if generated
     */
    public HqlAndContext() {
        setId(UUID.randomUUID().toString().replaceAll("-", ""));
    }

    /**
     * find the method in which the method containing the hql is called
     * @param cus the scope to search
     * @return the result object
     */
    public HqlAndContext populateCalledIn(List<CompilationUnit> cus) {
        if(getTypeName()!=null) {
            this.calledIn = findCalledIn(getDefinedIn(), getTypeName(), cus);
        }else{
            this.calledIn = findCalledIn(getMethodName(), cus);
        }
        return this;
    }

    /**
     * extract the names of the selected fields of dec
     * @param hql the hql to extract
     * @param dec the declaration concerned
     * @return the set of the names of the selected fields
     */
    public static Set<String> extractSelectedFields(String hql, Declaration dec){
        Set<String> result = new HashSet<>();
        if(hql == null || !hql.toLowerCase().contains(SELECT_EXPR+" ") || dec.getFields() == null)
            // if no select presents
            return result;

        String[] hql_from_split = hql.toLowerCase().split(FROM_EXPR+" "); // hql_from_split[0] is the strings before from
        hql = hql_from_split[0].replace(SELECT_EXPR+" ",""); // the content between select and from of an hql

        String[] hql_arr = hql.split(","); // hql_arr contains multiple selected columns
        for(String selected_field: hql_arr){
            // for every selected column
            // we should ignore its alias
            selected_field = selected_field.split(" as")[0];
            boolean in_from = false; // check if this selected column is in the entity presents in from
            if(hql_from_split.length>1){
                // from is presented
                String hql_from = hql_from_split[1].split("where")[0]; // the content between from and where of the hql
                if(hql_from.contains(AS_EXPR+" "+selected_field)){ // alias in from is presented
                    String[] arr = hql_from.split("as "+selected_field)[0].split(" ");
                    // split the expression with alias in from
                    if(arr.length>0){
                        String[] arr_dot = arr[arr.length-1].split("\\."); // extract the last part of the expression split by dots
                        result.add(arr_dot[arr_dot.length-1]); // this is the real name of an alias presented in from
                        in_from = true;
                    }
                }
            }
            if(!in_from) { // the real name of the selected column is in the select phrase
                result.add(selected_field); // so we add it to our set
            }
        }
        // we filter our results to keep only the fields present in dec
        Set<String> lowerCasedFields = dec.getFields().stream().map(i->i.getName().toLowerCase()).collect(Collectors.toSet());
        result = result.stream().filter(lowerCasedFields::contains).collect(Collectors.toSet());
        return result;
    }


    // accessors, hashcode, equals, compareto, tostring methods.

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


    public MethodDeclaration getDefinedIn() {
        return definedIn;
    }

    public HqlAndContext setDefinedIn(MethodDeclaration definedIn) {
        this.definedIn = definedIn;
        return this;
    }
}
