import { createOptics } from './optics.js';
import { copyFor } from './copy.mjs';

const text = copyFor(document.documentElement.lang);

const snippets = {
  button: 'GlassButton(onClick = onStart) {\n    Text("Start session")\n}',
  tabs: 'GlassNavigationBar(\n    tabs = tabs, // List<GlassTab>\n    selectedIndex = selectedIndex,\n    onSelect = { selectedIndex = it }\n)',
  segmented: 'GlassSegmentedControl(\n    labels = listOf("25 min", "45 min", "60 min"),\n    selected = preset,\n    onSelect = { preset = it }\n)',
  slider: 'GlassSlider(\n    value = minutes,\n    onValueChange = { minutes = it },\n    valueRange = 15f..60f,\n    steps = 8\n)',
  switch: 'GlassSwitch(\n    checked = hapticsEnabled,\n    onCheckedChange = { hapticsEnabled = it }\n)',
  text: 'GlassTextField(\n    value = query,\n    onValueChange = { query = it },\n    placeholder = { Text("Search sessions") },\n    singleLine = true\n)'
};

function highlight(element, text) {
  const pattern = /("(?:\\.|[^"\\])*"|\/\/[^\n]*|\b(?:val|fun|true|false|import)\b|\b[A-Z]\w*(?=\()|\b\d+(?:\.\d+)?f?\b)/g;
  const frag = document.createDocumentFragment();
  let from = 0;
  for (const match of text.matchAll(pattern)) {
    frag.append(text.slice(from, match.index));
    const token = document.createElement('span');
    const first = match[0][0];
    const type = first === '"' ? 'string' : first === '/' ? 'comment' : /\d/.test(first) ? 'number' : /[A-Z]/.test(first) ? 'call' : 'keyword';
    token.className = 'token-' + type;
    token.textContent = match[0];
    frag.append(token);
    from = match.index + match[0].length;
  }
  frag.append(text.slice(from));
  element.replaceChildren(frag);
}

for (const details of document.querySelectorAll('[data-code]')) {
  const kind = details.dataset.code;
  const block = document.createElement('div');
  block.className = 'code-block';
  block.innerHTML = '<div class="code-bar"><span>Kotlin</span><button type="button"></button></div><pre><code></code></pre>';
  const code = block.querySelector('code');
  code.id = 'code-' + kind;
  const button = block.querySelector('button');
  button.dataset.copy = code.id;
  button.textContent = text.copy;
  highlight(code, snippets[kind]);
  details.append(block);
}

async function copy(text) {
  try {
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(text);
      return true;
    }
  } catch {
    // Allow copy when a browser restricts Clipboard.writeText.
  }
  const active = document.activeElement;
  const field = document.createElement('textarea');
  field.value = text;
  field.className = 'sr-only';
  field.setAttribute('readonly', '');
  document.body.append(field);
  field.select();
  let succeeded = false;
  try { succeeded = document.execCommand('copy'); } catch { /* Unavailable. */ }
  field.remove();
  active?.focus({ preventScroll: true });
  return succeeded;
}

for (const button of document.querySelectorAll('[data-copy]')) {
  button.addEventListener('click', async () => {
    const code = document.getElementById(button.dataset.copy);
    button.textContent = await copy(code.textContent) ? text.copied : text.selectCode;
    setTimeout(() => { button.textContent = text.copy; }, 1600);
  });
}

const scene = document.querySelector('#scene');
const title = document.querySelector('#scene-title');
const subtitle = document.querySelector('#scene-subtitle');
const sceneButton = document.querySelector('#scene-press');
const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)');
const material = { frost: 8, bend: .7, dispersion: .3, rim: .15 };
const opticalState = { offset: 0, light: false, material };
const optics = createOptics(scene, opticalState);
const materialOptics = createOptics(document.querySelector('#material-preview'), { material, offset: 0, light: false });
const render = (ms = 0) => optics?.renderFor(reduceMotion.matches ? 0 : ms);
let activeSession = false;
let sceneTab = 0;

function refreshScene() {
  const scenes = [
    [text.focusSession, activeSession ? text.sessionActive : text.ready],
    [text.focusControls, text.focusPresets],
    [text.glassMaterials, text.glassAdjustments]
  ];
  title.textContent = scenes[sceneTab][0];
  subtitle.textContent = scenes[sceneTab][1];
  sceneButton.textContent = activeSession ? text.sessionFinish : text.sessionStart;
  render(360);
}

sceneButton.addEventListener('click', () => {
  activeSession = !activeSession;
  refreshScene();
});
sceneButton.addEventListener('pointerdown', () => render(300));
sceneButton.addEventListener('pointerup', () => render(340));
sceneButton.addEventListener('pointercancel', () => render(340));
sceneButton.addEventListener('keydown', () => render(300));
sceneButton.addEventListener('keyup', () => render(340));

const appearance = document.querySelector('#appearance');
appearance.addEventListener('click', () => {
  opticalState.light = !opticalState.light;
  scene.classList.toggle('is-light', opticalState.light);
  appearance.textContent = opticalState.light ? '☾' : '☼';
  appearance.setAttribute('aria-pressed', String(opticalState.light));
  appearance.setAttribute('aria-label', opticalState.light ? text.useDark : text.useLight);
  render();
});

