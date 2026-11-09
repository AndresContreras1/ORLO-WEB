/**
 * Al borrar una foto de Storage puede que ya no exista (la prenda no tenía foto o la Cloud Function
 * ya la borró): solo ese caso se ignora; cualquier otro error queda registrado.
 */
export function ignorarSiNoExiste(error: unknown): void {
  if ((error as { code?: string })?.code !== 'storage/object-not-found') {
    console.error('No se pudo borrar el archivo de Storage', error);
  }
}
