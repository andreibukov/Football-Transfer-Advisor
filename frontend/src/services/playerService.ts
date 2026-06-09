import apiClient from "./apiClient";
import { Player } from "../types/player";

export const getAllPlayers = async () => {
    const response = await apiClient.get<Player[]>("/players");
    return response.data;
};

export const createPlayer = async (player: Player) => {
    const response = await apiClient.post<Player>("/players", player);
    return response.data;
};
