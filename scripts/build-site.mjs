import { copyFileSync, cpSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { copyFor } from '../site/copy.mjs';

const repository = new URL('../', import.meta.url);
const output = new URL('site-dist/', repository);
const assets = new URL('assets/', output);

cpSync(new URL('site/', repository), output, { recursive: true });
mkdirSync(assets, { recursive: true });

const template = readFileSync(new URL('site/index.html', repository), 'utf8');
const entities = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' };
for (const [lang, homePath] of [['en', '/'], ['ja', '/ja/']]) {
  const page = new URL(homePath.slice(1), output);
  const values = {
    ...copyFor(lang), lang, homePath,
    englishCurrent: lang === 'en' ? 'page' : 'false',
    japaneseCurrent: lang === 'ja' ? 'page' : 'false',
  };
  mkdirSync(page, { recursive: true });
  writeFileSync(new URL('index.html', page), template.replace(/\{\{(\w+)\}\}/g, (_, key) => {
    if (!Object.hasOwn(values, key)) throw new Error(`Missing translation for ${key}`);
    return values[key].replace(/[&<>"]/g, char => entities[char]);
  }));
}

for (const name of [
  'overview.png', 'controls.png', 'materials.png',
  'button.mp4', 'tabs.mp4', 'button-poster.webp', 'tabs-poster.webp',
]) {
  copyFileSync(new URL(`docs/media/${name}`, repository), new URL(name, assets));
}
copyFileSync(
  new URL('app/src/main/res/drawable-nodpi/gallery_sunset.jpg', repository),
  new URL('gallery-sunset.jpg', assets),
);
