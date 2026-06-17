import apiClient from "./apiClient";
import { Recommendation } from "../types/recommendation";

export const getRecommendations = async (transferNeedId: number) => {
    const response = await apiClient.get<Recommendation[]>(
        `/transfer-needs/${transferNeedId}/recommendations`
    );

    return response.data;
};
