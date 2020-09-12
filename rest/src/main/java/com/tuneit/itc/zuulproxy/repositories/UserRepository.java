package com.tuneit.itc.zuulproxy.repositories;

import com.tuneit.itc.zuulproxy.model.User;
import com.tuneit.itc.zuulproxy.model.forms.RoleWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select distinct new com.tuneit.itc.zuulproxy.model.forms.RoleWrapper(role, true) from User u join u.roles role where u.id = :id")
    List<RoleWrapper> findSelectedRoleWrappersByUserId(@Param("id") Long id);

    @Query("SELECT distinct u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRoles();

    @Query("SELECT u FROM User u WHERE u.username =:username")
    User findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u JOIN u.roles role WHERE role.id = :id")
    List<User> findAllBySelectedRoleId(@Param("id") Long id);

    @Query("SELECT distinct u FROM User u JOIN FETCH u.roles WHERE u.username =:username")
    User findByUsernameWithRoles(String username);

    @Query("SELECT count(u)>0 " +
            "FROM User u " +
            "JOIN u.roles ur " +
            "JOIN ur.routes urr WHERE u.id=:id AND urr.outcomeUrl = :host AND ur.enabled=true")
    boolean checkPermissionForUserAndHost(@Param("id") Long id, @Param("host") String host);
}
