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

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import io.github.hzjdev.hbsniff.model.output.Metric;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellCSVLine;
import io.github.hzjdev.hbsniff.model.output.ProjectSmellReport;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class Utils {


    /**
     * String for represents the {@link java.util.Set} name interface.
     */
    public static final String SET_NAME = "Set";
    public static final String LIST_NAME = "List";
    private static final String[] lists = {"Collection", "BeanContext", "BeanContextServices", "BlockingQueue", "Deque", "Queue", "TransferQueue", "AbstractCollection", "AbstractCollection", "AbstractList", "AbstractQueue", "SequentialList", "ArrayBlockingQueue", "ArrayDeque", "ArrayList", "AttributeList", "BeanContextServicesSupport", "BeanContextSupport", "ConcurrentLinkedDeque", "CopyOnWriteArrayList", "DelayQueue", "LinkedBlockingDeque", "LinkedBlockingQueue", "LinkedList", "LinkedTransferQueue", "PriorityBlockingQueue", "PriorityQueue", "RoleList", "RoleUnresolvedList", "Stack", "SynchronousQueue", "Vector", "List"};
    private static final String[] sets = {"NavigableSet", "Set", "SortedSet", "AbstractSet", "ConcurrentHashMap.KeySetView", "ConcurrentSkipListSet", "CopyOnWriteArraySet", "EnumSet", "HashSet", "JobStateReasons", "LinkedHashSet", "TreeSet"};
    public static String BOOLEAN_CLASS = "Boolean";
    public static String BOOLEAN_PRIMITIVE = "boolean";

    /**
     * export smell detection results
     * @param jsonPath path of the json file
     * @param csvPath path of the csv file
     * @param results results
     */
    public static void outputSmells(String jsonPath, String csvPath, String xlsPath, ProjectSmellReport results) {
        //wirte to csv
        results.cleanup();
        List<String[]> csvContent = ProjectSmellCSVLine.toCSV(ProjectSmellCSVLine.fromProjectSmellJSONReport(results));
        try (FileOutputStream fos = new FileOutputStream(csvPath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {
            writer.writeAll(csvContent);
        } catch (IOException e) {
            System.out.println("Output path unavailable: " + csvPath);
        }

        //write to json
        Gson gs = new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        try (PrintWriter out = new PrintWriter(jsonPath)) {
            out.println(gs.toJson(results));
        } catch (IOException e) {
            System.out.println("Output path unavailable: " + jsonPath);
        }

        //write to excel
        try {
            ProjectSmellReport.generateXlsReport(xlsPath, results);
        }catch (Exception e ){
            System.out.println("XLS Export Failed");
            e.printStackTrace();
        }
    }

    /**
     * export metric calculation results
     * @param csvPath path of the csv file
     * @param metrics results
     */
    public static void outputMetrics(String csvPath, List<Metric> metrics) {
        //wirte to csv
        List<String[]> csvContent = Metric.toCSV(metrics);
        try (FileOutputStream fos = new FileOutputStream(csvPath);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {
            writer.writeAll(csvContent);
        } catch (IOException e) {
            System.out.println("Output path unavailable: " + csvPath);
        }
    }

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

    /**
     * clean a hql expression
     * @param hql hql to clean
     * @return cleaned hql
     */
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

    /**
     * find position of a parametre node
     * @param p parametre node
     * @return its range
     */
    public static String extractParametrePosition(Node p) {
        Range a = p.getRange().orElse(null);
        return a == null ? "" : a.toString();
    }

    /**
     * extractTypeFromCollections
     * @param expr expression such as Collection<Person>
     * @return type, e.g., Person
     */
    public static String extractTypeFromCollection(String expr) {
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


    /**
     * extract text from LiteralExpr
     * @param le LiteralExpr input
     * @return result string
     */
    public static String extractLiteralExpr(LiteralExpr le) {
        if (le instanceof StringLiteralExpr) {
            return ((StringLiteralExpr) le).getValue();
        } else if (le instanceof CharLiteralExpr) {
            return ((CharLiteralExpr) le).getValue();
        } else if (le instanceof TextBlockLiteralExpr) {
            return ((TextBlockLiteralExpr) le).getValue();
        } else {
            return le.toString();
        }
    }


    private static void concatBinaryExpr(StringBuilder hql_concatenated, BinaryExpr expr) {
        String op = expr.asBinaryExpr().getOperator().toString();
        if (op.equals("PLUS")) {
            for (Node e : expr.getChildNodes()) {
                if (e instanceof LiteralExpr) {
                    hql_concatenated.append(extractLiteralExpr((LiteralExpr) e));
                } else if (e instanceof BinaryExpr) {
                    concatBinaryExpr(hql_concatenated, ((BinaryExpr) e).asBinaryExpr());
                } else if (e instanceof NameExpr) {
                    hql_concatenated.append(":").append(((NameExpr) e).asNameExpr().getNameAsString());
                } else if (e instanceof MethodCallExpr) {
                    hql_concatenated.append(extractMethodCallExpr((MethodCallExpr) e));
                } else if (e instanceof EnclosedExpr) {
                    Expression inner = ((EnclosedExpr) e).asEnclosedExpr().getInner();
                    if (inner.isConditionalExpr()) {
                        hql_concatenated.append(":").append(((EnclosedExpr) e).asEnclosedExpr().getInner().asConditionalExpr().getCondition().toString());
                    } else {
                        System.out.println("#concatBinaryExpr1" + e.toString());
                    }
                } else {
                    System.out.println("#concatBinaryExpr2" + e.toString());
                }
            }
        }
    }

    /**
     * concatenate hql from a BinaryExpr
     * @param expr BinaryExpr input
     * @return hql concatenated
     */
    public static String concatBinaryExpr(BinaryExpr expr) {
        StringBuilder hql_concatenated = new StringBuilder();
        concatBinaryExpr(hql_concatenated, expr);
        return hql_concatenated.toString();
    }

    /**
     * replace method call in hqls with its name as a variable
     * @param mce method call to replace
     * @return processed hql
     */
    public static String extractMethodCallExpr(MethodCallExpr mce) {
        Optional<Expression> e = mce.getScope();
        if (e.isPresent() && e.get().isMethodCallExpr()) {
            if (e.get().asMethodCallExpr().getScope().isPresent()) {
                Expression expr = e.get().asMethodCallExpr().getScope().get();
                if (expr.isNameExpr()) {
                    return ":" + e.get().asMethodCallExpr().getScope().get().asNameExpr().getNameAsString();
                } else if (expr.isMethodCallExpr()) {
                    return extractMethodCallExpr(expr.asMethodCallExpr());
                } else {
                    return "";
                }
            }
        }
        return "";
    }

}
