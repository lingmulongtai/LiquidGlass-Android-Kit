import { copyFileSync, cpSync, mkdirSync } from 'node:fs';

const repository = new URL('../', import.meta.url);
const output = new URL('site-dist/', repository);
const assets = new URL('assets/', output);

cpSync(new URL('site/', repository), output, { recursive: true });
mkdirSync(assets, { recursive: true });

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
