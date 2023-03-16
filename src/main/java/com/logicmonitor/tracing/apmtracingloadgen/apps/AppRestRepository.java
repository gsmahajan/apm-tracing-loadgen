package com.logicmonitor.tracing.apmtracingloadgen.apps;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "apps", collectionResourceRel = "apps")
public interface AppRestRepository extends CrudRepository<MockApplication, Integer> {
    public List<MockApplication> findByName(@Param("name") String name);

    public List<MockApplication> findAll();

}