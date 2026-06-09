import apiClient from "./apiClient";

export const startAgentTransferAnalysis = async (transferNeedId: number) => {
    const response = await apiClient.post<string>(
        `/agents/transfer-analysis/${transferNeedId}`
    );

    return response.data;
};
