package io.github.hzjdev.hbsniff.example.mapping;

import io.github.hzjdev.hbsniff.example.mapping.domain.fig1.ClerkFig1;
import io.github.hzjdev.hbsniff.example.mapping.domain.fig1.EmployeeForJoinFig1;
import io.github.hzjdev.hbsniff.example.mapping.domain.fig3.StudentFig3;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * This sub project is a demonstration of MappingMetrics
 */
public class Main {
    private static SessionFactory factory;

    public static void main(String[] args) {

        // init
        try {
            factory = new Configuration().configure().addAnnotatedClass(StudentFig3.class).buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
        Session session = factory.openSession();
        Transaction tx = null;

        // we may save some entities to test the functionality of our mapping
        try {
            tx = session.beginTransaction();
//            session.save(new StudentFig3());
//            session.save(new ManagerFig3());
//            session.save(new EmployeeFig3());
//            session.save(new StudentFig1());
//            session.save(new ManagerFig1());
//            session.save(new EmployeeFig1());
            session.save(new ClerkFig1());
            session.save(new EmployeeForJoinFig1());

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}