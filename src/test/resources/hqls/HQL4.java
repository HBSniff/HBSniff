package dummy;

import br.com.setsoft.modelo.Feedback;

import javax.persistence.Query;
import java.util.List;

public class HQL4{

    public List<Feedback> hql() {

        final Query query = this.getEntityManager().createQuery("SELECT x FROM Feedback x WHERE 1=1");

        return query.getResultList();
    }

}
