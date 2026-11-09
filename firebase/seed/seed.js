// Carga datos de prueba en Firebase: dos cuentas fijas para probar a mano y varias generadas con
// Faker, cada una con perfil, resultado del cuestionario, prendas reales con foto en Storage,
// outfits, guardados de Explora, avance de Aprende, una conversación con el asesor y el outfit del día.
//
// Uso (con los emuladores y el backend encendidos):
//   npm install
//   npm run seed                       → emulador, proyecto demo-orlo
//   ORLO_SEED_DESTINO=real npm run seed → proyecto real (requiere GOOGLE_APPLICATION_CREDENTIALS)
//
// El contenido (estilos, lecciones, catálogo de Explora) se pide a la API para que las copias
// guardadas en Firestore coincidan con el backend.
import { randomUUID } from "node:crypto";
import { fakerES as faker } from "@faker-js/faker";
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";

import { ARMARIOS, PRENDAS } from "./prendas.js";

const DESTINO = process.env.ORLO_SEED_DESTINO ?? "emulador";
const PROYECTO = process.env.ORLO_FIREBASE_PROJECT_ID ?? "demo-orlo";
const BUCKET = process.env.ORLO_STORAGE_BUCKET ?? `${PROYECTO}.appspot.com`;
const API = process.env.ORLO_API_URL ?? "http://localhost:8090/api/v1";
const PASSWORD = process.env.ORLO_SEED_PASSWORD ?? "Prueba2026";
const EXTRA = Number(process.env.ORLO_SEED_USUARIOS ?? 4);

if (DESTINO === "emulador") {
  process.env.FIRESTORE_EMULATOR_HOST ??= "127.0.0.1:8080";
  process.env.FIREBASE_AUTH_EMULATOR_HOST ??= "127.0.0.1:9099";
  process.env.FIREBASE_STORAGE_EMULATOR_HOST ??= "127.0.0.1:9199";
}

initializeApp({ projectId: PROYECTO, storageBucket: BUCKET });
const auth = getAuth();
const db = getFirestore();
const bucket = getStorage().bucket();
faker.seed(42);

const CUENTAS_FIJAS = [
  { clave: "andres", email: "andres.prueba@orlo.test", name: "Andrés", ageRange: "18_24", garmentPronoun: "masculino",
    climate: "templado", budget: "medio", occasions: ["estudio", "salidas"], respuestas: [0, 1, 3, 1, 0, 2, 0, 1, 0, 0] },
  { clave: "laura", email: "laura.prueba@orlo.test", name: "Laura", ageRange: "25_34", garmentPronoun: "femenino",
    climate: "calido", budget: "medio", occasions: ["trabajo", "eventos"], respuestas: [4, 4, 0, 0, 1, 1, 2, 3, 1, 4],
    sensitiveDataConsent: true, bodyType: "reloj_de_arena", skinTone: "piel_media", hairColor: "cabello_castano_oscuro",
    eyeColor: "ojos_cafes" },
];

const OCASIONES = ["estudio", "trabajo", "salidas", "casa", "deporte", "eventos"];
const EDADES = ["18_24", "25_34", "35_44", "45_59", "60_mas"];
const TRATO = ["femenino", "masculino", "neutro"];
const CLIMAS = ["calido", "templado", "frio", "variable"];
const PRESUPUESTOS = ["bajo", "medio", "alto"];
const OCASIONES_OUTFIT = ["universidad", "trabajo", "salida", "otra"];

async function main() {
  console.log(`Destino: ${DESTINO} (${PROYECTO}) · API: ${API}`);
  const cuentas = [...CUENTAS_FIJAS];
  for (let i = 0; i < EXTRA; i++) {
    const nombre = faker.person.firstName();
    cuentas.push({
      clave: null,
      email: `${faker.helpers.slugify(nombre).toLowerCase()}.${i + 1}@orlo.test`,
      name: nombre,
      ageRange: faker.helpers.arrayElement(EDADES),
      garmentPronoun: faker.helpers.arrayElement(TRATO),
      climate: faker.helpers.arrayElement(CLIMAS),
      budget: faker.helpers.arrayElement(PRESUPUESTOS),
      occasions: faker.helpers.arrayElements(OCASIONES, { min: 1, max: 3 }),
      respuestas: null,
    });
  }

  for (const cuenta of cuentas) {
    const uid = await crearCuenta(cuenta.email);
    const token = await tokenDe(cuenta.email);
    const contenido = await contenidoDeLaApi(token);
    await cargarUsuario(uid, cuenta, contenido, token);
    console.log(`✔ ${cuenta.email}`);
  }
  console.log(`\nListo. Contraseña de todas las cuentas: ${PASSWORD}`);
  console.log("Cuentas para probar a mano: andres.prueba@orlo.test y laura.prueba@orlo.test");
}

