package io.github.hzjdev.hqlsniffer.example.domain.fig1;


import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="manager_fig1")
public class ManagerFig1 extends PersonPerClassFig1{

    public Double bonus;

    public ManagerFig1(){
        bonus=10086.0;
    }
}