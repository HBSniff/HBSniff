package io.github.hzjdev.hqlsniffer.example;

import io.github.hzjdev.hqlsniffer.example.domain.fig1.ClerkFig1;
import io.github.hzjdev.hqlsniffer.example.domain.fig3.StudentFig3;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class Main {
    private static SessionFactory factory;

    public static void main(String[] args) {
        try {
            factory = new Configuration().configure().addAnnotatedClass(StudentFig3.class).buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
        Session session = factory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
//            session.save(new StudentFig3());
//            session.save(new ManagerFig3());
//            session.save(new EmployeeFig3());
//            session.save(new StudentFig1());
//            session.save(new ManagerFig1());
//            session.save(new EmployeeFig1());
            session.save(new ClerkFig1());

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}