async function crearCuenta(email) {
  try {
    const existente = await auth.getUserByEmail(email);
    await db.recursiveDelete(db.collection("users").doc(existente.uid));
    await auth.deleteUser(existente.uid);
  } catch (error) {
    if (error.code !== "auth/user-not-found") {
      throw error;
    }
  }
  const user = await auth.createUser({ email, password: PASSWORD, emailVerified: true });
  return user.uid;
}

/**
 * ID token de la cuenta (para llamar a la API como la app). Contra el emulador no hace falta clave;
 * contra el proyecto real se usa la clave web del proyecto (ORLO_FIREBASE_WEB_API_KEY).
 */
async function tokenDe(email) {
  const emulador = process.env.FIREBASE_AUTH_EMULATOR_HOST;
  const base = emulador ? `http://${emulador}/identitytoolkit.googleapis.com` : "https://identitytoolkit.googleapis.com";
  const key = emulador ? "demo" : process.env.ORLO_FIREBASE_WEB_API_KEY;
  if (!key) {
    throw new Error("Para el proyecto real define ORLO_FIREBASE_WEB_API_KEY");
  }
  const res = await fetch(`${base}/v1/accounts:signInWithPassword?key=${key}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password: PASSWORD, returnSecureToken: true }),
  });
  return (await res.json()).idToken;
}

async function api(token, ruta, opciones = {}) {
  const res = await fetch(`${API}${ruta}`, {
    ...opciones,
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}`, ...(opciones.headers ?? {}) },
  });
  if (!res.ok) {
    throw new Error(`La API respondió ${res.status} en ${ruta}. ¿Está encendido el backend?`);
  }
  return res.json();
}

async function contenidoDeLaApi(token) {
  const [quiz, lecciones, explora] = await Promise.all([
    api(token, "/quiz/active"),
    api(token, "/lessons"),
    api(token, "/explore/items?section=FOR_YOUR_STYLE"),
  ]);
  return { quiz, lecciones, explora };
}

