package ${package}.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
public class ${entityName} implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

<#list attributes?keys as attribute>
    private ${attributes[attribute]} ${attribute};
</#list>

// Getters y Setters
<#list attributes?keys as attribute>
    public ${attributes[attribute]} get${attribute?cap_first}() {
    return ${attribute};
    }

    public void set${attribute?cap_first}(${attributes[attribute]} ${attribute}) {
    this.${attribute} = ${attribute};
    }
</#list>
}