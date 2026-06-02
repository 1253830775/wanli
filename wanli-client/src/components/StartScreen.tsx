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
    <main className="start-screen">
      <section className="start-card">
        <div className="start-eyebrow">AI HISTORICAL ADVENTURE</div>
        <h1>万历穿越</h1>
        <p className="start-subtitle">
          你将以十岁天子的身份醒在乾清宫，在高拱、张居正、冯保与两宫皇太后之间，
          用现代知识改写大明的权力节点。
        </p>
        <form className="start-form" onSubmit={handleSubmit}>
          <input
            value={name}
            onChange={e => setName(e.target.value)}
            placeholder="输入你的名字……"
            aria-label="玩家姓名"
          />
          <button className="primary-button" type="submit" disabled={!name.trim()}>
            踏入大明
          </button>
        </form>
        <p className="start-note">
          事件会以“节点”推进：例如上朝会开启朝会节点，完成奏对、定夺议题后，
          输入“宣布退朝/结束朝会”即可触发节点收束。
        </p>
      </section>
    </main>
  );
};
