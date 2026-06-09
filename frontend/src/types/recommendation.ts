export interface RecommendedPlayer {
    id?: number;
    name: string;
    age: number;
    currentClub: string;
    league: string;
    position: string;
    marketValue: number;
}

export interface Recommendation {
    id?: number;
    playerName?: string;
    position?: string;
    marketValue?: number;
    age?: number;
    player?: RecommendedPlayer;
    positionMatch: number;
    styleMatch: number;
    roleMatch: number;
    budgetMatch: number;
    ageMatch: number;
    totalScore: number;
    reasons: string[];
}
