import React from 'react';
import { detectCharacterInText, getCharacterPortrait, knownCharacterNames } from '../utils/characterPortraits';

interface NarrativeStreamProps {
  content: string;
  isStreaming: boolean;
  activeCharacter?: string;
}

interface NarrativeBlock {
  id: string;
  text: string;
  speaker?: string;
}

function splitNarrativeBlocks(content: string): NarrativeBlock[] {
  return content.split('\n').map((line, index) => {
    const speakerMatch = line.match(/^\s*(?:[「“]?)(张居正|高拱|冯保|李氏|陈氏|朱翊钧|万历|皇帝)[」”]?\s*[：:]/);
    const mentionMatch = line.match(/^\s*@?(张居正|高拱|冯保|李氏|陈氏|朱翊钧|万历|皇帝)\s*$/);
    return {
      id: `${index}-${line.slice(0, 12)}`,
      text: line,
      speaker: speakerMatch?.[1] || mentionMatch?.[1],
    };
  });
}

export const NarrativeStream: React.FC<NarrativeStreamProps> = ({ content, isStreaming, activeCharacter }) => {
  const bottomRef = React.useRef<HTMLDivElement>(null);
  const activePortrait = activeCharacter ? getCharacterPortrait(activeCharacter) : undefined;
  const activeName = activeCharacter && detectCharacterInText(activeCharacter, knownCharacterNames);

  React.useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [content]);

  return (
    <article className="content-card">
      <div className="narrative-stream">
        {activeName && activePortrait && (
          <div className="active-dialogue-card" aria-label={`正在与${activeName}对话`}>
            <img className="dialogue-portrait large" src={activePortrait} alt={`${activeName}像素半身像`} />
            <div>
              <div className="dialogue-kicker">当前对话</div>
              <div className="dialogue-name">{activeName}</div>
            </div>
          </div>
        )}
        {content ? (
          <div className="narrative-blocks">
            {splitNarrativeBlocks(content).map(block => {
              const portrait = block.speaker ? getCharacterPortrait(block.speaker) : undefined;
              return portrait && block.text.trim() ? (
                <div className="dialogue-line" key={block.id}>
                  <img className="dialogue-portrait" src={portrait} alt={`${block.speaker}像素半身像`} />
                  <div className="dialogue-copy">{block.text}</div>
                </div>
              ) : (
                <div className="narrative-line" key={block.id}>{block.text || '\u00A0'}</div>
              );
            })}
          </div>
        ) : (
          <span className="empty-copy">叙事将在你开始行动后显示在这里……</span>
        )}
        {isStreaming && <span className="cursor">▌</span>}
        <div ref={bottomRef} />
      </div>
    </article>
  );
};
