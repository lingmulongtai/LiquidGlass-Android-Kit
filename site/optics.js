// The DOM owns interaction and text. One canvas draws the photo and the lenses
// underneath it, using the current bounds of the animated DOM controls.
const vertexSource = [
  'attribute vec2 position;',
  'void main() { gl_Position = vec4(position, 0.0, 1.0); }'
].join('\n');

const fragmentSource = [
  'precision highp float;',
  'uniform vec2 viewport;',
  'uniform vec2 renderSize;',
  'uniform sampler2D wallpaper;',
  'uniform vec2 photoSize;',
  'uniform float offsetY;',
  'uniform float light;',
  'uniform vec4 material;',
  'uniform vec4 boxes[4];',
  'uniform vec4 corners;',
  '',
  'vec3 sceneAt(vec2 screenPoint) {',
  '  vec2 extent = photoSize * max(viewport.x / photoSize.x, viewport.y / photoSize.y);',
  '  vec2 origin = (viewport - extent) * vec2(0.5, 0.58);',
  '  origin.y = clamp(origin.y + offsetY, viewport.y - extent.y, 0.0);',
  '  vec2 uv = (screenPoint - origin) / extent;',
  '  return texture2D(wallpaper, uv).rgb;',
  '}',
  '',
  'vec3 pane(vec3 behind, vec2 p, vec4 box, float radius, bool lifted) {',
  '  if (box.z < 1.0 || box.w < 1.0) return behind;',
  '  vec2 halfExtent = box.zw * 0.5;',
  '  vec2 centered = p - box.xy - halfExtent;',
  '  radius = min(radius, min(halfExtent.x, halfExtent.y));',
  '  vec2 curve = abs(centered) - halfExtent + radius;',
  '  float distanceToEdge = length(max(curve, 0.0)) + min(max(curve.x, curve.y), 0.0) - radius;',
  '  if (distanceToEdge >= 1.0) return behind;',
  '',
  '  vec2 exterior = max(curve, 0.0);',
  '  vec2 normal = length(exterior) > 0.01 ? normalize(exterior) : (curve.x > curve.y ? vec2(1.0, 0.0) : vec2(0.0, 1.0));',
  '  normal *= sign(centered + 0.0001);',
  '  float depth = max(0.0, -distanceToEdge);',
  '  float rimBand = 1.0 - smoothstep(0.0, lifted ? 20.0 : 15.0, depth);',
  '  vec2 refraction = -normal * rimBand * material.y * (lifted ? 18.0 : 12.0);',
  '  float dispersion = material.z * 0.36;',
  '  vec2 sampleAt = p + refraction;',
  '  vec3 through = sceneAt(sampleAt);',
  '  through.r = sceneAt(sampleAt + refraction * dispersion).r;',
  '  through.b = sceneAt(sampleAt - refraction * dispersion).b;',
  '  float spread = material.x * 0.85;',
  '  vec3 scattered = (sceneAt(sampleAt + vec2(spread, 0.0)) + sceneAt(sampleAt - vec2(spread, 0.0))',
  '      + sceneAt(sampleAt + vec2(0.0, spread)) + sceneAt(sampleAt - vec2(0.0, spread))) * 0.25;',
  '  through = mix(through, scattered, min(material.x / 28.0, 0.75));',
  '  vec3 tint = mix(vec3(0.035, 0.082, 0.105), vec3(0.88, 0.96, 0.975), light);',
  '  float deposit = mix(lifted ? 0.35 : 0.47, lifted ? 0.63 : 0.74, light);',
  '  vec3 shaded = mix(through * (1.0 + 0.04 * rimBand), tint, deposit);',
  '  float edge = 1.0 - smoothstep(0.25, 2.4, depth);',
  '  float directional = abs(dot(normal, normalize(vec2(-0.55, -0.82))));',
  '  shaded += edge * (0.07 + directional * (0.20 + material.w * 0.40));',
  '  shaded += (1.0 - smoothstep(0.0, 8.0, depth)) * 0.035 * directional;',
  '  return mix(behind, shaded, 1.0 - smoothstep(-0.7, 0.7, distanceToEdge));',
  '}',
  '',
  'void main() {',
  '  vec2 p = vec2(gl_FragCoord.x, renderSize.y - gl_FragCoord.y) * viewport / renderSize;',
  '  vec3 color = sceneAt(p);',
  '  color = pane(color, p, boxes[0], corners.x, false);',
  '  color = pane(color, p, boxes[1], corners.y, false);',
  '  color = pane(color, p, boxes[2], corners.z, false);',
  '  color = pane(color, p, boxes[3], corners.w, true);',
  '  gl_FragColor = vec4(color, 1.0);',
  '}'
].join('\n');

