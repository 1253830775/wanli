import React from 'react';
import { CourtSessionState } from '../types';

interface CourtSessionPanelProps {
  courtSession: CourtSessionState | null;
  onSend: (text: string) => void;
  disabled: boolean;
}

const phaseLabels: Record<string, string> = {
  report: '奏报',
  inquiry: '问对',
  ruling: '裁决',
  reaction: '反应',
  ended: '已结束'
};

const phaseDescriptions: Record<string, string> = {
  report: '大臣正在上奏议题',
  inquiry: '向大臣追问细节',
  ruling: '等待皇帝裁决',
  reaction: '大臣们对裁决的反应',
  ended: '本次朝会已结束'
};

export const CourtSessionPanel: React.FC<CourtSessionPanelProps> = ({
  courtSession, onSend, disabled
}) => {
  if (!courtSession || !courtSession.isActive) {
    return (
      <section className="side-card court-card">
        <div className="panel-title">朝会</div>
        <div className="court-idle">当前非朝会时间</div>
        <div className="court-actions">
          <button
            className="quick-chip"
            type="button"
            disabled={disabled}
            onClick={() => onSend('上朝，召集群臣奏对')}
          >
            开启朝会
          </button>
        </div>
      </section>
    );
  }

  const currentTopic = courtSession.topics[courtSession.currentTopicIndex];
  const phases = ['report', 'inquiry', 'ruling', 'reaction'];
  const currentPhaseIndex = phases.indexOf(courtSession.phase);

  return (
    <section className="side-card court-card">
      <div className="panel-title">第 {courtSession.courtNumber} 次朝会</div>

      <div className="court-phases">
        {phases.map((phase, index) => (
          <div
            key={phase}
            className={`court-phase ${index <= currentPhaseIndex ? 'active' : ''} ${index === currentPhaseIndex ? 'current' : ''}`}
          >
            <span className="phase-dot">{index + 1}</span>
            <span className="phase-label">{phaseLabels[phase]}</span>
          </div>
        ))}
      </div>

      <div className="court-phase-desc">
        {phaseDescriptions[courtSession.phase]}
      </div>

      {currentTopic && (
        <div className="court-topic">
          <div className="topic-title">{currentTopic.title}</div>
          <div className="topic-reporter">奏报：{currentTopic.reporter}</div>
          {currentTopic.description && (
            <div className="topic-desc">{currentTopic.description}</div>
          )}
        </div>
      )}

      <div className="court-info">
        <span>问对轮数：{courtSession.inquiryCount}</span>
        <span>议题：{courtSession.currentTopicIndex + 1}/{Math.max(courtSession.topics.length, 1)}</span>
      </div>

      <div className="court-actions">
        {courtSession.phase === 'inquiry' && (
          <button
            className="quick-chip"
            type="button"
            disabled={disabled}
            onClick={() => onSend('朕意已决，请诸位静候裁决')}
          >
            进入裁决
          </button>
        )}
        {courtSession.phase !== 'ended' && (
          <button
            className="quick-chip"
            type="button"
            disabled={disabled}
            onClick={() => onSend('宣布退朝，今日朝会到此结束')}
          >
            退朝
          </button>
        )}
      </div>
    </section>
  );
};
