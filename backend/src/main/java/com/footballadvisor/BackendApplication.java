package com.footballadvisor;

import com.footballadvisor.ontology.OntologyLoader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(BackendApplication.class)
                .headless(false)
                .run(args);
    }

    @Bean
    CommandLineRunner loadOntologyOnStartup(OntologyLoader ontologyLoader) {
        return args -> ontologyLoader.loadOntology();
    }
}
