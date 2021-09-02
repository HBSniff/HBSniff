package io.github.hzjdev.hqlsniffer.utils;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {


    /**
     * String for represents the {@link java.util.Set} name interface.
     */
    public static final String SET_NAME = "Set";
    public static final String LIST_NAME = "List";
    public static String BOOLEAN_CLASS = "Boolean";
    public static String BOOLEAN_PRIMITIVE = "boolean";
    private static final String[] lists = {"Collection", "BeanContext", "BeanContextServices", "BlockingQueue", "Deque", "Queue", "TransferQueue", "AbstractCollection", "AbstractCollection", "AbstractList", "AbstractQueue", "SequentialList", "ArrayBlockingQueue", "ArrayDeque", "ArrayList", "AttributeList", "BeanContextServicesSupport", "BeanContextSupport", "ConcurrentLinkedDeque", "CopyOnWriteArrayList", "DelayQueue", "LinkedBlockingDeque", "LinkedBlockingQueue", "LinkedList", "LinkedTransferQueue", "PriorityBlockingQueue", "PriorityQueue", "RoleList", "RoleUnresolvedList", "Stack", "SynchronousQueue", "Vector"};
    private static final String[] sets = {"NavigableSet", "Set", "SortedSet", "AbstractSet", "ConcurrentHashMap.KeySetView", "ConcurrentSkipListSet", "CopyOnWriteArraySet", "EnumSet", "HashSet", "JobStateReasons", "LinkedHashSet", "TreeSet"};

    /**
     * Checks if the class name represents a java collection.
     *
     * @param name A complete class name with your package.
     * @return True if if the class is a java collection.
     */
    public static final boolean isCollection(final String name) {
        List<String> c = new ArrayList<>();
        c.addAll(Arrays.asList(lists));
        c.addAll(Arrays.asList(sets));
        return name != null && c.contains(name);
    }

    public static String cleanHql(String hql) {
        //        if(hql.startsWith("\"")){
        //            hql = hql.replaceFirst("\"","");
        //        }
        hql = hql.replaceAll("\\+", "");
        hql = hql.replaceAll("\"", "");
        Pattern pattern = Pattern.compile(" +");
        hql = pattern.matcher(hql).replaceAll(" ");
        return hql;
    }

    public static String extractParametrePosition(Node p) {
        Range a = p.getRange().orElse(null);
        return a == null ? "" : a.toString();
    }

    public static String extractTypeFromExpression(String expr) {
        if (expr != null) {
            if (expr.contains("<")) {
                String[] tmp = expr.split("<");
                expr = tmp[tmp.length - 1].split(">")[0];
            }
            expr = expr.replaceAll("\\[]", "");
        }
        return expr;
    }

    /**
     * Checks if the class name represents a collection of the Set type.
     *
     * @param name A complete class name with your package.
     * @return True if if the class name is a Set.
     */
    public static final boolean isSet(final String name) {
        return name != null && Arrays.asList(sets).contains(name);
    }


    /**
     * Checks if the class name represents a collection of the List type.
     *
     * @param name A complete class name with your package.
     * @return True if if the class name is a List.
     */
    public static final boolean isList(final String name) {
        return name != null && Arrays.asList(lists).contains(name);
    }


    static String trimSpace(String str) {
        StringBuilder result = new StringBuilder();

        // 去掉首尾的空格
        String trimStr = str.trim();

        int length = trimStr.length();

        for (int i = 0; i < length; i++) {
            char currentStr = trimStr.charAt(i);
            // 不是空格，加入
            if (currentStr != ' ') {
                result.append(currentStr);
            }

            // 是空格，判断下一个字符是否为空格，不为空格则加入，是空格则跳过
            if (currentStr == ' ' && trimStr.charAt(i + 1) != ' ') {
                result.append(' ');
            }
        }
        return result.toString();

    }
}
