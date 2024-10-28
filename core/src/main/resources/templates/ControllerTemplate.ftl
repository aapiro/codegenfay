package ${package}.web.rest;

import ${package}.domain.${entityName};
import ${package}.service.${entityName}Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/${entityNameLowerCase}")
public class ${entityName}Controller {
private final ${entityName}Service service;

public ${entityName}Controller(${entityName}Service service) {
this.service = service;
}

@PostMapping
public ${entityName} create(@RequestBody ${entityName} ${entityNameLowerCase}) {
return service.save(${entityNameLowerCase});
}


@GetMapping
public List<${entityName}> getAll() {
return service.findAll();
}

@GetMapping("/{id}")
public Optional<${entityName}> get(@PathVariable Long id) {
return service.findOne(id);
}

@DeleteMapping("/{id}")
public void delete(@PathVariable Long id) {
service.delete(id);
}
}
