package io.github.hzjdev.hqlsniffer;

import com.google.common.collect.Lists;

import java.util.*;

public class Utils {


        /**
         * String for represents the {@link java.util.Set} name interface.
         */
    public static final String SET_NAME = "Set";
    public static final String LIST_NAME = "List";

    private static String[] lists= {"Collection","BeanContext","BeanContextServices","BlockingQueue","Deque","Queue","TransferQueue","AbstractCollection","AbstractCollection","AbstractList","AbstractQueue","SequentialList","ArrayBlockingQueue","ArrayDeque","ArrayList","AttributeList","BeanContextServicesSupport","BeanContextSupport","ConcurrentLinkedDeque","CopyOnWriteArrayList","DelayQueue","LinkedBlockingDeque","LinkedBlockingQueue","LinkedList","LinkedTransferQueue","PriorityBlockingQueue","PriorityQueue","RoleList","RoleUnresolvedList","Stack","SynchronousQueue","Vector"};
    private static String[] sets= {"NavigableSet","Set","SortedSet","AbstractSet","ConcurrentHashMap.KeySetView","ConcurrentSkipListSet","CopyOnWriteArraySet","EnumSet","HashSet","JobStateReasons","LinkedHashSet","TreeSet"};
    public static String BOOLEAN_CLASS = "Boolean";
    public static  String BOOLEAN_PRIMITIVE = "boolean";


        /**
         * Checks if the class name represents a java collection.
         * @param name A complete class name with your package.
         * @return True if if the class is a java collection.
         */
        public static final boolean isCollection(final String name) {
            List<String> c = new ArrayList<>();
            c.addAll(Arrays.asList(lists));
            c.addAll(Arrays.asList(sets));
            if (name != null && c.contains(name)) {
                return true;
            }
            return false;
        }

        /**
         * Checks if the class name represents a collection of the Set type.
         * @param  name A complete class name with your package.
         * @return True if if the class name is a Set.
         */
        public static final boolean isSet(final String name) {
            if (name != null && Arrays.asList(sets).contains(name)) {
                return true;
            }
            return false;
        }


        /**
         * Checks if the class name represents a collection of the List type.
         * @param  name A complete class name with your package.
         * @return True if if the class name is a List.
         */
        public static final boolean isList(final String name) {
            if (name != null && Arrays.asList(lists).contains(name)) {
                return true;
            }
            return false;
        }


    static String trimSpace(String str){
        StringBuilder result = new StringBuilder();

        // 去掉首尾的空格
        String trimStr = str.trim();

        int length = trimStr.length();

        for (int i=0;i<length;i++){
            char currentStr = trimStr.charAt(i);
            // 不是空格，加入
            if (currentStr!=' '){
                result.append(currentStr);
            }

            // 是空格，判断下一个字符是否为空格，不为空格则加入，是空格则跳过
            if (currentStr==' '&&trimStr.charAt(i+1)!=' '){
                result.append(' ');
            }
        }
        return result.toString();

    }
}
