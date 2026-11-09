package com.orlo.api.arquitectura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

/** Reglas de capas: el controlador no se salta al servicio, nadie llama hacia arriba. */
@AnalyzeClasses(packages = "com.orlo.api", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    @ArchTest
    static final ArchRule capas = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Repository")
            .ignoreDependency(com.orlo.api.config.DataLoader.class, Object.class)
            .ignoreDependency(com.orlo.api.security.AdminUserDetailsService.class, Object.class);

    @ArchTest
    static final ArchRule controladoresSinEntidades = noClasses().that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().areAnnotatedWith(Entity.class);

    @ArchTest
    static final ArchRule controladoresSinRepositorios = noClasses().that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().areAssignableTo(JpaRepository.class);

    @ArchTest
    static final ArchRule implementacionesSonServicios = classes().that().haveSimpleNameEndingWith("ServiceImpl")
            .should().beAnnotatedWith(Service.class)
            .andShould().resideInAPackage("..service.impl..");

    @ArchTest
    static final ArchRule repositoriosSonInterfaces = classes().that().resideInAPackage("..repository..")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule sinSystemOut = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
}
