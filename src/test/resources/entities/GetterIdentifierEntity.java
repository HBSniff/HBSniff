package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Id;

public class GetterIdentifierEntity {

    private String id;

    public void setId(String id) {
        this.id = id;
    }

    @Id
    public String getId() {
        return id;
    }

}