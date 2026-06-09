import apiClient from "./apiClient";
import { AgentLog } from "../types/agentLog";

export const getAllAgentLogs = async () => {
    const response = await apiClient.get<AgentLog[]>("/agent-logs");
    return response.data;
};