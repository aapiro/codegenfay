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
public class ${entityName} {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    <#-- Atributos de la entidad -->
    <#list attributes?keys as attribute>
    private ${attributes[attribute]} ${attribute};
    </#list>

    <#-- Relaciones -->
<#list relationships as rel>
    <#if rel.relationshipType??>
        <#if rel.relationshipType == "OneToOne">
            // Genera el código para OneToOne
        <#elseif rel.relationshipType == "OneToMany">
            // Genera el código para OneToMany
        <#elseif rel.relationshipType == "ManyToOne">
            // Genera el código para ManyToOne
        <#elseif rel.relationshipType == "ManyToMany">
            // Genera el código para ManyToMany
        </#if>
    </#if>
</#list>


    // Getters y Setters
}
