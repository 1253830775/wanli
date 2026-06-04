export interface WorldState {
  year: number;
  eraName: string;
  treasury: number;
  publicSupport: number;
  imperialAuthority: number;
  playerLocation: string;
}

export interface CourtSessionState {
  courtNumber: number;
  phase: string;
  currentTopicIndex: number;
  topics: Topic[];
  inquiryCount: number;
  isActive: boolean;
}

export interface Topic {
  title: string;
  reporter: string;
  description: string;
  historicalNote: string;
  rulingStatus: string;
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
  courtSession?: CourtSessionState;
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
  courtSession?: CourtSessionState;
  worldState?: WorldState;
}
