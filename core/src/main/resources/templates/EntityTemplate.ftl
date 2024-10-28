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
<#-- Verificamos que entity y entity.relationships existan -->
<#if entity?? && entity.relationships??>
    <#list entity.relationships as rel>
        <#if rel.relationshipType??>
            <#if rel.relationshipType == "OneToOne">
                // lógica para OneToOne
            <#elseif rel.relationshipType == "OneToMany">
                // lógica para OneToMany
            <#elseif rel.relationshipType == "ManyToOne">
                // lógica para ManyToOne
            <#elseif rel.relationshipType == "ManyToMany">
                // lógica para ManyToMany
            </#if>
        </#if>
    </#list>
</#if>
    // Getters y Setters
}
