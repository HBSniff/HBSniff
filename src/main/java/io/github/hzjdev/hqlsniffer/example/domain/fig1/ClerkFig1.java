package io.github.hzjdev.hqlsniffer.example.domain.fig1;


import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "clerk_fig1")
public class ClerkFig1 extends PersonJoinFig1 {
    public String occupation;

    public ClerkFig1() {
        occupation = "Heaven Alchemist";
        name = "J. Taibai";
    }

}

