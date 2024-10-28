package ${package}.service;

import ${package}.domain.${entityName};
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ${entityName}Service {

    ${entityName} save(${entityName} ${entityNameLowerCase});

    List<${entityName}> findAll();

    Optional<${entityName}> findOne(Long id);

    void delete(Long id);

}
