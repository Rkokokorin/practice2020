package com.tuneit.itc.commons.service;


import lombok.Data;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@ApplicationScoped
@ManagedBean(eager = true, name = "emf")
@Data
public class EMFactory {
    private EntityManager em;
    private EntityManagerFactory factory;

    @PostConstruct
    public void init() {
        factory = Persistence.createEntityManagerFactory("feedback");
        this.em = (EntityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(),
            new Class[] {EntityManager.class}, new InvocationHandler() {
                private final ThreadLocal<EntityManager> managerThreadLocal =
                    ThreadLocal.withInitial(factory::createEntityManager);

                @Override
                public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                    EntityManager em = managerThreadLocal.get();
                    if (method.getName().equals("close")) {
                        managerThreadLocal.remove();
                    }
                    return method.invoke(em, objects);
                }
            });
    }

    @PreDestroy
    public void destroy() {
        factory.close();
    }
}
