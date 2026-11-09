// Cloud Functions de ORLO. Mantienen la integridad de los datos de cada usuario sin depender de que
// la app o la web hagan todos los pasos: contador de prendas, outfits sin prendas borradas y
// limpieza total al eliminar la cuenta.
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getStorage } = require("firebase-admin/storage");
const { onDocumentCreated, onDocumentDeleted } = require("firebase-functions/v2/firestore");
const authV1 = require("firebase-functions/v1/auth");
const { logger } = require("firebase-functions");

initializeApp();
const db = getFirestore();

// Al crear una prenda se suma al contador del usuario
exports.contarPrendaCreada = onDocumentCreated("users/{uid}/garments/{garmentId}", async (event) => {
  const userRef = db.collection("users").doc(event.params.uid);
  const user = await userRef.get();
  // Si el usuario ya no existe (cuenta borrándose), no se recrea el documento
  if (!user.exists) {
    return;
  }
  await userRef.update({ garmentsCount: FieldValue.increment(1) });
});

// Al borrar una prenda: se resta del contador, se quita de los outfits que la usaban y se borra su foto
exports.limpiarPrendaBorrada = onDocumentDeleted("users/{uid}/garments/{garmentId}", async (event) => {
  const { uid, garmentId } = event.params;
  const userRef = db.collection("users").doc(uid);
  const user = await userRef.get();
  if (!user.exists) {
    return;
  }

  const outfits = await userRef.collection("outfits").where("garmentIds", "array-contains", garmentId).get();
  // Un batch admite hasta 500 operaciones
  for (let i = 0; i < outfits.docs.length; i += 450) {
    const batch = db.batch();
    outfits.docs.slice(i, i + 450).forEach((doc) => {
      batch.update(doc.ref, { garmentIds: FieldValue.arrayRemove(garmentId) });
    });
    await batch.commit();
  }

  const actual = user.get("garmentsCount") || 0;
  await userRef.update({ garmentsCount: Math.max(0, actual - 1) });

  try {
    await getStorage().bucket().file(`garmentImages/${uid}/${garmentId}.jpg`).delete();
  } catch (error) {
    // La prenda podía no tener foto
    if (error.code !== 404) {
      logger.warn("No se pudo borrar la foto de la prenda", { uid, garmentId, error: error.message });
    }
  }
});

// Al eliminar la cuenta de Authentication se borran todos sus datos de Firestore y Storage
exports.limpiarUsuarioBorrado = authV1.user().onDelete(async (user) => {
  const uid = user.uid;
  await db.recursiveDelete(db.collection("users").doc(uid));
  const bucket = getStorage().bucket();
  await bucket.deleteFiles({ prefix: `garmentImages/${uid}/` });
  try {
    await bucket.file(`profileImages/${uid}.jpg`).delete();
  } catch (error) {
    if (error.code !== 404) {
      logger.warn("No se pudo borrar la foto de perfil", { uid, error: error.message });
    }
  }
  logger.info("Datos del usuario eliminados", { uid });
});
