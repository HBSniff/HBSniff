package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class NoArgConstructorEntity {

    @Id
    private String id;

    private NonListOrSetCollectionEntity missingManyToOne;

    public NoArgConstructorEntity() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}