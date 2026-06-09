package com.footballadvisor.config;

import com.footballadvisor.entity.ClubEntity;
import com.footballadvisor.entity.OntologyConceptEntity;
import com.footballadvisor.entity.OntologyRelationEntity;
import com.footballadvisor.entity.PlayerEntity;
import com.footballadvisor.repository.ClubRepository;
import com.footballadvisor.repository.OntologyConceptRepository;
import com.footballadvisor.repository.OntologyRelationRepository;
import com.footballadvisor.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SeedDataInitializer implements CommandLineRunner {

    private static final String SYSTEM_SOURCE = "SYSTEM_KNOWLEDGE";

    private final PlayerRepository playerRepository;
    private final ClubRepository clubRepository;
    private final OntologyConceptRepository ontologyConceptRepository;
    private final OntologyRelationRepository ontologyRelationRepository;

    @Override
    public void run(String... args) {
        seedClubs();
        seedPlayers();
        seedOntologyKnowledge();
    }

    private void seedClubs() {
        saveClub("Manchester City", "Premier League", 180000000, "Possession");
        saveClub("Arsenal", "Premier League", 150000000, "HighPressing");
        saveClub("Liverpool", "Premier League", 130000000, "CounterAttacking");
        saveClub("Aston Villa", "Premier League", 85000000, "VerticalTransitions");
        saveClub("Barcelona", "La Liga", 90000000, "Possession");
        saveClub("Real Madrid", "La Liga", 170000000, "CounterAttacking");
        saveClub("Real Sociedad", "La Liga", 45000000, "BalancedBuildUp");
        saveClub("Athletic Club", "La Liga", 55000000, "VerticalTransitions");
        saveClub("Bayern Munich", "Bundesliga", 160000000, "HighPressing");
        saveClub("Bayer Leverkusen", "Bundesliga", 95000000, "Possession");
        saveClub("RB Leipzig", "Bundesliga", 80000000, "HighPressing");
        saveClub("Inter", "Serie A", 85000000, "BalancedBuildUp");
        saveClub("Napoli", "Serie A", 70000000, "CounterAttacking");
        saveClub("AC Milan", "Serie A", 80000000, "VerticalTransitions");
        saveClub("Paris Saint-Germain", "Ligue 1", 150000000, "Possession");
    }

    private void seedPlayers() {
        savePlayer("Rodri", 29, "Manchester City", "Premier League", "DefensiveMidfielder", 110000000,
                68, 70, 91, 88, 90, 74, 82, 90, 86, 89, 92, 88);
        savePlayer("Martin Zubimendi", 26, "Real Sociedad", "La Liga", "DefensiveMidfielder", 60000000,
                70, 72, 86, 84, 85, 62, 78, 85, 78, 84, 86, 84);
        savePlayer("Declan Rice", 27, "Arsenal", "Premier League", "DefensiveMidfielder", 90000000,
                76, 75, 84, 82, 83, 70, 80, 91, 86, 88, 87, 90);
        savePlayer("Pedri", 23, "Barcelona", "La Liga", "CentralMidfielder", 70000000,
                76, 82, 91, 90, 92, 70, 88, 85, 62, 69, 84, 88);
        savePlayer("Vitinha", 26, "Paris Saint-Germain", "Ligue 1", "CentralMidfielder", 80000000,
                75, 81, 89, 88, 91, 72, 87, 86, 64, 72, 83, 86);
        savePlayer("Federico Valverde", 27, "Real Madrid", "La Liga", "CentralMidfielder", 100000000,
                86, 85, 85, 82, 84, 79, 83, 94, 82, 80, 84, 93);
        savePlayer("Bukayo Saka", 24, "Arsenal", "Premier League", "RightWinger", 120000000,
                88, 90, 84, 83, 89, 84, 91, 86, 70, 55, 85, 84);
        savePlayer("Mohamed Salah", 34, "Liverpool", "Premier League", "RightWinger", 65000000,
                86, 88, 82, 84, 88, 91, 89, 82, 74, 48, 91, 82);
        savePlayer("Ollie Watkins", 30, "Aston Villa", "Premier League", "Striker", 55000000,
                84, 83, 76, 77, 80, 86, 81, 88, 78, 52, 88, 86);
        savePlayer("Florian Wirtz", 23, "Bayer Leverkusen", "Bundesliga", "AttackingMidfielder", 130000000,
                82, 86, 88, 91, 92, 84, 90, 82, 65, 55, 86, 80);
        savePlayer("Jamal Musiala", 23, "Bayern Munich", "Bundesliga", "AttackingMidfielder", 125000000,
                85, 90, 86, 89, 94, 83, 93, 80, 62, 50, 84, 78);
        savePlayer("Victor Osimhen", 27, "Napoli", "Serie A", "Striker", 85000000,
                91, 89, 68, 70, 80, 90, 82, 84, 88, 40, 91, 82);
        savePlayer("Benjamin Sesko", 23, "RB Leipzig", "Bundesliga", "Striker", 65000000,
                87, 85, 70, 72, 79, 85, 78, 82, 86, 38, 84, 80);
        savePlayer("Lautaro Martinez", 28, "Inter", "Serie A", "Striker", 90000000,
                82, 84, 76, 78, 84, 89, 83, 87, 84, 58, 90, 88);
        savePlayer("Nico Williams", 23, "Athletic Club", "La Liga", "LeftWinger", 70000000,
                91, 92, 80, 81, 87, 80, 90, 82, 68, 45, 82, 79);
        savePlayer("Rafael Leao", 27, "AC Milan", "Serie A", "LeftWinger", 80000000,
                93, 91, 78, 79, 88, 84, 91, 78, 76, 42, 83, 74);
        savePlayer("William Saliba", 25, "Arsenal", "Premier League", "CentreBack", 85000000,
                78, 76, 78, 75, 80, 45, 70, 86, 89, 90, 88, 82);
        savePlayer("Alessandro Bastoni", 27, "Inter", "Serie A", "CentreBack", 70000000,
                72, 73, 83, 80, 81, 48, 68, 84, 86, 88, 86, 80);
        savePlayer("Achraf Hakimi", 27, "Paris Saint-Germain", "Ligue 1", "RightBack", 65000000,
                93, 92, 82, 80, 84, 76, 86, 89, 76, 78, 83, 86);
        savePlayer("Theo Hernandez", 28, "AC Milan", "Serie A", "LeftBack", 60000000,
                92, 90, 80, 78, 83, 78, 85, 86, 81, 75, 84, 83);
    }

    private void seedOntologyKnowledge() {
        String[] leagues = {"Premier League", "La Liga", "Bundesliga", "Serie A", "Ligue 1"};
        for (String league : leagues) {
            saveConcept(league, "League", "Football domestic league.");
        }

        clubRepository.findAll().forEach(club -> {
            saveConcept(club.getName(), "Club", "Football club stored in the database.");
            saveRelation(club.getName(), "playsInLeague", club.getLeague());
            saveRelation(club.getName(), "prefersPlayingStyle", club.getPreferredStyle());
        });

        playerRepository.findAll().forEach(player -> {
            saveConcept(player.getName(), "Player", "Football player stored in the database.");
            saveRelation(player.getName(), "playsForClub", player.getCurrentClub());
            saveRelation(player.getName(), "hasPosition", player.getPosition());
        });

        saveConcept("HighPressing", "PlayingStyle", "Aggressive pressing style focused on work rate and stamina.");
        saveConcept("VerticalTransitions", "PlayingStyle", "Fast forward progression after ball recovery.");
        saveConcept("BalancedBuildUp", "PlayingStyle", "Controlled buildup with balanced technical and defensive needs.");
        saveConcept("LowBlockCounter", "PlayingStyle", "Compact defending followed by direct attacks.");

        saveRelation("HighPressing", "styleRequiresAttribute", "WorkRate");
        saveRelation("HighPressing", "styleRequiresAttribute", "Stamina");
        saveRelation("HighPressing", "styleRequiresAttribute", "Tackling");
        saveRelation("VerticalTransitions", "styleRequiresAttribute", "Speed");
        saveRelation("VerticalTransitions", "styleRequiresAttribute", "Acceleration");
        saveRelation("VerticalTransitions", "styleRequiresAttribute", "Passing");
        saveRelation("BalancedBuildUp", "styleRequiresAttribute", "Passing");
        saveRelation("BalancedBuildUp", "styleRequiresAttribute", "Vision");
        saveRelation("BalancedBuildUp", "styleRequiresAttribute", "Positioning");
        saveRelation("LowBlockCounter", "styleRequiresAttribute", "Tackling");
        saveRelation("LowBlockCounter", "styleRequiresAttribute", "Strength");
        saveRelation("LowBlockCounter", "styleRequiresAttribute", "Speed");

        saveConcept("DeepLyingPlaymaker", "PlayerRole", "Midfielder role focused on build-up passing and vision.");
        saveConcept("BallWinningMidfielder", "PlayerRole", "Midfielder role focused on defensive duels and ball recovery.");
        saveConcept("BoxToBoxMidfielder", "PlayerRole", "Midfielder role that contributes across both phases.");
        saveConcept("InsideForward", "PlayerRole", "Wide attacking role that cuts inside to create and finish.");
        saveConcept("TargetForward", "PlayerRole", "Striker role focused on strength, positioning, and finishing.");
        saveConcept("PressingForward", "PlayerRole", "Forward role focused on pressing intensity and work rate.");
        saveConcept("BallPlayingDefender", "PlayerRole", "Defender role focused on distribution and defensive positioning.");
        saveConcept("AttackingFullBack", "PlayerRole", "Full-back role focused on speed, stamina, and ball progression.");

        saveRelation("DefensiveMidfielder", "hasRole", "DeepLyingPlaymaker");
        saveRelation("DefensiveMidfielder", "hasRole", "BallWinningMidfielder");
        saveRelation("CentralMidfielder", "hasRole", "DeepLyingPlaymaker");
        saveRelation("CentralMidfielder", "hasRole", "BoxToBoxMidfielder");
        saveRelation("RightWinger", "hasRole", "InsideForward");
        saveRelation("LeftWinger", "hasRole", "InsideForward");
        saveRelation("Striker", "hasRole", "TargetForward");
        saveRelation("Striker", "hasRole", "PressingForward");
        saveRelation("CentreBack", "hasRole", "BallPlayingDefender");
        saveRelation("RightBack", "hasRole", "AttackingFullBack");
        saveRelation("LeftBack", "hasRole", "AttackingFullBack");

        saveRelation("DeepLyingPlaymaker", "requiresAttribute", "Passing");
        saveRelation("DeepLyingPlaymaker", "requiresAttribute", "Vision");
        saveRelation("DeepLyingPlaymaker", "requiresAttribute", "Positioning");
        saveRelation("BallWinningMidfielder", "requiresAttribute", "Tackling");
        saveRelation("BallWinningMidfielder", "requiresAttribute", "Strength");
        saveRelation("BallWinningMidfielder", "requiresAttribute", "WorkRate");
        saveRelation("BoxToBoxMidfielder", "requiresAttribute", "Stamina");
        saveRelation("BoxToBoxMidfielder", "requiresAttribute", "WorkRate");
        saveRelation("BoxToBoxMidfielder", "requiresAttribute", "Passing");
        saveRelation("InsideForward", "requiresAttribute", "Dribbling");
        saveRelation("InsideForward", "requiresAttribute", "Finishing");
        saveRelation("InsideForward", "requiresAttribute", "Acceleration");
        saveRelation("TargetForward", "requiresAttribute", "Strength");
        saveRelation("TargetForward", "requiresAttribute", "Finishing");
        saveRelation("TargetForward", "requiresAttribute", "Positioning");
        saveRelation("PressingForward", "requiresAttribute", "WorkRate");
        saveRelation("PressingForward", "requiresAttribute", "Stamina");
        saveRelation("PressingForward", "requiresAttribute", "Acceleration");
        saveRelation("BallPlayingDefender", "requiresAttribute", "Passing");
        saveRelation("BallPlayingDefender", "requiresAttribute", "Positioning");
        saveRelation("BallPlayingDefender", "requiresAttribute", "Tackling");
        saveRelation("AttackingFullBack", "requiresAttribute", "Speed");
        saveRelation("AttackingFullBack", "requiresAttribute", "Stamina");
        saveRelation("AttackingFullBack", "requiresAttribute", "Dribbling");
    }

    private void saveClub(String name, String league, long budget, String preferredStyle) {
        if (clubRepository.existsByNameIgnoreCase(name)) {
            return;
        }

        clubRepository.save(ClubEntity.builder()
                .name(name)
                .league(league)
                .budget(BigDecimal.valueOf(budget))
                .preferredStyle(preferredStyle)
                .build());
    }

    private void savePlayer(
            String name,
            int age,
            String currentClub,
            String league,
            String position,
            long marketValue,
            int speed,
            int acceleration,
            int passing,
            int vision,
            int ballControl,
            int finishing,
            int dribbling,
            int stamina,
            int strength,
            int tackling,
            int positioning,
            int workRate
    ) {
        if (playerRepository.existsByNameIgnoreCase(name)) {
            return;
        }

        playerRepository.save(PlayerEntity.builder()
                .name(name)
                .age(age)
                .currentClub(currentClub)
                .league(league)
                .position(position)
                .marketValue(BigDecimal.valueOf(marketValue))
                .speed(speed)
                .acceleration(acceleration)
                .passing(passing)
                .vision(vision)
                .ballControl(ballControl)
                .finishing(finishing)
                .dribbling(dribbling)
                .stamina(stamina)
                .strength(strength)
                .tackling(tackling)
                .positioning(positioning)
                .workRate(workRate)
                .build());
    }

    private void saveConcept(String conceptName, String conceptType, String description) {
        if (ontologyConceptRepository.existsByConceptNameIgnoreCaseAndConceptTypeIgnoreCase(
                conceptName,
                conceptType
        )) {
            return;
        }

        ontologyConceptRepository.save(OntologyConceptEntity.builder()
                .conceptName(conceptName)
                .conceptType(conceptType)
                .description(description)
                .source(SYSTEM_SOURCE)
                .build());
    }

    private void saveRelation(String sourceConcept, String relationType, String targetConcept) {
        if (ontologyRelationRepository
                .existsBySourceConceptIgnoreCaseAndRelationTypeIgnoreCaseAndTargetConceptIgnoreCase(
                        sourceConcept,
                        relationType,
                        targetConcept
                )) {
            return;
        }

        ontologyRelationRepository.save(OntologyRelationEntity.builder()
                .sourceConcept(sourceConcept)
                .relationType(relationType)
                .targetConcept(targetConcept)
                .build());
    }
}
