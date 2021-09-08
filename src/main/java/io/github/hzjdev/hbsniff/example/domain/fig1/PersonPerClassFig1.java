package io.github.hzjdev.hbsniff.example.domain.fig1;


import io.github.hzjdev.hbsniff.example.domain.Person;

import javax.persistence.*;

@Entity
@Table(name = "person_fig1")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class PersonPerClassFig1 implements Person {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public Integer id;

    @Column(name = "name")
    public String name;

    public PersonPerClassFig1() {
        super();
        name = "person";
    }
}