const bound = (x, min, max) => Math.max(min, Math.min(max, x));
let pan;
scene.addEventListener('pointerdown', event => {
  if (event.button !== 0 || event.target.closest('button, [data-tabs]')) return;
  pan = { id: event.pointerId, x: event.clientX, y: event.clientY, offset: opticalState.offset };
  scene.setPointerCapture(event.pointerId);
  scene.dataset.panning = '';
});
scene.addEventListener('pointermove', event => {
  if (!pan || pan.id !== event.pointerId) return;
  const limit = scene.clientHeight * .46;
  const delta = event.clientY - pan.y + (event.clientX - pan.x) * .34;
  opticalState.offset = bound(pan.offset + delta, -limit, limit);
  scene.style.setProperty('--scene-offset', opticalState.offset + 'px');
  render();
});
scene.addEventListener('lostpointercapture', () => {
  pan = undefined;
  delete scene.dataset.panning;
});

for (const tabs of document.querySelectorAll('[data-tabs]')) {
  const buttons = [...tabs.querySelectorAll('[role=tab]')];
  const panel = document.getElementById(buttons[0].getAttribute('aria-controls'));
  const heroTabs = scene.contains(tabs);
  let selected = 0;
  let pointer;
  const requestPaint = (ms = 0) => { if (heroTabs) render(ms); };

  function select(index, focus = false) {
    selected = index;
    tabs.style.setProperty('--tab-position', selected);
    tabs.style.setProperty('--tab-scale', 1);
    buttons.forEach((button, position) => {
      button.setAttribute('aria-selected', String(selected === position));
      button.tabIndex = selected === position ? 0 : -1;
    });
    if (focus) buttons[selected].focus({ preventScroll: true });
    panel.setAttribute('aria-labelledby', buttons[selected].id);
    panel.textContent = buttons[selected].textContent.slice(1).trim();
    if (heroTabs) {
      sceneTab = selected;
      refreshScene();
    }
    requestPaint(350);
  }

  function positionAt(x) {
    const first = buttons[0].getBoundingClientRect();
    return bound((x - first.left) / first.width - .5, 0, buttons.length - 1);
  }

  tabs.addEventListener('pointerdown', event => {
    const button = event.target.closest('[role=tab]');
    if (!button || event.button !== 0 || !event.isPrimary || pointer) return;
    const index = buttons.indexOf(button);
    pointer = { id: event.pointerId, start: event.clientX, index, dragging: false, position: selected };
    if (index === selected) {
      tabs.style.setProperty('--tab-scale', 1.14);
      requestPaint(350);
    }
  });

  tabs.addEventListener('pointermove', event => {
    if (event.pointerId !== pointer?.id) return;
    if (!pointer.dragging) {
      if (Math.abs(event.clientX - pointer.start) < 6) return;
      pointer.dragging = true;
      tabs.setPointerCapture(event.pointerId);
      tabs.dataset.dragging = '';
    }
    pointer.position = positionAt(event.clientX);
    tabs.style.setProperty('--tab-position', pointer.position);
    tabs.style.setProperty('--tab-scale', 1.14);
    requestPaint();
  });

  function end(event, cancelled) {
    if (event.pointerId !== pointer?.id) return;
    const { dragging, position, index } = pointer;
    pointer = undefined;
    delete tabs.dataset.dragging;
    if (dragging || cancelled) {
      select(cancelled ? selected : Math.round(position));
    } else {
      tabs.style.setProperty('--tab-scale', 1);
      requestPaint(350);
      if (buttons[index].contains(event.target)) select(index);
    }
  }
  tabs.addEventListener('pointerup', event => end(event, false));
  tabs.addEventListener('pointercancel', event => end(event, true));
  tabs.addEventListener('lostpointercapture', event => end(event, true));
  tabs.addEventListener('pointerleave', event => {
    if (pointer && !pointer.dragging) end(event, true);
  });

  buttons.forEach((button, index) => {
    button.addEventListener('click', event => {
      if (event.detail === 0) select(index);
    });
    button.addEventListener('keydown', event => {
      const destinations = { ArrowLeft: (index + buttons.length - 1) % buttons.length,
        ArrowRight: (index + 1) % buttons.length, Home: 0, End: buttons.length - 1 };
      if (!(event.key in destinations)) return;
      event.preventDefault();
      select(destinations[event.key], true);
    });
  });
  select(0);
}

const buttonDemo = document.querySelector('#button-demo');
buttonDemo.addEventListener('click', () => {
  const active = buttonDemo.getAttribute('aria-pressed') === 'true';
  buttonDemo.setAttribute('aria-pressed', String(!active));
  buttonDemo.textContent = active ? text.sessionStart : text.sessionFinish;
});

for (const group of document.querySelectorAll('.segmented')) {
  group.addEventListener('click', event => {
    const chosen = event.target.closest('button');
    if (!chosen) return;
    for (const button of group.querySelectorAll('button')) {
      button.setAttribute('aria-pressed', String(button === chosen));
    }
  });
}

function bindRange(range, update) {
  const sync = () => {
    const value = Number(range.value);
    const progress = (value - Number(range.min)) / (Number(range.max) - Number(range.min));
    range.style.setProperty('--fill', progress * 100 + '%');
    update(value);
  };
  range.addEventListener('input', sync);
  sync();
}

bindRange(document.querySelector('#slider-demo'), value => {
  document.querySelector('#slider-value').value = value + text.minutes;
});

for (const key of Object.keys(material)) {
  const control = document.getElementById(key);
  bindRange(control, value => {
    material[key] = value;
    control.parentElement.querySelector('output').value = key === 'frost' ? value + ' dp' : value.toFixed(2);
    const settings = Object.entries(material).map(([name, amount]) => '        ' + (name === 'rim' ? 'shine' : name) + ' = ' + amount.toFixed(2) + 'f');
    highlight(document.querySelector('#material-code'), 'val style = GlassStyle(\n    containers = GlassTuning(\n' + settings.join(',\n') + '\n    )\n)');
    render();
    materialOptics?.renderFor();
  });
}
