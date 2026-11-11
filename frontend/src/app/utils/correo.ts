/**
 * Detecta errores de escritura en el dominio del correo (gmil.com, hotmial.com, gmail.co…).
 * Firebase acepta cualquier correo con formato válido, así que una cuenta creada con
 * "andres@gmil.com" existe, pero la persona después entra con "andres@gmail.com" y no le funciona.
 */
const DOMINIOS_COMUNES = [
  'gmail.com', 'hotmail.com', 'outlook.com', 'yahoo.com', 'icloud.com', 'live.com',
  'hotmail.es', 'outlook.es', 'yahoo.es', 'msn.com', 'protonmail.com', 'unal.edu.co',
];

/** Correo en minúsculas y sin espacios: Firebase no distingue mayúsculas en el correo. */
export function normalizarCorreo(correo: string): string {
  return correo.trim().toLowerCase();
}

/** Devuelve el correo corregido si el dominio se parece mucho a uno común, o null si se ve bien. */
export function sugerirCorreo(correo: string): string | null {
  const limpio = normalizarCorreo(correo);
  const arroba = limpio.lastIndexOf('@');
  if (arroba < 1 || arroba === limpio.length - 1) {
    return null;
  }
  const usuario = limpio.slice(0, arroba);
  const dominio = limpio.slice(arroba + 1);
  if (DOMINIOS_COMUNES.includes(dominio)) {
    return null;
  }
  let mejor: string | null = null;
  let menor = Infinity;
  for (const comun of DOMINIOS_COMUNES) {
    const d = distancia(dominio, comun);
    if (d < menor) {
      menor = d;
      mejor = comun;
    }
  }
  // Hasta dos letras de diferencia: gmil.com, gmail.co, hotmial.com, outlok.com
  return mejor && menor <= 2 ? `${usuario}@${mejor}` : null;
}

/** Distancia de edición (Levenshtein): cuántas letras hay que cambiar, quitar o poner. */
function distancia(a: string, b: string): number {
  const fila = Array.from({ length: b.length + 1 }, (_, j) => j);
  for (let i = 1; i <= a.length; i++) {
    let diagonal = fila[0];
    fila[0] = i;
    for (let j = 1; j <= b.length; j++) {
      const arriba = fila[j];
      fila[j] = Math.min(fila[j] + 1, fila[j - 1] + 1, diagonal + (a[i - 1] === b[j - 1] ? 0 : 1));
      diagonal = arriba;
    }
  }
  return fila[b.length];
}
