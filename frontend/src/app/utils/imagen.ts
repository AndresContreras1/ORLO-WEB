/**
 * Comprime una foto antes de subirla (mismo criterio que la app): lado mayor de hasta 1280 px,
 * JPEG de calidad 0.8. Las reglas de Storage aceptan hasta 2 MB.
 */
export async function comprimirFoto(archivo: Blob, ladoMaximo = 1280, calidad = 0.8): Promise<Blob> {
  const bitmap = await createImageBitmap(archivo);
  const escala = Math.min(1, ladoMaximo / Math.max(bitmap.width, bitmap.height));
  const ancho = Math.round(bitmap.width * escala);
  const alto = Math.round(bitmap.height * escala);
  const canvas = document.createElement('canvas');
  canvas.width = ancho;
  canvas.height = alto;
  const ctx = canvas.getContext('2d');
  if (!ctx) {
    throw new Error('No se pudo procesar la imagen');
  }
  ctx.drawImage(bitmap, 0, 0, ancho, alto);
  bitmap.close();
  const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/jpeg', calidad));
  if (!blob) {
    throw new Error('No se pudo procesar la imagen');
  }
  return blob;
}

export const TAMANO_MAXIMO_FOTO = 2 * 1024 * 1024;
