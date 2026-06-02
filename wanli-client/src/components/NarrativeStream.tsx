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
    <article className="content-card">
      <div className="narrative-stream">
        {content || (
          <span className="empty-copy">叙事将在你开始行动后显示在这里……</span>
        )}
        {isStreaming && <span className="cursor">▌</span>}
        <div ref={bottomRef} />
      </div>
    </article>
  );
};
