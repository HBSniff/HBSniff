package io.github.hzjdev.hqlsniffer.example.domain.fig3;


import io.github.hzjdev.hqlsniffer.example.domain.Person;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("person")
@Table(name = "person_fig3")
public class PersonSingleTableFig3 implements Person {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    public Integer id;

    @Column(name = "name")
    public String name;
}