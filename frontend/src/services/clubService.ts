import apiClient from "./apiClient";
import { Club } from "../types/club";

export const getAllClubs = async () => {
    const response = await apiClient.get<Club[]>("/clubs");
    return response.data;
};

export const createClub = async (club: Club) => {
    const response = await apiClient.post<Club>("/clubs", club);
    return response.data;
};
