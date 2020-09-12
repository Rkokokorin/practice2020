package com.tuneit.itc.zuulproxy.repositories;

import com.tuneit.itc.zuulproxy.model.Role;
import com.tuneit.itc.zuulproxy.model.forms.RoleWrapper;
import com.tuneit.itc.zuulproxy.model.forms.RouteWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r WHERE r.name = :name")
    Role findByName(@Param("name") String name);


    @Query("select distinct new com.tuneit.itc.zuulproxy.model.forms.RouteWrapper(route, true) from Role r join r.routes route where r.id = :id")
    List<RouteWrapper> findSelectedRouteWrappersByRoleId(@Param("id") Long id);


    @Modifying
    @Transactional
    @Query("delete from Role r where r.id = :id")
    void deleteByUuid(@Param("id") Long id);

    @Query("SELECT r FROM Role r JOIN r.routes route WHERE route.id = :id")
    List<Role> findAllBySelectedRouteId(@Param("id") Long id);

    @Query("select distinct new com.tuneit.itc.zuulproxy.model.forms.RoleWrapper(role, false) " +
            "from Role role " +
            "where role.id not in (SELECT ur.id FROM User u join u.roles ur where u.id = :id)")
    List<RoleWrapper> findNotSelectedRoleWrappersByUserId(@Param("id") Long id);
}
