export interface AgentLog {
    id: number;
    senderAgent: string;
    receiverAgent: string;
    performative: string;
    messageContent: string;
    createdAt: string;
}