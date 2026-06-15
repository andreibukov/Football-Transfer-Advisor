package com.footballadvisor.agent.launcher;

import com.footballadvisor.agent.ClubAnalysisAgent;
import com.footballadvisor.agent.OntologyManagerAgent;
import com.footballadvisor.agent.TransferRecommendationAgent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JadeAgentLauncher {

    @Value("${jade.gui.enabled:false}")
    private boolean jadeGuiEnabled;

    @Bean
    public AgentContainer jadeContainer() {
        try {
            Runtime runtime = Runtime.instance();

            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, Boolean.toString(jadeGuiEnabled));

            AgentContainer container = runtime.createMainContainer(profile);

            AgentController clubAgent = container.createNewAgent(
                    "ClubAnalysisAgent",
                    ClubAnalysisAgent.class.getName(),
                    null
            );

            AgentController ontologyAgent = container.createNewAgent(
                    "OntologyManagerAgent",
                    OntologyManagerAgent.class.getName(),
                    null
            );

            AgentController recommendationAgent = container.createNewAgent(
                    "TransferRecommendationAgent",
                    TransferRecommendationAgent.class.getName(),
                    null
            );

            clubAgent.start();
            ontologyAgent.start();
            recommendationAgent.start();

            System.out.println("JADE container started successfully. GUI enabled: " + jadeGuiEnabled);

            return container;

        } catch (Exception ex) {
            throw new RuntimeException("Failed to start JADE container", ex);
        }
    }
}
