package io.github.hzjdev.hbsniff.example.mapping.domain.fig3;


import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("student_fig3")
public class StudentFig3 extends PersonSingleTableFig3 {

    public String university;

    public Boolean isEnrolled;

    public String majorSubject;

    public StudentFig3() {
        university = "ECUST";
        isEnrolled = true;
        majorSubject = "Chinese Alchemy";
    }
}