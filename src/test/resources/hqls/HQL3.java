package dummy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.setsoft.crud.CrudGenerico;
import br.com.setsoft.modelo.Feedback;
import br.com.setsoft.utilidade.Fabrica;
import br.com.setsoft.utilidade.StringUtil;

public class HQL3{

    public List<Feedback> hql() {

        final String JPQL = "SELECT x FROM Feedback x WHERE 1=1";

        JPQL+=" AND UPPER(x.descricao) Like :descricaoFiltro";
        JPQL+=" AND x.tipoFeedback = :tipoFeedbackFiltro";

        final Query query = this.getEntityManager().createQuery(JPQL);

        return query.getResultList();
    }

}
