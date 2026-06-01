import { parseNarrativeLine } from './api';

function assert(condition: unknown, message: string): void {
  if (!condition) throw new Error(message);
}

const compactSse = parseNarrativeLine('data:{"type":"token","sessionId":"s1","token":"hello"}');

assert(compactSse?.type === 'token', 'parses compact SSE data lines');
assert(compactSse?.token === 'hello', 'preserves token content');

const jsonLine = parseNarrativeLine('{"type":"done","sessionId":"s1"}');

assert(jsonLine?.type === 'done', 'parses plain JSON lines');

assert(parseNarrativeLine('data: [DONE]') === null, 'ignores done sentinels');
