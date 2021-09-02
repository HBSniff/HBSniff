package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class NoArgConstructorEntity{

    @Id
    private String id;

    public NoArgConstructorEntity(){

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}