package io.github.hzjdev.hqlsniffer;

import antlr.collections.AST;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;

public class Utils {


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
