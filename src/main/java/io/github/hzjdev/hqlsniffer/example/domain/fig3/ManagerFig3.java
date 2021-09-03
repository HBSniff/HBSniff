package io.github.hzjdev.hqlsniffer.example.domain.fig3;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DiscriminatorValue("manager_fig3")
public class ManagerFig3 extends EmployeeFig3{

    public Double bonus;

    public ManagerFig3(){
        bonus=10086.0;
    }
}