import React from 'react';
import { WorldState } from '../types';

interface StatusPanelProps {
  state: WorldState | null;
}

const clamp = (value: number) => Math.max(0, Math.min(100, value));

const Meter: React.FC<{ value: number; color: string }> = ({ value, color }) => (
  <div className="meter" aria-label={`${value}%`}>
    <div className="meter-fill" style={{ width: `${clamp(value)}%`, background: color }} />
  </div>
);

export const StatusPanel: React.FC<StatusPanelProps> = ({ state }) => {
  if (!state) {
    return (
      <section className="side-card">
        <div className="panel-title">世界状态</div>
        <div className="muted">等待第一道旨意，世界状态将随叙事自动更新。</div>
      </section>
    );
  }

  return (
    <section className="side-card">
      <div className="panel-title">世界状态</div>
      <div className="stat-row">
        <span className="stat-label">纪年</span>
        <span className="stat-value">{state.year}年 · {state.eraName}</span>
      </div>
      <div className="stat-row">
        <span className="stat-label">国库</span>
        <span className="stat-value">{state.treasury.toLocaleString()} 两</span>
      </div>
      <div className="stat-row">
        <span className="stat-label">民望</span>
        <Meter value={state.publicSupport} color="#2f9e5d" />
      </div>
      <div className="stat-row">
        <span className="stat-label">帝威</span>
        <Meter value={state.imperialAuthority} color="#c07728" />
      </div>
      <div className="stat-row">
        <span className="stat-label">所在</span>
        <span className="stat-value">{state.playerLocation}</span>
      </div>
    </section>
  );
};