async function cargarUsuario(uid, cuenta, contenido, token) {
  const ahora = Date.now();
  const dia = 24 * 60 * 60 * 1000;
  const userRef = db.collection("users").doc(uid);

  // Resultado del cuestionario calculado por la API con respuestas fijas o al azar
  const answers = contenido.quiz.questions.map((q, i) => {
    const indice = cuenta.respuestas?.[i] ?? faker.number.int({ min: 0, max: q.options.length - 1 });
    return { questionId: q.id, optionIds: [q.options[Math.min(indice, q.options.length - 1)].id] };
  });
  const puntaje = await api(token, "/quiz/score", { method: "POST", body: JSON.stringify({ answers }) });
  const resultadoRef = userRef.collection("results").doc();
  const resultado = {
    id: resultadoRef.id,
    primary: copiaEstilo(puntaje.primary),
    secondary: copiaEstilo(puntaje.secondary),
    createdAt: ahora - 3 * dia,
  };

  await userRef.set({
    name: cuenta.name,
    ageRange: cuenta.ageRange,
    garmentPronoun: cuenta.garmentPronoun,
    bodyType: cuenta.sensitiveDataConsent ? cuenta.bodyType : "",
    skinTone: cuenta.sensitiveDataConsent ? cuenta.skinTone : "",
    hairColor: cuenta.sensitiveDataConsent ? cuenta.hairColor : "",
    eyeColor: cuenta.sensitiveDataConsent ? cuenta.eyeColor : "",
    climate: cuenta.climate,
    budget: cuenta.budget,
    occasions: cuenta.occasions,
    sensitiveDataConsent: !!cuenta.sensitiveDataConsent,
    currentResult: resultado,
    acceptedTermsAt: ahora - 5 * dia,
    createdAt: ahora - 5 * dia,
  });
  await resultadoRef.set(resultado);

  // Prendas reales con su foto subida a Storage, como lo hace la app
  const nombres = cuenta.clave ? ARMARIOS[cuenta.clave] : faker.helpers.arrayElements(PRENDAS, { min: 6, max: 10 }).map((p) => p.name);
  const prendas = [];
  for (const [i, nombre] of nombres.entries()) {
    const prenda = PRENDAS.find((p) => p.name === nombre);
    const ref = userRef.collection("garments").doc();
    const photoUrl = await subirFoto(uid, ref.id, prenda.url);
    const doc = {
      id: ref.id, name: prenda.name, category: prenda.category, color: prenda.color, photoUrl,
      favorite: i % 4 === 0, createdAt: ahora - (nombres.length - i) * 3600_000,
    };
    await ref.set(doc);
    prendas.push(doc);
  }

  // Outfits: dos armados a mano y uno propuesto por el asesor
  const de = (cat) => prendas.filter((p) => p.category === cat);
  const arriba = de("arriba");
  const abajo = de("abajo");
  const calzado = de("calzado");
  const abrigos = de("abrigo");
  const manuales = [
    { name: "Para la semana", occasion: cuenta.occasions.includes("trabajo") ? "trabajo" : "universidad",
      garmentIds: [arriba[0], abajo[0], calzado[0]].filter(Boolean).map((p) => p.id), favorite: true },
    { name: "Salida del viernes", occasion: "salida",
      garmentIds: [arriba[1] ?? arriba[0], abajo[1] ?? abajo[0], abrigos[0], calzado[1] ?? calzado[0]].filter(Boolean).map((p) => p.id),
      favorite: false },
  ];
  for (const [i, outfit] of manuales.entries()) {
    const ref = userRef.collection("outfits").doc();
    await ref.set({ id: ref.id, ...outfit, itemsText: [], why: "", origin: "manual", createdAt: ahora - (2 - i) * dia });
  }
  const propuesto = {
    title: "Cómodo y ordenado",
    items: [arriba[0]?.name, abajo[0]?.name, calzado[0]?.name].filter(Boolean),
    why: "Los colores combinan entre sí y sirve para casi cualquier plan del día.",
  };
  const outfitAsesor = userRef.collection("outfits").doc();
  await outfitAsesor.set({
    id: outfitAsesor.id, name: propuesto.title, occasion: faker.helpers.arrayElement(OCASIONES_OUTFIT),
    garmentIds: [], itemsText: propuesto.items, why: propuesto.why, origin: "asesor", favorite: false, createdAt: ahora - dia,
  });

  // Conversación con el asesor (sin llamar al modelo: es texto de ejemplo)
  const conversacion = userRef.collection("conversations").doc();
  await conversacion.set({ id: conversacion.id, title: "¿Qué me pongo para la semana?", updatedAt: ahora - dia });
  const pregunta = conversacion.collection("messages").doc();
  await pregunta.set({ id: pregunta.id, role: "user", text: "¿Qué me pongo para la semana?", outfits: [], followUpQuestion: null, createdAt: ahora - dia - 60_000 });
  const respuesta = conversacion.collection("messages").doc();
  await respuesta.set({
    id: respuesta.id, role: "assistant", text: `${cuenta.name}, con lo que ya tienes puedes armar esto:`,
    outfits: [propuesto], followUpQuestion: null, createdAt: ahora - dia,
  });

  // Avance de Aprende: primera completada, segunda en curso
  const lecciones = contenido.lecciones.slice(0, 2);
  for (const [i, leccion] of lecciones.entries()) {
    await userRef.collection("lessonProgress").doc(String(leccion.id))
      .set({ lessonId: leccion.id, status: i === 0 ? "completada" : "en_curso", updatedAt: ahora - (2 - i) * dia });
  }

  // Guardados de Explora
  for (const item of faker.helpers.arrayElements(contenido.explora, 3)) {
    await userRef.collection("saved").doc(String(item.id)).set({
      itemId: item.id, name: item.name, imageUrl: item.imageUrl, category: item.category, savedAt: ahora - dia,
    });
  }

  // Outfit del día de hoy
  await userRef.collection("dailyOutfits").doc(fechaLocal(new Date())).set({ ...propuesto, createdAt: ahora });
}

function copiaEstilo(e) {
  return { id: e.id, code: e.code, name: e.name, description: e.description, palette: e.palette, gallery: e.gallery };
}

/** Descarga la foto y la sube a garmentImages/{uid}/{id}.jpg con un token de descarga. */
async function subirFoto(uid, garmentId, url) {
  const res = await fetch(url);
  if (!res.ok) {
    console.warn(`  No se pudo descargar ${url}: la prenda queda sin foto`);
    return null;
  }
  const datos = Buffer.from(await res.arrayBuffer());
  const contentType = res.headers.get("content-type") ?? "image/jpeg";
  const ruta = `garmentImages/${uid}/${garmentId}.jpg`;
  const token = randomUUID();
  await bucket.file(ruta).save(datos, { contentType, metadata: { metadata: { firebaseStorageDownloadTokens: token } } });
  const host = process.env.FIREBASE_STORAGE_EMULATOR_HOST
    ? `http://${process.env.FIREBASE_STORAGE_EMULATOR_HOST}`
    : "https://firebasestorage.googleapis.com";
  return `${host}/v0/b/${BUCKET}/o/${encodeURIComponent(ruta)}?alt=media&token=${token}`;
}

function fechaLocal(d) {
  const dos = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}`;
}

main().catch((error) => {
  console.error(error.message ?? error);
  process.exit(1);
});
