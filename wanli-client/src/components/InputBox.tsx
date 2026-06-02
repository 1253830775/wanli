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
      const cleanName = name.replace('@', '');
      const atIdx = prev.lastIndexOf('@');
      if (atIdx >= 0) return prev.slice(0, atIdx) + `@${cleanName} `;
      return prev + `@${cleanName} `;
    });
    setShowNpcs(false);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      handleSubmit(e);
    }
  };

  return (
    <section className="command-deck">
      <form className="command-form" onSubmit={handleSubmit}>
        <div className="command-input-wrap">
          <input
            className="command-input"
            value={text}
            onChange={e => {
              setText(e.target.value);
              setShowNpcs(e.target.value.endsWith('@'));
            }}
            onKeyDown={handleKeyDown}
            placeholder="输入行动：召见张居正 / 上朝 / 宣布退朝…… 输入 @ 选择 NPC"
            disabled={disabled}
          />
          {showNpcs && sceneCharacters.length > 0 && (
            <div className="npc-menu">
              {sceneCharacters.map(name => (
                <button key={name} type="button" onClick={() => insertNpc(name)}>
                  {name}
                </button>
              ))}
            </div>
          )}
        </div>
        <button className="send-button" type="submit" disabled={disabled || !text.trim()}>
          {disabled ? '推演中' : '下旨'}
        </button>
      </form>
    </section>
  );
};
