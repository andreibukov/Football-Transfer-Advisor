# Football Transfer Advisor

Football Transfer Advisor е уеб приложение за подпомагане на футболни трансферни решения. Системата позволява на потребител да зададе нужда на клуб, например позиция, стил на игра, предпочитана роля, бюджет и възраст, след което връща класирани футболисти с процентна оценка и обяснения.

Проектът комбинира графичен интерфейс, Spring Boot backend, база данни, OWL онтология и JADE агенти с ACL комуникация.

## Основна идея

Целта на проекта е да демонстрира интелигентна система, която използва не само обикновени данни, но и онтологично знание. Част от логиката за препоръки се извлича от OWL файла, вместо да бъде hardcoded в кода.

Системата оценява футболистите според:

- позиция;
- стил на игра;
- предпочитана роля;
- бюджет;
- възраст;
- атрибути на футболиста;
- клубен контекст.

Резултатите включват както общ процент, така и обяснения защо даден играч е препоръчан.

## Технологии

### Backend

- Java 21
- Spring Boot
- Spring Data JPA
- H2 Database
- OWL API
- JADE agents
- Maven

### Frontend

- React
- TypeScript
- Vite
- Axios
- React Router

### Knowledge Layer

- OWL ontology
- Ontology concepts
- Ontology relations
- Football knowledge graph

## Основни функционалности

### Recommend Player

Основната страница за търсене на подходящ футболист. Потребителят избира клуб, позиция, стил на игра, предпочитана роля, бюджет и възраст. Системата връща класирани играчи с:

- общ score;
- score breakdown;
- position match;
- style match;
- role match;
- budget score;
- age score;
- текстови reasons.

### Football Data

Страница за добавяне на клубове и футболисти. При добавяне на данни системата ги записва в базата и създава съответни ontology concepts и relations.

Пример:

```text
MohamedSalah -> playsForClub -> Liverpool
Liverpool -> playsInLeague -> PremierLeague
```

### Ontology Manager

Страница за управление на онтологични знания. Позволява добавяне на:

- ontology concepts;
- ontology relations;
- връзки между играчи, клубове, първенства, позиции, роли и атрибути.

Има и graph/relationship view, който показва връзките във формат:

```text
Source -> Relation -> Target
```

### Agent Logs

Страница за проследяване на комуникацията между агентите. Показва ACL съобщенията, участващите агенти и последователността на действията при трансферен анализ.

## Онтология

OWL файлът се намира тук:

```text
backend/src/main/resources/ontology/football-ontology.owl
```

Онтологията съдържа футболни знания за:

- позиции;
- стилове на игра;
- роли;
- атрибути;
- клубове;
- първенства;
- връзки между понятията;
- scoring configuration.

Примерни връзки:

```text
HighPressing -> styleRequiresAttribute -> WorkRate
Striker -> hasRole -> FalseNine
FalseNine -> requiresAttribute -> Vision
Liverpool -> playsInLeague -> PremierLeague
```

## Recommendation Engine

Recommendation Engine използва данни от базата и знания от онтологията, за да оцени кандидатите. Важна част от проекта е, че част от scoring логиката е изнесена към OWL/config.

Подобрения в бизнес логиката:

- budget score намалява постепенно, ако играчът е над бюджета;
- age score намалява в двете посоки спрямо предпочитаната възраст;
- играчи от същия клуб не се връщат като резултати;
- preferred roles се четат от онтологията;
- recommendation results съдържат reasons.

## Агенти и ACL комуникация

Проектът използва JADE агенти, които комуникират чрез ACL съобщения.

Основни агенти:

| Agent | Роля |
|---|---|
| BackendSenderAgent | Стартира agent flow от backend-а |
| ClubAnalysisAgent | Анализира клубния контекст |
| OntologyManagerAgent | Използва онтологично знание |
| TransferRecommendationAgent | Генерира препоръки чрез RecommendationService |

ACL комуникацията се записва в базата и се визуализира в Agent Logs страницата.

## База данни

Проектът използва H2 база данни. Тя се създава локално при стартиране на backend приложението.

Основни entities:

- PlayerEntity
- ClubEntity
- TransferNeedEntity
- RecommendationEntity
- RecommendationReasonEntity
- AgentLogEntity
- OntologyConceptEntity
- OntologyRelationEntity

H2 console:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:file:./data/football_advisor_db
```

## Структура на проекта

```text
Football-Transfer-Advisor/
  backend/    Spring Boot backend, OWL ontology, JADE agents
  frontend/   React + TypeScript frontend
  docs/       Project documentation
```

## Стартиране на проекта

### 1. Backend

От папка `backend`:

```bash
cd backend
.\mvnw.cmd spring-boot:run
```

Backend-ът стартира на:

```text
http://localhost:8080
```

### 2. Frontend

От папка `frontend`:

```bash
cd frontend
npm install
npm run dev
```

Frontend-ът стартира обикновено на:

```text
http://localhost:5173
```

## Тестове и проверки

### Backend tests

```bash
cd backend
.\mvnw.cmd test
```

### Frontend build

```bash
cd frontend
npm run build
```

### Frontend lint

```bash
cd frontend
npm run lint
```

## Покритие на изискванията

| Изискване | Реализация |
|---|---|
| Графична част | React web приложение |
| Онтологии | OWL файл с футболно знание |
| Документация | `docs/Документация.docx` |
| Поне 2 типа агенти | JADE агенти с различни роли |
| ACL комуникация | ACL съобщения между агентите |
| Манипулация на онтологии | Ontology Manager и Football Data sync |
| База данни | H2 database със Spring Data JPA |

## Документация

Проектната документация се намира в:

```text
docs/Документация.docx
```

В нея са описани целта на проекта, архитектурата, онтологията, базата данни, recommendation логиката, агентите, ACL комуникацията и демонстрационният сценарий.

## Автор

Разработено като университетски проект за система с онтологии, агенти и графичен интерфейс.
