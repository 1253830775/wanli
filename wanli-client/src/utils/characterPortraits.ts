import zhangJuzhengPortrait from '../assets/portraits/zhang_juzheng.svg';
import gaoGongPortrait from '../assets/portraits/gao_gong.svg';
import fengBaoPortrait from '../assets/portraits/feng_bao.svg';
import liShiPortrait from '../assets/portraits/li_shi.svg';
import chenShiPortrait from '../assets/portraits/chen_shi.svg';
import zhuYijunPortrait from '../assets/portraits/zhu_yijun.svg';

const portraitByName: Record<string, string> = {
  张居正: zhangJuzhengPortrait,
  高拱: gaoGongPortrait,
  冯保: fengBaoPortrait,
  李氏: liShiPortrait,
  陈氏: chenShiPortrait,
  朱翊钧: zhuYijunPortrait,
  万历: zhuYijunPortrait,
  皇帝: zhuYijunPortrait,
};

export const knownCharacterNames = Object.keys(portraitByName).sort((a, b) => b.length - a.length);

export function normalizeCharacterName(name: string): string {
  return name.replace('@', '').trim();
}

export function getCharacterPortrait(name: string): string | undefined {
  return portraitByName[normalizeCharacterName(name)];
}

export function detectCharacterInText(text: string, candidates = knownCharacterNames): string | undefined {
  const normalized = text.replace('@', '').trim();
  return candidates.map(normalizeCharacterName).find(name => normalized.includes(name));
}
