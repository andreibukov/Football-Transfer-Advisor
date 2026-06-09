import apiClient from "./apiClient";
import { TransferNeed } from "../types/transferNeed";

export const createTransferNeed = async (
    clubId: number,
    transferNeed: TransferNeed
) => {
    const response = await apiClient.post<TransferNeed>(
        `/clubs/${clubId}/transfer-needs`,
        transferNeed
    );

    return response.data;
};