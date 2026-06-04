import React from 'react';
import { getCharacterPortrait, normalizeCharacterName } from '../utils/characterPortraits';

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
          {characters.map(name => {
            const cleanName = normalizeCharacterName(name);
            const portrait = getCharacterPortrait(cleanName);
            return (
              <button
                key={name}
                className="character-chip character-card"
                type="button"
                onClick={() => onSelect(cleanName)}
              >
                {portrait && <img className="character-portrait" src={portrait} alt={`${cleanName}像素半身像`} />}
                <span>@{cleanName}</span>
              </button>
            );
          })}
        </div>
      ) : (
        <div className="muted">当前场景暂无可直接点选的人物，继续推进剧情后会自动刷新。</div>
      )}
    </section>
  );
};
