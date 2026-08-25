package com.revature.SPR_GCE_ANNO_CONFIG.beans;

import com.revature.SPR_GCE_ANNO_CONFIG.MyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyRepository extends JpaRepository<MyEntity, Integer> {
    /*
    Repository beans offer us a bunch of free implementations, so we don't need to write anything of our own in here.
     */
}