function compile(gl, type, text) {
  const shader = gl.createShader(type);
  gl.shaderSource(shader, text);
  gl.compileShader(shader);
  if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
    gl.deleteShader(shader);
    return null;
  }
  return shader;
}

export function createOptics(scene, state) {
  const canvas = scene.querySelector('.optics');
  const gl = canvas.getContext('webgl', { alpha: false, antialias: false, depth: false });
  if (!gl) return null;
  const vert = compile(gl, gl.VERTEX_SHADER, vertexSource);
  const frag = compile(gl, gl.FRAGMENT_SHADER, fragmentSource);
  if (!vert || !frag) return null;
  const program = gl.createProgram();
  gl.attachShader(program, vert);
  gl.attachShader(program, frag);
  gl.linkProgram(program);
  gl.deleteShader(vert);
  gl.deleteShader(frag);
  if (!gl.getProgramParameter(program, gl.LINK_STATUS)) return null;
  gl.useProgram(program);

  const triangle = gl.createBuffer();
  gl.bindBuffer(gl.ARRAY_BUFFER, triangle);
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 3, -1, -1, 3]), gl.STATIC_DRAW);
  const position = gl.getAttribLocation(program, 'position');
  gl.enableVertexAttribArray(position);
  gl.vertexAttribPointer(position, 2, gl.FLOAT, false, 0, 0);

  const texture = gl.createTexture();
  gl.bindTexture(gl.TEXTURE_2D, texture);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
  gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);

  const uniformNames = ['viewport', 'renderSize', 'photoSize', 'offsetY', 'light', 'material', 'boxes[0]', 'corners'];
  const uniforms = Object.fromEntries(uniformNames.map(name => [name, gl.getUniformLocation(program, name)]));
  const photo = new Image();
  const lenses = [...scene.querySelectorAll('[data-lens]')];
  const radii = new Float32Array(4);
  radii.set(lenses.map(lens => Number(lens.dataset.lens)));
  const boxes = new Float32Array(16);
  let ready = false;
  let frame = 0;
  let until = 0;

  function render(time) {
    frame = 0;
    const rect = canvas.getBoundingClientRect();
    if (!ready || !rect.width || !rect.height) return;
    const scale = Math.min(devicePixelRatio || 1, 1.5);
    const width = Math.round(rect.width * scale);
    const height = Math.round(rect.height * scale);
    if (canvas.width !== width || canvas.height !== height) {
      canvas.width = width;
      canvas.height = height;
      gl.viewport(0, 0, width, height);
    }
    lenses.forEach((lens, index) => {
      const bounds = lens.getBoundingClientRect();
      const offset = index * 4;
      boxes[offset] = bounds.left - rect.left;
      boxes[offset + 1] = bounds.top - rect.top;
      boxes[offset + 2] = bounds.width;
      boxes[offset + 3] = bounds.height;
    });
    gl.uniform2f(uniforms.viewport, rect.width, rect.height);
    gl.uniform2f(uniforms.renderSize, width, height);
    gl.uniform1f(uniforms.offsetY, state.offset);
    gl.uniform1f(uniforms.light, Number(state.light));
    gl.uniform4f(uniforms.material, state.material.frost, state.material.bend, state.material.dispersion, state.material.rim);
    gl.uniform4fv(uniforms['boxes[0]'], boxes);
    gl.uniform4fv(uniforms.corners, radii);
    gl.drawArrays(gl.TRIANGLES, 0, 3);
    if (time < until) frame = requestAnimationFrame(render);
  }

  function renderFor(duration = 0) {
    if (!ready) return;
    until = Math.max(until, performance.now() + duration);
    if (!frame) frame = requestAnimationFrame(render);
  }

  photo.addEventListener('load', () => {
    if (gl.isContextLost()) return;
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGB, gl.RGB, gl.UNSIGNED_BYTE, photo);
    gl.uniform2f(uniforms.photoSize, photo.naturalWidth, photo.naturalHeight);
    ready = true;
    scene.classList.add('has-optics');
    new ResizeObserver(() => renderFor()).observe(scene);
    document.fonts?.ready.then(() => renderFor());
    renderFor();
  });
  canvas.addEventListener('webglcontextlost', () => {
    ready = false;
    scene.classList.remove('has-optics');
    cancelAnimationFrame(frame);
    frame = 0;
  });
  photo.src = '/assets/gallery-sunset.jpg';

  return { renderFor };
}
