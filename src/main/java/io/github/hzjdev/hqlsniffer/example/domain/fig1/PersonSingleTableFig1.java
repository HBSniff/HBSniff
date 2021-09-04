package io.github.hzjdev.hqlsniffer.example.domain.fig1;


import io.github.hzjdev.hqlsniffer.example.domain.Person;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("person")
@Table(name = "person_fig1")
public class PersonSingleTableFig1 implements Person {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public Integer id;

    @Column(name = "name")
    public String name;

    public PersonSingleTableFig1() {
        super();
    }
}