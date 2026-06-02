import { PlayerInput, NarrativeEvent, CreateSessionRequest, CreateSessionResponse, WorldState, NpcBrief } from '../types';

const API_BASE = '/api/game';

export function parseNarrativeLine(line: string): NarrativeEvent | null {
  const trimmed = line.trim();
  const payload = trimmed.startsWith('data:') ? trimmed.slice(5).trimStart() : trimmed;

  if (!payload || payload === '[DONE]') return null;

  return JSON.parse(payload) as NarrativeEvent;
}

export async function createSession(playerName: string): Promise<CreateSessionResponse> {
  const res = await fetch(`${API_BASE}/session`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ playerName } as CreateSessionRequest)
  });
  return res.json();
}

export function streamNarrative(
  input: PlayerInput,
  onToken: (token: string) => void,
  onState: (state: WorldState, characters: string[], activeEvent?: NarrativeEvent['activeEvent']) => void,
  onDone: () => void,
  onError: (err: string) => void
): AbortController {
  const controller = new AbortController();

  fetch(`${API_BASE}/narrate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
    signal: controller.signal
  }).then(async response => {
    const reader = response.body?.getReader();
    if (!reader) return;

    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop() || '';

      for (const line of lines) {
        try {
          const event = parseNarrativeLine(line);
          if (!event) continue;
          switch (event.type) {
            case 'token':
              if (event.token) onToken(event.token);
              break;
            case 'state':
              if (event.worldState && event.sceneCharacters) {
                onState(event.worldState, event.sceneCharacters, event.activeEvent);
              }
              break;
            case 'done':
              onDone();
              break;
            case 'error':
              if (event.error) onError(event.error);
              break;
          }
        } catch { /* skip malformed */ }
      }
    }
  }).catch(err => {
    if (err.name !== 'AbortError') onError(err.message);
  });

  return controller;
}

export async function getState(sessionId: string): Promise<WorldState> {
  const res = await fetch(`${API_BASE}/${sessionId}/state`);
  return res.json();
}

export async function getSceneNpcs(sessionId: string): Promise<NpcBrief[]> {
  const res = await fetch(`${API_BASE}/${sessionId}/npcs`);
  return res.json();
}
