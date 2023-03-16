package com.logicmonitor.tracing.apmtracingloadgen.config;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

//FIXME -
// This is a dummy evolving way to monitor the configuration as we will increase the fields,
// the ultimate aim is to form the config.json so the json will be referred
// rather the SQL to read the config. Little of jackson related stuff to code

@RepositoryRestResource(path = "config", collectionResourceRel = "config")
public interface ConfigurationRestRepository extends CrudRepository<LoadGenToolConfiguration, Integer> {
    public List<LoadGenToolConfiguration> findByName(@Param("name") String name);

    public List<LoadGenToolConfiguration> findAll();

}