import { BrowserRouter, Routes, Route } from "react-router-dom";

import AppLayout from "../../components/layout/AppLayout";

import OntologyManagerPage from "../../pages/OntologyManagerPage";
import AgentLogsPage from "../../pages/AgentLogsPage";
import PlayerRecommendationPage from "../../pages/PlayerRecommendationPage";
import FootballDataPage from "../../pages/FootballDataPage";

export default function AppRouter() {
    return (
        <BrowserRouter>
            <Routes>
                <Route element={<AppLayout />}>
                    <Route path="/" element={<PlayerRecommendationPage />} />
                    <Route path="/football-data" element={<FootballDataPage />} />
                    <Route path="/ontology" element={<OntologyManagerPage />} />
                    <Route path="/agent-logs" element={<AgentLogsPage />} />
                    <Route path="/recommend-player" element={<PlayerRecommendationPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    );
}
