package dummy;

import br.com.setsoft.modelo.Feedback;

import javax.persistence.Query;
import java.util.List;

public class HQL3{

    public List<Feedback> hql() {

        final String JPQL = "SELECT x FROM Feedback x WHERE 1=1";

        JPQL+=" AND UPPER(x.descricao) Like :descricaoFiltro";
        JPQL+=" AND x.tipoFeedback = :tipoFeedbackFiltro";

        final Query query = this.getEntityManager().createQuery(JPQL);

        return query.getResultList();
    }

}
