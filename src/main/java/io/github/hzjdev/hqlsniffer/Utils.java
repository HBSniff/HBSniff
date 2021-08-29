package io.github.hzjdev.hqlsniffer;

import java.util.HashSet;
import java.util.Set;

public class Utils {


        /**
         * String for represents the {@link java.util.Set} name interface.
         */
        public static final String SET_NAME = "java.util.Set";
    public static final String LIST_NAME = "java.util.List";

    private static Set<String> collections= new HashSet<>();
        private static Set<String> lists= new HashSet<>();
        private static Set<String> sets= new HashSet<>();
    public static String BOOLEAN_CLASS = "java.lang.Boolean";
    public static  String BOOLEAN_PRIMITIVE = "boolean";

        /**
         * Extract all informations of the collections classes.
         */
        public Utils() {
            this.sets = new HashSet<>();
            this.lists = new HashSet<>();

            // Hierarquia de Interfaces do Collection
            this.collections.add("java.util.Collection");
            this.collections.add("java.util.BeanContext");
            this.collections.add("java.util.BeanContextServices");
            this.collections.add("java.util.BlockingDeque");
            this.collections.add("java.util.BlockingQueue");
            this.collections.add("java.util.Deque");
            this.lists.add("java.util.List");
            this.sets.add("java.util.NavigableSet");
            this.collections.add("java.util.Queue");
            this.collections.add(SET_NAME);
            this.sets.add(SET_NAME);
            this.sets.add("java.util.SortedSet");
            this.collections.add("java.util.TransferQueue");

            this.collections.add("java.util.AbstractCollection");
            this.collections.add("java.util.AbstractList");
            this.collections.add("java.util.AbstractQueue");
            this.collections.add("java.util.SequentialList");
            this.sets.add("java.util.AbstractSet");
            this.collections.add("java.util.ArrayBlockingQueue");
            this.collections.add("java.util.ArrayDeque");
            this.collections.add("java.util.ArrayList");
            this.collections.add("java.util.AttributeList");
            this.collections.add("java.util.BeanContextServicesSupport");
            this.collections.add("java.util.BeanContextSupport");
            this.sets.add("java.util.ConcurrentHashMap.KeySetView");
            this.collections.add("java.util.ConcurrentLinkedDeque");
            this.collections.add("java.util.ConcurrentLinkedQueue");
            this.sets.add("java.util.ConcurrentSkipListSet");
            this.collections.add("java.util.CopyOnWriteArrayList");
            this.sets.add("java.util.CopyOnWriteArraySet");
            this.collections.add("java.util.DelayQueue");
            this.sets.add("java.util.EnumSet");
            this.sets.add("java.util.HashSet");
            this.sets.add("java.util.JobStateReasons");
            this.collections.add("java.util.LinkedBlockingDeque");
            this.collections.add("java.util.LinkedBlockingQueue");
            this.sets.add("java.util.LinkedHashSet");
            this.collections.add("java.util.LinkedList");
            this.collections.add("java.util.LinkedTransferQueue");
            this.collections.add("java.util.PriorityBlockingQueue");
            this.collections.add("java.util.PriorityQueue");
            this.collections.add("java.util.RoleList");
            this.collections.add("java.util.RoleUnresolvedList");
            this.collections.add("java.util.Stack");
            this.collections.add("java.util.SynchronousQueue");
            this.sets.add("java.util.TreeSet");
            this.collections.add("java.util.Vector");

            this.collections.addAll(sets);
            this.collections.addAll(lists);
        }

        /**
         * Checks if the classNode represents a java collection.
         * @param node A classNode.
         * @return True if if the classNode is a java collection.
         */
        public static final boolean isCollection(final Declaration node) {
            if (node != null && collections.contains(node.getName())) {
                return true;
            }
            return false;
        }

        /**
         * Checks if the class name represents a java collection.
         * @param name A complete class name with your package.
         * @return True if if the class is a java collection.
         */
        public static final boolean isCollection(final String name) {
            if (name != null && collections.contains(name)) {
                return true;
            }
            return false;
        }

        /**
         * Checks if the classNode represents a collection of the Set type.
         * @param node A classNode.
         * @return True if if the classNode is a Set.
         */
        public static final boolean isSet(final Declaration node) {
            if (node != null && sets.contains(node.getName())) {
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
            if (name != null && sets.contains(name)) {
                return true;
            }
            return false;
        }

        /**
         * Checks if the classNode represents a collection of the List type.
         * @param node A classNode.
         * @return True if if the classNode is a List.
         */
        public static final boolean isList(final Declaration node) {
            if (node != null && lists.contains(node.getName())) {
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
            if (name != null && lists.contains(name)) {
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
