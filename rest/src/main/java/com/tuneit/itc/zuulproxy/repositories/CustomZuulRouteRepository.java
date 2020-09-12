package com.tuneit.itc.zuulproxy.repositories;

import com.tuneit.itc.zuulproxy.model.CustomZuulRoute;
import com.tuneit.itc.zuulproxy.model.forms.RoleForm;
import com.tuneit.itc.zuulproxy.model.forms.RouteWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface CustomZuulRouteRepository extends JpaRepository<CustomZuulRoute, Long> {

    @Query("select r from CustomZuulRoute r where r.uuid = :uuid")
    public CustomZuulRoute findByUuid(@Param("uuid") String uuid);

    @Query("select r from CustomZuulRoute r where r.path = :path")
    public CustomZuulRoute findByPath(@Param("path") String path);

    @Modifying
    @Transactional
    @Query("delete from CustomZuulRoute r where r.uuid = :uuid")
    void deleteByUuid(@Param("uuid") String uuid);

    @Query("select distinct new com.tuneit.itc.zuulproxy.model.forms.RouteWrapper(route, false) " +
            "from CustomZuulRoute route " +
            "where route.id not in (SELECT rr.id FROM Role r join r.routes rr where r.id = :id)")
    List<RouteWrapper> findNotSelectedRouteWrappersByRoleId(@Param("id") Long id);
}
