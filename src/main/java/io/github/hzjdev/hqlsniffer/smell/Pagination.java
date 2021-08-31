package io.github.hzjdev.hqlsniffer.smell;

import com.github.javaparser.ast.CompilationUnit;
import io.github.hzjdev.hqlsniffer.Declaration;
import io.github.hzjdev.hqlsniffer.ProjectSmellReport;
import io.github.hzjdev.hqlsniffer.Result;
import io.github.hzjdev.hqlsniffer.Smell;

import java.util.ArrayList;
import java.util.List;

public class Pagination extends SmellDetector{



    public List<Smell> getPaged(List<Result> hqls, List<CompilationUnit> cus) {
        List<Smell> pagedSmell = new ArrayList<>();
        for (Result hql: hqls) {
            for (Declaration calledIn: hql.getCalledIn()){
                String body = calledIn.getBody();
                if(body.toLowerCase().contains("limit") || body.toLowerCase().contains("page")){
                    if(!hql.getMethodBody().contains(".setFirstResult(") || !hql.getMethodBody().contains(".setMaxResults(")){
                        Smell smell = new Smell();
                        String path = calledIn.getFullPath();
                        smell.setPosition(calledIn.getPosition());
                        smell.setFile(path).setComponent(calledIn.getName());
                        Declaration parentDeclaration = null;
                        for(CompilationUnit cu: cus){
                            String cuPath = null;
                            if(cu.getStorage().isPresent()){
                                cuPath = cu.getStorage().get().getPath().toString();
                                if (path.equals(cuPath)){
                                    parentDeclaration = new Declaration(cu);
                                    break;
                                }
                            }
                        }
                        if(parentDeclaration!=null) {
                            List<Smell> smells = psr.getSmells().get(parentDeclaration);
                            if (smells == null) {
                                smells = new ArrayList<>();
                            }
                            smells.add(smell.setClassName(parentDeclaration.getName()));
                            pagedSmell.add(smell);
                            psr.getSmells().put(parentDeclaration, smells);
                        }

                    }
                }
            }
        }
        return pagedSmell;
    }

    public List<Smell> exec() {
        return getPaged(hqls, cus);
    }

}
