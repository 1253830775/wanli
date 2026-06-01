import React from 'react';

interface StartScreenProps {
  onStart: (name: string) => void;
}

export const StartScreen: React.FC<StartScreenProps> = ({ onStart }) => {
  const [name, setName] = React.useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (name.trim()) onStart(name.trim());
  };

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      backgroundColor: '#0f172a',
      color: '#f1f5f9'
    }}>
      <div style={{ textAlign: 'center', marginBottom: 40 }}>
        <h1 style={{ fontSize: 36, margin: 0, letterSpacing: 4, fontWeight: 200 }}>
          万 历 穿 越
        </h1>
        <p style={{ fontSize: 14, color: '#94a3b8', marginTop: 12 }}>
          AI 驱动的历史穿越文字冒险
        </p>
      </div>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16, width: 320 }}>
        <input
          value={name}
          onChange={e => setName(e.target.value)}
          placeholder="输入你的名字……"
          style={{
            padding: '12px 16px',
            borderRadius: 8,
            border: '1px solid #334155',
            backgroundColor: '#1e293b',
            color: '#f1f5f9',
            fontSize: 16,
            outline: 'none',
            textAlign: 'center'
          }}
        />
        <button
          type="submit"
          disabled={!name.trim()}
          style={{
            padding: '12px',
            borderRadius: 8,
            border: 'none',
            backgroundColor: name.trim() ? '#2563eb' : '#475569',
            color: '#fff',
            fontSize: 16,
            fontWeight: 600,
            cursor: name.trim() ? 'pointer' : 'not-allowed'
          }}
        >
          踏入大明
        </button>
      </form>
      <p style={{ fontSize: 12, color: '#64748b', marginTop: 32, maxWidth: 360, textAlign: 'center', lineHeight: 1.6 }}>
        你将穿越成明神宗朱翊钧，从隆庆帝托孤开始，凭借现代知识改变历史走向。
      </p>
    </div>
  );
};
