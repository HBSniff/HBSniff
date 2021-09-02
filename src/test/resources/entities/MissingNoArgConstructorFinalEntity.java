package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Id;

public final class MissingNoArgConstructorFinalEntity {

    @Id
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}