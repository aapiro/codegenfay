package ${package}.service;

import ${package}.domain.${entityName};
import java.util.List;
import java.util.Optional;

public interface ${entityName}Service {
${entityName} save(${entityName} ${entityNameLowerCase});
List<${entityName}> findAll();
Optional<${entityName}> findOne(Long id);
void delete(Long id);
}
