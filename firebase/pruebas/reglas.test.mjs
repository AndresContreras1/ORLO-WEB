// Pruebas de las reglas de Firestore y Storage contra los emuladores (sin dependencias: node:test y fetch).
// Requisitos: emuladores corriendo (firebase emulators:start --project demo-orlo).
// Uso, desde ORLO-WEB/firebase:  node --test pruebas/reglas.test.mjs
import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';
import { before, describe, test } from 'node:test';

const AUTH = 'http://localhost:9099/identitytoolkit.googleapis.com/v1';
const FS = 'http://localhost:8080/v1/projects/demo-orlo/databases/(default)/documents';
const ST = 'http://localhost:9199/v0/b/demo-orlo.appspot.com/o';
const AHORA = Date.now();
const ESTILO = { id: 3, code: 'urbano', name: 'Urbano', description: 'Ropa de ciudad', palette: ['#1F1F1F'], gallery: ['https://cdn.dummyjson.com/x.webp'] };

async function crearCuenta() {
  const res = await fetch(`${AUTH}/accounts:signUp?key=demo`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: `reglas-${randomUUID()}@orlo.test`, password: 'Clave-Prueba-2026', returnSecureToken: true }),
  });
  const json = await res.json();
  return { uid: json.localId, token: json.idToken };
}

/** Valor de JavaScript → formato REST de Firestore. Los enteros van como integerValue (igual que los SDK). */
function valor(v) {
  if (v === null) return { nullValue: null };
  if (typeof v === 'boolean') return { booleanValue: v };
  if (Number.isInteger(v)) return { integerValue: String(v) };
  if (typeof v === 'number') return { doubleValue: v };
  if (Array.isArray(v)) return { arrayValue: { values: v.map(valor) } };
  if (typeof v === 'object') return { mapValue: { fields: Object.fromEntries(Object.entries(v).map(([k, x]) => [k, valor(x)])) } };
  return { stringValue: v };
}

async function escribir(ruta, token, datos) {
  const res = await fetch(`${FS}/${ruta}`, {
    method: 'PATCH', headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ fields: Object.fromEntries(Object.entries(datos).map(([k, v]) => [k, valor(v)])) }),
  });
  return res.status;
}

const leer = async (ruta, token) => (await fetch(`${FS}/${ruta}`, { headers: { Authorization: `Bearer ${token}` } })).status;
const subir = async (ruta, token, tipo, bytes) => (await fetch(`${ST}?uploadType=media&name=${encodeURIComponent(ruta)}`, {
  method: 'POST', headers: { Authorization: `Firebase ${token}`, 'Content-Type': tipo }, body: Buffer.alloc(bytes, 1),
})).status;

let ana;
let beto;

before(async () => {
  ana = await crearCuenta();
  beto = await crearCuenta();
  await escribir(`users/${beto.uid}`, beto.token, { name: 'Beto', acceptedTermsAt: AHORA, createdAt: AHORA });
});

describe('documentos válidos (como los escriben la web y la app)', () => {
  // La ruta depende del uid, que se conoce después de crear la cuenta: por eso cada caso es una función
  const casos = [
    ['registro', () => [`users/${ana.uid}`, { name: 'Ana', acceptedTermsAt: AHORA, createdAt: AHORA }]],
    ['perfil completo con null de Kotlin', () => [`users/${ana.uid}`, {
      name: 'Ana', ageRange: '18_24', garmentPronoun: 'femenino', bodyType: '', skinTone: '', hairColor: '', eyeColor: '',
      climate: 'calido', budget: 'medio', occasions: ['estudio'], sensitiveDataConsent: false,
      currentResult: { id: 'r1', primary: ESTILO, secondary: ESTILO, createdAt: AHORA }, photoUrl: null, acceptedTermsAt: AHORA, createdAt: AHORA,
    }]],
    ['prenda sin foto', () => [`users/${ana.uid}/garments/g1`, { name: 'Jean', category: 'abajo', color: 'azul', photoUrl: null, favorite: false, createdAt: AHORA }]],
    ['outfit del asesor', () => [`users/${ana.uid}/outfits/o1`, { name: 'Salida', occasion: 'salida', garmentIds: [], itemsText: ['Jean azul'], why: 'Combina', origin: 'asesor', favorite: true, createdAt: AHORA }]],
    ['conversación', () => [`users/${ana.uid}/conversations/c1`, { title: '¿Qué me pongo?', updatedAt: AHORA }]],
    ['mensaje del asesor', () => [`users/${ana.uid}/conversations/c1/messages/m1`, { role: 'assistant', text: 'Hola', outfits: [{ title: 'A', items: ['x'], why: 'y' }], followUpQuestion: null, createdAt: AHORA }]],
    ['resultado', () => [`users/${ana.uid}/results/r1`, { id: 'r1', primary: ESTILO, secondary: ESTILO, createdAt: AHORA }]],
    ['progreso de lección', () => [`users/${ana.uid}/lessonProgress/1`, { lessonId: 1, status: 'en_curso', updatedAt: AHORA }]],
    ['guardado de Explora', () => [`users/${ana.uid}/saved/7`, { itemId: 7, name: 'Camisa', imageUrl: 'https://x.co/a.jpg', category: 'arriba', savedAt: AHORA }]],
    ['outfit del día', () => [`users/${ana.uid}/dailyOutfits/2026-11-10`, { title: 'Hoy', items: ['Jean'], why: 'Frío', createdAt: AHORA }]],
  ];
  for (const [nombre, caso] of casos) {
    test(nombre, async () => {
      const [ruta, datos] = caso();
      assert.equal(await escribir(ruta, ana.token, datos), 200);
    });
  }
});

