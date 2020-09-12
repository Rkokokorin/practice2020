package com.tuneit.itc.zuulproxy.repositories;

import com.tuneit.itc.zuulproxy.model.ApiUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiUserRepository extends JpaRepository<ApiUser, Long> {

    @Query("SELECT u from ApiUser u where u.username=:username")
    public ApiUser findByUsername(@Param("username") String username);
}
