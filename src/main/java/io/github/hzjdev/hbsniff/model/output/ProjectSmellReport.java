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

package io.github.hzjdev.hbsniff.model.output;

import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.annotations.Expose;
import io.github.hzjdev.hbsniff.model.Declaration;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.hzjdev.hbsniff.parser.EntityParser.genDeclarationsFromCompilationUnits;

public class ProjectSmellReport implements Serializable {

    @Expose
    Map<Declaration, List<Smell>> smells;

    public ProjectSmellReport() {
        smells = new HashMap<>();
    }

    /**
     * generate ProjectSmellJSONReport from CompilationUnits
     * @param entities compilation units
     * @return ProjectSmellJSONReport
     */
    public static ProjectSmellReport fromCompilationUnits(List<CompilationUnit> entities) {
        ProjectSmellReport toReturn = new ProjectSmellReport();
        for (Declaration d : genDeclarationsFromCompilationUnits(entities)) {
            toReturn.getSmells().put(d, new ArrayList<>());
        }
        return toReturn;
    }

    /**
     * the getter of the smells Map containing the tuple of a Declaration and its corresponding smells
     * @return the smells Map
     */
    public Map<Declaration, List<Smell>> getSmells() {
        return smells;
    }

    /**
     * the setter of the smells Map containing the tuple of a Declaration and its corresponding smells
     * @param smells the map to set
     * @return the smells Map
     */
    public ProjectSmellReport setSmells(Map<Declaration, List<Smell>> smells) {
        this.smells = smells;
        return this;
    }

    /**
     * delete the classes with no smells and sort the classes alphabetically for better presentation
     * @return the class itself
     */
    public ProjectSmellReport cleanup() {

        // delete classes with no smells
        Map<Declaration, List<Smell>> cleanedSmells = new LinkedHashMap<>();
        //sort alphabetically the classes
        List<Declaration> sortedKeyset = smells.keySet().stream().sorted().collect(Collectors.toList());
        for(Declaration d : sortedKeyset){
            if(smells.get(d).size()>0){
                cleanedSmells.put(d,smells.get(d));
            }
        }

        smells = cleanedSmells;
        return this;
    }

    /**
     * delete the classes with no smells and sort the classes alphabetically for better presentation
     * @return the class itself
     */
    public boolean isEmpty() {
        for(Declaration d:  smells.keySet()){
            List<Smell> val = smells.get(d);
            if(val!=null && !val.isEmpty()){
                return false;
            }
        }
        return true;
    }

    /**
     * generate head strings from all existing smells
     * @param psr project smell report
     * @return list of head strings of xls
     */
    public static List<String> generateHead(ProjectSmellReport psr){
        List<String> head = new ArrayList<>();
        head.add("Class Name / Smell");
        List<Smell> source = new ArrayList<>();
        for(List<Smell> tmp: psr.getSmells().values()){
            source.addAll(tmp);
        }
        head.addAll(source.stream().map(Smell::getName).distinct().sorted().collect(Collectors.toList()));
        head.add("Path");
        return head;
    }

    /**
     * generating xls
     * @param path generated path
     * @param psr project smell report
     * @throws IOException
     */
    public static void generateXlsReport(String path, ProjectSmellReport psr) throws IOException {
        if(path == null || psr == null || psr.getSmells() == null || psr.getSmells().size()<1) return;
        //init
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("default");
        sheet.setDefaultColumnWidth(17);


        //gen header
        Row row = sheet.createRow(0);
        List<String> head = generateHead(psr);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setWrapText(true);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        int column_num = 0;
        for(String s: head){
            Cell c =  row.createCell(column_num);
            c.setCellValue(s);
            c.setCellStyle(headerStyle);
            column_num++;
        }
        //cell styles
        CellStyle smelly = workbook.createCellStyle();
        CellStyle clean = workbook.createCellStyle();
        smelly.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_ORANGE.getIndex());
        clean.setFillForegroundColor(HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
        smelly.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        clean.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        //generate rows
        Integer row_num = 1;
        for(Map.Entry<Declaration,List<Smell>> e: psr.getSmells().entrySet()){
            Set<Integer> visited_column = new HashSet<>();
            row = sheet.createRow(row_num);
            row.createCell(0).setCellValue(e.getKey().getName());
            row.getCell(0).setCellStyle(headerStyle);
            for(Smell s: e.getValue()){
                column_num = head.indexOf(s.getName());
                if(visited_column.contains(column_num)){
                    //multiple same smells with different instances(e.g., fields in a class)
                    Cell c = row.getCell(column_num);
                    c.setCellValue(c.getStringCellValue()+" | "+s.getComment());
                    c.setCellStyle(smelly);
                }else {
                    //smelly cell
                    Cell c = row.createCell(column_num);
                    c.setCellValue(s.getComment() == null ? " ": s.getComment());
                    c.setCellStyle(smelly);
                    visited_column.add(column_num);
                }
            }
            for(int i = 1; i < head.size(); i++){
                if(!visited_column.contains(i)){
                    // non-smelly cells
                    row.createCell(i).setCellStyle(clean);
                    row.getCell(i).setCellValue(" ");
                }
            }
            // class path
            Cell pathCell = row.createCell(head.size() - 1);
            pathCell.setCellValue(e.getKey().getFullPath());
            pathCell.getCellStyle().setWrapText(false);
            pathCell.getCellStyle().setAlignment(HorizontalAlignment.LEFT);
            row_num++;
        }

        // write out
        try  (OutputStream fileOut = new FileOutputStream(path)) {
            workbook.write(fileOut);
        }

    }
}
