import React from 'react';
import { NarrativeStream } from './components/NarrativeStream';
import { InputBox } from './components/InputBox';
import { StatusPanel } from './components/StatusPanel';
import { CharacterList } from './components/CharacterList';
import { StartScreen } from './components/StartScreen';
import { createSession, streamNarrative } from './services/api';
import { WorldState } from './types';

export default function App() {
  const [started, setStarted] = React.useState(false);
  const [sessionId, setSessionId] = React.useState('');
  const [narrative, setNarrative] = React.useState('');
  const [isStreaming, setIsStreaming] = React.useState(false);
  const [worldState, setWorldState] = React.useState<WorldState | null>(null);
  const [sceneCharacters, setSceneCharacters] = React.useState<string[]>([]);
  const [error, setError] = React.useState('');

  const handleStart = async (playerName: string) => {
    try {
      const res = await createSession(playerName);
      setSessionId(res.sessionId);
      setNarrative(res.message);
      setStarted(true);
    } catch (err: any) {
      setError('无法连接服务器，请确保后端已启动。');
    }
  };

  const handleSend = (text: string) => {
    if (!sessionId || isStreaming) return;

    setIsStreaming(true);
    setError('');

    streamNarrative(
      { sessionId, text },
      (token) => {
        setNarrative(prev => prev + token);
      },
      (state, characters) => {
        setWorldState(state);
        setSceneCharacters(characters);
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
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      backgroundColor: '#f5f5f0',
      maxWidth: 1200,
      margin: '0 auto',
      padding: '12px 16px',
      boxSizing: 'border-box'
    }}>
      <div style={{
        display: 'flex',
        gap: 12,
        flex: 1,
        minHeight: 0
      }}>
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 12 }}>
          <NarrativeStream content={narrative} isStreaming={isStreaming} />
          {error && (
            <div style={{ padding: 8, backgroundColor: '#fef2f2', color: '#dc2626', borderRadius: 6, fontSize: 13 }}>
              {error}
            </div>
          )}
          <InputBox
            onSend={handleSend}
            disabled={isStreaming}
            sceneCharacters={sceneCharacters}
          />
        </div>
        <div style={{ width: 280, display: 'flex', flexDirection: 'column', gap: 12, flexShrink: 0 }}>
          <StatusPanel state={worldState} />
          <CharacterList characters={sceneCharacters} onSelect={handleSelectNpc} />
        </div>
      </div>
    </div>
  );
}
