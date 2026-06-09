import { NavLink, Outlet } from "react-router-dom";

export default function AppLayout() {
    return (
        <div className="app-shell">
            <nav className="top-nav">
                <div className="top-nav-inner">
                    <NavLink className="brand" to="/">
                        <span className="brand-mark">FTA</span>
                        <span className="brand-text">
                            <span className="brand-title">
                                Football Transfer Advisor
                            </span>
                            <span className="brand-subtitle">
                                Ontology driven recommendations
                            </span>
                        </span>
                    </NavLink>

                    <div className="nav-links">
                        <NavLink className="nav-link" to="/">
                            Recommend Player
                        </NavLink>

                        <NavLink className="nav-link" to="/football-data">
                            Football Data
                        </NavLink>

                        <NavLink className="nav-link" to="/ontology">
                            Ontology Manager
                        </NavLink>

                        <NavLink className="nav-link" to="/agent-logs">
                            Agent Logs
                        </NavLink>
                    </div>
                </div>
            </nav>

            <main className="app-content">
                <Outlet />
            </main>
        </div>
    );
}
