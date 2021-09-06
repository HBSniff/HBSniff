package io.github.hzjdev.hqlsniffer.utils;

import java.util.Arrays;
import java.util.List;

public class Const {
    public final static String IDENT_ANNOT_EXPR = "@Id";
    public final static String TABLE_ANNOT_EXPR = "@Table";
    public final static String GETTER_ANNOT_EXPR = "@Getter";
    public final static String SETTER_ANNOT_EXPR = "@Setter";
    public final static String VOID_TYPE_EXPR = "void";
    public final static String Object_TYPE_EXPR = "Object";
    public final static String EQUALS_METHOD_NAME = "equals";
    public final static String HASHCODE_METHOD_NAME = "hashCode";
    public final static String CREATE_QUERY_METHOD_NAME = "createQuery";
    public final static String APPEND_METHOD_CALL_NAME = "append";
    public final static String CONCAT_METHOD_CALL_NAME = "concat";

    public final static String CLASS_SUFFIX = ".class";

    public final static String SETTER_METHOD_PREFIX = "set";
    public final static String GETTER_METHOD_PREFIX_NORMAL = "get";
    public final static String GETTER_METHOD_PREFIX_BOOL = "is";

    public final static String HQL_PLUS_OP = "PLUS";
    public final static String HQL_ASSIGN_OP = "ASSIGN";

    public final static String SINGLE_TABLE_ANNOT_EXPR = "SINGLE_TABLE";
    public final static String TABLE_PER_CLASS_ANNOT_EXPR = "TABLE_PER_CLASS";
    public final static String EAGER_ANNOT_EXPR = "EAGER";
    public final static String LAZY_ANNOT_EXPR = "LAZY";

    public final static String MANY_TO_ONE_ANNOT_EXPR = "ManyToOne";
    public final static String ONE_TO_MANY_ANNOT_EXPR = "OneToMany";
    public final static String TO_MANY_ANNOT_EXPR = "ToMany";
    public final static String BATCH_SIZE_ANNOT_EXPR = "BatchSize";

    public final static String SERIALIZABLE_ANNOT_EXPR = "Serializable";

    public final static Integer LEVEL_TO_POPULATE_DECLARATION = 4;
    public final static List<String> builtinTypes = Arrays.asList("Boolean,Byte,Short,Character,Integer,Long,Float,Double,boolean,byte,short,char,int,long,float,double,BigInteger,BigDecimal,String,List,ArrayList,Set,Collection,Iterator,LinkedList,LinkedHashSet,LinkedHashMap,Hashtable,HashSet,HashMap,Vector".split(","));
}
