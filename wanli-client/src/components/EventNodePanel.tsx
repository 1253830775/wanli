import React from 'react';
import { EventNode } from '../types';

interface EventNodePanelProps {
  eventNode: EventNode | null;
  onSend: (text: string) => void;
  disabled: boolean;
}

const statusText: Record<string, string> = {
  available: '可触发',
  active: '进行中',
  completed: '已收束'
};

export const EventNodePanel: React.FC<EventNodePanelProps> = ({ eventNode, onSend, disabled }) => {
  if (!eventNode) {
    return (
      <section className="side-card event-card">
        <div className="panel-title">事件节点</div>
        <div className="event-name">自由行动</div>
        <div className="event-desc">输入“上朝”可开启朝会节点；输入“召见某人”会转入人物交互。</div>
        <div className="event-actions">
          <button className="quick-chip" type="button" disabled={disabled} onClick={() => onSend('上朝，召集群臣奏对')}>
            开启上朝
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="side-card event-card">
      <div className="panel-title">事件节点 · {statusText[eventNode.status] || eventNode.status}</div>
      <div className="event-name">{eventNode.title}</div>
      <div className="event-desc">{eventNode.description}</div>
      <div className="event-steps">
        {eventNode.steps.map((step, index) => (
          <div key={step} className={`event-step ${index < eventNode.currentStep ? 'done' : ''}`}>
            <span className="step-dot">{index + 1}</span>
            <span>{step}</span>
          </div>
        ))}
      </div>
      <div className="event-actions">
        {eventNode.quickActions.map(action => (
          <button
            key={action}
            className="quick-chip"
            type="button"
            disabled={disabled}
            onClick={() => onSend(action)}
          >
            {action}
          </button>
        ))}
      </div>
    </section>
  );
};
