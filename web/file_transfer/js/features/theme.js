/**
 * 深色模式：跟随系统偏好
 */

export function initTheme() {
  if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
    document.documentElement.classList.add('dark');
  }

  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    document.documentElement.classList.toggle('dark', e.matches);
  });
}

