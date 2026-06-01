import React from 'react';

interface CharacterListProps {
  characters: string[];
  onSelect: (name: string) => void;
}

export const CharacterList: React.FC<CharacterListProps> = ({ characters, onSelect }) => {
  if (!characters.length) {
    return (
      <div style={{ padding: 12, fontSize: 13, color: '#999' }}>
        场景中无可选人物
      </div>
    );
  }

  return (
    <div style={{
      backgroundColor: '#fff',
      borderRadius: '8px',
      border: '1px solid #e5e7eb',
      padding: '12px 16px'
    }}>
      <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 8, color: '#111827' }}>
        👥 场景人物
      </div>
      {characters.map(name => (
        <div
          key={name}
          onClick={() => onSelect(name.replace('@', ''))}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            padding: '6px 8px',
            borderRadius: '6px',
            cursor: 'pointer',
            fontSize: '14px',
            color: '#374151',
            marginBottom: 2
          }}
          onMouseEnter={e => (e.currentTarget.style.backgroundColor = '#f3f4f6')}
          onMouseLeave={e => (e.currentTarget.style.backgroundColor = '')}
        >
          <span style={{ color: '#2563eb', fontWeight: 600 }}>@</span>
          {name.replace('@', '')}
        </div>
      ))}
    </div>
  );
};
