import apiClient from "./apiClient";
import { Recommendation } from "../types/recommendation";

export const generateRecommendations = async (transferNeedId: number) => {
    const response = await apiClient.post<Recommendation[]>(
        `/transfer-needs/${transferNeedId}/recommendations`
    );

    return response.data;
};

export const getRecommendations = async (transferNeedId: number) => {
    const response = await apiClient.get<Recommendation[]>(
        `/transfer-needs/${transferNeedId}/recommendations`
    );

    return response.data;
};