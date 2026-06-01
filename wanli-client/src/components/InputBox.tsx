import React from 'react';

interface InputBoxProps {
  onSend: (text: string) => void;
  disabled: boolean;
  sceneCharacters: string[];
}

export const InputBox: React.FC<InputBoxProps> = ({ onSend, disabled, sceneCharacters }) => {
  const [text, setText] = React.useState('');
  const [showNpcs, setShowNpcs] = React.useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!text.trim() || disabled) return;
    onSend(text.trim());
    setText('');
  };

  const insertNpc = (name: string) => {
    setText(prev => {
      const atIdx = prev.lastIndexOf('@');
      if (atIdx >= 0) return prev.slice(0, atIdx) + `@${name} `;
      return prev + `@${name} `;
    });
    setShowNpcs(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      handleSubmit(e);
    }
  };

  return (
    <div style={{ position: 'relative' }}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 8 }}>
        <div style={{ position: 'relative', flex: 1 }}>
          <input
            value={text}
            onChange={e => {
              setText(e.target.value);
              if (e.target.value.endsWith('@')) setShowNpcs(true);
            }}
            onKeyDown={handleKeyDown}
            placeholder="输入你的行动… 输入 @ 选择 NPC"
            disabled={disabled}
            style={{
              width: '100%',
              padding: '10px 12px',
              borderRadius: '8px',
              border: '1px solid #d1d5db',
              fontSize: '14px',
              outline: 'none',
              boxSizing: 'border-box',
              backgroundColor: disabled ? '#f3f4f6' : '#fff'
            }}
          />
          {showNpcs && sceneCharacters.length > 0 && (
            <div style={{
              position: 'absolute',
              bottom: '100%',
              left: 0,
              right: 0,
              backgroundColor: '#fff',
              border: '1px solid #d1d5db',
              borderRadius: '8px',
              boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
              zIndex: 10,
              maxHeight: 200,
              overflowY: 'auto'
            }}>
              {sceneCharacters.map(name => (
                <div
                  key={name}
                  onClick={() => insertNpc(name.replace('@', ''))}
                  style={{
                    padding: '8px 12px',
                    cursor: 'pointer',
                    borderBottom: '1px solid #f3f4f6',
                    fontSize: '14px'
                  }}
                  onMouseEnter={e => (e.currentTarget.style.backgroundColor = '#f9fafb')}
                  onMouseLeave={e => (e.currentTarget.style.backgroundColor = '')}
                >
                  {name}
                </div>
              ))}
            </div>
          )}
        </div>
        <button
          type="submit"
          disabled={disabled || !text.trim()}
          style={{
            padding: '10px 20px',
            borderRadius: '8px',
            border: 'none',
            backgroundColor: disabled ? '#9ca3af' : '#2563eb',
            color: '#fff',
            fontSize: '14px',
            fontWeight: 600,
            cursor: disabled ? 'not-allowed' : 'pointer'
          }}
        >
          发送
        </button>
      </form>
    </div>
  );
};
