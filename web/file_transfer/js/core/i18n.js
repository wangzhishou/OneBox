/**
 * i18n 文案加载与应用
 *
 * 支持 zh-CN / en / es / pt-BR / in / hi / ru / tr / ja / ko / fil / de，字典由服务端按语言下发（res/raw/i18n[_lang].json）。
 */

const STORAGE_KEY = 'ft-lang';

/**
 * 语言检测优先级：?lang= URL query > localStorage(ft-lang) > navigator.language
 * 规范化：zh* → zh-CN，es* → es，pt* → pt-BR，in/id* → in（印尼语），hi* → hi（印地语），ru* → ru（俄语），tr* → tr（土耳其语），ja* → ja（日语），ko* → ko（韩语），fil* → fil（菲律宾语），de* → de（德语），其余 → en（即未支持语言默认英文）
 */
export function detectLang() {
  const normalize = (raw) => {
    const v = String(raw || '').toLowerCase();
    if (v.startsWith('zh')) return 'zh-CN';
    if (v.startsWith('es')) return 'es';
    if (v.startsWith('pt')) return 'pt-BR';
    // 印尼语: Android 用旧码 in, 浏览器 navigator.language 多为 id / id-ID
    if (v.startsWith('in') || v.startsWith('id')) return 'in';
    // 印地语
    if (v.startsWith('hi')) return 'hi';
    // 俄语
    if (v.startsWith('ru')) return 'ru';
    // 土耳其语
    if (v.startsWith('tr')) return 'tr';
    // 日语
    if (v.startsWith('ja')) return 'ja';
    // 韩语
    if (v.startsWith('ko')) return 'ko';
    // 菲律宾语
    if (v.startsWith('fil')) return 'fil';
    // 德语
    if (v.startsWith('de')) return 'de';
    return 'en';
  };

  try {
    const fromQuery = new URLSearchParams(window.location.search).get('lang');
    if (fromQuery) {
      const lang = normalize(fromQuery);
      // 记住用户显式选择
      try { localStorage.setItem(STORAGE_KEY, lang); } catch (_) {}
      return lang;
    }
  } catch (_) {}

  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) return normalize(stored);
  } catch (_) {}

  return normalize(navigator.language || navigator.userLanguage);
}

export const i18n = {
  lang: 'zh-CN',
  dict: {},

  async load(lang = 'zh-CN') {
    this.lang = lang;

    const candidates = [
      `/i18n/${encodeURIComponent(lang)}.json`,
      // 本地打开 web/index.html 调试时的兜底
      `./i18n/${encodeURIComponent(lang)}.json`,
    ];

    for (const url of candidates) {
      try {
        const res = await fetch(url, { cache: 'no-cache' });
        if (!res.ok) continue;
        this.dict = await res.json();
        document.documentElement.lang = this.lang;
        this.apply();
        return;
      } catch (_) {
        // try next
      }
    }

    // 目标语言字典不可用时回退 zh-CN（服务端一定会兜底返回中文）
    if (lang !== 'zh-CN') {
      return this.load('zh-CN');
    }

    // i18n 加载失败不影响主功能
    console.warn('i18n load failed');
  },

  t(key, fallback = '') {
    return this.dict?.[key] ?? fallback ?? key;
  },

  /**
   * 扫描 root 下所有 data-i18n* 属性并替换
   */
  apply(root = document) {
    // 文本
    root.querySelectorAll('[data-i18n]').forEach((el) => {
      const key = el.getAttribute('data-i18n');
      if (!key) return;
      el.textContent = this.t(key, el.textContent);
    });

    // placeholder
    root.querySelectorAll('[data-i18n-placeholder]').forEach((el) => {
      const key = el.getAttribute('data-i18n-placeholder');
      if (!key) return;
      el.setAttribute('placeholder', this.t(key, el.getAttribute('placeholder') || ''));
    });

    // title
    root.querySelectorAll('[data-i18n-title]').forEach((el) => {
      const key = el.getAttribute('data-i18n-title');
      if (!key) return;
      el.setAttribute('title', this.t(key, el.getAttribute('title') || ''));
    });

    // alt
    root.querySelectorAll('[data-i18n-alt]').forEach((el) => {
      const key = el.getAttribute('data-i18n-alt');
      if (!key) return;
      el.setAttribute('alt', this.t(key, el.getAttribute('alt') || ''));
    });
  },
};

