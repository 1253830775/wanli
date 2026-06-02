export interface WorldState {
  year: number;
  eraName: string;
  treasury: number;
  publicSupport: number;
  militaryLoyalty: number;
  playerLocation: string;
}

export interface EventNode {
  id: string;
  title: string;
  description: string;
  status: 'available' | 'active' | 'completed';
  currentStep: number;
  steps: string[];
  quickActions: string[];
  triggerHint: string;
  completionHint: string;
}

export interface NpcBrief {
  id: string;
  name: string;
  status: string;
  attitude: string;
}

export interface NarrativeEvent {
  type: 'token' | 'state' | 'done' | 'error';
  sessionId: string;
  content?: string;
  token?: string;
  worldState?: WorldState;
  sceneCharacters?: string[];
  activeEvent?: EventNode;
  error?: string;
}

export interface PlayerInput {
  sessionId: string;
  text: string;
  targetNpc?: string;
}

export interface CreateSessionRequest {
  playerName: string;
}

export interface CreateSessionResponse {
  sessionId: string;
  message: string;
  activeEvent?: EventNode;
  worldState?: WorldState;
}
