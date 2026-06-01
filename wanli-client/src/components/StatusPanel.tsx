import React from 'react';
import { WorldState } from '../types';

interface StatusPanelProps {
  state: WorldState | null;
}

const statStyle: React.CSSProperties = {
  display: 'flex',
  justifyContent: 'space-between',
  padding: '4px 0',
  fontSize: '13px',
  borderBottom: '1px solid #f3f4f6'
};

const labelStyle: React.CSSProperties = { color: '#6b7280' };
const valueStyle: React.CSSProperties = { fontWeight: 600, color: '#1f2937' };

const bar = (value: number, color: string): React.CSSProperties => ({
  height: 6,
  width: '100%',
  backgroundColor: '#e5e7eb',
  borderRadius: 3,
  position: 'relative' as const,
  marginTop: 4
});

const fill = (value: number, color: string): React.CSSProperties => ({
  height: '100%',
  width: `${value}%`,
  backgroundColor: color,
  borderRadius: 3
});

export const StatusPanel: React.FC<StatusPanelProps> = ({ state }) => {
  if (!state) {
    return (
      <div style={{ padding: 12, fontSize: 13, color: '#999' }}>
        等待游戏开始……
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
        📜 世界状态
      </div>
      <div style={statStyle}>
        <span style={labelStyle}>年份</span>
        <span style={valueStyle}>{state.year}年（{state.eraName}）</span>
      </div>
      <div style={statStyle}>
        <span style={labelStyle}>国库</span>
        <span style={valueStyle}>{state.treasury.toLocaleString()}两</span>
      </div>
      <div style={statStyle}>
        <span style={labelStyle}>民望</span>
        <span>
          <div style={bar(state.publicSupport, '#16a34a')}>
            <div style={fill(state.publicSupport, '#16a34a')} />
          </div>
        </span>
      </div>
      <div style={statStyle}>
        <span style={labelStyle}>军队忠诚</span>
        <span>
          <div style={bar(state.militaryLoyalty, '#2563eb')}>
            <div style={fill(state.militaryLoyalty, '#2563eb')} />
          </div>
        </span>
      </div>
      <div style={statStyle}>
        <span style={labelStyle}>当前位置</span>
        <span style={valueStyle}>{state.playerLocation}</span>
      </div>
    </div>
  );
};
