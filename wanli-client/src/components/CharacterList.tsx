import React from 'react';

interface CharacterListProps {
  characters: string[];
  onSelect: (name: string) => void;
}

export const CharacterList: React.FC<CharacterListProps> = ({ characters, onSelect }) => {
  return (
    <section className="side-card">
      <div className="panel-title">场景人物</div>
      {characters.length ? (
        <div className="character-list">
          {characters.map(name => (
            <button
              key={name}
              className="character-chip"
              type="button"
              onClick={() => onSelect(name.replace('@', ''))}
            >
              @{name.replace('@', '')}
            </button>
          ))}
        </div>
      ) : (
        <div className="muted">当前场景暂无可直接点选的人物，继续推进剧情后会自动刷新。</div>
      )}
    </section>
  );
};
