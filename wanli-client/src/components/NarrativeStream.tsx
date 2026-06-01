import React from 'react';

interface NarrativeStreamProps {
  content: string;
  isStreaming: boolean;
}

export const NarrativeStream: React.FC<NarrativeStreamProps> = ({ content, isStreaming }) => {
  const bottomRef = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [content]);

  return (
    <div style={{
      flex: 1,
      overflowY: 'auto',
      padding: '16px 20px',
      backgroundColor: '#fafaf8',
      borderRadius: '8px',
      border: '1px solid #e5e5e0',
      minHeight: '400px',
      maxHeight: '500px',
      lineHeight: '1.8',
      fontSize: '15px',
      color: '#1c1c1a',
      whiteSpace: 'pre-wrap'
    }}>
      {content || (
        <span style={{ color: '#999' }}>
          叙事将在你开始游戏后显示在这里……
        </span>
      )}
      {isStreaming && <span style={{ animation: 'blink 0.8s infinite', marginLeft: 2 }}>▌</span>}
      <div ref={bottomRef} />
      <style>{`@keyframes blink { 50% { opacity: 0; } }`}</style>
    </div>
  );
};
