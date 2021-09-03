package io.github.hzjdev.hqlsniffer.example.domain.fig1;


import javax.persistence.*;

@Entity
@DiscriminatorValue("employee_fig1")
public class EmployeeFig1 extends PersonSingleTableFig1{
    Double salary;
    String department;

    public EmployeeFig1(){
        salary = 666.66;
        department = "Royal Vehicle Maintenance";
        name = "W. Sun";
    }
}