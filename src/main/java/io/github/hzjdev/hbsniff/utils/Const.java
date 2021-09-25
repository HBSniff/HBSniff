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

package io.github.hzjdev.hbsniff.utils;

import java.util.Arrays;
import java.util.List;

public class Const {
    public final static String DEFAULT_INPUT_PATH = "D:\\tools\\hql\\projects\\BroadleafCommerce";
    public final static String DEFAULT_OUTPUT_PATH = "D:\\tools\\hql\\projects";

    public final static String IDENT_ANNOT_EXPR = "Id";
    public final static String TABLE_ANNOT_EXPR = "Table";
    public final static String GETTER_ANNOT_EXPR = "Getter"; //lombok annotation
    public final static String SETTER_ANNOT_EXPR = "Setter";
    public final static String ENTITY_ANNOT_EXPR = "Entity";
    public final static String TRANSIENT_ANNOT_EXPR = "@Transient";
    public final static String DATA_ANNOT_EXPR = "@Data";
    public final static String EQUALS_AND_HASH_CODE_ANNOT_EXPR = "EqualsAndHashCode";
    public final static String REFLECTION_HASHCODE_CALL = "reflectionHashCode";
    public final static String REFLECTION_EQUALS_CALL = "reflectionEquals";

    public final static String SINGLE_TABLE_ANNOT_EXPR = "SINGLE_TABLE";
    public final static String TABLE_PER_CLASS_ANNOT_EXPR = "TABLE_PER_CLASS";
    public final static String JOINED_ANNOT_EXPR = "JOINED";

    public final static String FETCH_ANNOT_EXPR = "fetch";

    public final static String EAGER_ANNOT_EXPR = "EAGER";
    public final static String LAZY_ANNOT_EXPR = "LAZY";
    public final static String JOIN_FETCH_EXPR = "join fetch";
    public final static String DELETE_EXPR = "delete";
    public final static String UPDATE_EXPR = "update";
    public final static String INSERT_EXPR = "insert";
    public final static String SELECT_EXPR = "select";
    public final static String AS_EXPR = "as";
    public final static String FROM_EXPR = "from";
    public final static String LIMIT_EXPR = "limit";
    public final static String PAGE_EXPR = "page";
    public final static String SET_FIRST_RESULT_EXPR = "setFirstResult";
    public final static String SET_MAX_RESULTS_EXPR = "setMaxResults";

    public final static String MANY_TO_ONE_ANNOT_EXPR = "ManyToOne";
    public final static String ONE_TO_MANY_ANNOT_EXPR = "OneToMany";
    public final static String TO_MANY_ANNOT_EXPR = "ToMany";
    public final static String TO_ONE_ANNOT_EXPR = "ToOne";

    public final static String BATCH_SIZE_ANNOT_EXPR = "BatchSize";

    public final static String SERIALIZABLE_EXPR = "Serializable";

    public final static String VOID_TYPE_EXPR = "void";
    public final static String Object_TYPE_EXPR = "Object";
    public final static String EQUALS_METHOD_NAME = "equals";
    public final static String HASHCODE_METHOD_NAME = "hashCode";
    public final static String CREATE_QUERY_METHOD_NAME = "createQuery";
    public final static String APPEND_METHOD_CALL_NAME = "append";
    public final static String CONCAT_METHOD_CALL_NAME = "concat";

    public final static String CLASS_SUFFIX = ".class";
    public final static String JAVA_SUFFIX = ".java";

    public final static String SETTER_METHOD_PREFIX = "set";
    public final static String GETTER_METHOD_PREFIX_NORMAL = "get";
    public final static String GETTER_METHOD_PREFIX_BOOL = "is";

    public final static String PRIMITIVE_INT = "int";
    public final static String PRIMITIVE_LONG = "long";
    public final static String INTEGER = "Integer";
    public final static String LONG = "Long";

    public final static String HQL_PLUS_OP = "PLUS";
    public final static String HQL_ASSIGN_OP = "ASSIGN";
    public final static String SERIAL_VERSION_UID = "serialVersionUID";

    public final static Integer LEVEL_TO_POPULATE_DECLARATION = 4;
    public final static List<String> builtinTypes = Arrays.asList("Boolean,Byte,Short,Character,Integer,Long,Float,Double,boolean,byte,short,char,int,long,float,double,BigInteger,BigDecimal,String,List,ArrayList,Set,Collection,Iterator,LinkedList,LinkedHashSet,LinkedHashMap,Hashtable,HashSet,HashMap,Vector".split(","));
}
