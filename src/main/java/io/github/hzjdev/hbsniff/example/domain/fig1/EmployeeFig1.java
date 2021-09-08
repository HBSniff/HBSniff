package io.github.hzjdev.hbsniff.example.domain.fig1;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("employee_fig1")
public class EmployeeFig1 extends PersonSingleTableFig1 {
    Double salary;
    String department;

    public EmployeeFig1() {
        salary = 666.66;
        department = "Royal Vehicle Maintenance";
        name = "W. Sun";
    }
}