import React from 'react';
import { NarrativeStream } from './components/NarrativeStream';
import { InputBox } from './components/InputBox';
import { StatusPanel } from './components/StatusPanel';
import { CharacterList } from './components/CharacterList';
import { StartScreen } from './components/StartScreen';
import { CourtSessionPanel } from './components/CourtSessionPanel';
import { createSession, streamNarrative } from './services/api';
import { CourtSessionState, WorldState } from './types';
import './App.css';

export default function App() {
  const [started, setStarted] = React.useState(false);
  const [sessionId, setSessionId] = React.useState('');
  const [narrative, setNarrative] = React.useState('');
  const [isStreaming, setIsStreaming] = React.useState(false);
  const [worldState, setWorldState] = React.useState<WorldState | null>(null);
  const [courtSession, setCourtSession] = React.useState<CourtSessionState | null>(null);
  const [sceneCharacters, setSceneCharacters] = React.useState<string[]>([]);
  const [error, setError] = React.useState('');

  const handleStart = async (playerName: string) => {
    try {
      const res = await createSession(playerName);
      setSessionId(res.sessionId);
      setNarrative(res.message);
      setWorldState(res.worldState || null);
      setCourtSession(res.courtSession || null);
      setStarted(true);
    } catch (err: any) {
      setError('无法连接服务器，请确保后端已启动。');
    }
  };

  const handleSend = (text: string) => {
    if (!sessionId || isStreaming) return;

    setIsStreaming(true);
    setError('');
    setNarrative(prev => `${prev}\n\n【旨意】${text}\n`);

    streamNarrative(
      { sessionId, text },
      (token) => {
        setNarrative(prev => prev + token);
      },
      (state, characters, court) => {
        setWorldState(state);
        setSceneCharacters(characters);
        setCourtSession(court || null);
      },
      () => {
        setIsStreaming(false);
      },
      (err) => {
        setError(err);
        setIsStreaming(false);
      }
    );
  };

  const handleSelectNpc = (name: string) => {
    handleSend(`@${name}`);
  };

  if (!started) {
    return <StartScreen onStart={handleStart} />;
  }

  return (
    <main className="app-shell">
      <div className="app-frame">
        <header className="hero-bar">
          <div>
            <div className="app-kicker">
              {worldState ? `${worldState.eraName} · ${worldState.year}年` : '隆庆六年 · 乾清宫'}
            </div>
            <h1 className="hero-title">万历朝局推演</h1>
          </div>
          <div className="hero-actions" aria-label="快捷行动">
            <button className="quick-chip" type="button" disabled={isStreaming} onClick={() => handleSend('上朝，召集群臣奏对')}>
              上朝
            </button>
            <button className="quick-chip" type="button" disabled={isStreaming} onClick={() => handleSend('罢朝，今日不上朝')}>
              罢朝
            </button>
            <button className="quick-chip" type="button" disabled={isStreaming} onClick={() => handleSend('宣布退朝，今日朝会到此结束')}>
              退朝
            </button>
          </div>
        </header>

        <section className="game-grid">
          <div className="main-column">
            <NarrativeStream content={narrative} isStreaming={isStreaming} />
            {error && <div className="error-banner">{error}</div>}
            <InputBox
              onSend={handleSend}
              disabled={isStreaming}
              sceneCharacters={sceneCharacters}
            />
          </div>
          <aside className="side-column">
            <CourtSessionPanel courtSession={courtSession} onSend={handleSend} disabled={isStreaming} />
            <StatusPanel state={worldState} />
            <CharacterList characters={sceneCharacters} onSelect={handleSelectNpc} />
          </aside>
        </section>
      </div>
    </main>
  );
}