describe('ataques que las reglas deben bloquear', () => {
  test('leer el documento de otro usuario', async () => assert.equal(await leer(`users/${beto.uid}`, ana.token), 403));
  test('escribir el documento de otro usuario', async () => assert.equal(await escribir(`users/${beto.uid}`, ana.token, { name: 'x' }), 403));
  test('listar todos los usuarios', async () => assert.equal(await leer('users', ana.token), 403));
  test('escribir garmentsCount', async () => assert.equal(await escribir(`users/${ana.uid}`, ana.token, { name: 'Ana', garmentsCount: 999 }), 403));
  test('campos inventados (premium)', async () => assert.equal(await escribir(`users/${ana.uid}`, ana.token, { name: 'Ana', premium: true }), 403));
  test('guardar el correo', async () => assert.equal(await escribir(`users/${ana.uid}`, ana.token, { name: 'Ana', email: 'a@a.co' }), 403));
  test('texto de 500 KB en el perfil', async () => assert.equal(await escribir(`users/${ana.uid}`, ana.token, { name: 'Ana', climate: 'x'.repeat(500_000) }), 403));
  test('photoUrl con javascript:', async () => assert.equal(await escribir(`users/${ana.uid}/garments/g2`, ana.token, { name: 'X', category: 'arriba', color: 'azul', photoUrl: 'javascript:alert(1)', createdAt: AHORA }), 403));
  test('categoría inventada', async () => assert.equal(await escribir(`users/${ana.uid}/garments/g3`, ana.token, { name: 'X', category: 'nave', color: 'azul', createdAt: AHORA }), 403));
  test('mensaje de 900 KB', async () => assert.equal(await escribir(`users/${ana.uid}/conversations/c1/messages/m9`, ana.token, { role: 'user', text: 'm'.repeat(900_000), createdAt: AHORA }), 403));
  test('mensaje con rol system', async () => assert.equal(await escribir(`users/${ana.uid}/conversations/c1/messages/m8`, ana.token, { role: 'system', text: 'x', createdAt: AHORA }), 403));
  test('resultado con contenido arbitrario', async () => assert.equal(await escribir(`users/${ana.uid}/results/r9`, ana.token, { basura: 'z' }), 403));
  test('outfit con 5.000 prendas', async () => assert.equal(await escribir(`users/${ana.uid}/outfits/o9`, ana.token, {
    name: 'O', occasion: 'otra', garmentIds: Array.from({ length: 5000 }, (_, i) => `g${i}`), itemsText: [], why: '', origin: 'manual', favorite: false, createdAt: AHORA,
  }), 403));
  test('outfit del día con id que no es fecha', async () => assert.equal(await escribir(`users/${ana.uid}/dailyOutfits/hoy`, ana.token, { title: 'x', items: [], why: '', createdAt: AHORA }), 403));
  // El emulador responde 400 (token mal formado); producción, 401 o 403. Lo importante: no entra.
  test('token falso', async () => assert.notEqual(await leer(`users/${ana.uid}`, 'token-falso'), 200));
});

describe('Storage', () => {
  test('foto propia JPEG', async () => assert.equal(await subir(`garmentImages/${ana.uid}/ok.jpg`, ana.token, 'image/jpeg', 50_000), 200));
  test('foto en la carpeta de otro usuario', async () => assert.equal(await subir(`garmentImages/${beto.uid}/x.jpg`, ana.token, 'image/jpeg', 1000), 403));
  test('HTML disfrazado', async () => assert.equal(await subir(`garmentImages/${ana.uid}/x.html`, ana.token, 'text/html', 1000), 403));
  test('SVG (puede llevar scripts)', async () => assert.equal(await subir(`garmentImages/${ana.uid}/x.svg`, ana.token, 'image/svg+xml', 1000), 403));
  test('imagen de 3 MB', async () => assert.equal(await subir(`garmentImages/${ana.uid}/grande.jpg`, ana.token, 'image/jpeg', 3 * 1024 * 1024), 403));
  test('foto de perfil de otro usuario', async () => assert.equal(await subir(`profileImages/${beto.uid}.jpg`, ana.token, 'image/jpeg', 1000), 403));
});
