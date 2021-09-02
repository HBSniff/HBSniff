package tests.io.github.hzjdev.hqlsniffer.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToMany;

import java.util.Set;

@Entity
@Table(name="Dummy")
public class SetCollectionEntity{

    @Id
    private String id;

    @OneToMany
    public Set<NoArgConstructorEntity> lst;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}